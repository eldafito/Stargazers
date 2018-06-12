package com.gianessi.stargazers.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gianessi.stargazers.R;
import com.gianessi.stargazers.listeners.OnUserSelectedListener;
import com.gianessi.stargazers.models.User;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_PROGRESS = 1;

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

    @Override
    public int getItemViewType(int position) {
        return this.users.get(position) == null ? VIEW_TYPE_PROGRESS : VIEW_TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
            return new UserViewHolder(view);
        }else{
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_progress, parent, false);
            return new ProgressViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if(viewHolder instanceof UserViewHolder){
            UserViewHolder userVH = (UserViewHolder) viewHolder;
            final User user = this.users.get(position);
            userVH.bind(user);
            userVH.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(listener != null)
                        listener.onUserSelected(user);
                }
            });
        }else{
            Log.i("REC","top");
        }
    }

    @Override
    public int getItemCount() {
        return this.users.size();
    }

    private static class UserViewHolder extends RecyclerView.ViewHolder {
        private TextView usernameTxt;
        private UserViewHolder(View view) {
            super(view);
            usernameTxt = view.findViewById(R.id.user_username_txt);
        }

        private void bind(User user){
            this.usernameTxt.setText(user.getUsername());
        }
    }

    private static class ProgressViewHolder extends RecyclerView.ViewHolder {
        private ProgressViewHolder(View view) {
            super(view);
        }
    }

}
