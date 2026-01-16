package com.fh.anitrack.ui.browse.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.fh.anitrack.R;
import com.fh.anitrack.api.response.UserSearchResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying users in a grid layout for user search.
 */
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private List<UserSearchResponse.User> users = new ArrayList<>();
    private OnUserClickListener listener;

    public interface OnUserClickListener {
        void onUserClick(UserSearchResponse.User user, int position);
    }

    public void setOnUserClickListener(OnUserClickListener listener) {
        this.listener = listener;
    }

    public void setUsers(List<UserSearchResponse.User> users) {
        this.users = users != null ? users : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addUsers(List<UserSearchResponse.User> newUsers) {
        if (newUsers != null && !newUsers.isEmpty()) {
            int startPos = users.size();
            users.addAll(newUsers);
            notifyItemRangeInserted(startPos, newUsers.size());
        }
    }

    public void clearUsers() {
        users.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.browse_item_user_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserSearchResponse.User user = users.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView userAvatar;
        private final TextView userName;

        ViewHolder(View itemView) {
            super(itemView);
            userAvatar = itemView.findViewById(R.id.userAvatar);
            userName = itemView.findViewById(R.id.userName);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        listener.onUserClick(users.get(pos), pos);
                    }
                }
            });
        }

        void bind(UserSearchResponse.User user) {
            Context context = itemView.getContext();

            // Set username
            userName.setText(user.name);

            // Load avatar with Glide
            String avatarUrl = user.avatar != null ? user.avatar.large : null;
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                Glide.with(context)
                        .load(avatarUrl)
                        .placeholder(R.drawable.profile_picture)
                        .error(R.drawable.profile_picture)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(userAvatar);
            } else {
                userAvatar.setImageResource(R.drawable.profile_picture);
            }
        }
    }
}
