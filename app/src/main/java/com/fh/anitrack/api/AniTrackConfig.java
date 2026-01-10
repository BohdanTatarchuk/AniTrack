package com.fh.anitrack.api;

import com.fh.anitrack.BuildConfig;

public class AniTrackConfig {
    private AniTrackConfig() {
    }

    public static final String CLIENT_ID = BuildConfig.ANILIST_CLIENT_ID;
    public static final String REDIRECT_URI = BuildConfig.ANILIST_REDIRECT_URI;
    public static final String AUTH_URL = BuildConfig.ANILIST_AUTH_URL;
    public static final String API_URL = BuildConfig.ANILIST_API_URL;
}
