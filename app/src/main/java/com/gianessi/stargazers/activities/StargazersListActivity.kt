package com.gianessi.stargazers.activities

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
import kotlinx.android.synthetic.main.activity_stargazers_list.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "StargazersListActivity"

class StargazersListActivity : AppCompatActivity() {

    private val stargazers = mutableListOf<User?>()
    private lateinit var adapter: UsersAdapter

    // Need these values for future API calls
    // GitHub API requests start from page 1 (page will be incremented at the first call)
    private var page: Int? = NetworkManager.FIRST_PAGE_INDEX - 1

    private var username: String? = null
    private var repoName: String? = null
    set(value) {
        field = value
        this.title = value
    }

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
                this.adapter.notifyItemRemoved(lastIndex)
            } else {
                this.stargazers.add(null)
                this.adapter.notifyItemInserted(this.stargazers.size - 1)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stargazers_list)

        stargazers_list_recycler.setHasFixedSize(true)
        stargazers_list_recycler.layoutManager = LinearLayoutManager(this)

        this.adapter = UsersAdapter(this.stargazers)
        stargazers_list_recycler.adapter = adapter

        // Load more items when scroll reach the bottom
        stargazers_list_recycler.addOnScrollListener(PaginationScrollListener())

        intent?.let { i ->
            this.username = i.getStringExtra(User.USERNAME)
            this.repoName = i.getStringExtra(Repo.NAME)
            this.requestMoreStargazers()
        }
    }

    private fun requestMoreStargazers() {
        // page will be null if end of list reached
        val page = this.page ?: return
        val username = this.username ?: return
        val repo = this.repoName ?: return
        this.page = page + 1
        this.isLoading = true
        NetworkManager.instance.listStargazers(username, repo, page).enqueue(object : Callback<List<User>> {

            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                this@StargazersListActivity.isLoading = false
                if (!response.isSuccessful) {
                    Log.e(TAG, "Bad Response")
                    return
                }
                val body = response.body()
                if (body == null || body.isEmpty())
                    this@StargazersListActivity.page = null
                else
                    this@StargazersListActivity.addStargazers(body)
                checkNoData()
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                this@StargazersListActivity.isLoading = false
                Log.e(TAG, t.localizedMessage)
            }
        })
    }

    private fun addStargazers(stargazers: List<User>) {
        this.stargazers.addAll(stargazers)
        this.adapter.notifyItemRangeInserted(this.stargazers.size - stargazers.size, stargazers.size)
    }

    private fun checkNoData() {
        stargazers_empty_placeholder.visibility = if (this.stargazers.isEmpty()) View.VISIBLE else View.GONE
    }

    private inner class PaginationScrollListener : RecyclerView.OnScrollListener() {

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

}
