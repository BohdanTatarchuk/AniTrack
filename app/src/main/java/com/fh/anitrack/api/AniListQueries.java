package com.fh.anitrack.api;

//Class used to store custom queries
public class AniListQueries {
    // Get current user data for navigation sidebar
    public static final String GET_CURRENT_USER =
            "query { Viewer { name bannerImage  avatar { large } } }";


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

    // Media detail query for media page
    public static final String GET_MEDIA_DETAILS =
            "query ($id: Int, $type: MediaType) {" +
                    "  Media (id: $id, type: $type) {" +
                    "    id " +
                    "    title { userPreferred romaji english native } " +
                    "    coverImage { extraLarge large color } " +
                    "    bannerImage " +
                    "    startDate { year month day } " +
                    "    endDate { year month day } " +
                    "    description " +
                    "    season " +
                    "    seasonYear " +
                    "    type " +
                    "    format " +
                    "    status(version: 2) " +
                    "    episodes " +
                    "    duration " +
                    "    chapters " +
                    "    volumes " +
                    "    genres " +
                    "    synonyms " +
                    "    source(version: 3) " +
                    "    isAdult " +
                    "    isLocked " +
                    "    meanScore " +
                    "    averageScore " +
                    "    popularity " +
                    "    favourites " +
                    "    hashtag " +
                    "    countryOfOrigin " +
                    "    isLicensed " +
                    "    isFavourite " +
                    "    isRecommendationBlocked " +
                    "    isFavouriteBlocked " +
                    "    isReviewBlocked " +
                    "    nextAiringEpisode { airingAt timeUntilAiring episode } " +
                    "    relations { " +
                    "      edges { " +
                    "        id " +
                    "        relationType(version: 2) " +
                    "        node { " +
                    "          id " +
                    "          title { userPreferred } " +
                    "          format " +
                    "          type " +
                    "          status(version: 2) " +
                    "          bannerImage " +
                    "          coverImage { large } " +
                    "        } " +
                    "      } " +
                    "    } " +
                    "    characterPreview: characters(perPage: 6, sort: [ROLE, RELEVANCE, ID]) { " +
                    "      edges { " +
                    "        id " +
                    "        role " +
                    "        name " +
                    "        voiceActors(language: JAPANESE, sort: [RELEVANCE, ID]) { " +
                    "          id " +
                    "          name { userPreferred } " +
                    "          language: languageV2 " +
                    "          image { large } " +
                    "        } " +
                    "        node { " +
                    "          id " +
                    "          name { userPreferred } " +
                    "          image { large } " +
                    "        } " +
                    "      } " +
                    "    } " +
                    "    staff(perPage: 8, sort: [RELEVANCE, ID]) { " +
                    "      edges { " +
                    "        id " +
                    "        role " +
                    "        node { " +
                    "          id " +
                    "          name { userPreferred } " +
                    "          language: languageV2 " +
                    "          image { large } " +
                    "        } " +
                    "      } " +
                    "    } " +
                    "    studios { " +
                    "      edges { " +
                    "        isMain " +
                    "        node { " +
                    "          id " +
                    "          name " +
                    "        } " +
                    "      } " +
                    "    } " +
                    "    recommendations(perPage: 7, sort: [RATING_DESC, ID]) { " +
                    "      pageInfo { total } " +
                    "      nodes { " +
                    "        id " +
                    "        rating " +
                    "        userRating " +
                    "        mediaRecommendation { " +
                    "          id " +
                    "          title { userPreferred } " +
                    "          format " +
                    "          type " +
                    "          status(version: 2) " +
                    "          bannerImage " +
                    "          coverImage { large } " +
                    "        } " +
                    "      } " +
                    "    } " +
                    "    stats { " +
                    "      statusDistribution { status amount } " +
                    "      scoreDistribution { score amount } " +
                    "    } " +
                    "    tags { " +
                    "      id " +
                    "      name " +
                    "      description " +
                    "      category " +
                    "      rank " +
                    "      isGeneralSpoiler " +
                    "      isMediaSpoiler " +
                    "      isAdult " +
                    "      userId " +
                    "    } " +
                    "    mediaListEntry { " +
                    "      id " +
                    "      status " +
                    "      score " +
                    "      progress " +
                    "      progressVolumes " +
                    "      repeat " +
                    "      priority " +
                    "      private " +
                    "      notes " +
                    "      hiddenFromStatusLists " +
                    "      customLists " +
                    "      advancedScores " +
                    "      startedAt { year month day } " +
                    "      completedAt { year month day } " +
                    "      updatedAt " +
                    "      createdAt " +
                    "    } " +
                    "    rankings { " +
                    "      id " +
                    "      rank " +
                    "      type " +
                    "      format " +
                    "      year " +
                    "      season " +
                    "      allTime " +
                    "      context " +
                    "    } " +
                    "    externalLinks { " +
                    "      id " +
                    "      site " +
                    "      url " +
                    "      type " +
                    "      language " +
                    "      color " +
                    "      icon " +
                    "      notes " +
                    "      isDisabled " +
                    "    } " +
                    "    streamingEpisodes { " +
                    "      title " +
                    "      thumbnail " +
                    "      url " +
                    "      site " +
                    "    } " +
                    "    trailer { " +
                    "      id " +
                    "      site " +
                    "      thumbnail " +
                    "    } " +
                    "  } " +
                    "}";

