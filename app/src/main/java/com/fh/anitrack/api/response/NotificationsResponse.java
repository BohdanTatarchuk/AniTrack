package com.fh.anitrack.api.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class NotificationsResponse {
    @SerializedName("data")
    public Data data;

    public static class Data {
        @SerializedName("Page")
        public Page page;
    }

    public static class Page {
        @SerializedName("pageInfo")
        public PageInfo pageInfo;

        @SerializedName("notifications")
        public List<Notification> notifications;
    }

    public static class PageInfo {
        @SerializedName("total")
        public int total;

        @SerializedName("perPage")
        public int perPage;

        @SerializedName("currentPage")
        public int currentPage;

        @SerializedName("lastPage")
        public int lastPage;

        @SerializedName("hasNextPage")
        public boolean hasNextPage;
    }

    public static class Notification {
        @SerializedName("id")
        public int id;

        @SerializedName("type")
        public String type;

        @SerializedName("context")
        public String context;

        @SerializedName("contexts")
        public List<String> contexts;

        @SerializedName("episode")
        public Integer episode;

        @SerializedName("activityId")
        public Integer activityId;

        @SerializedName("commentId")
        public Integer commentId;

        @SerializedName("reason")
        public String reason;

        @SerializedName("deletedMediaTitle")
        public String deletedMediaTitle;

        @SerializedName("deletedMediaTitles")
        public List<String> deletedMediaTitles;

        @SerializedName("media")
        public Media media;

        @SerializedName("user")
        public User user;

        @SerializedName("thread")
        public Thread thread;

        @SerializedName("createdAt")
        public long createdAt;
    }

    public static class Media {
        @SerializedName("id")
        public int id;

        @SerializedName("type")
        public String type;

        @SerializedName("bannerImage")
        public String bannerImage;

        @SerializedName("title")
        public MediaTitle title;

        @SerializedName("coverImage")
        public CoverImage coverImage;
    }

    public static class MediaTitle {
        @SerializedName("userPreferred")
        public String userPreferred;
    }

    public static class CoverImage {
        @SerializedName("large")
        public String large;
    }

    public static class User {
        @SerializedName("id")
        public int id;

        @SerializedName("name")
        public String name;

        @SerializedName("avatar")
        public Avatar avatar;
    }

    public static class Avatar {
        @SerializedName("large")
        public String large;
    }

    public static class Thread {
        @SerializedName("id")
        public int id;

        @SerializedName("title")
        public String title;
    }
}
