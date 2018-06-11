package com.gianessi.stargazers.network;

import com.gianessi.stargazers.models.User;
import com.gianessi.stargazers.models.UsersResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GitHubService {

    @GET("/search/users")
    Call<UsersResponse> searchUsers(@Query("q") String query, @Query("page") int page);

}
