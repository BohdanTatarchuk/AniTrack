package com.fh.anitrack.ui.profile;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fh.anitrack.R;
import com.fh.anitrack.api.AniListQueries;
import com.fh.anitrack.api.AniListService;
import com.fh.anitrack.api.AuthRepository;
import com.fh.anitrack.api.GraphQLRequest;
import com.fh.anitrack.api.RetrofitClient;
import com.fh.anitrack.api.response.FollowersResponse;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Profile Social Fragment - displays user's social connections and activity.
 */
public class ProfileSocial extends BaseProfileFragment {

    private MaterialButton btnFollowing;
    private MaterialButton btnFollowers;
    private RecyclerView rvSocialUsers;
    private SocialUserAdapter adapter;
    private boolean showingFollowers = true;

    public ProfileSocial() {
        // Required empty public constructor
    }

    public static ProfileSocial newInstance() {
        return new ProfileSocial();
    }

    @Override
    protected void loadContent(View view) {
        // Get the content container
        FrameLayout contentContainer = view.findViewById(R.id.profileContentContainer);
        
        // Inflate the specific content for social
        View socialContent = LayoutInflater.from(requireContext())
                .inflate(R.layout.profile_content_social, contentContainer, false);
        
        // Add to container
        contentContainer.removeAllViews();
        contentContainer.addView(socialContent);
        
        // Setup views
        setupSocialViews(socialContent);
    }

    private void setupSocialViews(View view) {
        btnFollowing = view.findViewById(R.id.btnFollowing);
        btnFollowers = view.findViewById(R.id.btnFollowers);
        rvSocialUsers = view.findViewById(R.id.rvSocialUsers);

        // Setup grid layout (3 columns)
        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 3);
        rvSocialUsers.setLayoutManager(gridLayoutManager);

        // Setup adapter
        adapter = new SocialUserAdapter();
        rvSocialUsers.setAdapter(adapter);

        // Setup tab buttons
        setupTabButtons();

