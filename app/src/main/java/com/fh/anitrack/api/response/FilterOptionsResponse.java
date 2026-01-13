package com.fh.anitrack.api.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Response class for filter options (genres and tags) from AniList GraphQL API.
 * Query: query { genres: GenreCollection tags: MediaTagCollection { name description category isAdult } }
 */
public class FilterOptionsResponse {
    public Data data;

    public static class Data {
        @SerializedName("genres")
        public List<String> genres;

        @SerializedName("tags")
        public List<MediaTag> tags;
    }

    public static class MediaTag {
        public String name;
        public String description;
        public String category;
        public boolean isAdult;
    }
}
