package com.gianessi.stargazers.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

import com.gianessi.stargazers.R
import com.gianessi.stargazers.models.Repo

class ReposAdapter(context: Context, objects: List<Repo>) : ArrayAdapter<Repo>(context, 0, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_repo, parent, false)
        val repo = getItem(position) ?: return view
        val nameTxt = view.findViewById<TextView>(R.id.repo_name_txt)
        nameTxt.text = repo.name
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getView(position, convertView, parent)
    }
}
