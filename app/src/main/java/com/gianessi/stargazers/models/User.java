package com.gianessi.stargazers.models;

import com.google.gson.annotations.SerializedName;

public class User {

    public static final String USERNAME = "username";

    @SerializedName("login")
    private String username;
    @SerializedName("avatar_url")
    private String avatarUrl;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

}
