package com.example.uthtodo.auth

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.example.uthtodo.model.TodoRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import com.example.uthtodo.services.AnalyticsService
import com.example.uthtodo.services.NotificationService
import com.example.uthtodo.services.PresenceService

object AuthManager {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    fun initAuthState() {
        TodoRepository.updateWithFirebaseUser(auth.currentUser)
    }

    suspend fun signInAnonymously(): FirebaseUser {
        val user = checkNotNull(auth.signInAnonymously().await().user) {
            "Không nhận được thông tin tài khoản khách."
        }
        TodoRepository.updateWithFirebaseUser(user)
        AnalyticsService.logLogin("anonymous")
        return user
    }

    suspend fun signInWithGoogleToken(
        idToken: String,
        linkAnonymous: Boolean = false
    ): FirebaseUser {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val currentUser = auth.currentUser
        // Linking upgrades the guest account without changing its Firebase UID.
        val result = if (linkAnonymous && currentUser?.isAnonymous == true) {
            currentUser.linkWithCredential(credential).await()
        } else {
            auth.signInWithCredential(credential).await()
        }
        val user = checkNotNull(result.user) { "Không nhận được thông tin tài khoản Google." }
        TodoRepository.updateWithFirebaseUser(user)
        AnalyticsService.logLogin("google")
        return user
    }

    suspend fun signOut(context: Context) {
        withTimeoutOrNull(2500) {
            runCatching { NotificationService.unregisterDevice() }
        }
        PresenceService.stop()
        auth.signOut()
        TodoRepository.updateWithFirebaseUser(null)
        try {
            CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
        } catch (error: ClearCredentialException) {
            // Firebase is already signed out, even if the credential provider is unavailable.
            Log.w("AuthManager", "Could not clear credential provider session", error)
        }
    }

    fun errorMessage(error: Exception): String = when (error) {
        is GetCredentialCancellationException -> "Bạn đã hủy đăng nhập Google."
        is NoCredentialException -> "Không tìm thấy tài khoản Google. Hãy thêm tài khoản Google trên thiết bị rồi thử lại."
        is FirebaseNetworkException -> "Không có kết nối mạng. Vui lòng kiểm tra Internet và thử lại."
        is FirebaseTooManyRequestsException -> "Bạn đã thử quá nhiều lần. Vui lòng thử lại sau."
        is FirebaseAuthUserCollisionException -> "Tài khoản Google này đã được sử dụng. Hãy đăng xuất tài khoản khách rồi đăng nhập bằng Google."
        is FirebaseAuthException -> when (error.errorCode) {
            "ERROR_OPERATION_NOT_ALLOWED" -> "Phương thức đăng nhập chưa được bật trong Firebase."
            "ERROR_USER_DISABLED" -> "Tài khoản này đã bị vô hiệu hóa."
            "ERROR_INVALID_CREDENTIAL" -> "Thông tin đăng nhập không hợp lệ. Vui lòng thử lại."
            else -> error.localizedMessage ?: "Đăng nhập thất bại. Vui lòng thử lại."
        }
        else -> error.localizedMessage ?: "Không thể đăng nhập. Vui lòng thử lại."
    }
}
