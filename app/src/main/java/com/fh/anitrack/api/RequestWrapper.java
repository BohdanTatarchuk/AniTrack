package com.fh.anitrack.api;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RequestWrapper {

    public static <T> void sendRequest(Call<T> call, OnSuccessCallback<T> onSuccess, Context context) {
        sendRequest(call, onSuccess, t -> {
            Log.e("AniTrack", "Request Failed: " + t.getMessage());
        }, context);
    }

    public static <T> void sendRequest(Call<T> call,
                                       OnSuccessCallback<T> onSuccess,
                                       OnFailureCallback onFailure,
                                       Context context) {

        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
                onSuccess.onSuccess(response);
            }

            @Override
            public void onFailure(@NonNull Call<T> call, @NonNull Throwable t) {
                onFailure.onFailure(t);
            }
        });
    }

    public interface OnSuccessCallback<T> {
        void onSuccess(Response<T> response);
    }

    public interface OnFailureCallback {
        void onFailure(Throwable t);
    }
}