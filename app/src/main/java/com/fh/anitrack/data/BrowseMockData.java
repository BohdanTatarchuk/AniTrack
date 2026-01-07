package com.fh.anitrack.data;

import com.fh.anitrack.data.model.AnimeItem;
import com.fh.anitrack.data.model.FilterOption;

import java.util.ArrayList;
import java.util.List;

/**
 * Mock data provider for browse page testing.
 * Replace with actual API calls in production.
 */
public class BrowseMockData {

    // Media Types
    public static List<FilterOption> getMediaTypes() {
        List<FilterOption> types = new ArrayList<>();
        types.add(new FilterOption("USERS", "Users"));
        types.add(new FilterOption("ANIME", "Anime"));
        types.add(new FilterOption("MANGA", "Manga"));
        types.add(new FilterOption("CHARACTERS", "Characters"));
        types.add(new FilterOption("STAFF", "Staff"));
        types.add(new FilterOption("STUDIOS", "Studios"));
        return types;
    }

    // Filter Types
    public static List<FilterOption> getFilterTypes() {
        List<FilterOption> types = new ArrayList<>();
        types.add(new FilterOption("GENRES", "Genres"));
        types.add(new FilterOption("YEAR", "Year"));
        types.add(new FilterOption("SEASON", "Season"));
        types.add(new FilterOption("FORMAT", "Format"));
        types.add(new FilterOption("AIRING_STATUS", "Airing Status"));
        types.add(new FilterOption("STREAMING_ON", "Streaming On"));
        types.add(new FilterOption("COUNTRY", "Country of Origin"));
        types.add(new FilterOption("SOURCE", "Source Material"));
        return types;
    }

    // Genre options (subset of actual AniList genres)
    public static List<FilterOption> getGenres() {
        List<FilterOption> genres = new ArrayList<>();
        genres.add(new FilterOption("action", "Action"));
        genres.add(new FilterOption("adventure", "Adventure"));
        genres.add(new FilterOption("comedy", "Comedy"));
        genres.add(new FilterOption("drama", "Drama"));
        genres.add(new FilterOption("fantasy", "Fantasy"));
        genres.add(new FilterOption("horror", "Horror"));
        genres.add(new FilterOption("mystery", "Mystery"));
        genres.add(new FilterOption("psychological", "Psychological"));
        genres.add(new FilterOption("romance", "Romance"));
        genres.add(new FilterOption("sci-fi", "Sci-Fi"));
        genres.add(new FilterOption("slice-of-life", "Slice of Life"));
        genres.add(new FilterOption("sports", "Sports"));
        genres.add(new FilterOption("supernatural", "Supernatural"));
        genres.add(new FilterOption("thriller", "Thriller"));
        return genres;
    }

    // Year options
    public static List<FilterOption> getYears() {
        List<FilterOption> years = new ArrayList<>();
        for (int year = 2026; year >= 1970; year--) {
            years.add(new FilterOption(String.valueOf(year), String.valueOf(year)));
        }
        return years;
    }

    // Season options
    public static List<FilterOption> getSeasons() {
        List<FilterOption> seasons = new ArrayList<>();
        seasons.add(new FilterOption("WINTER", "Winter"));
        seasons.add(new FilterOption("SPRING", "Spring"));
        seasons.add(new FilterOption("SUMMER", "Summer"));
        seasons.add(new FilterOption("FALL", "Fall"));
        return seasons;
    }

    // Format options
    public static List<FilterOption> getFormats() {
        List<FilterOption> formats = new ArrayList<>();
        formats.add(new FilterOption("TV", "TV"));
        formats.add(new FilterOption("TV_SHORT", "TV Short"));
        formats.add(new FilterOption("MOVIE", "Movie"));
        formats.add(new FilterOption("SPECIAL", "Special"));
        formats.add(new FilterOption("OVA", "OVA"));
        formats.add(new FilterOption("ONA", "ONA"));
        formats.add(new FilterOption("MUSIC", "Music"));
        return formats;
    }

    // Airing Status options
    public static List<FilterOption> getAiringStatuses() {
        List<FilterOption> statuses = new ArrayList<>();
        statuses.add(new FilterOption("RELEASING", "Airing"));
        statuses.add(new FilterOption("FINISHED", "Finished"));
        statuses.add(new FilterOption("NOT_YET_RELEASED", "Not Yet Aired"));
        statuses.add(new FilterOption("CANCELLED", "Cancelled"));
        return statuses;
    }

