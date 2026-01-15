package com.fh.anitrack.ui.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fh.anitrack.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying users in a grid layout (followers/following).
 */
public class SocialUserAdapter extends RecyclerView.Adapter<SocialUserAdapter.UserViewHolder> {

    private List<ProfileSocial.SocialUser> users = new ArrayList<>();

    public void setUsers(List<ProfileSocial.SocialUser> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_social_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        ProfileSocial.SocialUser user = users.get(position);
        holder.username.setText(user.name);
        
        Glide.with(holder.itemView.getContext())
                .load(user.avatarUrl)
                .circleCrop()
                .placeholder(R.drawable.profile_picture)
                .into(holder.avatar);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView avatar;
        TextView username;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.userAvatar);
            username = itemView.findViewById(R.id.username);
        }
    }
}
