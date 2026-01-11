package com.fh.anitrack.ui.home;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fh.anitrack.R;
import com.fh.anitrack.api.response.ActivityResponse;

import java.util.ArrayList;
import java.util.List;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ViewHolder> {

    private final List<ActivityResponse.Activity> activities = new ArrayList<>();

    // used for pagination to add new items to the bottom of the list
    public void addItems(List<ActivityResponse.Activity> newItems) {
        int startPosition = activities.size();
        activities.addAll(newItems);
        notifyItemRangeInserted(startPosition, newItems.size());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_activity, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ActivityResponse.Activity activity = activities.get(position);

        // 1. Load media cover image
        if (activity.media != null && activity.media.coverImage != null) {
            Glide.with(holder.itemView.getContext())
                    .load(activity.media.coverImage.large)
                    .centerCrop()
                    .into(holder.mediaCoverImage);
        }

        // 2. Load user avatar
        if (activity.user != null && activity.user.avatar != null) {
            Glide.with(holder.itemView.getContext())
                    .load(activity.user.avatar.large)
                    .circleCrop()
                    .into(holder.userAvatar);

            holder.tvUsername.setText(activity.user.name);
        }

        // 3. Set time
        // AniList returns seconds and DateUtils needs milliseconds, so convert
        long timeMillis = activity.createdAt * 1000L;
        CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(
                timeMillis,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
        );
        holder.tvTimeAgo.setText(relativeTime);

        // 4. Make status text
        String statusText = formatStatus(activity);
        holder.tvStatus.setText(statusText);

        // 5. Set likes and comms amountb
        holder.tvLikeCount.setText(String.valueOf(activity.likeCount));
        holder.tvCommentCount.setText(String.valueOf(activity.replyCount));
    }

    private String formatStatus(ActivityResponse.Activity activity) {
        StringBuilder sb = new StringBuilder();

        // capitalized first letter of status
        String status = activity.status;
        if (status != null && !status.isEmpty()) {
            sb.append(status.substring(0, 1).toUpperCase()).append(status.substring(1));
        }

        // Add progress if exists (e.g., "episode 71 - 356")
        if (activity.progress != null && !activity.progress.isEmpty()) {
            sb.append(" ").append(activity.progress);
        }

        // Add media title
        if (activity.media != null && activity.media.title != null) {
            sb.append(" of ").append(activity.media.title.userPreferred);
        }

        return sb.toString();
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView mediaCoverImage, userAvatar;
        TextView tvUsername, tvTimeAgo, tvStatus, tvLikeCount, tvCommentCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mediaCoverImage = itemView.findViewById(R.id.mediaCoverImage);
            userAvatar = itemView.findViewById(R.id.userAvatar);
            tvUsername = itemView.findViewById(R.id.userName);
            tvTimeAgo = itemView.findViewById(R.id.timeAgo);
            tvStatus = itemView.findViewById(R.id.activityStatus);
            tvLikeCount = itemView.findViewById(R.id.likeCount);
            tvCommentCount = itemView.findViewById(R.id.commentCount);
        }
    }
}