    // Save Media List Entry Mutation
    public static final String SAVE_MEDIA_LIST_ENTRY =
            "mutation (" +
                    "  $mediaId: Int, " +
                    "  $status: MediaListStatus, " +
                    "  $score: Float, " +
                    "  $progress: Int, " +
                    "  $progressVolumes: Int, " +
                    "  $repeat: Int, " +
                    "  $private: Boolean, " +
                    "  $notes: String, " +
                    "  $startedAt: FuzzyDateInput, " +
                    "  $completedAt: FuzzyDateInput " +
                    ") { " +
                    "  SaveMediaListEntry (" +
                    "    mediaId: $mediaId, " +
                    "    status: $status, " +
                    "    score: $score, " +
                    "    progress: $progress, " +
                    "    progressVolumes: $progressVolumes, " +
                    "    repeat: $repeat, " +
                    "    private: $private, " +
                    "    notes: $notes, " +
                    "    startedAt: $startedAt, " +
                    "    completedAt: $completedAt " +
                    "  ) { " +
                    "    id " +
                    "    mediaId " +
                    "    status " +
                    "    score " +
                    "    progress " +
                    "    progressVolumes " +
                    "    repeat " +
                    "    private " +
                    "    notes " +
                    "    startedAt { year month day } " +
                    "    completedAt { year month day } " +
                    "  } " +
                    "}";

    // Get Notifications Query
    public static final String GET_NOTIFICATIONS =
            "query ($page: Int, $types: [NotificationType]) { " +
                    "  Page(page: $page, perPage: 15) { " +
                    "    pageInfo { total perPage currentPage lastPage hasNextPage } " +
                    "    notifications(type_in: $types, resetNotificationCount: true) { " +
                    "      ... on AiringNotification { " +
                    "        id type episode contexts " +
                    "        media { id type bannerImage title { userPreferred } coverImage { large } } " +
                    "        createdAt " +
                    "      } " +
                    "      ... on RelatedMediaAdditionNotification { " +
                    "        id type context " +
                    "        media { id type bannerImage title { userPreferred } coverImage { large } } " +
                    "        createdAt " +
                    "      } " +
                    "      ... on FollowingNotification { " +
                    "        id type context " +
                    "        user { id name avatar { large } } " +
                    "        createdAt " +
                    "      } " +
                    "      ... on ActivityMessageNotification { " +
                    "        id type context activityId " +
                    "        user { id name avatar { large } } " +
                    "        createdAt " +
                    "      } " +
                    "      ... on ActivityMentionNotification { " +
                    "        id type context activityId " +
                    "        user { id name avatar { large } } " +
                    "        createdAt " +
                    "      } " +
                    "      ... on ActivityReplyNotification { " +
                    "        id type context activityId " +
                    "        user { id name avatar { large } } " +
                    "        createdAt " +
                    "      } " +
                    "      ... on ActivityReplySubscribedNotification { " +
                    "        id type context activityId " +
                    "        user { id name avatar { large } } " +
                    "        createdAt " +
                    "      } " +
                    "      ... on ActivityLikeNotification { " +
                    "        id type context activityId " +
                    "        user { id name avatar { large } } " +
                    "        createdAt " +
                    "      } " +
                    "      ... on ActivityReplyLikeNotification { " +
                    "        id type context activityId " +
                    "        user { id name avatar { large } } " +
                    "        createdAt " +
                    "      } " +
                    "      ... on ThreadCommentMentionNotification { " +
                    "        id type context commentId " +
                    "        thread { id title } " +
                    "        user { id name avatar { large } } " +
                    "        createdAt " +
                    "      } " +
                    "      ... on ThreadCommentReplyNotification { " +
                    "        id type context commentId " +
                    "        thread { id title } " +
                    "        user { id name avatar { large } } " +
                    "        createdAt " +
                    "      } " +
                    "      ... on ThreadCommentSubscribedNotification { " +
                    "        id type context commentId " +
                    "        thread { id title } " +
                    "        user { id name avatar { large } } " +
                    "        createdAt " +
                    "      } " +
                    "      ... on ThreadCommentLikeNotification { " +
                    "        id type context commentId " +
                    "        thread { id title } " +
                    "        user { id name avatar { large } } " +
                    "        createdAt " +
                    "      } " +
                    "      ... on ThreadLikeNotification { " +
                    "        id type context " +
                    "        thread { id title } " +
                    "        user { id name avatar { large } } " +
                    "        createdAt " +
                    "      } " +
                    "      ... on MediaDataChangeNotification { " +
                    "        id type context reason " +
                    "        media { id type bannerImage title { userPreferred } coverImage { large } } " +
                    "        createdAt " +
                    "      } " +
                    "      ... on MediaMergeNotification { " +
                    "        id type context reason deletedMediaTitles " +
                    "        media { id type bannerImage title { userPreferred } coverImage { large } } " +
                    "        createdAt " +
                    "      } " +
                    "      ... on MediaDeletionNotification { " +
                    "        id type context deletedMediaTitle reason " +
                    "        createdAt " +
                    "      } " +
                    "    } " +
                    "  } " +
                    "}";

