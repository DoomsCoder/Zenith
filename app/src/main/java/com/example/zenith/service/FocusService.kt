package com.example.zenith.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
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
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
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

    private var lastEventTime: Long = 0

    private val sessionScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * onBind is required by the Service class.
     * There are two types of services:
     * 1. Bound: The Activity and Service "talk" to each other.
     * 2. Started: The Service runs independently.
     * For Zenith, we use a 'Started Service', so we return null.
     */
    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * Entry point of the service. Triggered by createNotificationChannel() from the Activity.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
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

        sessionScope.launch {
            // Only create new session if we don't have active event
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

            while (true) {

                checkForegroundApp()
                delay(2000)
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

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)

        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()

        sensorManager.unregisterListener(this)

        // Update the session in background
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
        }

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

    private suspend fun checkForegroundApp() {

        val endtime = System.currentTimeMillis()
        val beginTime = endtime - (1000 * 60 * 10)
        val queryUsageStatsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            beginTime,
            endtime
        )

        val mostRecentPackage = queryUsageStatsList.maxByOrNull { it.lastTimeUsed }?.packageName

        if (endtime - lastEventTime > 5000) {
            lastEventTime = endtime

            if (mostRecentPackage != null && mostRecentPackage != packageName) {
                val event = DistractionEvent(
                    sessionId = currentSessionId.toInt(),
                    timeStamp = lastEventTime,
                    distractionType = "APP_SWITCH"
                )

                distractionEventDao.insertEvent(event)

                Log.d("FocusService","Database: Saved App Switch Event")
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val magnitude = sqrt(x * x + y * y + z * z)

            val threshold = 1.5f

            if (abs( magnitude - SensorManager.GRAVITY_EARTH) >= threshold) {
                val currentTime = System.currentTimeMillis()

                if (currentTime - lastEventTime > 5000) {
                    lastEventTime = currentTime

                    Log.d("FocusService", "Pickup detected! Magnitude: $magnitude")

                    sessionScope.launch {
                        val event = DistractionEvent(
                            sessionId = currentSessionId.toInt(),
                            timeStamp = lastEventTime,
                            distractionType = "PICKUP"
                        )

                        distractionEventDao.insertEvent(event)
                        Log.d("FocusService", "Database: Saved Pickup Event!")
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }
}