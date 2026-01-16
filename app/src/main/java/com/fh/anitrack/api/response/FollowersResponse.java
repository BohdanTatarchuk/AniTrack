package com.fh.anitrack.api.response;

import java.util.List;

public class FollowersResponse {
    public Data data;

    public static class Data {
        public Page Page;
    }

    public static class Page {
        public PageInfo pageInfo;
        public List<User> followers;
        public List<User> following;
    }

    public static class PageInfo {
        public int total;
        public int perPage;
        public int currentPage;
        public int lastPage;
        public boolean hasNextPage;
    }

    public static class User {
        public int id;
        public String name;
        public Avatar avatar;
    }

    public static class Avatar {
        public String large;
    }
}
