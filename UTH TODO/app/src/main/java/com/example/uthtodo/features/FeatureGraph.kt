package com.example.uthtodo.features

import android.content.Context
import android.content.Intent
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow

object FeatureGraph {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    lateinit var store: FeatureStore
        private set
    lateinit var scheduler: DeviceReminderScheduler
        private set
    lateinit var preferences: PermissionPreferences
        private set
    val destination = MutableStateFlow<ReminderDestination?>(null)
    fun initialize(context: Context) {
        scheduler = DeviceReminderScheduler(context)
        val db = Room.databaseBuilder(context, FeatureDatabase::class.java, "uth_features.db").build()
        store = FeatureStore(context, db.records(), scheduler, scope)
        preferences = PermissionPreferences(context)
        store.bind(FirebaseAuth.getInstance().currentUser?.uid)
    }
    fun handleIntent(intent: Intent?) {
        val uid = intent?.getStringExtra("reminder_owner") ?: return
        val id = intent.getStringExtra("reminder_task") ?: return
        val identity = intent.getStringExtra("reminder_identity") ?: return
        destination.value = ReminderDestination(uid, id, identity)
        intent.removeExtra("reminder_owner")
    }
}
