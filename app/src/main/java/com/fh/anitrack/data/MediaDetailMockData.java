package com.fh.anitrack.data;

/**
 * Mock data for testing the media detail screen.
 * This class contains all hardcoded test data that should be replaced
 * with actual backend data in production.
 * 
 * To use mock data, call MediaDetailMockData.loadIntoActivity() from your activity.
 * For production, remove the mock data loading and use your backend service instead.
 */
public class MediaDetailMockData {

    // Media info
    public static final String TITLE = "Jujutsu Kaisen Season 2";
    public static final String DESCRIPTION = "The second season of Jujutsu Kaisen.\n\n" +
            "The past comes to light when second-year students Satoru Gojou and Suguru Getou " +
            "are tasked with escorting young Riko Amanai to Master Tengen. But when a non-sorcerer " +
            "user tries to kill them, their mission to protect the Star Plasma Vessel threatens to " +
            "turn them into bitter enemies and cement their destinies—one as the world's strongest " +
            "sorcerer, and the other its most twisted curse user!";
    public static final String DESCRIPTION_SOURCE = "(Source: Crunchyroll)";

    // Badges
    public static final String BADGE_HIGHEST_RATED = "#34 Highest of All Time";
    public static final String BADGE_MOST_POPULAR = "#64 Most Popular of All Time";

    // Stats
    public static final String FAVORITE_COUNT = "18.1k";
    public static final String STAT_AVERAGE_SCORE = "85%";
    public static final String STAT_MEAN_SCORE = "84%";
    public static final String STAT_POPULARITY = "#25";
    public static final String STAT_FAVORITES = "18.1k";
    public static final String STAT_STUDIOS = "MAPPA";
    public static final String STAT_FORMAT = "TV";
    public static final String STAT_EPISODES = "23";
    public static final String STAT_DURATION = "24 min";
    public static final String STAT_STATUS = "Finished";
    public static final String STAT_SEASON = "Summer 2023";

    // Format items
    public static final String[] FORMAT_ITEMS = {"TV", "23 Episodes", "24 min", "Finished", "Summer 2023"};

    // Tags with percentages
    public static final TagData[] TAGS = {
            new TagData("Magic", 93),
            new TagData("Superpowers", 91),
            new TagData("Curses", 76),
            new TagData("Urban", 64),
            new TagData("Shounen", 87),
            new TagData("School", 54),
            new TagData("Gore", 72)
    };

    // Relations
    public static final RelationData[] RELATIONS = {
            new RelationData("Source", "Jujutsu Kaisen (Manga)"),
            new RelationData("Prequel", "Jujutsu Kaisen Season 1"),
            new RelationData("Side Story", "Jujutsu Kaisen 0"),
            new RelationData("Sequel", "Jujutsu Kaisen Season 3")
    };

    // Characters
    public static final CharacterData[] CHARACTERS = {
            new CharacterData("Yuuji Itadori", "Main", "Junya Enoki", "Japanese"),
            new CharacterData("Satoru Gojou", "Main", "Yuuichi Nakamura", "Japanese"),
            new CharacterData("Megumi Fushiguro", "Main", "Yuuma Uchida", "Japanese"),
            new CharacterData("Nobara Kugisaki", "Main", "Asami Seto", "Japanese"),
            new CharacterData("Suguru Getou", "Supporting", "Takahiro Sakurai", "Japanese"),
            new CharacterData("Kento Nanami", "Supporting", "Kenjiro Tsuda", "Japanese"),
            new CharacterData("Toji Fushiguro", "Supporting", "Takehito Koyasu", "Japanese"),
            new CharacterData("Riko Amanai", "Supporting", "Anna Nagase", "Japanese")
    };

    // Recommendations
    public static final RecommendationData[] RECOMMENDATIONS = {
            new RecommendationData("Bleach: Thousand-Year Blood War"),
            new RecommendationData("Chainsaw Man"),
            new RecommendationData("Demon Slayer"),
            new RecommendationData("My Hero Academia"),
            new RecommendationData("Attack on Titan"),
            new RecommendationData("Hunter x Hunter")
    };

    // Data classes
    public static class TagData {
        public final String name;
        public final int percentage;

        public TagData(String name, int percentage) {
            this.name = name;
            this.percentage = percentage;
        }
    }

    public static class RelationData {
        public final String type;
        public final String title;

        public RelationData(String type, String title) {
            this.type = type;
            this.title = title;
        }
    }

    public static class CharacterData {
        public final String characterName;
        public final String role;
        public final String voiceActorName;
        public final String language;

        public CharacterData(String characterName, String role, String voiceActorName, String language) {
            this.characterName = characterName;
            this.role = role;
            this.voiceActorName = voiceActorName;
            this.language = language;
        }
    }

    public static class RecommendationData {
        public final String title;

        public RecommendationData(String title) {
            this.title = title;
        }
    }
}
