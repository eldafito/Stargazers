package com.gianessi.stargazers.models

import com.google.gson.annotations.SerializedName

class UsersResponse(
        @SerializedName("total_count") val totalCount: Int,
        @SerializedName("incomplete_results") val isIncompleteResults: Boolean,
        @SerializedName("items") val items: List<User>?)