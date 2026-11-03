package com.example.uthtodo.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.example.uthtodo.R
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseUser

object GoogleSignInHelper {
    suspend fun signIn(context: Context, linkAnonymous: Boolean = false): FirebaseUser {
        // Use the explicit Google button flow and the Web OAuth client ID.
        val googleOption = GetSignInWithGoogleOption.Builder(
            context.getString(R.string.default_web_client_id)
        ).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleOption)
            .build()
        val credential = CredentialManager.create(context)
            .getCredential(context = context, request = request).credential

        check(credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            "Không nhận được thông tin đăng nhập Google hợp lệ."
        }
        val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
        return AuthManager.signInWithGoogleToken(googleCredential.idToken, linkAnonymous)
    }
}