        // Load initial data (followers)
        loadFollowers();
    }

    private void setupTabButtons() {
        btnFollowing.setOnClickListener(v -> {
            if (showingFollowers) {
                showingFollowers = false;
                updateTabStyles();
                loadFollowing();
            }
        });

        btnFollowers.setOnClickListener(v -> {
            if (!showingFollowers) {
                showingFollowers = true;
                updateTabStyles();
                loadFollowers();
            }
        });

        updateTabStyles();
    }

    private void updateTabStyles() {
        if (showingFollowers) {
            // Followers tab is active
            btnFollowers.setTextColor(requireContext().getColor(R.color.pearl));
            btnFollowers.setBackgroundTintList(requireContext().getColorStateList(R.color.darkBlue35op));
            // Following tab is inactive
            btnFollowing.setTextColor(requireContext().getColor(R.color.darkBlue));
            btnFollowing.setBackgroundTintList(requireContext().getColorStateList(R.color.darkBlue10op));
        } else {
            // Following tab is active
            btnFollowing.setTextColor(requireContext().getColor(R.color.pearl));
            btnFollowing.setBackgroundTintList(requireContext().getColorStateList(R.color.darkBlue35op));
            // Followers tab is inactive
            btnFollowers.setTextColor(requireContext().getColor(R.color.darkBlue));
            btnFollowers.setBackgroundTintList(requireContext().getColorStateList(R.color.darkBlue10op));
        }
    }

    private void loadFollowers() {
        AniListService api = RetrofitClient.getInstance(requireContext()).create(AniListService.class);
        AuthRepository authRepo = AuthRepository.getInstance(requireContext());
        
        int userId = authRepo.getUserId();
        if (userId == 0) {
            Toast.makeText(requireContext(), "User ID not available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("id", userId);
        variables.put("page", 1);
        
        GraphQLRequest request = new GraphQLRequest(AniListQueries.GET_FOLLOWERS, variables);
        Call<FollowersResponse> call = api.getFollowers(request);
        
        call.enqueue(new Callback<FollowersResponse>() {
            @Override
            public void onResponse(Call<FollowersResponse> call, Response<FollowersResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().data != null && response.body().data.Page != null) {
                        List<FollowersResponse.User> followers = response.body().data.Page.followers;
                        if (followers != null && !followers.isEmpty()) {
                            List<SocialUser> socialUsers = new ArrayList<>();
                            for (FollowersResponse.User user : followers) {
                                String avatarUrl = user.avatar != null ? user.avatar.large : null;
                                socialUsers.add(new SocialUser(String.valueOf(user.id), user.name, avatarUrl));
                            }
                            adapter.setUsers(socialUsers);
                        } else {
                            Log.d("ProfileSocial", "No followers found");
                            adapter.setUsers(new ArrayList<>());
                            Toast.makeText(requireContext(), "No followers", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("ProfileSocial", "Response data is null");
                        Toast.makeText(requireContext(), "Invalid response format", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                        Log.e("ProfileSocial", "Failed to load followers: " + response.code() + " - " + errorBody);
                    } catch (Exception e) {
                        Log.e("ProfileSocial", "Failed to load followers: " + response.code());
                    }
                    Toast.makeText(requireContext(), "Failed to load followers: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<FollowersResponse> call, Throwable t) {
                Log.e("ProfileSocial", "Error loading followers", t);
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFollowing() {
        AniListService api = RetrofitClient.getInstance(requireContext()).create(AniListService.class);
        AuthRepository authRepo = AuthRepository.getInstance(requireContext());
        
        int userId = authRepo.getUserId();
        if (userId == 0) {
            Toast.makeText(requireContext(), "User ID not available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("id", userId);
        variables.put("page", 1);
        
        GraphQLRequest request = new GraphQLRequest(AniListQueries.GET_FOLLOWING, variables);
        Call<FollowersResponse> call = api.getFollowing(request);
        
        call.enqueue(new Callback<FollowersResponse>() {
            @Override
            public void onResponse(Call<FollowersResponse> call, Response<FollowersResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().data != null && response.body().data.Page != null) {
                        List<FollowersResponse.User> following = response.body().data.Page.following;
                        if (following != null && !following.isEmpty()) {
                            List<SocialUser> socialUsers = new ArrayList<>();
                            for (FollowersResponse.User user : following) {
                                String avatarUrl = user.avatar != null ? user.avatar.large : null;
                                socialUsers.add(new SocialUser(String.valueOf(user.id), user.name, avatarUrl));
                            }
                            adapter.setUsers(socialUsers);
                        } else {
                            Log.d("ProfileSocial", "No following found");
                            adapter.setUsers(new ArrayList<>());
                            Toast.makeText(requireContext(), "Not following anyone", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("ProfileSocial", "Response data is null");
                        Toast.makeText(requireContext(), "Invalid response format", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                        Log.e("ProfileSocial", "Failed to load following: " + response.code() + " - " + errorBody);
                    } catch (Exception e) {
                        Log.e("ProfileSocial", "Failed to load following: " + response.code());
                    }
                    Toast.makeText(requireContext(), "Failed to load following: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<FollowersResponse> call, Throwable t) {
                Log.e("ProfileSocial", "Error loading following", t);
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void highlightCurrentTab() {
        setTabState(getBtnOverview(), false);
        setTabState(getBtnAnimeList(), false);
        setTabState(getBtnMangaList(), false);
        setTabState(getBtnStats(), false);
        setTabState(getBtnFavorites(), false);
        setTabState(getBtnSocial(), true);
    }

    /**
     * Simple user data class for social lists.
     */
    public static class SocialUser {
        public String id;
        public String name;
        public String avatarUrl;

        public SocialUser(String id, String name, String avatarUrl) {
            this.id = id;
            this.name = name;
            this.avatarUrl = avatarUrl;
        }
    }
}