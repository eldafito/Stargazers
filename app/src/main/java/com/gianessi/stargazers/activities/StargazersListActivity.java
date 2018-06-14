package com.gianessi.stargazers.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.gianessi.stargazers.R;
import com.gianessi.stargazers.adapters.UsersAdapter;
import com.gianessi.stargazers.models.Repo;
import com.gianessi.stargazers.models.User;
import com.gianessi.stargazers.network.NetworkManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StargazersListActivity extends AppCompatActivity {

    private static final String TAG = "StargazersListActivity";

    private final List<User> stargazers = new ArrayList<>();
    private UsersAdapter adapter;

    // Need these values for future API calls
    // GitHub API requests start from page 1 (page will be incremented at the first call)
    private Integer page = NetworkManager.FIRST_PAGE_INDEX - 1;

    private String username;
    private String repoName;

    private View emptyPlaceholder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stargazers_list);
        RecyclerView recyclerView = findViewById(R.id.stargazers_list_recycler);
        this.emptyPlaceholder = findViewById(R.id.stargazers_empty_placeholder);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        this.adapter = new UsersAdapter(this.stargazers);
        recyclerView.setAdapter(adapter);

        // Load more items when scroll reach the bottom
        recyclerView.addOnScrollListener(new PaginationScrollListener());

        Intent intent = getIntent();
        if (intent != null) {
            this.username = intent.getStringExtra(User.USERNAME);
            this.setRepoName(intent.getStringExtra(Repo.NAME));
            this.requestMoreStargazers();
        }
    }

    private void setRepoName(String repoName){
        this.repoName = repoName;
        this.setTitle(repoName);
    }

    private void requestMoreStargazers() {
        // page will be null if end of list reached
        if (page == null || username == null || repoName == null)
            return;
        this.page++;
        this.setLoading(true);
        NetworkManager.getInstance().listStargazers(username, repoName, page).enqueue(new StargazersNetworkListener());
    }

    private void addStargazers(List<User> stargazers) {
        this.stargazers.addAll(stargazers);
        this.adapter.notifyItemRangeInserted(this.stargazers.size() - stargazers.size(), stargazers.size());
    }

    private void checkNoData(){
        this.emptyPlaceholder.setVisibility(this.stargazers.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private boolean isLoading() {
        return !this.stargazers.isEmpty() && this.stargazers.get(this.stargazers.size() - 1) == null;
    }

    // Update adapter to show progressview or not
    private void setLoading(boolean loading) {
        boolean oldValue = this.isLoading();
        if (oldValue == loading)
            return;
        if (oldValue) {
            int lastIndex = this.stargazers.size() - 1;
            this.stargazers.remove(lastIndex);
            this.adapter.notifyItemRemoved(lastIndex);
        } else {
            this.stargazers.add(null);
            this.adapter.notifyItemInserted(this.stargazers.size() - 1);
        }
    }


    private class StargazersNetworkListener implements Callback<List<User>> {

        @Override
        public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {
            StargazersListActivity.this.setLoading(false);
            if (!response.isSuccessful()) {
                Log.e(TAG, "Bad Response");
                return;
            }
            List<User> body = response.body();
            if (body == null || body.isEmpty())
                page = null;
            else
                StargazersListActivity.this.addStargazers(body);
            checkNoData();
        }

        @Override
        public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {
            StargazersListActivity.this.setLoading(false);
            Log.e(TAG, t.getLocalizedMessage());
        }
    }

    private class PaginationScrollListener extends RecyclerView.OnScrollListener {

        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
            if (lm == null)
                return;
            int visibleItemCount = lm.getChildCount();
            int totalItemCount = lm.getItemCount();
            int firstVisibleItemPosition = lm.findFirstVisibleItemPosition();

            if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                recyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        requestMoreStargazers();
                    }
                });
            }
        }

    }
}
