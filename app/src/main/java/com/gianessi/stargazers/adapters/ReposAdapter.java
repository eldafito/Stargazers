package com.gianessi.stargazers.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.gianessi.stargazers.R;
import com.gianessi.stargazers.models.Repo;

import java.util.List;

public class ReposAdapter extends ArrayAdapter<Repo> {

    public ReposAdapter(@NonNull Context context, @NonNull List<Repo> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View view = convertView;
        if (view == null)
            view = LayoutInflater.from(getContext()).inflate(R.layout.item_repo, parent, false);

        Repo repo = getItem(position);
        if(repo == null)
            return view;

        TextView nameTxt = view.findViewById(R.id.repo_name_txt);
        nameTxt.setText(repo.getName());

        return view;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getView(position, convertView, parent);
    }
}
