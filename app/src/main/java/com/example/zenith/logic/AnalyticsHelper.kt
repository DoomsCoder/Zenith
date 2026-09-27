package com.example.zenith.logic

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

class AnalyticsHelper(context: Context) {
    private val firebaseAnalytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(context)

    fun logSessionStarted(missionName: String, plannedMinutes: Int) {
        val bundle = Bundle().apply {
            putString("mission_name", missionName)
            putInt("planned_duration_minutes", plannedMinutes)
        }
        firebaseAnalytics.logEvent("session_started", bundle)
    }

    fun logSessionFinished(missionName: String, actualDurationSeconds: Int) {
        val bundle = Bundle().apply {
            putString("mission_name", missionName)
            putInt("actual_duration_seconds", actualDurationSeconds)
        }
        firebaseAnalytics.logEvent("session_finished", bundle)
    }

    fun logSessionFailed(missionName: String, failureReason: String) {
        val bundle = Bundle().apply {
            putString("mission_name", missionName)
            putString("failure_reason", failureReason)
        }
        firebaseAnalytics.logEvent("session_failed", bundle)
    }
}
