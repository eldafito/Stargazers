package com.gianessi.stargazers.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;

import com.gianessi.stargazers.R;
import com.gianessi.stargazers.adapters.UsersAdapter;
import com.gianessi.stargazers.listeners.OnUserSelectedListener;
import com.gianessi.stargazers.models.User;
import com.gianessi.stargazers.models.UsersResponse;
import com.gianessi.stargazers.network.NetworkManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class UsersListActivity extends AppCompatActivity implements OnUserSelectedListener {

    private static final String TAG = "UsersListActivity";
    private static final int SEARCH_DELAY_MILLIS = 600;

    private Handler liveSearchHandler = new Handler();
    private LiveSearchRunnable liveSearchRunnable;
    private Call<UsersResponse> call;
    private List<User> users = new ArrayList<>();
    private UsersAdapter adapter;

    // Need these values for future API calls
    private int page = 0;
    private String query;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);
        RecyclerView recyclerView = findViewById(R.id.users_list_recycler);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        this.adapter = new UsersAdapter(this.users);
        recyclerView.setAdapter(adapter);

        // Load more items when scroll reach the bottom
        recyclerView.addOnScrollListener(new PaginationScrollListener());
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.adapter.setListener(this);
    }

    @Override
    protected void onStop() {
        this.adapter.removeListener();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.users_list, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setOnQueryTextListener(new UsersQueryTextListener());
        searchView.setIconifiedByDefault(false);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        // Focus on startup
        MenuItem searchItem = menu.findItem(R.id.search);
        searchItem.getActionView().requestFocus();
        searchItem.expandActionView();

        return super.onPrepareOptionsMenu(menu);
    }

    // API call with new value of query
    public void requestUsers(String query) {
        if(this.call != null)
            this.call.cancel();
        this.page = 0;
        this.query = query;
        this.clearUsers();
        this.call = NetworkManager.getInstance().getService().searchUsers(query, page);
        this.call.enqueue(new UsersNetworkListener());
        this.setLoading(true);
    }

    // API call with different page
    public void requestMoreUsers() {
        if(isLoading())
            return;
        this.setLoading(true);
        this.page++;
        this.call = NetworkManager.getInstance().getService().searchUsers(query, page);
        this.call.enqueue(new UsersNetworkListener());
    }

    public void clearUsers(){
        this.users.clear();
        this.adapter.notifyDataSetChanged();
    }

    public void addUsers(List<User> users) {
        this.users.addAll(users);
        this.adapter.notifyItemRangeInserted(this.users.size() - users.size(), users.size());
    }

    public boolean isLoading() {
        return !this.users.isEmpty() && this.users.get(this.users.size() - 1) == null;
    }

    // Update adapter to show progressview or not
    public void setLoading(boolean loading) {
        boolean oldValue = this.isLoading();
        if(oldValue == loading)
            return;
        if(oldValue) {
            int lastIndex = this.users.size() - 1;
            this.users.remove(lastIndex);
            this.adapter.notifyItemRemoved(lastIndex);
        }else {
            this.users.add(null);
            this.adapter.notifyItemInserted(this.users.size() - 1);
        }
    }

    @Override
    public void onUserSelected(User user) {
        Intent intent = new Intent();
        intent.putExtra(User.ENTITY, user);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    // API request are delayed due to quota limitations and avoiding useless network operation while query typing
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
            liveSearchHandler.removeCallbacks(liveSearchRunnable);
            if(TextUtils.isEmpty(query))
                return true;
            liveSearchRunnable = new LiveSearchRunnable(query);
            liveSearchHandler.postDelayed(liveSearchRunnable, SEARCH_DELAY_MILLIS);
            return true;
        }
    }

    private class UsersNetworkListener implements Callback<UsersResponse>{

        @Override
        public void onResponse(@NonNull Call<UsersResponse> call, @NonNull Response<UsersResponse> response) {
            UsersListActivity.this.setLoading(false);
            if(!response.isSuccessful()) {
                Log.e(TAG, "Bad Response");
                return;
            }
            UsersResponse body = response.body();
            if( body == null)
                return;
            List<User> result = body.getItems();
            UsersListActivity.this.addUsers(result);
        }

        @Override
        public void onFailure(@NonNull Call<UsersResponse> call, @NonNull Throwable t) {
            UsersListActivity.this.setLoading(false);
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

    private class PaginationScrollListener extends RecyclerView.OnScrollListener{

        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
            if(lm == null)
                return;
            int visibleItemCount = lm.getChildCount();
            int totalItemCount = lm.getItemCount();
            int firstVisibleItemPosition= lm.findFirstVisibleItemPosition();

            if ( (visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                recyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        requestMoreUsers();
                    }
                });
            }
        }

    }

}
