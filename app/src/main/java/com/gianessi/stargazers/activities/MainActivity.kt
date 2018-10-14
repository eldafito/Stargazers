package com.gianessi.stargazers.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView

import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.gianessi.stargazers.R
import com.gianessi.stargazers.adapters.ReposAdapter
import com.gianessi.stargazers.models.Repo
import com.gianessi.stargazers.models.User
import com.gianessi.stargazers.network.NetworkManager

import java.util.ArrayList

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private var user: User? = null
    private var repo: Repo? = null
    private val userRepos = ArrayList<Repo>()
    private var adapter: ReposAdapter? = null

    // Need these values for future API calls
    // GitHub API requests start from page 1 (page will be incremented at the first call)
    private var page: Int? = NetworkManager.FIRST_PAGE_INDEX - 1

    private var usernameTxt: TextView? = null
    private var avatarImg: ImageView? = null
    private var progressBar: ProgressBar? = null
    private var submitBtn: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progressBar = findViewById(R.id.main_progress)
        submitBtn = findViewById(R.id.main_submit_btn)
        usernameTxt = findViewById(R.id.user_username_txt)
        avatarImg = findViewById(R.id.user_avatar_img)

        findViewById<View>(R.id.main_user).setOnClickListener { chooseUser() }

        val repoSpinner = findViewById<Spinner>(R.id.main_repo_spinner)
        adapter = ReposAdapter(this, userRepos)
        repoSpinner.adapter = adapter
        repoSpinner.onItemSelectedListener = this

        this.setUser(null)
    }

    private fun setLoading(loading: Boolean) {
        this.progressBar!!.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun setUser(user: User?) {
        this.user = user
        this.clearUserRepos()
        if (user != null) {
            this.usernameTxt!!.text = user.username
            Glide.with(this).load(user.avatarUrl).apply(RequestOptions.circleCropTransform()).into(avatarImg!!)
            this.requestMoreUserRepos()
        } else {
            this.usernameTxt!!.setText(R.string.no_user_hint)
            this.avatarImg!!.setImageResource(R.drawable.avatar_placeholder)
        }
    }

    private fun clearUserRepos() {
        this.page = NetworkManager.FIRST_PAGE_INDEX - 1
        this.userRepos.clear()
        this.adapter!!.notifyDataSetChanged()
        this.setRepo(null)
    }

    private fun addUserRepos(userRepos: List<Repo>) {
        this.userRepos.addAll(userRepos)
        this.adapter!!.notifyDataSetChanged()
    }

    private fun setRepo(repo: Repo?) {
        this.repo = repo
        this.submitBtn!!.isEnabled = repo != null
    }

    private fun requestMoreUserRepos() {
        if (this.page == null || this.user == null)
            return
        this.page = this.page!! + 1
        this.setLoading(true)
        NetworkManager.instance.listRepos(user!!.username!!, page!!).enqueue(UserReposNetworkListener())
    }

    private fun chooseUser() {
        val intent = Intent(this, UsersListActivity::class.java)
        startActivityForResult(intent, USERS_LIST_REQ)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == USERS_LIST_REQ) {
            if (resultCode == Activity.RESULT_OK && data!!.hasExtra(User.ENTITY)) {
                this.setUser(data.getParcelableExtra<Parcelable>(User.ENTITY) as User)
            }
        }
    }

    fun discoverStargazers(view: View) {
        if (user == null || repo == null)
            return
        val intent = Intent(this, StargazersListActivity::class.java)
        intent.putExtra(User.USERNAME, user!!.username)
        intent.putExtra(Repo.NAME, repo!!.name)
        startActivity(intent)
    }

    override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View, position: Int, id: Long) {
        this.setRepo(this.userRepos[position])
    }

    override fun onNothingSelected(parentView: AdapterView<*>) {
        this.setRepo(null)
    }


    private inner class UserReposNetworkListener : Callback<List<Repo>> {

        override fun onResponse(call: Call<List<Repo>>, response: Response<List<Repo>>) {
            setLoading(false)
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
            this@MainActivity.setLoading(false)
            Log.e(TAG, t.localizedMessage)
        }
    }

    companion object {

        private val TAG = "MainActivity"

        private val USERS_LIST_REQ = 34
    }
}
