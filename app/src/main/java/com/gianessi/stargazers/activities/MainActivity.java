package com.gianessi.stargazers.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.gianessi.stargazers.R;
import com.gianessi.stargazers.adapters.ReposAdapter;
import com.gianessi.stargazers.models.Repo;
import com.gianessi.stargazers.models.User;
import com.gianessi.stargazers.network.NetworkManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String TAG = "MainActivity";

    private static final int USERS_LIST_REQ = 34;

    private User user;
    private Repo repo;
    private List<Repo> userRepos = new ArrayList<>();
    private ReposAdapter adapter;

    // Need these values for future API calls
    // GitHub API requests start from page 1 (page will be incremented at the first call)
    private Integer page = NetworkManager.FIRST_PAGE_INDEX - 1;

    private TextView usernameTxt;
    private ImageView avatarImg;
    private ProgressBar progressBar;
    private Button submitBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.main_progress);
        submitBtn = findViewById(R.id.main_submit_btn);
        usernameTxt = findViewById(R.id.user_username_txt);
        avatarImg = findViewById(R.id.user_avatar_img);

        findViewById(R.id.main_user).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseUser();
            }
        });

        Spinner repoSpinner = findViewById(R.id.main_repo_spinner);
        adapter = new ReposAdapter(this, userRepos);
        repoSpinner.setAdapter(adapter);
        repoSpinner.setOnItemSelectedListener(this);

        this.setUser(null);
    }

    private void setLoading(boolean loading){
        this.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    public void setUser(User user) {
        this.user = user;
        this.clearUserRepos();
        if(user != null) {
            this.usernameTxt.setText(user.getUsername());
            Glide.with(this).load(user.getAvatarUrl()).apply(RequestOptions.circleCropTransform()).into(avatarImg);
            this.requestMoreUserRepos();
        }else{
            this.usernameTxt.setText(R.string.no_user_hint);
            this.avatarImg.setImageResource(R.drawable.avatar_placeholder);
        }
    }

    public void clearUserRepos(){
        this.page = 0;
        this.userRepos.clear();
        this.adapter.notifyDataSetChanged();
        this.setRepo(null);
    }

    public void addUserRepos(List<Repo> userRepos) {
        this.userRepos.addAll(userRepos);
        this.adapter.notifyDataSetChanged();
    }

    public void setRepo(Repo repo) {
        this.repo = repo;
        this.submitBtn.setEnabled(repo != null);
    }

    private void requestMoreUserRepos(){
        if(this.page == null || this.user == null)
            return;
        this.page ++;
        this.setLoading(true);
        NetworkManager.getInstance().getService().listRepos(user.getUsername(), page).enqueue(new UserReposNetworkListener());
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

    public void discoverStargazers(View view){
        if(user == null || repo == null)
            return;
        Intent intent = new Intent(this, StargazersListActivity.class);
        intent.putExtra(User.USERNAME, user.getUsername());
        intent.putExtra(Repo.NAME, repo.getName());
        startActivity(intent);
    }

    @Override
    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
        this.setRepo(this.userRepos.get(position));
    }

    @Override
    public void onNothingSelected(AdapterView<?> parentView) {
        this.setRepo(null);
    }


    private class UserReposNetworkListener implements Callback<List<Repo>> {

        @Override
        public void onResponse(@NonNull Call<List<Repo>> call, @NonNull Response<List<Repo>> response) {
            setLoading(false);
            if(!response.isSuccessful()) {
                Log.e(TAG, "Bad Response");
                return;
            }
            List<Repo> body = response.body();
            if(body == null || body.isEmpty())
                MainActivity.this.page = null;
            else {
                MainActivity.this.addUserRepos(body);
                MainActivity.this.requestMoreUserRepos();
            }
        }

        @Override
        public void onFailure(@NonNull Call<List<Repo>> call, @NonNull Throwable t) {
            setLoading(false);
            Log.e(TAG, t.getLocalizedMessage());
        }
    }
}
