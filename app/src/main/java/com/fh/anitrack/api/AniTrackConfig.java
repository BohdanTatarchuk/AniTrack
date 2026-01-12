package com.fh.anitrack.api;

import com.fh.anitrack.BuildConfig;

// A class to access sensitive data from generated class BuildConfig, which takes all props from build.gradle.kts (Module:app).
// build.gradle.kts in its turn looks for defined properties in local.properties file. So to define those props:
// 1) Find local.properties file
// 2) define anilist.client.id (e.g. anilist.client.id=12345),
//           anilist.redirect.uri (e.g. anilist.redirect.uri=com.fh.anitrack://auth),
//           anilist.auth.uri (e.g. anilist.auth.uri=https://anilist.co/api/v2/oauth/authorize)
//           anilist.api.uri (e.g. anilist.api.uri=https://graphql.anilist.co)
public class AniTrackConfig {
    private AniTrackConfig() {
    }

    public static final String CLIENT_ID = BuildConfig.ANILIST_CLIENT_ID;
    public static final String REDIRECT_URI = BuildConfig.ANILIST_REDIRECT_URI;
    public static final String AUTH_URL = BuildConfig.ANILIST_AUTH_URL;
    public static final String API_URL = BuildConfig.ANILIST_API_URL;
}
