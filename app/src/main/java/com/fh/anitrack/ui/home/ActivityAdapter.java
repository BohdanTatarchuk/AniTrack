package com.fh.anitrack.ui.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.fh.anitrack.R;
import com.fh.anitrack.api.AniListQueries;
import com.fh.anitrack.api.AniListService;
import com.fh.anitrack.api.GraphQLRequest;
import com.fh.anitrack.api.RequestWrapper;
import com.fh.anitrack.api.RetrofitClient;
import com.fh.anitrack.api.response.ActivityResponse;
import com.fh.anitrack.api.response.ToggleLikeResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.noties.markwon.Markwon;
import retrofit2.Call;

public class ActivityAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_LIST = 0;
    private static final int TYPE_TEXT = 1;
    private final List<ActivityResponse.Activity> activities = new ArrayList<>();
    private Markwon markwon;

    // Flag to hide header (avatar/username) when on profile page
    private boolean isPersonalProfile = false;

    public void setMarkwon(Markwon markwon) {
        this.markwon = markwon;
    }

    public void setPersonalProfile(boolean isPersonalProfile) {
        this.isPersonalProfile = isPersonalProfile;
    }

    // used for pagination to add new items to the bottom of the list
    public void addItems(List<ActivityResponse.Activity> newItems) {
        int startPosition = activities.size();
        activities.addAll(newItems);
        notifyItemRangeInserted(startPosition, newItems.size());
    }

    @Override
    public int getItemViewType(int position) {
        if ("TEXT".equals(activities.get(position).type)) {
            return TYPE_TEXT;
        }
        return TYPE_LIST;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_TEXT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_status_activity, parent, false);
            return new StatusViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_activity, parent, false);
            return new MediaViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ActivityResponse.Activity activity = activities.get(position);

        if (holder instanceof StatusViewHolder) {
            bindStatusActivity((StatusViewHolder) holder, activity);
        } else if (holder instanceof MediaViewHolder) {
            bindMediaActivity((MediaViewHolder) holder, activity);
        }
    }

    private void bindStatusActivity(StatusViewHolder holder, ActivityResponse.Activity activity) {
        Context context = holder.itemView.getContext();

        // Toggle visibility based on page type
        if (isPersonalProfile) {
            holder.userAvatar.setVisibility(View.GONE);
            holder.tvUsername.setVisibility(View.GONE);
        } else {
            holder.userAvatar.setVisibility(View.VISIBLE);
            holder.tvUsername.setVisibility(View.VISIBLE);
            holder.tvUsername.setText(activity.user.name);
            Glide.with(context).load(activity.user.avatar.large).circleCrop().into(holder.userAvatar);
        }

        setTime(holder.tvTimeAgo, activity.createdAt);

        if (markwon != null && activity.text != null) {
            markwon.setMarkdown(holder.tvStatusText, activity.text);
        }

        setupLikeLogic(context, activity, holder.ivHeart, holder.tvLikeCount);
        holder.tvCommentCount.setText(String.valueOf(activity.replyCount));
    }

    private void bindMediaActivity(MediaViewHolder holder, ActivityResponse.Activity activity) {
        Context context = holder.itemView.getContext();

        if (activity.media != null && activity.media.coverImage != null) {
            Glide.with(context).load(activity.media.coverImage.large).centerCrop().into(holder.mediaCoverImage);
        }

        // Toggle visibility based on page type
        if (isPersonalProfile) {
            holder.userAvatar.setVisibility(View.GONE);
            holder.tvUsername.setVisibility(View.GONE);
        } else {
            holder.userAvatar.setVisibility(View.VISIBLE);
            holder.tvUsername.setVisibility(View.VISIBLE);
            if (activity.user != null && activity.user.avatar != null) {
                Glide.with(context).load(activity.user.avatar.large).circleCrop().into(holder.userAvatar);
                holder.tvUsername.setText(activity.user.name);
            }
        }

        setTime(holder.tvTimeAgo, activity.createdAt);
        holder.tvStatus.setText(formatStatus(activity));

        setupLikeLogic(context, activity, holder.ivLike, holder.tvLikeCount);
        holder.tvCommentCount.setText(String.valueOf(activity.replyCount));
    }

    private void setTime(TextView tvTime, long createdAt) {
        long timeMillis = createdAt * 1000L;
        long nowSeconds = System.currentTimeMillis() / 1000L;
        long diffSeconds = nowSeconds - createdAt;

        long minResolution = (diffSeconds < 60) ? DateUtils.SECOND_IN_MILLIS : DateUtils.MINUTE_IN_MILLIS;
        CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(timeMillis, System.currentTimeMillis(), minResolution);

        if (diffSeconds < 1) {
            tvTime.setText("Just now");
        } else {
            tvTime.setText(relativeTime);
        }
    }

    private void setupLikeLogic(Context context, ActivityResponse.Activity activity, ImageView ivHeart, TextView tvCount) {
        updateLikeUI(ivHeart, tvCount, activity.isLiked, activity.likeCount);

        ivHeart.setOnClickListener(v -> {
            final boolean wasLiked = activity.isLiked;
            final int originalCount = activity.likeCount;

            activity.isLiked = !wasLiked;
            activity.likeCount = activity.isLiked ? originalCount + 1 : originalCount - 1;
            updateLikeUI(ivHeart, tvCount, activity.isLiked, activity.likeCount);

            Map<String, Object> vars = new HashMap<>();
            vars.put("id", activity.id);
            vars.put("type", "ACTIVITY");

            AniListService service = RetrofitClient.getInstance(context).create(AniListService.class);
            Call<ToggleLikeResponse> call = service.toggleLike(new GraphQLRequest(AniListQueries.TOGGLE_LIKE, vars));

            RequestWrapper.sendRequest(call, response -> {
                if (response.isSuccessful() && response.body() != null && response.body().data.ToggleLikeV2 != null) {
                    ToggleLikeResponse.ToggleLikeV2 res = response.body().data.ToggleLikeV2;
                    activity.isLiked = res.isLiked;
                    activity.likeCount = res.likeCount;
                    updateLikeUI(ivHeart, tvCount, activity.isLiked, activity.likeCount);
                } else {
                    revertLike(activity, wasLiked, originalCount, ivHeart, tvCount);
                    Toast.makeText(context, R.string.anilist_error_updating_like, Toast.LENGTH_SHORT).show();
                }
            }, t -> {
                revertLike(activity, wasLiked, originalCount, ivHeart, tvCount);
                Toast.makeText(context, R.string.network_failure, Toast.LENGTH_SHORT).show();
            }, context);
        });
    }

    private void updateLikeUI(ImageView ivHeart, TextView tvCount, boolean isLiked, int count) {
        tvCount.setText(String.valueOf(count));
        if (isLiked) {
            ivHeart.setImageResource(R.drawable.ic_heart_filled);
        } else {
            ivHeart.setImageResource(R.drawable.ic_heart_outline);
        }
    }

    private void revertLike(ActivityResponse.Activity activity, boolean wasLiked, int originalCount, ImageView ivHeart, TextView tvCount) {
        activity.isLiked = wasLiked;
        activity.likeCount = originalCount;
        updateLikeUI(ivHeart, tvCount, wasLiked, originalCount);
    }

    private String formatStatus(ActivityResponse.Activity activity) {
        StringBuilder sb = new StringBuilder();
        String status = activity.status;
        if (status != null && !status.isEmpty()) {
            sb.append(status.substring(0, 1).toUpperCase()).append(status.substring(1));
        }
        if (activity.progress != null && !activity.progress.isEmpty()) {
            sb.append(" ").append(activity.progress);
        }
        if (activity.media != null && activity.media.title != null) {
            sb.append(" of ").append(activity.media.title.userPreferred);
        }
        return sb.toString();
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clearItems() {
        this.activities.clear();
        notifyDataSetChanged();
    }

    public static class StatusViewHolder extends RecyclerView.ViewHolder {
        ImageView userAvatar, ivHeart;
        TextView tvUsername, tvTimeAgo, tvStatusText, tvLikeCount, tvCommentCount;

        public StatusViewHolder(@NonNull View itemView) {
            super(itemView);
            userAvatar = itemView.findViewById(R.id.userAvatar);
            ivHeart = itemView.findViewById(R.id.ivHeart);
            tvUsername = itemView.findViewById(R.id.userName);
            tvTimeAgo = itemView.findViewById(R.id.timeAgo);
            tvStatusText = itemView.findViewById(R.id.statusText);
            tvLikeCount = itemView.findViewById(R.id.likeCount);
            tvCommentCount = itemView.findViewById(R.id.commentCount);
        }
    }

    public static class MediaViewHolder extends RecyclerView.ViewHolder {
        ImageView mediaCoverImage, userAvatar, ivLike;
        TextView tvUsername, tvTimeAgo, tvStatus, tvLikeCount, tvCommentCount;

        public MediaViewHolder(@NonNull View itemView) {
            super(itemView);
            mediaCoverImage = itemView.findViewById(R.id.mediaCoverImage);
            userAvatar = itemView.findViewById(R.id.userAvatar);
            ivLike = itemView.findViewById(R.id.ivLike);
            tvUsername = itemView.findViewById(R.id.userName);
            tvTimeAgo = itemView.findViewById(R.id.timeAgo);
            tvStatus = itemView.findViewById(R.id.activityStatus);
            tvLikeCount = itemView.findViewById(R.id.likeCount);
            tvCommentCount = itemView.findViewById(R.id.commentCount);
        }
    }
}