package com.gianessi.stargazers.network;

import com.gianessi.stargazers.models.Repo;
import com.gianessi.stargazers.models.UsersResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface GitHubService {

    @GET("/search/users")
    Call<UsersResponse> searchUsers(@Query("q") String query, @Query("page") int page);

    @GET("users/{user}/repos")
    Call<List<Repo>> listRepos(@Path("user") String username);

}