    // Stats for profile overview page
    public static final String GET_USER_STATS =
            "query { " +
                    "  Viewer { " +
                    "    id " +
                    "    statistics { " +
                    "      anime { count minutesWatched meanScore } " +
                    "      manga { count chaptersRead meanScore } " +
                    "    } " +
                    "  } " +
                    "}";

    // Profile overview user activities
    public static final String GET_USER_ACTIVITIES =
            "query ($userId: Int, $page: Int, $perPage: Int) {" +
                    "  Page (page: $page, perPage: $perPage) {" +
                    "    pageInfo { hasNextPage currentPage }" +
                    "    activities (userId: $userId, sort: ID_DESC) {" +
                    "      __typename" +
                    "      ... on ListActivity {" +
                    "        id type status progress createdAt likeCount replyCount isLiked" +
                    "        media { title { userPreferred } coverImage { large } }" +
                    "      }" +
                    "      ... on TextActivity {" +
                    "        id type text createdAt likeCount replyCount isLiked" +
                    "      }" +
                    "    }" +
                    "  }" +
                    "}";

    // Profile anime and manga lists query
    public static final String GET_USER_MEDIA_LIST =
            "query ($userId: Int, $type: MediaType, $status: MediaListStatus, $page: Int) {" +
                    "  Page(page: $page, perPage: 10) {" +
                    "    pageInfo { hasNextPage currentPage }" +
                    "    mediaList(userId: $userId, type: $type, status: $status, sort: UPDATED_TIME_DESC) {" +
                    "      id score(format: POINT_10) progress progressVolumes" +
                    "      media {" +
                    "        id title { userPreferred } format episodes chapters" +
                    "        coverImage { large }" +
                    "      }" +
                    "    }" +
                    "  }" +
                    "}";

    // Profile favourites - get all favourites
    public static final String GET_USER_FAVORITES =
            "query ($userId: Int, $page: Int) {" +
                    "  User (id: $userId) {" +
                    "    favourites {" +
                    "      anime (page: $page, perPage: 10) {" +
                    "        pageInfo { hasNextPage currentPage }" +
                    "        nodes { id title { userPreferred } format }" +
                    "      }" +
                    "      manga (page: $page, perPage: 10) {" +
                    "        pageInfo { hasNextPage currentPage }" +
                    "        nodes { id title { userPreferred } format }" +
                    "      }" +
                    "      characters (page: $page, perPage: 10) {" +
                    "        pageInfo { hasNextPage currentPage }" +
                    "        nodes { id name { full } }" +
                    "      }" +
                    "      staff (page: $page, perPage: 10) {" +
                    "        pageInfo { hasNextPage currentPage }" +
                    "        nodes { id name { full } }" +
                    "      }" +
                    "    }" +
                    "  }" +
                    "}";
}