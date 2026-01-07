package com.fh.anitrack.data.model;

/**
 * Data model representing an anime/manga item in the browse list.
 */
public class AnimeItem {
    private int id;
    private String title;
    private String coverImageUrl;
    private String description;
    private String releaseInfo;        // e.g., "2007 - 2017" or "Spring 2009"
    private String nextEpisodeInfo;    // e.g., "Ep 10 airing in 5 days, 21 hours"
    private int score;                 // 0-100
    private String studio;
    private String format;             // TV, Movie, OVA, etc.
    private int episodes;
    private int duration;              // in minutes
    private String status;             // FINISHED, RELEASING, NOT_YET_RELEASED, CANCELLED
    private String mediaType;          // ANIME or MANGA

    public AnimeItem() {
    }

    public AnimeItem(int id, String title, String coverImageUrl, String description,
                     String releaseInfo, int score) {
        this.id = id;
        this.title = title;
        this.coverImageUrl = coverImageUrl;
        this.description = description;
        this.releaseInfo = releaseInfo;
        this.score = score;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReleaseInfo() {
        return releaseInfo;
    }

    public void setReleaseInfo(String releaseInfo) {
        this.releaseInfo = releaseInfo;
    }

    public String getNextEpisodeInfo() {
        return nextEpisodeInfo;
    }

    public void setNextEpisodeInfo(String nextEpisodeInfo) {
        this.nextEpisodeInfo = nextEpisodeInfo;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getStudio() {
        return studio;
    }

    public void setStudio(String studio) {
        this.studio = studio;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public int getEpisodes() {
        return episodes;
    }

    public void setEpisodes(int episodes) {
        this.episodes = episodes;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    /**
     * Returns the appropriate emoji based on score.
     * >= 70: Happy (green)
     * >= 50: Neutral (yellow)
     * < 50: Sad (red)
     */
    public ScoreCategory getScoreCategory() {
        if (score >= 70) {
            return ScoreCategory.POSITIVE;
        } else if (score >= 50) {
            return ScoreCategory.NEUTRAL;
        } else {
            return ScoreCategory.NEGATIVE;
        }
    }

    public enum ScoreCategory {
        POSITIVE,   // >= 70%
        NEUTRAL,    // 50-69%
        NEGATIVE    // < 50%
    }
}
