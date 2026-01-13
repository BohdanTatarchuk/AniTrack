package com.fh.anitrack.api;

//Class used to store custom queries
public class AniListQueries {
    // Get current user data for navigation sidebar
    public static final String GET_CURRENT_USER =
            "query { Viewer { name avatar { large } } }";


    // Activity Feed Query (home page)
    public static final String GET_ACTIVITIES =
            "query ($page: Int, $perPage: Int) {" +
                    "  Page (page: $page, perPage: $perPage) {" +
                    "    pageInfo { total currentPage lastPage hasNextPage }" +
                    "    activities (sort: ID_DESC) {" +
                    "      __typename" +
                    "      ... on ListActivity {" +
                    "        id type status progress createdAt likeCount replyCount isLiked" +
                    "        user { name avatar { large } }" +
                    "        media { title { userPreferred } coverImage { large } }" +
                    "      }" +
                    "      ... on TextActivity {" +
                    "        id type text createdAt likeCount replyCount isLiked" +
                    "        user { name avatar { large } }" +
                    "      }" +
                    "    }" +
                    "  }" +
                    "}";

    // Toggle Like Mutation
    public static final String TOGGLE_LIKE =
            "mutation ($id: Int, $type: LikeableType) {" +
                    "  ToggleLikeV2 (id: $id, type: $type) {" +
                    "    ... on ListActivity { id isLiked likeCount }" +
                    "    ... on TextActivity { id isLiked likeCount }" +
                    "  }" +
                    "}";

    // Trending Media Query (home page)
    public static final String GET_TRENDING =
            "query ($page: Int, $perPage: Int) {" +
                    "  Page (page: $page, perPage: $perPage) {" +
                    "    media (sort: [TRENDING_DESC, POPULARITY_DESC]) {" +
                    "      id title { userPreferred } coverImage { large }" +
                    "    }" +
                    "  }" +
                    "}";

    // Publish status (home page)
    public static final String SAVE_TEXT_ACTIVITY =
            "mutation ($text: String) {" +
                    "  SaveTextActivity (text: $text) {" +
                    "    id text" +
                    "  }" +
                    "}";

    // Filter options (genres and tags) for browse page
    public static final String GET_FILTER_OPTIONS =
            "query {" +
                    "  genres: GenreCollection" +
                    "  tags: MediaTagCollection { name description category isAdult }" +
                    "}";

    // Media search query for browse page
    public static final String SEARCH_MEDIA =
            "query(" +
                    "$page: Int = 1 " +
                    "$id: Int " +
                    "$type: MediaType " +
                    "$isAdult: Boolean = false " +
                    "$search: String " +
                    "$format: [MediaFormat] " +
                    "$status: MediaStatus " +
                    "$countryOfOrigin: CountryCode " +
                    "$source: MediaSource " +
                    "$season: MediaSeason " +
                    "$seasonYear: Int " +
                    "$year: String " +
                    "$onList: Boolean " +
                    "$yearLesser: FuzzyDateInt " +
                    "$yearGreater: FuzzyDateInt " +
                    "$episodeLesser: Int " +
                    "$episodeGreater: Int " +
                    "$durationLesser: Int " +
                    "$durationGreater: Int " +
                    "$chapterLesser: Int " +
                    "$chapterGreater: Int " +
                    "$volumeLesser: Int " +
                    "$volumeGreater: Int " +
                    "$licensedBy: [Int] " +
                    "$isLicensed: Boolean " +
                    "$genres: [String] " +
                    "$excludedGenres: [String] " +
                    "$tags: [String] " +
                    "$excludedTags: [String] " +
                    "$minimumTagRank: Int " +
                    "$sort: [MediaSort] = [POPULARITY_DESC, SCORE_DESC]" +
                    ") {" +
                    "  Page(page: $page, perPage: 20) {" +
                    "    pageInfo { total perPage currentPage lastPage hasNextPage }" +
                    "    media(" +
                    "      id: $id " +
                    "      type: $type " +
                    "      season: $season " +
                    "      format_in: $format " +
                    "      status: $status " +
                    "      countryOfOrigin: $countryOfOrigin " +
                    "      source: $source " +
                    "      search: $search " +
                    "      onList: $onList " +
                    "      seasonYear: $seasonYear " +
                    "      startDate_like: $year " +
                    "      startDate_lesser: $yearLesser " +
                    "      startDate_greater: $yearGreater " +
                    "      episodes_lesser: $episodeLesser " +
                    "      episodes_greater: $episodeGreater " +
                    "      duration_lesser: $durationLesser " +
                    "      duration_greater: $durationGreater " +
                    "      chapters_lesser: $chapterLesser " +
                    "      chapters_greater: $chapterGreater " +
                    "      volumes_lesser: $volumeLesser " +
                    "      volumes_greater: $volumeGreater " +
                    "      licensedById_in: $licensedBy " +
                    "      isLicensed: $isLicensed " +
                    "      genre_in: $genres " +
                    "      genre_not_in: $excludedGenres " +
                    "      tag_in: $tags " +
                    "      tag_not_in: $excludedTags " +
                    "      minimumTagRank: $minimumTagRank " +
                    "      sort: $sort " +
                    "      isAdult: $isAdult" +
                    "    ) {" +
                    "      id " +
                    "      title { userPreferred } " +
                    "      coverImage { extraLarge large color } " +
                    "      startDate { year month day } " +
                    "      endDate { year month day } " +
                    "      bannerImage " +
                    "      season " +
                    "      seasonYear " +
                    "      description " +
                    "      type " +
                    "      format " +
                    "      status(version: 2) " +
                    "      episodes " +
                    "      duration " +
                    "      chapters " +
                    "      volumes " +
                    "      genres " +
                    "      isAdult " +
                    "      averageScore " +
                    "      popularity " +
                    "      nextAiringEpisode { airingAt timeUntilAiring episode } " +
                    "      mediaListEntry { id status } " +
                    "      studios(isMain: true) { edges { isMain node { id name } } }" +
                    "    }" +
                    "  }" +
                    "}";

}