package com.gianessi.stargazers.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.AdapterView

import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.gianessi.stargazers.R
import com.gianessi.stargazers.adapters.ReposAdapter
import com.gianessi.stargazers.models.Repo
import com.gianessi.stargazers.models.User
import com.gianessi.stargazers.network.NetworkManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_user.*

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "MainActivity"
private const val USERS_LIST_REQ = 34

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private var user: User? = null
    set(value) {
        field = value
        this.clearUserRepos()
        value?.let { user ->
            user_username_txt.text = user.username
            Glide.with(this).load(user.avatarUrl).apply(RequestOptions.circleCropTransform()).into(user_avatar_img)
            requestMoreUserRepos()
        } ?: kotlin.run {
            user_username_txt.setText(R.string.no_user_hint)
            user_avatar_img.setImageResource(R.drawable.avatar_placeholder)
        }
    }
    private var repo: Repo? = null
    set(value) {
        field = value
        main_submit_btn.isEnabled = value != null
    }

    private val userRepos = mutableListOf<Repo>()
    private lateinit var adapter: ReposAdapter

    // Need these values for future API calls
    // GitHub API requests start from page 1 (page will be incremented at the first call)
    private var page: Int? = NetworkManager.FIRST_PAGE_INDEX - 1

    private var isLoading = false
    set(value) {
        field = value
        this.main_progress.visibility = if (value) View.VISIBLE else View.GONE
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        main_user.setOnClickListener { chooseUser() }
        main_submit_btn.setOnClickListener { discoverStargazers() }

        adapter = ReposAdapter(this, userRepos)
        main_repo_spinner.adapter = adapter
        main_repo_spinner.onItemSelectedListener = this

        this.user = null
    }


    private fun clearUserRepos() {
        this.page = NetworkManager.FIRST_PAGE_INDEX - 1
        this.userRepos.clear()
        this.adapter.notifyDataSetChanged()
        this.repo = null
    }

    private fun addUserRepos(userRepos: List<Repo>) {
        this.userRepos.addAll(userRepos)
        this.adapter.notifyDataSetChanged()
    }

    private fun requestMoreUserRepos() {
        val page = this.page ?: return
        val user = this.user ?: return
        this.page = page + 1
        this.isLoading = true
        NetworkManager.instance.listRepos(user.username!!, page).enqueue(object : Callback<List<Repo>>{
            override fun onResponse(call: Call<List<Repo>>, response: Response<List<Repo>>) {
                isLoading = false
                if (!response.isSuccessful) {
                    Log.e(TAG, "Bad Response")
                    return
                }
                val body = response.body()
                if (body == null || body.isEmpty())
                    this@MainActivity.page = null
                else {
                    this@MainActivity.addUserRepos(body)
                    this@MainActivity.requestMoreUserRepos()
                }
            }

            override fun onFailure(call: Call<List<Repo>>, t: Throwable) {
                this@MainActivity.isLoading = false
                Log.e(TAG, t.localizedMessage)
            }
        })
    }

    private fun chooseUser() {
        Intent(this, UsersListActivity::class.java).run {
            startActivityForResult(this, USERS_LIST_REQ)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == USERS_LIST_REQ) {
            if (resultCode == Activity.RESULT_OK) {
                this.user = data?.getParcelableExtra(User.ENTITY)
            }
        }
    }

    private fun discoverStargazers() {
        val user = this.user ?: return
        val repo = this.repo ?: return
        Intent(this, StargazersListActivity::class.java).apply {
            putExtra(User.USERNAME, user.username)
            putExtra(Repo.NAME, repo.name)
        }.run {
            startActivity(this)
        }
    }

    override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View, position: Int, id: Long) {
        this.repo = this.userRepos[position]
    }

    override fun onNothingSelected(parentView: AdapterView<*>) {
        this.repo = null
    }

}
