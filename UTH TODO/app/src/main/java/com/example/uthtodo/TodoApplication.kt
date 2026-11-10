package com.example.uthtodo

import android.app.Application
import com.example.uthtodo.model.TodoRepository
import com.example.uthtodo.services.AppCheckSetup
import com.example.uthtodo.services.NotificationService
import com.example.uthtodo.services.RemoteConfigService
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.os.Handler
import android.os.Looper
import com.example.uthtodo.components.AdService

class TodoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppCheckSetup.install()
        NotificationService.initialize(this)
        com.example.uthtodo.features.FeatureGraph.initialize(this)
        RemoteConfigService.initialize()
        FirebaseAuth.getInstance().addAuthStateListener {
            TodoRepository.updateWithFirebaseUser(it.currentUser)
        }
    }
}
