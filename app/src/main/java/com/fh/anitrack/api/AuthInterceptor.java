package com.fh.anitrack.data.network;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.fh.anitrack.api.AuthRepository;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private final Context context;

    public AuthInterceptor(Context context) {
        this.context = context.getApplicationContext();
    }

    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        AuthRepository authRepo = AuthRepository.getInstance(context);
        String authHeader = authRepo.getAuthorizationHeader();

        Request.Builder builder = chain.request().newBuilder();
        if (authHeader != null) {
            builder.header("Authorization", authHeader);
        }

        return chain.proceed(builder.build());
    }
}

