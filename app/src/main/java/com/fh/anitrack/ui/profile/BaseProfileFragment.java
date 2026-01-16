package com.fh.anitrack.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.fh.anitrack.R;
import com.fh.anitrack.api.AuthRepository;
import com.fh.anitrack.ui.MainActivity;
import com.google.android.material.button.MaterialButton;

public abstract class BaseProfileFragment extends Fragment {

    private ImageView profileBanner;
    private ImageView profileAvatar;
    private TextView profileUsername;
    private MaterialButton btnOverview;
    private MaterialButton btnAnimeList;
    private MaterialButton btnMangaList;
    private MaterialButton btnFavorites;
    private MaterialButton btnSocial;

    // Data
    private AuthRepository authRepository;

    protected SwipeRefreshLayout swipeRefresh;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_layout_base, container, false);

        authRepository = AuthRepository.getInstance(requireContext());

        profileBanner = view.findViewById(R.id.profileBanner);
        profileAvatar = view.findViewById(R.id.profileAvatar);
        profileUsername = view.findViewById(R.id.profileUsername);
        btnOverview = view.findViewById(R.id.btnOverview);
        btnAnimeList = view.findViewById(R.id.btnAnimeList);
        btnMangaList = view.findViewById(R.id.btnMangaList);
        btnFavorites = view.findViewById(R.id.btnFavorites);
        btnSocial = view.findViewById(R.id.btnSocial);

        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        swipeRefresh.setColorSchemeResources(R.color.darkBlue);
        swipeRefresh.setOnRefreshListener(() -> {
            loadUserProfile();
            onRefreshTriggered();
        });

        setupNavigation();
        loadUserProfile();
        loadContent(view);

        androidx.core.widget.NestedScrollView scrollView = view.findViewById(R.id.nestedScrollView);
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setupScrollToTop(scrollView);
        }

        return view;
    }

    private void loadUserProfile() {
        String username = authRepository.getUsername();
        String avatarUrl = authRepository.getAvatarUrl();

        if (username != null && !username.isEmpty()) {
            profileUsername.setText(username);
        }

        if (avatarUrl != null && !avatarUrl.isEmpty() && isAdded()) {
            Glide.with(this)
                    .load(avatarUrl)
                    .circleCrop()
                    .placeholder(R.drawable.profile_picture)
                    .into(profileAvatar);
        }

        if (!swipeRefresh.isRefreshing()) stopRefreshing();
    }

    /**
     * Helper to stop the animation from child fragments
     */
    protected void stopRefreshing() {
        if (swipeRefresh != null && swipeRefresh.isRefreshing()) {
            swipeRefresh.setRefreshing(false);
        }
    }

    /**
     * used in child fragments to reset their specific lists/data
     */
    protected void onRefreshTriggered() {
        // Default implementation does nothing
    }

    private void setupNavigation() {
        btnOverview.setOnClickListener(v -> navigateToProfile(new ProfileOverview()));
        btnAnimeList.setOnClickListener(v -> navigateToProfile(new ProfileAnimeList()));
        btnMangaList.setOnClickListener(v -> navigateToProfile(new ProfileMangaList()));
        btnFavorites.setOnClickListener(v -> navigateToProfile(new ProfileFavorites()));
        btnSocial.setOnClickListener(v -> navigateToProfile(new ProfileSocial()));
        highlightCurrentTab();
    }

    private void navigateToProfile(Fragment fragment) {
        if (getParentFragmentManager() != null) {
            getParentFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    protected abstract void loadContent(View view);

    protected abstract void highlightCurrentTab();

    protected void setTabState(MaterialButton button, boolean isActive) {
        if (isActive) {
            button.setTextColor(requireContext().getColor(R.color.darkBlue));
            button.setBackgroundTintList(requireContext().getColorStateList(R.color.pearl));
        } else {
            button.setTextColor(requireContext().getColor(R.color.darkBlue));
            button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    requireContext().getColor(android.R.color.transparent)));
        }
    }

    protected MaterialButton getBtnOverview() {
        return btnOverview;
    }

    protected MaterialButton getBtnAnimeList() {
        return btnAnimeList;
    }

    protected MaterialButton getBtnMangaList() {
        return btnMangaList;
    }

    protected MaterialButton getBtnFavorites() {
        return btnFavorites;
    }

    protected MaterialButton getBtnSocial() {
        return btnSocial;
    }
}