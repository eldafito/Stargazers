package com.gianessi.stargazers.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.SearchView

import com.gianessi.stargazers.R
import com.gianessi.stargazers.adapters.UsersAdapter
import com.gianessi.stargazers.listeners.OnUserSelectedListener
import com.gianessi.stargazers.models.User
import com.gianessi.stargazers.models.UsersResponse
import com.gianessi.stargazers.network.NetworkManager
import kotlinx.android.synthetic.main.activity_users_list.*
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI


import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "UsersListActivity"
private const val SEARCH_DELAY_MILLIS = 600

class UsersListActivity : AppCompatActivity(), OnUserSelectedListener {

    private var call: Call<UsersResponse>? = null
    private val users = mutableListOf<User?>()
    private lateinit var adapter: UsersAdapter

    // Need these values for future API calls
    private var page: Int? = null
    private var query: String? = null
    private var searchJob: Job? = null
    set(value) {
        searchJob?.cancel()
        field = value
    }


    // Update adapter to show progressview or not
    private var isLoading = false
        set(loading) {
            val oldValue = this.isLoading
            field = loading
            if (oldValue == loading)
                return
            if (oldValue) {
                val lastIndex = this.users.size - 1
                this.users.removeAt(lastIndex)
                this.adapter.notifyItemRemoved(lastIndex)
            } else {
                this.users.add(null)
                this.adapter.notifyItemInserted(this.users.size - 1)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users_list)

        users_list_recycler.setHasFixedSize(true)
        users_list_recycler.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)

        this.adapter = UsersAdapter(this.users)
        users_list_recycler.adapter = adapter

        // Load more items when scroll reach the bottom
        users_list_recycler.addOnScrollListener(PaginationScrollListener())
    }

    override fun onStart() {
        super.onStart()
        this.adapter.listener = this
    }

    override fun onStop() {
        this.adapter.listener = null
        super.onStop()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.users_list, menu)

        val searchView = menu.findItem(R.id.search).actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String): Boolean {
                search(query, true)
                return true
            }

            override fun onQueryTextChange(query: String): Boolean {
                Log.i(TAG, query)
                search(query)
                return true
            }
        })
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


    // API call with different page
    private fun requestMoreUsers() {
        // page will be null if end of list reached
        if (isLoading)
            return
        val page = this.page ?: return
        val query = this.query ?: return
        this.isLoading = true
        this.page = page + 1
        this.call = NetworkManager.instance.searchUsers(query, page)
        this.call!!.enqueue(object : Callback<UsersResponse> {
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
        })
    }

    private fun clearUsers() {
        this.users.clear()
        this.adapter.notifyDataSetChanged()
    }

    private fun addUsers(users: List<User>) {
        this.users.addAll(users)
        this.adapter.notifyItemRangeInserted(this.users.size - users.size, users.size)
    }

    private fun checkNoData() {
        users_empty_placeholder.visibility = if (this.users.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onUserSelected(user: User) {
        Intent().apply {
            putExtra(User.ENTITY, user)
        }.also { i ->
            setResult(Activity.RESULT_OK, i)
        }
        finish()
    }

    private fun search(query: String, force: Boolean = false){
        clearUsers()
        this.searchJob = launch(CommonPool){
            if(!query.isEmpty() && !force)
                delay(SEARCH_DELAY_MILLIS)
            call?.cancel()
            page = NetworkManager.FIRST_PAGE_INDEX - 1
            this@UsersListActivity.query = query
            requestMoreUsers()
        }
    }

    private inner class PaginationScrollListener : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {

        override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val lm = recyclerView.layoutManager as androidx.recyclerview.widget.LinearLayoutManager? ?: return
            val visibleItemCount = lm.childCount
            val totalItemCount = lm.itemCount
            val firstVisibleItemPosition = lm.findFirstVisibleItemPosition()

            if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                recyclerView.post { requestMoreUsers() }
            }
        }

    }



}
