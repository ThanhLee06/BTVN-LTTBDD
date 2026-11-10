package com.example.uthtodo.features

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.SystemClock
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

class TaskLocationService(private val context: Context) {
    suspend fun current(): TaskLocation {
        val permission = PermissionSnapshot.read(context)
        check(permission.coarse || permission.fine) { "Chưa cấp quyền vị trí." }
        val manager = context.getSystemService(LocationManager::class.java)
        check(LocationManagerCompat.isLocationEnabled(manager)) { "Dịch vụ vị trí đang tắt. Hãy bật Vị trí trong Cài đặt hệ thống." }
        val client = LocationServices.getFusedLocationProviderClient(context)
        val cancellation = CancellationTokenSource()
        try {
            val location = withTimeout(15_000) {
                client.getCurrentLocation(if (permission.fine) Priority.PRIORITY_HIGH_ACCURACY else Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                    cancellation.token).await()
            } ?: withTimeout(3_000) { client.lastLocation.await() }?.takeIf {
                SystemClock.elapsedRealtimeNanos() - it.elapsedRealtimeNanos < 120_000_000_000L
            }
            checkNotNull(location) { "Chưa lấy được vị trí mới. Ra nơi thoáng hơn rồi thử lại." }
            val precise = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            return TaskLocation(location.latitude, location.longitude, location.accuracy, location.time, precise)
        } finally { cancellation.cancel() }
    }
}
