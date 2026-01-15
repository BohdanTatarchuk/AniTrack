package com.fh.anitrack.api.response;

import java.util.List;

//this class describes how does the response to getActivities() api call looks. Is used on the home page
public class ActivityResponse {
    public Data data;

    public static class Data {
        public Page Page;
    }

    public static class Page {
        public PageInfo pageInfo;
        public List<Activity> activities;

        public List<Media> media;
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
        public boolean isLiked;
        public int replyCount;
        public User user;
        public Media media;
    }

    public static class Media {
        public int id;
        public Title title;
        public CoverImage coverImage;
    }

    public static class Title {
        public String userPreferred;
    }

    public static class CoverImage {
        public String large;
    }

    public static class User {
        public String name;
        public Avatar avatar;
    }

    public static class Avatar {
        public String large;
    }
}