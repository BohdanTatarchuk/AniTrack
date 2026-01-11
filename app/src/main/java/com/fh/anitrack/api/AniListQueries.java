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
                    "    activities (type: MEDIA_LIST, sort: ID_DESC) {" +
                    "      ... on ListActivity {" +
                    "        id status progress createdAt likeCount replyCount" +
                    "        user { name avatar { large } }" +
                    "        media { title { userPreferred } coverImage { large } }" +
                    "      }" +
                    "    }" +
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
}