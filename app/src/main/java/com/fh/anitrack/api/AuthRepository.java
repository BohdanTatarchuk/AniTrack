package com.fh.anitrack.api;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

//class, which stores access token, username, pfp and is used to define if the user is logged in
public class AuthRepository {

    private static final String KEY_USERNAME = "username";
    private static final String KEY_AVATAR = "avatar_url";

    private static final String PREFS_NAME = "auth_prefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_BANNER = "banner_url";

    private static AuthRepository instance;
    //Preferences are stored in form of key-value pairs
    private final SharedPreferences prefs;

    private AuthRepository(Context context) {
        try {
            prefs = EncryptedSharedPreferences.create(
                    PREFS_NAME,
                    MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
                    context.getApplicationContext(),
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize AuthRepository", e);
        }
    }

    //singleton class
    public static synchronized AuthRepository getInstance(Context context) {
        if (instance == null) {
            instance = new AuthRepository(context);
        }
        return instance;
    }

    public void saveAccessToken(String token) {
        prefs.edit()
                .putString(KEY_ACCESS_TOKEN, token)
                .apply();
    }

    public void saveUserInfo(String username, String avatarUrl, String bannerUrl) {
        prefs.edit()
                .putString(KEY_USERNAME, username)
                .putString(KEY_AVATAR, avatarUrl)
                .putString(KEY_BANNER, bannerUrl)
                .apply();
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "Guest");
    }

    public String getAvatarUrl() {
        return prefs.getString(KEY_AVATAR, null);
    }

    public String getAccessToken() {
        return prefs.getString(KEY_ACCESS_TOKEN, null);
    }

    public String getBannerUrl() {
        return prefs.getString(KEY_BANNER, null);
    }

    public String getAuthorizationHeader() {
        String token = getAccessToken();
        return token != null ? "Bearer " + token : null;
    }

    public boolean isLoggedIn() {
        return getAccessToken() != null;
    }

    public void logout() {
        prefs.edit()
                .clear()
                .apply();
    }
}
