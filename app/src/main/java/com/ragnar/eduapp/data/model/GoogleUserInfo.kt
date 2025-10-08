package com.ragnar.eduapp.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GoogleUserInfo(
    val id: String,
    val email: String,
    val displayName: String?,
    val profilePictureUri: String?,
) : Parcelable