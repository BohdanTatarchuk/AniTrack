package com.fh.anitrack.ui.notifications;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fh.anitrack.R;
import com.fh.anitrack.api.response.NotificationsResponse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder> {
    private List<NotificationsResponse.Notification> notifications = new ArrayList<>();

    public void setNotifications(List<NotificationsResponse.Notification> notifications) {
        this.notifications = notifications != null ? notifications : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationsResponse.Notification notification = notifications.get(position);
        holder.bind(notification);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        private final ImageView avatar;
        private final TextView text;
        private final TextView time;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.notificationAvatar);
            text = itemView.findViewById(R.id.notificationText);
            time = itemView.findViewById(R.id.notificationTime);
        }

        public void bind(NotificationsResponse.Notification notification) {
            // Format notification text and load avatar based on type
            String notificationText = formatNotificationText(notification);
            text.setText(notificationText);

            // Load avatar
            String avatarUrl = getAvatarUrl(notification);
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(avatarUrl)
                        .circleCrop()
                        .into(avatar);
            } else {
                // Load media cover as fallback
                String mediaImageUrl = getMediaImageUrl(notification);
                if (mediaImageUrl != null && !mediaImageUrl.isEmpty()) {
                    Glide.with(itemView.getContext())
                            .load(mediaImageUrl)
                            .circleCrop()
                            .into(avatar);
                } else {
                    avatar.setImageResource(android.R.drawable.ic_menu_report_image);
                }
            }

            // Format time
            time.setText(formatTime(notification.createdAt));
        }

        private String formatNotificationText(NotificationsResponse.Notification notification) {
            if (notification == null || notification.type == null) {
                return "";
            }

            switch (notification.type) {
                case "AIRING":
                    if (notification.media != null && notification.media.title != null) {
                        return notification.media.title.userPreferred + " episode " +
                                notification.episode + " aired";
                    }
                    return "An episode aired";

                case "FOLLOWING":
                    if (notification.user != null) {
                        return notification.user.name + " started following you.";
                    }
                    return "Someone started following you.";

                case "ACTIVITY_MESSAGE":
                    if (notification.user != null) {
                        return notification.user.name + " sent you a message";
                    }
                    return "You received a message";

                case "ACTIVITY_MENTION":
                    if (notification.user != null) {
                        return notification.user.name + " mentioned you in an activity";
                    }
                    return "Someone mentioned you in an activity";

                case "ACTIVITY_REPLY":
                    if (notification.user != null) {
                        return notification.user.name + " replied to your activity";
                    }
                    return "Someone replied to your activity";

                case "ACTIVITY_REPLY_SUBSCRIBED":
                    if (notification.user != null) {
                        return notification.user.name + " replied to an activity you're subscribed to";
                    }
                    return "New reply on subscribed activity";

                case "ACTIVITY_LIKE":
                    if (notification.user != null) {
                        return notification.user.name + " liked your activity";
                    }
                    return "Someone liked your activity";

                case "ACTIVITY_REPLY_LIKE":
                    if (notification.user != null) {
                        return notification.user.name + " liked your reply";
                    }
                    return "Someone liked your reply";

                case "THREAD_COMMENT_MENTION":
                    if (notification.user != null && notification.thread != null) {
                        return notification.user.name + " mentioned you in " + notification.thread.title;
                    }
                    return "Someone mentioned you in a thread";

                case "THREAD_COMMENT_REPLY":
                    if (notification.user != null && notification.thread != null) {
                        return notification.user.name + " replied to your comment in " + notification.thread.title;
                    }
                    return "Someone replied to your comment";

                case "THREAD_COMMENT_SUBSCRIBED":
                    if (notification.user != null && notification.thread != null) {
                        return notification.user.name + " commented in " + notification.thread.title;
                    }
                    return "New comment in subscribed thread";

                case "THREAD_COMMENT_LIKE":
                    if (notification.user != null && notification.thread != null) {
                        return notification.user.name + " liked your comment in " + notification.thread.title;
                    }
                    return "Someone liked your comment";

                case "THREAD_LIKE":
                    if (notification.user != null && notification.thread != null) {
                        return notification.user.name + " liked your thread " + notification.thread.title;
                    }
                    return "Someone liked your thread";

                case "RELATED_MEDIA_ADDITION":
                    if (notification.media != null && notification.media.title != null) {
                        return notification.media.title.userPreferred + " was added to the site";
                    }
                    return "Related media was added to the site";

                case "MEDIA_DATA_CHANGE":
                    if (notification.media != null && notification.media.title != null) {
                        String reason = notification.reason != null ? " - " + notification.reason : "";
                        return notification.media.title.userPreferred + " data was changed" + reason;
                    }
                    return "Media data was changed";

                case "MEDIA_MERGE":
                    if (notification.media != null && notification.media.title != null) {
                        return notification.media.title.userPreferred + " was merged";
                    }
                    return "Media entries were merged";

                case "MEDIA_DELETION":
                    if (notification.deletedMediaTitle != null) {
                        return notification.deletedMediaTitle + " was deleted";
                    }
                    return "A media entry was deleted";

                default:
                    return notification.context != null ? notification.context : "New notification";
            }
        }

        private String getAvatarUrl(NotificationsResponse.Notification notification) {
            if (notification.user != null && notification.user.avatar != null) {
                return notification.user.avatar.large;
            }
            return null;
        }

        private String getMediaImageUrl(NotificationsResponse.Notification notification) {
            if (notification.media != null && notification.media.coverImage != null) {
                return notification.media.coverImage.large;
            }
            return null;
        }

        private String formatTime(long timestamp) {
            long now = System.currentTimeMillis() / 1000; // Convert to seconds
            long diff = now - timestamp;

            if (diff < 60) {
                return "just now";
            } else if (diff < 3600) {
                long minutes = diff / 60;
                return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
            } else if (diff < 86400) {
                long hours = diff / 3600;
                return hours + (hours == 1 ? " hour ago" : " hours ago");
            } else if (diff < 604800) {
                long days = diff / 86400;
                return days + (days == 1 ? " day ago" : " days ago");
            } else if (diff < 2592000) {
                long weeks = diff / 604800;
                return weeks + (weeks == 1 ? " week ago" : " weeks ago");
            } else if (diff < 31536000) {
                long months = diff / 2592000;
                return months + (months == 1 ? " month ago" : " months ago");
            } else {
                long years = diff / 31536000;
                return years + (years == 1 ? " year ago" : " years ago");
            }
        }
    }
}
