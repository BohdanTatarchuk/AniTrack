package com.fh.anitrack.api;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

// every time a request is being made, this class intercepts it, checks the AuthRepository if we have an auth token
// and attaches it in the auth header
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

