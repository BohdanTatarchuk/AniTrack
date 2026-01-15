package com.fh.anitrack.ui.common;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.fh.anitrack.R;
import com.fh.anitrack.api.AuthRepository;
import com.fh.anitrack.api.AniListQueries;
import com.fh.anitrack.api.AniListService;
import com.fh.anitrack.api.GraphQLRequest;
import com.fh.anitrack.api.RetrofitClient;
import com.fh.anitrack.api.response.UserResponse;
import com.fh.anitrack.ui.browse.BrowsePage;
import com.fh.anitrack.ui.browse.MediaPage;
import com.fh.anitrack.ui.home.HomeFragment;
import com.fh.anitrack.api.AniListQueries;
import com.fh.anitrack.api.AniListService;
import com.fh.anitrack.api.AuthRepository;
import com.fh.anitrack.api.GraphQLRequest;
import com.fh.anitrack.api.RetrofitClient;
import com.fh.anitrack.api.response.UserResponse;
import com.fh.anitrack.ui.browse.BrowsePage;
import com.fh.anitrack.ui.notifications.NotificationsPage;
import com.fh.anitrack.ui.profile.ProfileAnimeList;
import com.fh.anitrack.ui.profile.ProfileMangaList;
import com.fh.anitrack.ui.profile.ProfileOverview;
import com.fh.anitrack.ui.settings.SettingsProfile;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NavOverlay extends Fragment {

    private DrawerLayout drawerLayout;
    private AuthRepository authRepository;
    private ImageView userAvatar;
    private TextView navUsername;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nav_overlay, container, false);

        drawerLayout = getActivity().findViewById(R.id.main);
        authRepository = AuthRepository.getInstance(requireContext());

        userAvatar = view.findViewById(R.id.userAvatar);
        navUsername = view.findViewById(R.id.navUsername);

        view.findViewById(R.id.btnClose).setOnClickListener(v -> closeDrawer());

        view.findViewById(R.id.navHome).setOnClickListener(v -> replaceFragment(new HomeFragment()));
        view.findViewById(R.id.navProfile).setOnClickListener(v -> replaceFragment(new ProfileOverview()));
        view.findViewById(R.id.navAnimeList).setOnClickListener(v -> replaceFragment(new ProfileAnimeList()));
        view.findViewById(R.id.navMangaList).setOnClickListener(v -> replaceFragment(new ProfileMangaList()));
        view.findViewById(R.id.navBrowse).setOnClickListener(v -> replaceFragment(new MediaPage()));
        view.findViewById(R.id.navSettings).setOnClickListener(v -> replaceFragment(new SettingsProfile()));
        view.findViewById(R.id.navNotifications).setOnClickListener(v -> replaceFragment(new NotificationsPage()));

        View logoutBtn = view.findViewById(R.id.navLogout);
        if (logoutBtn != null) {
            logoutBtn.setOnClickListener(v -> handleLogout());
        }

        displayUser(authRepository.getUsername(), authRepository.getAvatarUrl());
        if (authRepository.isLoggedIn()) {
            fetchUserData();
        }

        return view;
    }

    private void handleLogout() {
        authRepository.logout();

        CookieManager.getInstance().removeAllCookies(null);
        CookieManager.getInstance().flush();

        Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    private void displayUser(String name, String avatarUrl) {
        navUsername.setText(name);
        if (avatarUrl != null && isAdded()) {
            Glide.with(this)
                    .load(avatarUrl)
                    .circleCrop()
                    .placeholder(R.drawable.profile_picture)
                    .into(userAvatar);
        }
    }

    // Detailed example of a request by Bohdan Tatarchuk
    private void fetchUserData() {
        //1. Create a new query in AniListQueries class
        String query = AniListQueries.GET_CURRENT_USER;
        //2. Create a new request using query from step 1 and our wrapper GraphQLRequest
        GraphQLRequest request = new GraphQLRequest(query, new HashMap<>());

        //3. Init service using retrofit client
        AniListService service = RetrofitClient.getInstance(requireContext()).create(AniListService.class);
        //4. Define a new request in AniListService (if needed) and use the request object from step 3.
        service.getCurrentUser(request).enqueue(new Callback<>() {
            // 6. Create an anonymous callback
            // 7. Override onResponse method - will be called if the request was successful
            @Override
            public void onResponse(@NonNull Call<UserResponse> call, @NonNull Response<UserResponse> response) {
                // its still a good practise to check if the response is successful, if the response body is not null
                // because of different error types (onFailure invokes mostly when there is a network error, so here we cover other issues)
                if (response.isSuccessful() && response.body() != null && response.body().data.viewer != null) {
                    UserResponse.Viewer viewer = response.body().data.viewer;
                    displayUser(viewer.name, viewer.avatar.large);
                    authRepository.saveUserInfo(viewer.name, viewer.avatar.large);
                }
            }

            // 8. Override onFailure method - will be called if the request was not successful
            @Override
            public void onFailure(@NonNull Call<UserResponse> call, @NonNull Throwable t) {
                Log.e("AniTrack", "API Error: " + t.getMessage()); //In this case error will be outputted in the console
            }
        });
    }

    private void replaceFragment(Fragment fragment) {
        getParentFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragment_container, fragment)
                .commit();

        closeDrawer();
    }

    private void closeDrawer() {
        if (drawerLayout != null) {
            drawerLayout.closeDrawer(GravityCompat.END);
        }
    }
}