package com.gianessi.stargazers.network

import com.gianessi.stargazers.models.Repo
import com.gianessi.stargazers.models.User
import com.gianessi.stargazers.models.UsersResponse

import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val BASE_URL = "https://api.github.com/"

class NetworkManager {

    private val service: GitHubService

    init {
        val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        service = retrofit.create(GitHubService::class.java)
    }

    fun searchUsers(query: String, page: Int): Call<UsersResponse> {
        //Search query only in username field
        var encodedQuery = query
        try {
            encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.displayName())
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return service.searchUsers("$encodedQuery+in:login", page)
    }

    fun listRepos(username: String, page: Int): Call<List<Repo>> {
        return service.listRepos(username, page)
    }

    fun listStargazers(username: String, repoName: String, page: Int): Call<List<User>> {
        return service.listStargazers(username, repoName, page)
    }

    companion object {

        const val FIRST_PAGE_INDEX = 1

        val instance = NetworkManager()
    }

}