    // Streaming platform options
    public static List<FilterOption> getStreamingPlatforms() {
        List<FilterOption> platforms = new ArrayList<>();
        platforms.add(new FilterOption("crunchyroll", "Crunchyroll"));
        platforms.add(new FilterOption("funimation", "Funimation"));
        platforms.add(new FilterOption("netflix", "Netflix"));
        platforms.add(new FilterOption("amazon", "Amazon Prime"));
        platforms.add(new FilterOption("hulu", "Hulu"));
        platforms.add(new FilterOption("hidive", "HIDIVE"));
        return platforms;
    }

    // Country options
    public static List<FilterOption> getCountries() {
        List<FilterOption> countries = new ArrayList<>();
        countries.add(new FilterOption("JP", "Japan"));
        countries.add(new FilterOption("KR", "South Korea"));
        countries.add(new FilterOption("CN", "China"));
        countries.add(new FilterOption("TW", "Taiwan"));
        return countries;
    }

    // Source material options
    public static List<FilterOption> getSourceMaterials() {
        List<FilterOption> sources = new ArrayList<>();
        sources.add(new FilterOption("ORIGINAL", "Original"));
        sources.add(new FilterOption("MANGA", "Manga"));
        sources.add(new FilterOption("LIGHT_NOVEL", "Light Novel"));
        sources.add(new FilterOption("VISUAL_NOVEL", "Visual Novel"));
        sources.add(new FilterOption("VIDEO_GAME", "Video Game"));
        sources.add(new FilterOption("NOVEL", "Novel"));
        sources.add(new FilterOption("WEB_NOVEL", "Web Novel"));
        return sources;
    }

    /**
     * Get filter options based on filter type selection.
     */
    public static List<FilterOption> getOptionsForFilterType(String filterTypeId) {
        if (filterTypeId == null) return new ArrayList<>();

        switch (filterTypeId) {
            case "GENRES":
                return getGenres();
            case "YEAR":
                return getYears();
            case "SEASON":
                return getSeasons();
            case "FORMAT":
                return getFormats();
            case "AIRING_STATUS":
                return getAiringStatuses();
            case "STREAMING_ON":
                return getStreamingPlatforms();
            case "COUNTRY":
                return getCountries();
            case "SOURCE":
                return getSourceMaterials();
            default:
                return new ArrayList<>();
        }
    }

    // Mock anime list
    public static List<AnimeItem> getMockAnimeList() {
        List<AnimeItem> items = new ArrayList<>();

        AnimeItem item1 = new AnimeItem();
        item1.setId(1);
        item1.setTitle("Jujutsu Kaisen");
        item1.setReleaseInfo("2007 - 2017");
        item1.setScore(30);
        item1.setDescription("A boy fights... for \"the right death.\" Hardship, regret, shame: the negative feelings that humans feel become Curses that lurk in our everyday lives. The Curses run rampant throughout the world, capable of leading people to terrible misfortune and even death. What's more, the Curses can only be exorcised by another Curse.");
        item1.setStudio("Itadori Yuji is a boy with tremendous physical strength, though he lives a completely ordinary high school life. One day");
        items.add(item1);

        AnimeItem item2 = new AnimeItem();
        item2.setId(2);
        item2.setTitle("Solo Leveling");
        item2.setReleaseInfo("Ep 10 airing in");
        item2.setNextEpisodeInfo("5 days, 21 hours");
        item2.setScore(75);
        item2.setDescription("A boy fights... for \"the right death.\" Hardship, regret, shame: the negative feelings that humans feel become Curses that lurk in our everyday lives. The Curses run rampant throughout the world, capable of leading people to terrible misfortune and even death. What's more, the Curses can only be exorcised by another Curse.");
        item2.setStudio("Itadori Yuji is a boy with tremendous physical strength, though he lives a completely ordinary high school life.");
        items.add(item2);

        AnimeItem item3 = new AnimeItem();
        item3.setId(3);
        item3.setTitle("Frieren: Beyond Journey's End");
        item3.setReleaseInfo("Spring 2009");
        item3.setScore(60);
        item3.setDescription("A boy fights... for \"the right death.\" Hardship, regret, shame: the negative feelings that humans feel become Curses that lurk in our everyday lives. The Curses run rampant throughout the world, capable of leading people to terrible misfortune and even death. What's more, the Curses can only be exorcised by another Curse.");
        item3.setStudio("Itadori Yuji is a boy with tremendous physical strength, though he lives a completely ordinary high school life. One day");
        items.add(item3);

        return items;
    }
}
