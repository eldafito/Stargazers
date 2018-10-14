package com.gianessi.stargazers.models

import android.os.Parcelable

import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
        @SerializedName("login") var username: String?,
        @SerializedName("avatar_url") var avatarUrl: String?) : Parcelable {

    companion object {
        const val ENTITY = "user"
        const val USERNAME = "username"
    }
}
