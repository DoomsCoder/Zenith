package com.example.zenith.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.zenith.R
import com.example.zenith.data.AppDatabase
import com.example.zenith.data.DistractionEvent
import com.example.zenith.data.DistractionEventDao
import com.example.zenith.data.FocusSession
import com.example.zenith.data.FocusSessionDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * FocusService manages the background lifecycle of a focus session,
 * ensuring telemetry tracking persists when the app is backgrounded.
 *
 * WHY WE NEED THIS:
 *  * Android aggressively kills background tasks to save battery.
 *  * Because Zenith needs to track sensors (accelerometer) for 25+ minutes,
 *  * a standard Activity or background thread would be killed.
 *  * A "Foreground Service" tells the OS: "I am doing important work the user
 *  * is aware of, please don't kill me.
 */
class FocusService : Service(), SensorEventListener {

    private lateinit var db : AppDatabase
    private lateinit var focusSessionDao: FocusSessionDao
    private lateinit var distractionEventDao: DistractionEventDao
    private lateinit var usageStatsManager: UsageStatsManager

    private lateinit var sensorManager: SensorManager

    // Unique ID for the Notification Channel (Required for Android 8.0+)
    private val channelID = "focus_service_channel"
    private var currentSessionId: Long = -1

    private var monitoringJob: Job? = null

    private var lastPickupTime: Long = 0
    private var lastAppSwitchTime: Long = 0
    private var lastCheckedTimestamp: Long = System.currentTimeMillis()
    private val sessionScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * onBind is required by the Service class.
     * There are two types of services:
     * 1. Bound: The Activity and Service "talk" to each other.
     * 2. Started: The Service runs independently.
     * For Zenith, we use a 'Started Service', so we return null.
     */
    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_STOP = "ACTION_STOP"
        const val EXTRA_IS_FINISHED = "EXTRA_IS_FINISHED"
    }

    /**
     * Entry point of the service. Triggered by createNotificationChannel() from the Activity.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent?.action == ACTION_STOP) {
            val isExplicitFinish = intent.getBooleanExtra(EXTRA_IS_FINISHED, false)
            handleStopCommand(isExplicitFinish)
            return START_NOT_STICKY
        }
        createNotificationChannel()
        val notification = buildNotification()

        /**
         * START FOREGROUND
         * CONCEPT: This is the magic command that keeps the service alive.
         * Android 14+ (API 34) requires a specific foreground service type
         * declaration both in the manifest and at runtime.
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            // Fallback for older Android versions that don't require types
            startForeground(1, notification)
        }

        // Register the Accelerometer
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        accelerometer?.let {
            // SENSOR_DELAY_NORMAL is perfect for battery-efficient pickup detection
            sensorManager.registerListener(this,it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        val missionName = intent?.getStringExtra("MISSION_NAME") ?: "Untitled Mission"
        val plannedMins = intent?.getIntExtra("PLANNED_MINUTES",25) ?: 25

        if (monitoringJob == null) {
            monitoringJob = sessionScope.launch {
                if (currentSessionId == -1L) {
                    val newSession = FocusSession(
                        missionName = missionName,
                        plannedDurationMinutes = plannedMins,
                        actualDurationSeconds = 0,
                        isCompleted = false,
                        timestamp = System.currentTimeMillis()
                    )

                    currentSessionId = focusSessionDao.insertSession(newSession)
                }

                lastCheckedTimestamp = System.currentTimeMillis()

                while (true) {
                    detectAppSwitches()
                    delay(3000)
                }
            }
        }

        // Keep service running; restart if evicted by the system.
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()

        db = AppDatabase.getDatabase(this)
        focusSessionDao = db.focusSessionDao()
        distractionEventDao = db.distractionEventDao()

        usageStatsManager = getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

    }

    private fun handleStopCommand(isExplicitFinish: Boolean) {
        // Update the session in background
        monitoringJob?.cancel()
        monitoringJob = null
        sessionScope.launch {
            if (currentSessionId != -1L) {
                val session = focusSessionDao.getSessionById(currentSessionId.toInt())
                session?.let {
                    val endTime = System.currentTimeMillis()
                    val actualSeconds = ((endTime - it.timestamp) / 1000).toInt()

                    val completedOrNot = it.plannedDurationMinutes.let { it1 -> actualSeconds >= (it1 * 60) }
                    // Update with current Time
                    focusSessionDao.updateSession( it.copy(
                        actualDurationSeconds = actualSeconds,
                        isCompleted = completedOrNot
                    )
                    )
                }
            }

            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        monitoringJob?.cancel()

        sensorManager.unregisterListener(this)
        // Cancel the scope to prevent memory leak's
        sessionScope.cancel()
    }

    /**
     * Registers the notification channel.
     * SDK_INT check is omitted as project minSdk is 26 (Oreo).
     */
    private fun createNotificationChannel() {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val serviceChannel = NotificationChannel(
                channelID,
                "Zenith Focus Session", // Name show in android system settings
                importance)

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(serviceChannel)
    }


    /**
     * Constructs the persistent notification required to maintain foreground status.
     */
    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, channelID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Zenith Focus Session")
            .setContentText("Tracking physical pickup and focus quality...")
            .setOngoing(true) // Prevents the user from swiping it away
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
    }

    private fun detectAppSwitches() {

        val now = System.currentTimeMillis()
        val events = usageStatsManager.queryEvents(lastCheckedTimestamp, now)
        val event = UsageEvents.Event()

        var detectedViolation = false

        while (events.hasNextEvent()) {
            events.getNextEvent(event)

            val eventType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                UsageEvents.Event.ACTIVITY_RESUMED
            } else {
                @Suppress("DEPRECATION")
                UsageEvents.Event.MOVE_TO_FOREGROUND
            }

            if (event.eventType == eventType) {
                if (event.packageName != packageName && !isSystemPackage(event.packageName)) {
                    detectedViolation = true
                }
            }
        }

        if (detectedViolation) {

            if (now - lastAppSwitchTime > 5000) {
                lastAppSwitchTime = now
                saveDistraction("APP_SWITCH")
                Log.d("FocusService","Event: App Switch Detected!")
            }
        }

        lastCheckedTimestamp = now
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        // we don't use this now
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {

            val magnitude = sqrt(event.values[0].pow(2) + event.values[1].pow(2) + event.values[2].pow(2))

            val threshold = 2.0f

            if (abs( magnitude - SensorManager.GRAVITY_EARTH) >= threshold) {
                val currentTime = System.currentTimeMillis()

                if (currentTime - lastPickupTime > 10000) {
                    lastPickupTime= currentTime

                    Log.d("FocusService", "Pickup detected! Magnitude: $magnitude")

                    saveDistraction("PICKUP")
                    Log.d("FocusService", "Database: Saved Pickup Event!")
                }
            }
        }
    }

    private fun saveDistraction(type: String) {
        sessionScope.launch {
            if (currentSessionId != -1L) {
                distractionEventDao.insertEvent(
                    DistractionEvent(
                        sessionId = currentSessionId.toInt(),
                        timeStamp = System.currentTimeMillis(),
                        distractionType = type
                    )
                )
            }
        }
    }

    private fun isSystemPackage(pkg: String): Boolean {
        return pkg == "com.android.systemui" || pkg.contains("launcher") || pkg == "android"
    }


}