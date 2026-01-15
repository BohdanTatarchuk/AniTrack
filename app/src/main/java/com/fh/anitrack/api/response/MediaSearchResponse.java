package com.fh.anitrack.api.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Response class for media search from AniList GraphQL API.
 */
public class MediaSearchResponse {
    public Data data;

    public static class Data {
        @SerializedName("Page")
        public Page page;
    }

    public static class Page {
        public PageInfo pageInfo;
        public List<Media> media;
    }

    public static class PageInfo {
        public int total;
        public int perPage;
        public int currentPage;
        public int lastPage;
        public boolean hasNextPage;
    }

    public static class Media {
        public int id;
        public Title title;
        public CoverImage coverImage;
        public FuzzyDate startDate;
        public FuzzyDate endDate;
        public String bannerImage;
        public String season;
        public Integer seasonYear;
        public String description;
        public String type;
        public String format;
        public String status;
        public Integer episodes;
        public Integer duration;
        public Integer chapters;
        public Integer volumes;
        public List<String> genres;
        public boolean isAdult;
        public Integer averageScore;
        public int popularity;
        public NextAiringEpisode nextAiringEpisode;
        public MediaListEntry mediaListEntry;
        public Studios studios;
    }

    public static class Title {
        public String userPreferred;
    }

    public static class CoverImage {
        public String extraLarge;
        public String large;
        public String color;
    }

    public static class FuzzyDate {
        public Integer year;
        public Integer month;
        public Integer day;
    }

    public static class NextAiringEpisode {
        public long airingAt;
        public long timeUntilAiring;
        public int episode;
    }

    public static class MediaListEntry {
        public int id;
        public String status;
    }

    public static class Studios {
        public List<StudioEdge> edges;
    }

    public static class StudioEdge {
        public boolean isMain;
        public StudioNode node;
    }

    public static class StudioNode {
        public int id;
        public String name;
    }
}
