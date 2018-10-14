package com.gianessi.stargazers.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.SearchView

import com.gianessi.stargazers.R
import com.gianessi.stargazers.adapters.UsersAdapter
import com.gianessi.stargazers.listeners.OnUserSelectedListener
import com.gianessi.stargazers.models.User
import com.gianessi.stargazers.models.UsersResponse
import com.gianessi.stargazers.network.NetworkManager

import java.util.ArrayList

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "UsersListActivity"
private const val SEARCH_DELAY_MILLIS = 600

class UsersListActivity : AppCompatActivity(), OnUserSelectedListener {

    private val liveSearchHandler = Handler()
    private var liveSearchRunnable: LiveSearchRunnable? = null
    private var call: Call<UsersResponse>? = null
    private val users = ArrayList<User>()
    private var adapter: UsersAdapter? = null

    // Need these values for future API calls
    private var page: Int? = null
    private var query: String? = null

    private var emptyPlaceholder: View? = null

    // Update adapter to show progressview or not
    private var isLoading: Boolean
        get() = !this.users.isEmpty() && this.users[this.users.size - 1] == null
        set(loading) {
            val oldValue = this.isLoading
            if (oldValue == loading)
                return
            if (oldValue) {
                val lastIndex = this.users.size - 1
                this.users.removeAt(lastIndex)
                this.adapter!!.notifyItemRemoved(lastIndex)
            } else {
                this.users.add(null)
                this.adapter!!.notifyItemInserted(this.users.size - 1)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users_list)
        val recyclerView = findViewById<RecyclerView>(R.id.users_list_recycler)
        this.emptyPlaceholder = findViewById(R.id.users_empty_placeholder)

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)

        this.adapter = UsersAdapter(this.users)
        recyclerView.adapter = adapter

        // Load more items when scroll reach the bottom
        recyclerView.addOnScrollListener(PaginationScrollListener())
    }

    override fun onStart() {
        super.onStart()
        this.adapter?.listener = this
    }

    override fun onStop() {
        this.adapter?.listener = null
        super.onStop()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.users_list, menu)

        val searchView = menu.findItem(R.id.search).actionView as SearchView
        searchView.setOnQueryTextListener(UsersQueryTextListener())
        searchView.setIconifiedByDefault(false)

        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {

        // Focus on startup
        val searchItem = menu.findItem(R.id.search)
        searchItem.actionView.requestFocus()
        searchItem.expandActionView()

        return super.onPrepareOptionsMenu(menu)
    }

    // API call with new value of query
    // GitHub API requests start from page 1
    private fun requestUsers(query: String) {
        if (this.call != null)
            this.call!!.cancel()
        this.page = NetworkManager.FIRST_PAGE_INDEX - 1
        this.query = query
        this.clearUsers()
        this.requestMoreUsers()
    }

    // API call with different page
    private fun requestMoreUsers() {
        // page will be null if end of list reached
        if (this.page == null || isLoading)
            return
        this.isLoading = true
        this.page++
        this.call = NetworkManager.instance.searchUsers(query, page!!)
        this.call!!.enqueue(UsersNetworkListener())
    }

    private fun clearUsers() {
        this.users.clear()
        this.adapter!!.notifyDataSetChanged()
    }

    private fun addUsers(users: List<User>) {
        this.users.addAll(users)
        this.adapter!!.notifyItemRangeInserted(this.users.size - users.size, users.size)
    }

    private fun checkNoData() {
        this.emptyPlaceholder!!.visibility = if (this.users.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onUserSelected(user: User) {
        val intent = Intent()
        intent.putExtra(User.ENTITY, user)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    // API request are delayed due to quota limitations and avoiding useless network operation while query typing
    private inner class UsersQueryTextListener : SearchView.OnQueryTextListener {

        override fun onQueryTextSubmit(query: String): Boolean {
            liveSearchHandler.removeCallbacks(liveSearchRunnable)
            liveSearchRunnable = LiveSearchRunnable(query)
            liveSearchHandler.post(liveSearchRunnable)
            return true
        }

        override fun onQueryTextChange(query: String): Boolean {
            Log.i(TAG, query)
            this@UsersListActivity.clearUsers()
            liveSearchHandler.removeCallbacks(liveSearchRunnable)
            if (TextUtils.isEmpty(query))
                return true
            liveSearchRunnable = LiveSearchRunnable(query)
            liveSearchHandler.postDelayed(liveSearchRunnable, SEARCH_DELAY_MILLIS.toLong())
            return true
        }
    }

    private inner class UsersNetworkListener : Callback<UsersResponse> {

        override fun onResponse(call: Call<UsersResponse>, response: Response<UsersResponse>) {
            this@UsersListActivity.isLoading = false
            if (!response.isSuccessful) {
                Log.e(TAG, "Bad Response")
                return
            }
            val body = response.body() ?: return
            val result = body.items
            if (result == null || result.isEmpty())
                this@UsersListActivity.page = null
            else
                this@UsersListActivity.addUsers(result)
            this@UsersListActivity.checkNoData()
        }

        override fun onFailure(call: Call<UsersResponse>, t: Throwable) {
            this@UsersListActivity.isLoading = false
            Log.e(TAG, t.localizedMessage)
        }
    }

    private inner class LiveSearchRunnable private constructor(private val query: String) : Runnable {

        override fun run() {
            this@UsersListActivity.requestUsers(query)
        }
    }

    private inner class PaginationScrollListener : RecyclerView.OnScrollListener() {

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val lm = recyclerView.layoutManager as LinearLayoutManager? ?: return
            val visibleItemCount = lm.childCount
            val totalItemCount = lm.itemCount
            val firstVisibleItemPosition = lm.findFirstVisibleItemPosition()

            if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                recyclerView.post { requestMoreUsers() }
            }
        }

    }



}
