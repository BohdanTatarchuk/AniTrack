package com.fh.anitrack.api.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class UserSearchResponse {
    public Data data;

    public static class Data {
        @SerializedName("Page")
        public Page page;
    }

    public static class Page {
        public PageInfo pageInfo;
        public List<User> users;
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
        public String about;
        public String bannerImage;
    }

    public static class Avatar {
        public String large;
        public String medium;
    }
}
