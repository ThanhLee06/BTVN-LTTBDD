package com.example.uthtodo.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.example.uthtodo.components.TaskColors
import com.example.uthtodo.components.TaskStyle
import com.example.uthtodo.features.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.io.File
import java.util.UUID

@Composable
fun CameraScreen(controller: PermissionController, onBack: () -> Unit, onPhoto: (TaskPhoto) -> Unit) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    var captured by rememberSaveable { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var front by rememberSaveable { mutableStateOf(false) }
    var canSwitch by remember { mutableStateOf(false) }
    var ready by remember { mutableStateOf(false) }
    val preview = remember { PreviewView(context).apply { implementationMode = PreviewView.ImplementationMode.COMPATIBLE } }
    val capture = remember { ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build() }
    val providerFuture = remember { ProcessCameraProvider.getInstance(context) }
    val allowed = controller.snapshot.granted(PermissionKind.CAMERA)
    fun cancel() { captured?.let { FeatureGraph.store.safeDelete(it) }; captured = null; onBack() }
    BackHandler { if (!busy) cancel() }
    DisposableEffect(lifecycle, allowed, front, captured) {
        ready = false
        var disposed = false
        val executor = ContextCompat.getMainExecutor(context)
        val listener = Runnable {
            if (!disposed && allowed && captured == null) runCatching {
                val provider = providerFuture.get()
                canSwitch = provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) && provider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)
                val selection = if (front) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
                val source = Preview.Builder().build().also { it.surfaceProvider = preview.surfaceProvider }
                provider.unbindAll()
                provider.bindToLifecycle(lifecycle, selection, source, capture)
                ready = true
            }.onFailure { error = "Không mở được camera. Camera có thể đang được ứng dụng khác sử dụng." }
        }
        providerFuture.addListener(listener, executor)
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                controller.refresh()
                if (!controller.snapshot.camera && providerFuture.isDone) runCatching { providerFuture.get().unbindAll() }
            }
        }
        lifecycle.lifecycle.addObserver(observer)
        onDispose {
            disposed = true; lifecycle.lifecycle.removeObserver(observer)
            if (providerFuture.isDone) runCatching { providerFuture.get().unbindAll() }
        }
    }
    TaskStyle {
        Column(Modifier.fillMaxSize().background(TaskColors.Background).statusBarsPadding().navigationBarsPadding().padding(16.dp)) {
            TextButton(onClick = ::cancel, enabled = !busy) { Text("‹ Quay lại công việc") }
            Text("Chụp ảnh đính kèm", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(12.dp))
            if (captured != null) AsyncImage(File(captured!!), "Ảnh vừa chụp", Modifier.weight(1f).fillMaxWidth(), contentScale = ContentScale.Fit)
            else if (allowed) AndroidView(factory = { preview }, modifier = Modifier.weight(1f).fillMaxWidth())
            else {
                Spacer(Modifier.weight(1f))
                Text("Cấp quyền Camera để xem hình ảnh trực tiếp.")
                Button({ controller.request(PermissionKind.CAMERA) }) { Text("Cấp quyền Camera") }
                Spacer(Modifier.weight(1f))
            }
            error?.let { Text(it, color = TaskColors.Red, modifier = Modifier.padding(vertical = 8.dp)) }
            if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
            if (captured != null) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton({ FeatureGraph.store.safeDelete(captured!!); captured = null; error = null }, Modifier.weight(1f), enabled = !busy) { Text("Chụp lại") }
                Button({
                    busy = true
                    scope.launch {
                        try {
                            val raw = captured!!
                            val photo = PhotoFiles.import(context, Uri.fromFile(File(raw)))
                            FeatureGraph.store.safeDelete(raw); captured = null
                            withContext(Dispatchers.Main.immediate) { onPhoto(photo) }
                        } catch (e: CancellationException) { throw e }
                        catch (e: Exception) { error = "Không lưu được ảnh. Kiểm tra dung lượng rồi thử lại." }
                        finally { busy = false }
                    }
                }, Modifier.weight(1f), enabled = !busy) { Text("Dùng ảnh") }
            } else {
                if (canSwitch) TextButton({ front = !front }, enabled = !busy && allowed) { Text("Đổi camera") }
                Button({
                    controller.refresh()
                    if (!controller.snapshot.camera) { controller.request(PermissionKind.CAMERA); return@Button }
                    busy = true; error = null
                    capture.targetRotation = preview.display?.rotation ?: android.view.Surface.ROTATION_0
                    val file = File(FeatureStore.photoDirectory(context), "capture-${UUID.randomUUID()}.jpg")
                    capture.takePicture(ImageCapture.OutputFileOptions.Builder(file).build(), ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(result: ImageCapture.OutputFileResults) { captured = file.absolutePath; busy = false }
                            override fun onError(exception: ImageCaptureException) { file.delete(); error = "Chụp ảnh chưa thành công. Thử lại."; busy = false }
                        })
                }, Modifier.fillMaxWidth().height(52.dp), enabled = allowed && ready && !busy) { Text("Chụp ảnh") }
            }
        }
    }
}

@Composable
fun AttachmentViewer(photo: TaskPhoto?, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Color.Black).statusBarsPadding().navigationBarsPadding()) {
        TextButton(onBack) { Text("‹ Quay lại", color = Color.White) }
        if (photo == null || !File(photo.path).exists()) Text("Ảnh không còn trên thiết bị.", color = Color.White)
        else AsyncImage(File(photo.path), "Ảnh đính kèm", Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
    }
}
