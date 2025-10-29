package com.ragnar.eduapp.core

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.credentials.CredentialManager
import androidx.credentials.CredentialOption
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.media3.common.BuildConfig
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.ragnar.eduapp.R
import com.ragnar.eduapp.data.dataClass.GoogleUserInfo
import com.ragnar.eduapp.utils.DebugLogger
import com.ragnar.eduapp.utils.GoogleUserInfoExtractor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class GoogleSignIn {

    companion object {
        fun doGoogleSignIn(
            context: Context,
            scope: CoroutineScope,
            launcher: ManagedActivityResultLauncher<Intent, ActivityResult>?,
            onLoginSuccess: (userInfo: GoogleUserInfo) -> Unit // a lambda method that will take GoogleUserInfo as parameter and return no value used to handle Login Success case
            //  It works as a callback
        ) {
            val credentialManager = CredentialManager.create(context)

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(getCredentialOptions(context))
                .build()

            scope.launch {
                try {
                    val result = credentialManager.getCredential(context, request)
                    when(result.credential) {
                        is CustomCredential -> {
                            if(result.credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)

                                // Extract user information
                                val userInfo : GoogleUserInfo = GoogleUserInfoExtractor.extractAndLogUserInfo(googleIdTokenCredential)


                                // Call success callback with user info
                                onLoginSuccess(userInfo)
                            }
                        }
                        else -> {
                            DebugLogger.errorLog("GoogleSignIn", "Unexpected credential type: ${result.credential.type}")
                        }
                    }
                } catch (e: NoCredentialException) {
                    DebugLogger.errorLog("GoogleSignIn", "No credentials found $e")
                    launcher?.launch(getIntent())
                } catch (e: GetCredentialException) {
                    DebugLogger.errorLog("GoogleSignIn", "Credential exception $e")
                    e.printStackTrace()
                }
            }
        }

        private fun getIntent(): Intent {
            return Intent(Settings.ACTION_ADD_ACCOUNT).apply {
                putExtra(Settings.EXTRA_ACCOUNT_TYPES, arrayOf("com.google"))
            }
        }

        private fun getCredentialOptions(context: Context): CredentialOption {
            return GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false) //show all accounts
                .setAutoSelectEnabled(false) // avoid auto login
                .setServerClientId(context.getString(R.string.web_client_id))
                .build()
        }
    }
}