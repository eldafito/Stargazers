package com.gianessi.stargazers.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gianessi.stargazers.R;
import com.gianessi.stargazers.listeners.OnUserSelectedListener;
import com.gianessi.stargazers.models.User;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {

    private List<User> users;
    private OnUserSelectedListener listener;

    public UsersAdapter(List<User> users) {
        this.users = users;
    }

    public void setListener(OnUserSelectedListener listener) {
        this.listener = listener;
    }

    public void removeListener(){
        this.listener = null;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        final User user = this.users.get(position);
        viewHolder.bind(user);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listener != null)
                    listener.onUserSelected(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.users.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView usernameTxt;
        private ViewHolder(View view) {
            super(view);
            usernameTxt = view.findViewById(R.id.user_username_txt);
        }

        private void bind(User user){
            this.usernameTxt.setText(user.getUsername());
        }
    }

}
