package com.example.uthtodo.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

object AdService {
    val ready = mutableStateOf(false)
    private var initializing = false
    @Synchronized fun initialize(context: Context) {
        if (initializing) return
        initializing = true
        val app = context.applicationContext
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            MobileAds.initialize(app) { Handler(Looper.getMainLooper()).post { ready.value = true } }
        }
    }
}

/**
 * 7. GOOGLE ADMOB BANNER (Nhóm EARN trong hệ sinh thái Firebase)
 */
@Composable
fun AdMobBanner(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    LaunchedEffect(context) { AdService.initialize(context) }
    if (!AdService.ready.value) return
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                // Test Banner Ad Unit ID chính thức của Google
                adUnitId = "ca-app-pub-3940256099942544/6300978111"
                loadAd(AdRequest.Builder().build())
            }
        },
        onRelease = { it.destroy() }
    )
}

