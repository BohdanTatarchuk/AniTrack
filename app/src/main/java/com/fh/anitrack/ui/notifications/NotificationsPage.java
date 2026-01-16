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
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

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

    private ChipGroup filterChipGroup;
    private Chip chipAll, chipAiring, chipActivity, chipFollows, chipMedia;

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
        filterChipGroup = view.findViewById(R.id.filterChipGroup);
        chipAll = view.findViewById(R.id.chipAll);
        chipAiring = view.findViewById(R.id.chipAiring);
        chipActivity = view.findViewById(R.id.chipActivity);
        chipFollows = view.findViewById(R.id.chipFollows);
        chipMedia = view.findViewById(R.id.chipMedia);

        // Setup RecyclerView
        adapter = new NotificationsAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        // Setup filter chips
        setupFilterChips();

        // Load initial notifications
        loadNotifications(false);
    }

    private void setupFilterChips() {
        filterChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            
            int checkedId = checkedIds.get(0);
            
            if (checkedId == R.id.chipAll) {
                currentFilter = "ALL";
            } else if (checkedId == R.id.chipAiring) {
                currentFilter = "AIRING";
            } else if (checkedId == R.id.chipActivity) {
                currentFilter = "ACTIVITY";
            } else if (checkedId == R.id.chipFollows) {
                currentFilter = "FOLLOWING";
            } else if (checkedId == R.id.chipMedia) {
                currentFilter = "MEDIA";
            }
            
            loadNotifications(false);
        });
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