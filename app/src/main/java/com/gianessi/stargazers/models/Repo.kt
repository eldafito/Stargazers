package com.gianessi.stargazers.models

import com.google.gson.annotations.SerializedName



data class Repo(
        @SerializedName("id") val id: Long,
        @SerializedName("name") val name: String?) {

    companion object {
        const val NAME = "reponame"
    }
}
