package com.gianessi.stargazers.listeners

import com.gianessi.stargazers.models.User

interface OnUserSelectedListener {
    fun onUserSelected(user: User)
}
