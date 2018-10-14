package com.gianessi.stargazers.network

import com.gianessi.stargazers.models.Repo
import com.gianessi.stargazers.models.User
import com.gianessi.stargazers.models.UsersResponse

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

internal interface GitHubService {

    @GET("search/users")
    fun searchUsers(@Query(value = "q", encoded = true) query: String, @Query("page") page: Int): Call<UsersResponse>

    @GET("users/{user}/repos")
    fun listRepos(@Path("user") username: String, @Query("page") page: Int): Call<List<Repo>>

    @GET("repos/{owner}/{repo}/stargazers")
    fun listStargazers(@Path("owner") owner: String, @Path("repo") repo: String, @Query("page") page: Int): Call<List<User>>

}
