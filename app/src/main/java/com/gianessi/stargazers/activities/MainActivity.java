package com.gianessi.stargazers.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Spinner;

import com.gianessi.stargazers.R;
import com.gianessi.stargazers.adapters.ReposAdapter;
import com.gianessi.stargazers.adapters.UsersAdapter;
import com.gianessi.stargazers.models.Repo;
import com.gianessi.stargazers.models.User;
import com.gianessi.stargazers.network.NetworkManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int USERS_LIST_REQ = 34;

    private User user;
    private Repo repo;
    private List<Repo> userRepos = new ArrayList<>();
    private ReposAdapter adapter;

    private UsersAdapter.UserViewHolder userViewHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userViewHolder = new UsersAdapter.UserViewHolder(findViewById(R.id.main_user));
        userViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseUser();
            }
        });

        Spinner repoSpinner = findViewById(R.id.main_repo_spinner);
        adapter = new ReposAdapter(this, userRepos);
        repoSpinner.setAdapter(adapter);
    }

    public void setUser(User user) {
        this.user = user;
        this.clearUserRepos();
        this.userViewHolder.bind(user);
        if(user != null)
            this.requestUserRepos();
    }

    public void clearUserRepos(){
        this.userRepos.clear();
        this.adapter.notifyDataSetChanged();
    }

    public void setUserRepos(List<Repo> userRepos) {
        this.userRepos.clear();
        this.userRepos.addAll(userRepos);
        this.adapter.notifyDataSetChanged();
    }

    public void setRepo(Repo repo) {
        this.repo = repo;
    }

    private void requestUserRepos(){
        if(this.user == null)
            return;
        NetworkManager.getInstance().getService().listRepos(user.getUsername()).enqueue(new UserReposNetworkListener());
    }

    private void chooseUser(){
        Intent intent = new Intent(this, UsersListActivity.class);
        startActivityForResult(intent, USERS_LIST_REQ);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == USERS_LIST_REQ) {
            if (resultCode == RESULT_OK && data.hasExtra(User.ENTITY)) {
                this.setUser((User) data.getParcelableExtra(User.ENTITY));
            }
        }
    }

    private class UserReposNetworkListener implements Callback<List<Repo>> {

        @Override
        public void onResponse(@NonNull Call<List<Repo>> call, @NonNull Response<List<Repo>> response) {
            if(!response.isSuccessful()) {
                Log.e(TAG, "Bad Response");
                return;
            }
            List<Repo> body = response.body();
            if( body == null)
                return;
            MainActivity.this.setUserRepos(body);
        }

        @Override
        public void onFailure(@NonNull Call<List<Repo>> call, @NonNull Throwable t) {
            Log.e(TAG, t.getLocalizedMessage());
        }
    }
}
