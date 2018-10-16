package com.gianessi.stargazers.adapters

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.gianessi.stargazers.R
import com.gianessi.stargazers.listeners.OnUserSelectedListener
import com.gianessi.stargazers.models.User

private const val VIEW_TYPE_ITEM = 0
private const val VIEW_TYPE_PROGRESS = 1

class UsersAdapter(private val users: List<User?>) : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {

    var listener: OnUserSelectedListener? = null

    override fun getItemViewType(position: Int): Int {
        return if (this.users[position] == null) VIEW_TYPE_PROGRESS else VIEW_TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_ITEM) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
            UserViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_progress, parent, false)
            ProgressViewHolder(view)
        }
    }

    override fun onBindViewHolder(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        (viewHolder as? UserViewHolder)?.let { userViewHolder ->
            val user = this.users[position] ?: return
            userViewHolder.bind(user)
            viewHolder.itemView.setOnClickListener { _ ->
                if (listener != null)
                    listener!!.onUserSelected(user)
            }
        }
    }

    override fun getItemCount(): Int {
        return this.users.size
    }

    inner class UserViewHolder (view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {

        private val usernameTxt: TextView = view.findViewById(R.id.user_username_txt)
        private val avatarImg: ImageView = view.findViewById(R.id.user_avatar_img)

        fun bind(user: User) {
            Glide.with(this.itemView.context).load(user.avatarUrl).apply(RequestOptions.circleCropTransform()).into(avatarImg)
            this.usernameTxt.text = user.username
        }
    }

    inner class ProgressViewHolder (view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view)

}
