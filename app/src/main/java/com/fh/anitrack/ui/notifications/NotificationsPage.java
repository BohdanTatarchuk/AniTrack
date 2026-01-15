package com.fh.anitrack.ui.notifications;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fh.anitrack.R;
import com.fh.anitrack.api.AniListQueries;
import com.fh.anitrack.api.AniListService;
import com.fh.anitrack.api.AuthRepository;
import com.fh.anitrack.api.GraphQLRequest;
import com.fh.anitrack.api.RetrofitClient;
import com.fh.anitrack.api.response.NotificationsResponse;
import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsPage extends Fragment {
    private static final String TAG = "NotificationsPage";

    private RecyclerView recyclerView;
    private NotificationsAdapter adapter;

    private MaterialButton btnAll, btnAiring, btnActivity, btnFollows, btnMedia;
    private MaterialButton btnMarkAllRead;

    private String currentFilter = "ALL";
    private int currentPage = 1;
    private boolean isLoading = false;

    private AniListService apiService;
    private AuthRepository authRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.notifications_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize API
        apiService = RetrofitClient.getAniListService(requireContext());
        authRepository = AuthRepository.getInstance(requireContext());

        // Initialize views
        recyclerView = view.findViewById(R.id.notificationsRecyclerView);
        btnAll = view.findViewById(R.id.btnAll);
        btnAiring = view.findViewById(R.id.btnAiring);
        btnActivity = view.findViewById(R.id.btnActivity);
        btnFollows = view.findViewById(R.id.btnFollows);
        btnMedia = view.findViewById(R.id.btnMedia);
        btnMarkAllRead = view.findViewById(R.id.btnMarkAllRead);

        // Setup RecyclerView
        adapter = new NotificationsAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        // Setup filter buttons
        setupFilterButtons();

        // Setup mark all as read button
        btnMarkAllRead.setOnClickListener(v -> loadNotifications(true));

        // Load initial notifications
        loadNotifications(false);
    }

    private void setupFilterButtons() {
        btnAll.setOnClickListener(v -> {
            currentFilter = "ALL";
            updateFilterButtons();
            loadNotifications(false);
        });

        btnAiring.setOnClickListener(v -> {
            currentFilter = "AIRING";
            updateFilterButtons();
            loadNotifications(false);
        });

        btnActivity.setOnClickListener(v -> {
            currentFilter = "ACTIVITY";
            updateFilterButtons();
            loadNotifications(false);
        });

        btnFollows.setOnClickListener(v -> {
            currentFilter = "FOLLOWING";
            updateFilterButtons();
            loadNotifications(false);
        });

        btnMedia.setOnClickListener(v -> {
            currentFilter = "MEDIA";
            updateFilterButtons();
            loadNotifications(false);
        });
    }

    private void updateFilterButtons() {
        // Reset all buttons to inactive state
        setButtonInactive(btnAll);
        setButtonInactive(btnAiring);
        setButtonInactive(btnActivity);
        setButtonInactive(btnFollows);
        setButtonInactive(btnMedia);

        // Set active button
        switch (currentFilter) {
            case "ALL":
                setButtonActive(btnAll);
                break;
            case "AIRING":
                setButtonActive(btnAiring);
                break;
            case "ACTIVITY":
                setButtonActive(btnActivity);
                break;
            case "FOLLOWING":
                setButtonActive(btnFollows);
                break;
            case "MEDIA":
                setButtonActive(btnMedia);
                break;
        }
    }

    private void setButtonActive(MaterialButton button) {
        button.setBackgroundTintList(getResources().getColorStateList(R.color.darkBlue35op, null));
        button.setTextColor(getResources().getColor(R.color.pearl, null));
    }

    private void setButtonInactive(MaterialButton button) {
        button.setBackgroundTintList(getResources().getColorStateList(R.color.darkBlue10op, null));
        button.setTextColor(getResources().getColor(R.color.darkBlue, null));
    }

    private void loadNotifications(boolean resetCount) {
        if (isLoading) return;
        isLoading = true;

        // Build types list based on filter
        List<String> types = getNotificationTypes(currentFilter);

        // Build variables
        Map<String, Object> variables = new HashMap<>();
        variables.put("page", currentPage);
        if (!types.isEmpty()) {
            variables.put("types", types);
        }

        GraphQLRequest request = new GraphQLRequest(AniListQueries.GET_NOTIFICATIONS, variables);

        apiService.getNotifications(request).enqueue(new Callback<NotificationsResponse>() {
            @Override
            public void onResponse(@NonNull Call<NotificationsResponse> call, @NonNull Response<NotificationsResponse> response) {
                isLoading = false;

                if (response.isSuccessful() && response.body() != null) {
                    NotificationsResponse.Data data = response.body().data;
                    if (data != null && data.page != null && data.page.notifications != null) {
                        adapter.setNotifications(data.page.notifications);

                        // Update button counts if available
                        if (data.page.pageInfo != null) {
                            updateButtonCounts(data.page.pageInfo);
                        }
                    } else {
                        Log.e(TAG, "Response data is null");
                        adapter.setNotifications(new ArrayList<>());
                    }
                } else {
                    Log.e(TAG, "API call failed: " + response.code());
                    if (response.errorBody() != null) {
                        try {
                            Log.e(TAG, "Error body: " + response.errorBody().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(requireContext(), "Failed to load notifications", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<NotificationsResponse> call, @NonNull Throwable t) {
                isLoading = false;
                Log.e(TAG, "API call failed", t);
                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<String> getNotificationTypes(String filter) {
        List<String> types = new ArrayList<>();

        switch (filter) {
            case "AIRING":
                types.add("AIRING");
                break;
            case "ACTIVITY":
                types.add("ACTIVITY_MESSAGE");
                types.add("ACTIVITY_MENTION");
                types.add("ACTIVITY_REPLY");
                types.add("ACTIVITY_REPLY_SUBSCRIBED");
                types.add("ACTIVITY_LIKE");
                types.add("ACTIVITY_REPLY_LIKE");
                break;
            case "FOLLOWING":
                types.add("FOLLOWING");
                break;
            case "MEDIA":
                types.add("RELATED_MEDIA_ADDITION");
                types.add("MEDIA_DATA_CHANGE");
                types.add("MEDIA_MERGE");
                types.add("MEDIA_DELETION");
                break;
            case "ALL":
            default:
                // Empty list means all types
                break;
        }

        return types;
    }

    private void updateButtonCounts(NotificationsResponse.PageInfo pageInfo) {
        // Update button text with counts (would need separate API calls for accurate counts)
        // For now, just show the filter names
    }
}