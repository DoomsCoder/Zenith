package com.example.zenith.ui.screens.settings

import android.app.Application
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.zenith.data.AppDatabase
import com.example.zenith.data.WhitelistedApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WhitelistViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).whitelistedAppDao()
    private val packageManager = application.packageManager

    val whitelistedApps: StateFlow<List<WhitelistedApp>> = dao.getAllWhitelistedApps()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val installedApps = _installedApps.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        loadInstalledApps()
    }

    private fun loadInstalledApps() {
        viewModelScope.launch {
            _isLoading.value = true
            val apps = withContext(Dispatchers.IO) {
                // Query all launcher activities to get apps that the user actually interacts with
                val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                }
                val resolveInfos = packageManager.queryIntentActivities(mainIntent, 0)
                
                resolveInfos.mapNotNull { resolveInfo ->
                    val packageName = resolveInfo.activityInfo.packageName
                    if (packageName == getApplication<Application>().packageName) return@mapNotNull null
                    
                    val appInfo = try {
                        packageManager.getApplicationInfo(packageName, 0)
                    } catch (e: Exception) {
                        return@mapNotNull null
                    }
                    
                    val appLabel = resolveInfo.loadLabel(packageManager).toString()
                    if (isDistractionApp(appInfo, appLabel)) return@mapNotNull null
                    
                    AppInfo(
                        packageName = packageName,
                        appName = appLabel,
                        category = appInfo.category,
                        icon = resolveInfo.loadIcon(packageManager)
                    )
                }.distinctBy { it.packageName }.sortedBy { it.appName }
            }
            _installedApps.value = apps
            _isLoading.value = false
        }
    }

    private fun isDistractionApp(info: ApplicationInfo, label: String): Boolean {
        val identifyString = (info.packageName + " " + label).lowercase()

        val strictlyForbidden = listOf(
            "instagram", "facebook", "tiktok", "twitter", "x.android", "snapchat", "reddit",
            "youtube", "netflix", "hotstar", "primevideo", "disney", "voot", "zee5", "sony", "jiocinema",
            "streaming", "ott", "anime", "manga", "game", "arcade", "casino", "betting", "browser", "chrome", "firefox",
            "drive", "google", "search", "lens", "assistant", "files", "manager", "explorer", "gallery", "photos", "player"
        )
        if (strictlyForbidden.any { identifyString.contains(it) }) return true

        val isOfficiallyProductive = when (info.category) {
            ApplicationInfo.CATEGORY_PRODUCTIVITY,
            ApplicationInfo.CATEGORY_MAPS -> true
            else -> false
        }
        if (isOfficiallyProductive) return false

        val productiveKeywords = listOf(
            "calc", "clock", "note", "calendar", "mail", "authenticator", 
            "terminal", "code", "editor", "ide", "git", "compiler",
            "pdf", "reader", "dictionary", "translate", "voice", "recorder",
            "slack", "teams", "meet", "zoom", "workspace", "education", "study", "learn"
        )
        if (productiveKeywords.any { identifyString.contains(it) }) return false

        return true 
    }

    fun toggleWhitelist(packageName: String, appName: String, isWhitelisted: Boolean) {
        viewModelScope.launch {
            if (isWhitelisted) {
                dao.insertApp(WhitelistedApp(packageName, appName))
            } else {
                dao.deleteApp(WhitelistedApp(packageName, appName))
            }
        }
    }
}

data class AppInfo(
    val packageName: String,
    val appName: String,
    val category: Int,
    val icon: android.graphics.drawable.Drawable? = null
)
