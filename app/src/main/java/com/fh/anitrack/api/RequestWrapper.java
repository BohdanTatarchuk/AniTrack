package com.fh.anitrack.api;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.fh.anitrack.api.response.ActivityResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public  class RequestWrapper {
    public static void sendRequest(String query, Map<String, Object> vars, OnSuccessCallback callback, Context context) {
        GraphQLRequest request = new GraphQLRequest(query, vars);
        AniListService service = RetrofitClient.getInstance(context).create(AniListService.class);

        service.postQuery(request).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ActivityResponse> call, @NonNull Response<ActivityResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response);
                } else {
                    Log.e("AniTrack", "Server Error: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ActivityResponse> call, @NonNull Throwable t) {
                Log.e("AniTrack", "Network Failure", t);
            }
        });
    }
    public interface OnSuccessCallback {
        void onSuccess(Response<ActivityResponse> response);
    }
}
