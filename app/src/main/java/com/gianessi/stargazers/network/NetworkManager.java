package com.gianessi.stargazers.network;

import com.gianessi.stargazers.models.Repo;
import com.gianessi.stargazers.models.User;
import com.gianessi.stargazers.models.UsersResponse;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkManager {

    public static final int FIRST_PAGE_INDEX = 1;
    private static final String BASE_URL = "https://api.github.com/";

    private static final NetworkManager ourInstance = new NetworkManager();

    public static NetworkManager getInstance() {
        return ourInstance;
    }

    private GitHubService service;

    private NetworkManager() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(GitHubService.class);
    }

    public Call<UsersResponse> searchUsers(String query, int page){
        //Search query only in username field
        String encodedQuery = query;
        try{
            encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.displayName());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return service.searchUsers(String.format("%s+in:login", encodedQuery), page);
    }

    public Call<List<Repo>> listRepos(String username, int page){
        return service.listRepos(username, page);
    }

    public Call<List<User>> listStargazers(String username, String repoName, int page){
        return service.listStargazers(username, repoName, page);
    }

}
