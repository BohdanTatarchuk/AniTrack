package com.fh.anitrack.ui.home;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fh.anitrack.R;
import com.fh.anitrack.api.AniListQueries;
import com.fh.anitrack.api.AniListService;
import com.fh.anitrack.api.AuthRepository;
import com.fh.anitrack.api.GraphQLRequest;
import com.fh.anitrack.api.RetrofitClient;
import com.fh.anitrack.api.response.ActivityResponse;
import com.google.android.material.button.MaterialButton;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    //adapters are used for pagination (load more functionality to be precise)
    private ActivityAdapter activityAdapter;
    private TrendingAdapter trendingAdapter;

    //counter variables for pagination. they will be increased if user presses on "load more"
    private int activityPage = 1;
    private int trendingPage = 1;

    public HomeFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // ui elements binding
        RecyclerView rvFeed = view.findViewById(R.id.rvFeed);
        RecyclerView rvTrending = view.findViewById(R.id.rvTrending);
        MaterialButton btnLoadMoreFeed = view.findViewById(R.id.btnLoadMoreFeed);
        MaterialButton btnLoadMoreTitles = view.findViewById(R.id.btnLoadMoreTitles);
        EditText etStatus = view.findViewById(R.id.etStatus);
        TextView tvPreviewContent = view.findViewById(R.id.tvPreviewStatusContent);
        TextView tvPreviewUsername = view.findViewById(R.id.tvPreviewUsername);

        View previewAvatarCard = view.findViewById(R.id.previewAvatarCard);
        ImageView previewAvatar = previewAvatarCard.findViewById(R.id.userAvatar);

        //setting image and username for status edit text
        AuthRepository authRepo = AuthRepository.getInstance(requireContext());
        tvPreviewUsername.setText(authRepo.getUsername());
        Glide.with(this)
                .load(authRepo.getAvatarUrl())
                .circleCrop()
                .placeholder(R.drawable.profile_picture)
                .into(previewAvatar);

        etStatus.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvPreviewContent.setText(s.length() > 0 ? s.toString() : getString(R.string.your_status_will_appear_here));
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        //adapters init
        activityAdapter = new ActivityAdapter();
        rvFeed.setAdapter(activityAdapter);
        rvFeed.setNestedScrollingEnabled(false); // for better performance
        trendingAdapter = new TrendingAdapter();
        rvTrending.setAdapter(trendingAdapter);
        rvTrending.setNestedScrollingEnabled(false); //for better performance

        //listeners for load more buttons
        btnLoadMoreFeed.setOnClickListener(v -> fetchFeed(++activityPage));
        btnLoadMoreTitles.setOnClickListener(v -> fetchTrending(++trendingPage));

        //initial data loading
        fetchFeed(1);
        fetchTrending(1);

        return view;
    }

    private void fetchFeed(int page) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("page", page);
        vars.put("perPage", 10);

        sendRequest(AniListQueries.GET_ACTIVITIES, vars, response -> {
            if (response.body() != null && response.body().data.Page != null) {
                activityAdapter.addItems(response.body().data.Page.activities);
            }
        });
    }

    private void fetchTrending(int page) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("page", page);
        vars.put("perPage", 6);

        sendRequest(AniListQueries.GET_TRENDING, vars, response -> {
            if (response.body() != null && response.body().data.Page != null) {
                trendingAdapter.addItems(response.body().data.Page.media);
            }
        });
    }

    private void sendRequest(String query, Map<String, Object> vars, OnSuccessCallback callback) {
        GraphQLRequest request = new GraphQLRequest(query, vars);
        AniListService service = RetrofitClient.getInstance(requireContext()).create(AniListService.class);

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

    interface OnSuccessCallback {
        void onSuccess(Response<ActivityResponse> response);
    }
}