package com.example.uthtodo.model

import androidx.compose.runtime.mutableStateOf
import com.example.uthtodo.data.TaskGraph
import com.example.uthtodo.services.AnalyticsService
import com.example.uthtodo.services.CrashlyticsService
import com.example.uthtodo.services.NotificationService
import com.example.uthtodo.services.PresenceService
import com.google.firebase.auth.FirebaseUser

/** Firebase owns identity and device services; task data is owned by the REST repository. */
object TodoRepository {
    val currentUser = mutableStateOf(UserProfile())
    private var boundUid: String? = null

    fun updateWithFirebaseUser(user: FirebaseUser?) {
        currentUser.value = if (user == null) UserProfile() else UserProfile(
            uid = user.uid,
            name = user.displayName?.takeIf { it.isNotBlank() }
                ?: if (user.isAnonymous) "Khách" else user.email?.substringBefore('@') ?: "Người dùng",
            email = user.email,
            isAnonymous = user.isAnonymous,
            isGoogleLinked = user.providerData.any { it.providerId == "google.com" },
            photoUrl = user.photoUrl?.toString()
        )
        if (boundUid == user?.uid) return
        PresenceService.stop()
        TaskGraph.repository.clear()
        com.example.uthtodo.features.FeatureGraph.store.bind(user?.uid)
        boundUid = user?.uid
        NotificationService.bindUser(user?.uid)
        CrashlyticsService.setUserId(user?.uid.orEmpty())
        AnalyticsService.setUser(user)
        if (user != null) {
            PresenceService.start(user.uid)
            NotificationService.registerDevice()
        }
    }
}
