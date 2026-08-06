package com.example.zenith.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.graphics.BitmapFactory
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.zenith.R
import com.example.zenith.data.AppDatabase
import com.example.zenith.data.DistractionEvent
import com.example.zenith.data.DistractionEventDao
import com.example.zenith.data.FocusSession
import com.example.zenith.data.FocusSessionDao
import com.example.zenith.data.SettingsRepository
import com.example.zenith.data.UserPreferences
import kotlinx.coroutines.*
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class FocusService : Service(), SensorEventListener {

    private lateinit var db : AppDatabase
    private lateinit var focusSessionDao: FocusSessionDao
    private lateinit var distractionEventDao: DistractionEventDao
    private lateinit var usageStatsManager: UsageStatsManager
    private lateinit var sensorManager: SensorManager
    private lateinit var vibrator: Vibrator
    private val settingsRepository by lazy { SettingsRepository(this) }

    private val mainChannelID = "focus_service_channel"
    private var currentSessionId: Long = -1

    private var monitoringJob: Job? = null
    private var callGraceJob: Job? = null
    private var roastIntervalJob: Job? = null

    private var notificationCounter = 2
    private var callGraceActive = false
    private var lastPickupTime: Long = 0
    private var lastCheckedTimestamp: Long = System.currentTimeMillis()
    private var lastRoastTime: Long = 0
    private var isCurrentlyDistracted = false
    private var isManuallyPaused = false
    private var ignoredViolations = 0
    private var dndWasAppliedByZenith = false
    private var interruptionFilterBeforeSession = NotificationManager.INTERRUPTION_FILTER_ALL

    @Volatile
    private var userPreferences = UserPreferences(
        strictnessLevel = 1, isCallShieldEnabled = true, mercyBuffer = 0,
        roastIntensity = 1, isAutoDndEnabled = false,
        notificationThrottlingSeconds = 30, isHapticsEnabled = true,
        vibrationStrength = 100, showFocusTrends = true
    )

    private val sessionScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // --- 1. RECEIVERS ---

    private val screenStartReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_SCREEN_ON && !callGraceActive) {
                handlePickupViolation()
            }
        }
    }

    private val phoneStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val state = intent?.getStringExtra(TelephonyManager.EXTRA_STATE)
            when (state) {
                TelephonyManager.EXTRA_STATE_RINGING, TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                    if (userPreferences.isCallShieldEnabled) activateCallShield()
                }
                TelephonyManager.EXTRA_STATE_IDLE -> {
                    deactivateCallShield()
                }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_STOP = "ACTION_STOP"
        const val EXTRA_IS_FINISHED = "EXTRA_IS_FINISHED"
    }

    // --- 2. LIFECYCLE ---

    override fun onCreate() {
        super.onCreate()
        db = AppDatabase.getDatabase(this)
        focusSessionDao = db.focusSessionDao()
        distractionEventDao = db.distractionEventDao()
        usageStatsManager = getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION") getSystemService(VIBRATOR_SERVICE) as Vibrator
        }

        registerReceiver(screenStartReceiver, IntentFilter(Intent.ACTION_SCREEN_ON))
        registerReceiver(phoneStateReceiver, IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED))

        sessionScope.launch {
            settingsRepository.userPreferenceFlow.collect { preferences ->
                userPreferences = preferences
                if (preferences.isAutoDndEnabled && monitoringJob != null) {
                    applyDoNotDisturbIfEnabled()
                } else if (!preferences.isAutoDndEnabled) {
                    restoreDoNotDisturb()
                }
            }
        }

        sessionScope.launch {
            SessionEventBus.events.collect { event ->
                when(event) {
                    SessionEventBus.SessionEvent.UserManualPause -> {
                        isManuallyPaused = true
                        stopPeriodicRoasting()
                        Log.d("FocusService", "User is on break. Monitoring silenced.")
                    }
                    SessionEventBus.SessionEvent.UserManualResume -> {
                        isManuallyPaused = false
                        lastCheckedTimestamp = System.currentTimeMillis()
                        Log.d("FocusService", "User resumed focus session.")
                    }

                    else -> {}
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            handleStopCommand(intent.getBooleanExtra(EXTRA_IS_FINISHED, false))
            return START_NOT_STICKY
        }

        createMainChannel()
        startForeground(1, buildPersistentNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        applyDoNotDisturbIfEnabled()

        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        if (monitoringJob == null) {
            monitoringJob = sessionScope.launch {
                if (currentSessionId == -1L) {
                    currentSessionId = focusSessionDao.insertSession(FocusSession(
                        missionName = intent?.getStringExtra("MISSION_NAME") ?: "Untitled",
                        plannedDurationMinutes = intent?.getIntExtra("PLANNED_MINUTES", 25) ?: 25,
                        actualDurationSeconds = 0, isCompleted = false, timestamp = System.currentTimeMillis()
                    ))
                }
                lastCheckedTimestamp = System.currentTimeMillis()
                while (true) {
                    if (!callGraceActive) detectAppSwitches()
                    delay(3000)
                }
            }
        }
        return START_STICKY
    }

    private fun handleStopCommand(isExplicitFinish: Boolean) {
        monitoringJob?.cancel()
        stopPeriodicRoasting()
        restoreDoNotDisturb()
        sessionScope.launch {
            if (currentSessionId != -1L) {
                focusSessionDao.getSessionById(currentSessionId.toInt())?.let {
                    val duration = ((System.currentTimeMillis() - it.timestamp) / 1000).toInt()
                    focusSessionDao.updateSession(it.copy(actualDurationSeconds = duration, isCompleted = isExplicitFinish))
                }
            }
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(screenStartReceiver)
        unregisterReceiver(phoneStateReceiver)
        sensorManager.unregisterListener(this)
        restoreDoNotDisturb()
        sessionScope.cancel()
    }

    // --- 3. TELEMETRY & VIOLATIONS ---

    private fun detectAppSwitches() {
        val now = System.currentTimeMillis()
        val events = usageStatsManager.queryEvents(lastCheckedTimestamp, now)
        val event = UsageEvents.Event()
        var latestPkg: String? = null

        if (isManuallyPaused || callGraceActive) {
            lastCheckedTimestamp = System.currentTimeMillis()
            return
        }

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) UsageEvents.Event.ACTIVITY_RESUMED else UsageEvents.Event.MOVE_TO_FOREGROUND
            if (event.eventType == type) latestPkg = event.packageName
        }

        if (latestPkg != null) {
            if (latestPkg == packageName) {
                if (isCurrentlyDistracted) {
                    isCurrentlyDistracted = false
                    stopPeriodicRoasting()
                    updateNotification("ZENITH: FOCUS RESTORED", "Welcome back. Let's finish this.")
                }
            } else if (!isSystemPackage(latestPkg) && !isCurrentlyDistracted) {
                isCurrentlyDistracted = true
                if (triggerPunishment("APP_SWITCH")) startPeriodicRoasting()
            }
        }
        lastCheckedTimestamp = now
    }

    private fun handlePickupViolation() {

        if (isManuallyPaused || callGraceActive) return
        val now = System.currentTimeMillis()
        if (now - lastPickupTime > pickupCooldownMs()) {
            lastPickupTime = now
            triggerPunishment("PICKUP")
        }
    }

    /** Returns true only when this violation should receive a penalty. */
    private fun triggerPunishment(type: String): Boolean {
        if (callGraceActive) return false

        if (ignoredViolations < userPreferences.mercyBuffer) {
            ignoredViolations++
            Log.d("FocusService", "Mercy buffer absorbed $type ($ignoredViolations/${userPreferences.mercyBuffer})")
            return false
        }

        saveDistraction(type)
        if (userPreferences.isHapticsEnabled) triggerExtremeVibration()

        val now = System.currentTimeMillis()
        if (now - lastRoastTime > roastIntervalMs()) {
            val (title, msg) = RoastManager.getRoast()
            updateNotification(title, msg)
            lastRoastTime = now
        }
        return true
    }

    private fun startPeriodicRoasting() {
        roastIntervalJob?.cancel()
        roastIntervalJob = sessionScope.launch {
            var count = 1
            while (isCurrentlyDistracted) {
                delay(roastIntervalMs())
                if (!isCurrentlyDistracted || callGraceActive) break
                count++
                val isBrutal = count >= 3
                val (title, msg) = RoastManager.getRoast(isBrutal = isBrutal)
                updateNotification(title, msg, isUrgent = isBrutal)
            }
        }
    }

    private fun stopPeriodicRoasting() {
        roastIntervalJob?.cancel()
        roastIntervalJob = null
    }

    // --- 4. CALL SHIELD ---

    private fun activateCallShield() {
        if (!callGraceActive) {
            callGraceActive = true
            callGraceJob?.cancel()
            stopPeriodicRoasting()
            // Freeze Timer in ViewModel
            sessionScope.launch { SessionEventBus.emit(SessionEventBus.SessionEvent.PauseForCall) }
            Log.d("FocusService", "Call detected: Timer Paused")
        }
    }

    private fun deactivateCallShield() {
        if (!callGraceActive) return
        callGraceJob?.cancel()
        callGraceJob = sessionScope.launch {
            // Give 5 seconds grace after hanging up to put the phone back down
            delay(5000L)
            callGraceActive = false
            // Resume Timer in ViewModel
            lastCheckedTimestamp = System.currentTimeMillis()
            isCurrentlyDistracted = false

            SessionEventBus.emit(SessionEventBus.SessionEvent.ResumeAfterCall)
            Log.d("FocusService", "Call ended: Timer Resumed")
        }
    }

    private fun pickupCooldownMs(): Long = when (userPreferences.strictnessLevel) {
        0 -> 30_000L // Low: avoid penalising accidental movement.
        2 -> 5_000L  // Merciless: detect repeated pickups quickly.
        else -> 10_000L
    }

    private fun roastIntervalMs(): Long = when (userPreferences.strictnessLevel) {
        0 -> 60_000L
        2 -> 15_000L
        else -> 30_000L
    }

    private fun applyDoNotDisturbIfEnabled() {
        if (!userPreferences.isAutoDndEnabled || dndWasAppliedByZenith) return
        val notificationManager = getSystemService(NotificationManager::class.java)
        if (!notificationManager.isNotificationPolicyAccessGranted) {
            Log.w("FocusService", "DND is enabled in settings but policy access has not been granted.")
            return
        }
        interruptionFilterBeforeSession = notificationManager.currentInterruptionFilter
        notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
        dndWasAppliedByZenith = true
    }

    private fun restoreDoNotDisturb() {
        if (!dndWasAppliedByZenith) return
        val notificationManager = getSystemService(NotificationManager::class.java)
        if (notificationManager.isNotificationPolicyAccessGranted) {
            notificationManager.setInterruptionFilter(interruptionFilterBeforeSession)
        }
        dndWasAppliedByZenith = false
    }

    // --- 5. NOTIFICATIONS & FEEDBACK ---

    private fun updateNotification(title: String, message: String, isUrgent: Boolean = false) {
        // ROTATING CHANNEL: Bypasses Android's heads-up suppression
        val roastChannelId = "zenith_roast_channel_${notificationCounter % 3}"
        createRoastChannel(roastChannelId)

        val builder = NotificationCompat.Builder(this, roastChannelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
            .setContentTitle(title)
            .setContentText(message)
            .setSubText("ZENITH FOCUS ENGINE")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(Notification.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setColor(0xFF6366F1.toInt())
            .setColorized(false) // Zomato Style: Dark BG, White Title
            .setVibrate(longArrayOf(0, 100))

        if (isUrgent) {
            builder.setDefaults(Notification.DEFAULT_ALL)
        }

        // ROTATING ID: Ensures a fresh "Pop" every time
        val notifId = 100 + (notificationCounter % 10)
        notificationCounter++

        getSystemService(NotificationManager::class.java).notify(notifId, builder.build())
    }

    private fun triggerExtremeVibration() {
        val timings = longArrayOf(0, 300, 100, 300, 100, 600)
        val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255)
        vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
    }

    private fun createMainChannel() {
        val channel = NotificationChannel(mainChannelID, "Zenith Focus Session", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun createRoastChannel(id: String) {
        val channel = NotificationChannel(id, "Zenith Roast Alerts", NotificationManager.IMPORTANCE_HIGH).apply {
            enableVibration(true)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildPersistentNotification(): Notification {
        return NotificationCompat.Builder(this, mainChannelID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("ZENITH ENGINE ACTIVE")
            .setContentText("Focus integrity being monitored...")
            .setOngoing(true)
            .build()
    }

    private fun saveDistraction(type: String) {
        sessionScope.launch {
            if (currentSessionId != -1L) {
                distractionEventDao.insertEvent(DistractionEvent(sessionId = currentSessionId.toInt(), timeStamp = System.currentTimeMillis(), distractionType = type))
            }
        }
    }

    private fun isSystemPackage(pkg: String): Boolean = pkg == "com.android.systemui" || pkg.contains("launcher") || pkg == "android"
    override fun onSensorChanged(event: SensorEvent?) { if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) { if (abs(sqrt(event.values[0].pow(2) + event.values[1].pow(2) + event.values[2].pow(2)) - SensorManager.GRAVITY_EARTH) >= 2.0f && !callGraceActive) handlePickupViolation() } }
    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}
}
