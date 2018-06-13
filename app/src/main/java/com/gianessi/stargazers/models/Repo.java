package com.gianessi.stargazers.models;

import com.google.gson.annotations.SerializedName;

public class Repo {

    public static final String NAME = "reponame";

    @SerializedName("id")
    private long id;
    @SerializedName("name")
    private String name;

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
