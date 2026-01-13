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


    // Stats for profile overview page
    public static final String GET_USER_STATS =
            "query { " +
                    "  Viewer { " +
                    "    statistics { " +
                    "      anime { " +
                    "        count " +
                    "        minutesWatched " +
                    "        meanScore " +
                    "        activityHistory { date amount } " +
                    "      } " +
                    "      manga { " +
                    "        count " +
                    "        chaptersRead " +
                    "        meanScore " +
                    "      } " +
                    "    } " +
                    "  } " +
                    "}";
}