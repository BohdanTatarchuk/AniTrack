package com.fh.anitrack.api.response;

import com.google.gson.annotations.SerializedName;

public class UserStatsResponse {
    public Data data;

    public static class Data {
        @SerializedName("Viewer")
        public Viewer viewer;
    }

    public static class Viewer {
        public int id;

        public String name;
        public Avatar avatar;
        @SerializedName("statistics")
        public Statistics statistics;
    }

    public static class Statistics {
        @SerializedName("anime")
        public StatItem anime;
        @SerializedName("manga")
        public StatItem manga;
    }

    public static class StatItem {
        public int count;
        public int minutesWatched;
        public int chaptersRead;
        public float meanScore;
    }

    public static class Avatar { public String large; }
}