package com.fh.anitrack.api;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

public class AuthRepository {

    private static final String PREFS_NAME = "auth_prefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";

    private static AuthRepository instance;
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

    public String getAccessToken() {
        return prefs.getString(KEY_ACCESS_TOKEN, null);
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
