package com.fh.anitrack.api.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class FavoritesResponse {
    public Data data;

    public static class Data {
        @SerializedName("User")
        public User user;
    }

    public static class User {
        public Favourites favourites;
    }

    public static class Favourites {
        public Connection anime;
        public Connection manga;
        public Connection characters;
        public Connection staff;
    }

    public static class Connection {
        public PageInfo pageInfo;
        public List<FavoriteNode> nodes;
    }

    public static class PageInfo {
        public boolean hasNextPage;
        public int currentPage;
    }

    public static class FavoriteNode {
        public int id;
        public Title title;
        public String format;
        public Name name;
    }

    public static class Title {
        public String userPreferred;
    }

    public static class Name {
        public String full;
    }
}