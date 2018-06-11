package com.gianessi.stargazers.activities;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;

import com.gianessi.stargazers.R;
import com.gianessi.stargazers.adapters.UsersAdapter;
import com.gianessi.stargazers.models.User;
import com.gianessi.stargazers.models.UsersResponse;
import com.gianessi.stargazers.network.NetworkManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class UsersListActivity extends AppCompatActivity {

    private static final String TAG = "UsersListActivity";
    private static final int MAX_ITEMS = 30;
    private static final int SEARCH_DELAY_MILLIS = 1000;

    private Handler liveSearchHandler = new Handler();
    private LiveSearchRunnable liveSearchRunnable;
    private Call<UsersResponse> call;
    private List<User> users = new ArrayList<>();
    private UsersAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);
        RecyclerView recyclerView = findViewById(R.id.users_list_recycler);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        this.adapter = new UsersAdapter(this.users);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.users_list, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setOnQueryTextListener(new UsersQueryTextListener());

        return true;
    }

    public void requestUsers(String query) {
        if(this.call != null)
            this.call.cancel();
        int page = 0;
        this.clearUsers();
        this.call = NetworkManager.getInstance().getService().searchUsers(query, page);
        this.call.enqueue(new UsersNetworkListener(query,page));
    }

    public void clearUsers(){
        this.users.clear();
        this.adapter.notifyDataSetChanged();
    }

    public void addUsers(List<User> users) {
        this.users.addAll(users);
        this.adapter.notifyItemRangeInserted(this.users.size() - users.size(), users.size());
        Log.i(TAG, String.format("%d utenti", this.users.size()));
    }

    private class UsersQueryTextListener implements SearchView.OnQueryTextListener{

        @Override
        public boolean onQueryTextSubmit(String query) {
            liveSearchHandler.removeCallbacks(liveSearchRunnable);
            liveSearchRunnable = new LiveSearchRunnable(query);
            liveSearchHandler.post(liveSearchRunnable);
            return true;
        }

        @Override
        public boolean onQueryTextChange(final String query) {
            Log.i(TAG, query);
            UsersListActivity.this.clearUsers();
            if(TextUtils.isEmpty(query))
                return true;
            liveSearchHandler.removeCallbacks(liveSearchRunnable);
            liveSearchRunnable = new LiveSearchRunnable(query);
            liveSearchHandler.postDelayed(liveSearchRunnable, SEARCH_DELAY_MILLIS);
            return true;
        }
    }

    private class UsersNetworkListener implements Callback<UsersResponse>{

        private String query;
        private int page;

        private UsersNetworkListener(String query, int page) {
            this.query = query;
            this.page = page;
        }

        @Override
        public void onResponse(@NonNull Call<UsersResponse> call, @NonNull Response<UsersResponse> response) {
            if(!response.isSuccessful()) {
                Log.e(TAG, "Bad Response");
                return;
            }
            UsersResponse body = response.body();
            if( body == null)
                return;
            List<User> result = body.getItems();
            UsersListActivity.this.addUsers(result);
            if(users.size() < MAX_ITEMS && users.size() < body.getTotalCount()) {
                UsersListActivity.this.call = NetworkManager.getInstance().getService().searchUsers(query, ++page);
                UsersListActivity.this.call.enqueue(this);
            }
        }

        @Override
        public void onFailure(@NonNull Call<UsersResponse> call, @NonNull Throwable t) {
            Log.e(TAG, t.getLocalizedMessage());
        }
    }

    private class LiveSearchRunnable implements Runnable {

        private String query;

        private LiveSearchRunnable(String query) {
            this.query = query;
        }

        @Override
        public void run() {
            UsersListActivity.this.requestUsers(query);
        }
    }

}
