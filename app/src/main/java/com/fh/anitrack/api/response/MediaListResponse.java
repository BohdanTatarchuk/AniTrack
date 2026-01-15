package com.fh.anitrack.api.response;

import java.util.List;

public class MediaListResponse {
    public Data data;
    public static class Data { public Page Page; }
    public static class Page {
        public PageInfo pageInfo;
        public List<MediaListEntry> mediaList;
    }
    public static class PageInfo { public boolean hasNextPage; public int currentPage; }

    public static class MediaListEntry {
        public int id;
        public double score;
        public int progress;
        public int progressVolumes;
        public Media media;
    }

    public static class Media {
        public int id;
        public Title title;
        public String format;
        public Integer episodes;
        public Integer chapters;
        public CoverImage coverImage;
    }

    public static class Title { public String userPreferred; }
    public static class CoverImage { public String large; }
}