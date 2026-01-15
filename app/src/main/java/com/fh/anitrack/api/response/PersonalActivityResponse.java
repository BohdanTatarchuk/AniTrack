package com.fh.anitrack.api.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PersonalActivityResponse {
    public Data data;

    public static class Data {
        @SerializedName("Page")
        public Page page;
    }

    public static class Page {
        public PageInfo pageInfo;
        public List<Activity> activities;
    }

    public static class PageInfo {
        public boolean hasNextPage;
        public int currentPage;
    }

    public static class Activity {
        public int id;
        public String type;
        public String status;
        public String progress;
        public String text;
        public long createdAt;
        public int likeCount;
        public int replyCount;
        public boolean isLiked;
        public Media media;
    }

    public static class Media {
        public Title title;
        public CoverImage coverImage;
    }

    public static class Title {
        public String userPreferred;
    }

    public static class CoverImage {
        public String large;
    }
}