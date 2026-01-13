package com.fh.anitrack.api;

import com.fh.anitrack.api.response.ActivityResponse;
import com.fh.anitrack.api.response.SaveActivityResponse;
import com.fh.anitrack.api.response.ToggleLikeResponse;
import com.fh.anitrack.api.response.UserResponse;
import com.fh.anitrack.api.response.UserStatsResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

// A retrofit interface, which defines api endpoints. Anilist uses graphQl, so all calls are POST to the root "/".
public interface AniListService {
    @POST("/")
    Call<UserResponse> getCurrentUser(@Body GraphQLRequest body);

    @POST("/")
    Call<ActivityResponse> postQuery(@Body GraphQLRequest body);

    @POST("/")
    Call<SaveActivityResponse> saveTextActivity(@Body GraphQLRequest body);

    @POST("/")
    Call<ToggleLikeResponse> toggleLike(@Body GraphQLRequest body);

    @POST("/")
    Call<UserStatsResponse> getUserStats(@Body GraphQLRequest body);

}
