package com.gianessi.stargazers.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View

import com.gianessi.stargazers.R
import com.gianessi.stargazers.adapters.UsersAdapter
import com.gianessi.stargazers.models.Repo
import com.gianessi.stargazers.models.User
import com.gianessi.stargazers.network.NetworkManager

import java.util.ArrayList

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StargazersListActivity : AppCompatActivity() {

    private val stargazers = ArrayList<User?>()
    private var adapter: UsersAdapter? = null

    // Need these values for future API calls
    // GitHub API requests start from page 1 (page will be incremented at the first call)
    private var page: Int? = NetworkManager.FIRST_PAGE_INDEX - 1

    private var username: String? = null
    private var repoName: String? = null

    private var emptyPlaceholder: View? = null

    // Update adapter to show progressview or not
    private var isLoading: Boolean
        get() = !this.stargazers.isEmpty()
        set(loading) {
            val oldValue = this.isLoading
            if (oldValue == loading)
                return
            if (oldValue) {
                val lastIndex = this.stargazers.size - 1
                this.stargazers.removeAt(lastIndex)
                this.adapter!!.notifyItemRemoved(lastIndex)
            } else {
                this.stargazers.add(null)
                this.adapter!!.notifyItemInserted(this.stargazers.size - 1)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stargazers_list)
        val recyclerView = findViewById<RecyclerView>(R.id.stargazers_list_recycler)
        this.emptyPlaceholder = findViewById(R.id.stargazers_empty_placeholder)

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)

        this.adapter = UsersAdapter(this.stargazers)
        recyclerView.adapter = adapter

        // Load more items when scroll reach the bottom
        recyclerView.addOnScrollListener(PaginationScrollListener())

        val intent = intent
        if (intent != null) {
            this.username = intent.getStringExtra(User.USERNAME)
            this.setRepoName(intent.getStringExtra(Repo.NAME))
            this.requestMoreStargazers()
        }
    }

    private fun setRepoName(repoName: String) {
        this.repoName = repoName
        this.title = repoName
    }

    private fun requestMoreStargazers() {
        // page will be null if end of list reached
        val page = this.page ?: return
        val username = this.username ?: return
        val repo = this.repoName ?: return
        this.page = page + 1
        this.isLoading = true
        NetworkManager.instance.listStargazers(username, repo, page).enqueue(StargazersNetworkListener())
    }

    private fun addStargazers(stargazers: List<User>) {
        this.stargazers.addAll(stargazers)
        this.adapter!!.notifyItemRangeInserted(this.stargazers.size - stargazers.size, stargazers.size)
    }

    private fun checkNoData() {
        this.emptyPlaceholder!!.visibility = if (this.stargazers.isEmpty()) View.VISIBLE else View.GONE
    }


    private inner class StargazersNetworkListener : Callback<List<User>> {

        override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
            this@StargazersListActivity.isLoading = false
            if (!response.isSuccessful) {
                Log.e(TAG, "Bad Response")
                return
            }
            val body = response.body()
            if (body == null || body.isEmpty())
                page = null
            else
                this@StargazersListActivity.addStargazers(body)
            checkNoData()
        }

        override fun onFailure(call: Call<List<User>>, t: Throwable) {
            this@StargazersListActivity.isLoading = false
            Log.e(TAG, t.localizedMessage)
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
                recyclerView.post { requestMoreStargazers() }
            }
        }

    }

    companion object {

        private val TAG = "StargazersListActivity"
    }
}
