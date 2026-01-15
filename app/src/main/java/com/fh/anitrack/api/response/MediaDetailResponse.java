package com.fh.anitrack.api.response;

import java.util.List;

/**
 * Response class for media details from AniList GraphQL API.
 */
public class MediaDetailResponse {
    public Data data;

    public static class Data {
        public Media Media;
    }

    public static class Media {
        public int id;
        public Title title;
        public CoverImage coverImage;
        public String bannerImage;
        public FuzzyDate startDate;
        public FuzzyDate endDate;
        public String description;
        public String season;
        public Integer seasonYear;
        public String type;
        public String format;
        public String status;
        public Integer episodes;
        public Integer duration;
        public Integer chapters;
        public Integer volumes;
        public List<String> genres;
        public List<String> synonyms;
        public String source;
        public boolean isAdult;
        public boolean isLocked;
        public Integer meanScore;
        public Integer averageScore;
        public int popularity;
        public int favourites;
        public String hashtag;
        public String countryOfOrigin;
        public Boolean isLicensed;
        public boolean isFavourite;
        public boolean isRecommendationBlocked;
        public boolean isFavouriteBlocked;
        public boolean isReviewBlocked;
        public NextAiringEpisode nextAiringEpisode;
        public Relations relations;
        public Characters characterPreview;
        public Staff staff;
        public Studios studios;
        public Recommendations recommendations;
        public Stats stats;
        public List<Tag> tags;
        public MediaListEntry mediaListEntry;
        public List<Ranking> rankings;
        public List<ExternalLink> externalLinks;
        public List<StreamingEpisode> streamingEpisodes;
        public Trailer trailer;
    }

    public static class Title {
        public String userPreferred;
        public String romaji;
        public String english;
        public String nativeName;
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

    public static class Relations {
        public List<RelationEdge> edges;
    }

    public static class RelationEdge {
        public int id;
        public String relationType;
        public MediaNode node;
    }

    public static class MediaNode {
        public int id;
        public Title title;
        public String format;
        public String type;
        public String status;
        public String bannerImage;
        public CoverImage coverImage;
    }

    public static class Characters {
        public List<CharacterEdge> edges;
    }

    public static class CharacterEdge {
        public int id;
        public String role;
        public String name;
        public List<VoiceActor> voiceActors;
        public CharacterNode node;
    }

    public static class CharacterNode {
        public int id;
        public Name name;
        public Image image;
    }

    public static class VoiceActor {
        public int id;
        public Name name;
        public String language;
        public Image image;
    }

    public static class Name {
        public String userPreferred;
    }

    public static class Image {
        public String large;
    }

    public static class Staff {
        public List<StaffEdge> edges;
    }

    public static class StaffEdge {
        public int id;
        public String role;
        public StaffNode node;
    }

    public static class StaffNode {
        public int id;
        public Name name;
        public String language;
        public Image image;
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

    public static class Recommendations {
        public PageInfo pageInfo;
        public List<RecommendationNode> nodes;
    }

    public static class RecommendationNode {
        public int id;
        public int rating;
        public String userRating;
        public MediaNode mediaRecommendation;
    }

    public static class PageInfo {
        public int total;
    }

    public static class Stats {
        public List<StatusDistribution> statusDistribution;
        public List<ScoreDistribution> scoreDistribution;
    }

    public static class StatusDistribution {
        public String status;
        public int amount;
    }

    public static class ScoreDistribution {
        public int score;
        public int amount;
    }

    public static class Tag {
        public int id;
        public String name;
        public String description;
        public String category;
        public int rank;
        public boolean isGeneralSpoiler;
        public boolean isMediaSpoiler;
        public boolean isAdult;
        public Integer userId;
    }

    public static class MediaListEntry {
        public int id;
        public String status;
        public double score;
        public int progress;
        public Integer progressVolumes;
        public int repeat;
        public int priority;
        public boolean isPrivate;
        public String notes;
        public boolean hiddenFromStatusLists;
        public Object customLists;
        public Object advancedScores;
        public FuzzyDate startedAt;
        public FuzzyDate completedAt;
        public long updatedAt;
        public long createdAt;
    }

    public static class Ranking {
        public int id;
        public int rank;
        public String type;
        public String format;
        public Integer year;
        public String season;
        public Boolean allTime;
        public String context;
    }

    public static class ExternalLink {
        public int id;
        public String site;
        public String url;
        public String type;
        public String language;
        public String color;
        public String icon;
        public String notes;
        public boolean isDisabled;
    }

    public static class StreamingEpisode {
        public String title;
        public String thumbnail;
        public String url;
        public String site;
    }

    public static class Trailer {
        public String id;
        public String site;
        public String thumbnail;
    }
}
