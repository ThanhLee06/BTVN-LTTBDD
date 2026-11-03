package com.example.uthtodo.services

import android.os.Bundle
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.inappmessaging.FirebaseInAppMessaging
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import androidx.compose.runtime.mutableStateOf
import com.example.uthtodo.BuildConfig

object AnalyticsService {
    private val analytics get() = FirebaseAnalytics.getInstance(FirebaseAuth.getInstance().app.applicationContext)
    fun setUser(user: FirebaseUser?) {
        analytics.setUserId(user?.uid)
        analytics.setUserProperty("account_type", when { user == null -> null; user.isAnonymous -> "guest"; else -> "registered" })
    }
    fun logScreen(screen: String) {
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screen)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, "MainActivity")
        })
        FirebaseInAppMessaging.getInstance().triggerEvent("screen_$screen")
    }
    fun logLogin(method: String) {
        analytics.logEvent(FirebaseAnalytics.Event.LOGIN, Bundle().apply { putString(FirebaseAnalytics.Param.METHOD, method) })
    }
    fun event(name: String, category: String? = null, completed: Boolean? = null) {
        analytics.logEvent(name, Bundle().apply {
            category?.let { putString("category", it) }
            completed?.let { putLong("completed", if (it) 1 else 0) }
        })
        FirebaseInAppMessaging.getInstance().triggerEvent(name)
    }
}

object CrashlyticsService {
    fun setUserId(uid: String) { FirebaseCrashlytics.getInstance().setUserId(uid) }
    fun logError(operation: String, error: Throwable) {
        FirebaseCrashlytics.getInstance().log(operation)
        // Avoid recording task text, email or FCM tokens.
        FirebaseCrashlytics.getInstance().recordException(IllegalStateException("$operation (${error.javaClass.simpleName})"))
    }
    fun sendDemoReport() {
        FirebaseCrashlytics.getInstance().recordException(IllegalStateException("UTH Todo BT2 non-fatal verification"))
    }
}

object RemoteConfigService {
    val banner = mutableStateOf("Mỗi bước nhỏ hôm nay đều có ý nghĩa!")
    val showAds = mutableStateOf(true)
    val status = mutableStateOf("Đang tải cấu hình")
    private val config get() = FirebaseRemoteConfig.getInstance()
    fun initialize() {
        config.setConfigSettingsAsync(FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(if (BuildConfig.DEBUG) 0 else 3600).build())
        config.setDefaultsAsync(mapOf("banner_message" to banner.value, "show_test_ads" to true))
            .addOnCompleteListener { refresh() }
        config.addOnConfigUpdateListener(object : ConfigUpdateListener {
            override fun onUpdate(update: ConfigUpdate) { config.activate().addOnSuccessListener { applyValues() } }
            override fun onError(error: FirebaseRemoteConfigException) { status.value = "Đang dùng cấu hình đã lưu" }
        })
    }
    fun refresh() {
        config.fetchAndActivate().addOnCompleteListener { task ->
            applyValues()
            status.value = if (task.isSuccessful) "Đã cập nhật cấu hình" else "Đang dùng cấu hình đã lưu"
        }
    }
    private fun applyValues() {
        banner.value = config.getString("banner_message").ifBlank { "Mỗi bước nhỏ hôm nay đều có ý nghĩa!" }
        showAds.value = config.getBoolean("show_test_ads")
    }
}
