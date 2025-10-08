package com.ragnar.eduapp.utils

import android.util.Log
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.ragnar.eduapp.data.model.GoogleUserInfo

class GoogleUserInfoExtractor {

    companion object {

        fun extractUserInfo(googleIdTokenCredential: GoogleIdTokenCredential): GoogleUserInfo {
            return GoogleUserInfo(
                id = googleIdTokenCredential.id,
                email = googleIdTokenCredential.id, // id is mostly email address
                displayName = googleIdTokenCredential.displayName,
                profilePictureUri = googleIdTokenCredential.profilePictureUri?.toString(),
            )
        }

        fun extractAndLogUserInfo(googleIdTokenCredential: GoogleIdTokenCredential): GoogleUserInfo {
            val userInfo = extractUserInfo(googleIdTokenCredential)

            Log.d("GoogleUserInfo", "User ID: ${userInfo.id}")
            Log.d("GoogleUserInfo", "Email: ${userInfo.email}")
            Log.d("GoogleUserInfo", "Display Name: ${userInfo.displayName}")
            Log.d("GoogleUserInfo", "Profile Picture: ${userInfo.profilePictureUri}")

            return userInfo
        }
    }
}
