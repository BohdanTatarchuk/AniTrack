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

import com.bumptech.glide.Glide;
import com.fh.anitrack.R;
import com.fh.anitrack.api.AuthRepository;
import com.google.android.material.button.MaterialButton;

/**
 * Base fragment for all profile views.
 * Contains common elements: banner, avatar, username, and navigation tabs.
 * Child fragments can be loaded into the content container.
 */
public abstract class BaseProfileFragment extends Fragment {

    // Views
    private ImageView profileBanner;
    private ImageView profileAvatar;
    private TextView profileUsername;
    private MaterialButton btnOverview;
    private MaterialButton btnAnimeList;
    private MaterialButton btnMangaList;
    private MaterialButton btnStats;
    private MaterialButton btnFavorites;
    private MaterialButton btnSocial;

    // Data
    private AuthRepository authRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_layout_base, container, false);

        // Initialize repository
        authRepository = AuthRepository.getInstance(requireContext());

        // Initialize views
        profileBanner = view.findViewById(R.id.profileBanner);
        profileAvatar = view.findViewById(R.id.profileAvatar);
        profileUsername = view.findViewById(R.id.profileUsername);
        btnOverview = view.findViewById(R.id.btnOverview);
        btnAnimeList = view.findViewById(R.id.btnAnimeList);
        btnMangaList = view.findViewById(R.id.btnMangaList);
        btnStats = view.findViewById(R.id.btnStats);
        btnFavorites = view.findViewById(R.id.btnFavorites);
        btnSocial = view.findViewById(R.id.btnSocial);

        // Setup navigation
        setupNavigation();

        // Load user data
        loadUserProfile();

        // Load specific content
        loadContent(view);

        return view;
    }

    private void setupNavigation() {
        btnOverview.setOnClickListener(v -> navigateToProfile(new ProfileOverview()));
        btnAnimeList.setOnClickListener(v -> navigateToProfile(new ProfileAnimeList()));
        btnMangaList.setOnClickListener(v -> navigateToProfile(new ProfileMangaList()));
        btnStats.setOnClickListener(v -> navigateToProfile(new ProfileStats()));
        btnFavorites.setOnClickListener(v -> navigateToProfile(new ProfileFavorites()));
        btnSocial.setOnClickListener(v -> navigateToProfile(new ProfileSocial()));

        // Highlight current tab
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

    private void loadUserProfile() {
        // Get username and avatar from AuthRepository
        String username = authRepository.getUsername();
        String avatarUrl = authRepository.getAvatarUrl();

        // Set username
        if (username != null && !username.isEmpty()) {
            profileUsername.setText(username);
        }

        // Load avatar using Glide
        if (avatarUrl != null && !avatarUrl.isEmpty() && isAdded()) {
            Glide.with(this)
                    .load(avatarUrl)
                    .circleCrop()
                    .placeholder(R.drawable.profile_picture)
                    .into(profileAvatar);
        }

        // Load banner (you can add banner URL to AuthRepository if needed)
        // For now using default background color
    }

    /**
     * Abstract method to be implemented by child fragments.
     * Use this to load view-specific content into the content container.
     */
    protected abstract void loadContent(View view);

    /**
     * Abstract method to highlight the current tab.
     * Each child fragment should override this to indicate which tab is active.
     */
    protected abstract void highlightCurrentTab();

    /**
     * Helper method to set tab states
     */
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

    protected MaterialButton getBtnOverview() { return btnOverview; }
    protected MaterialButton getBtnAnimeList() { return btnAnimeList; }
    protected MaterialButton getBtnMangaList() { return btnMangaList; }
    protected MaterialButton getBtnStats() { return btnStats; }
    protected MaterialButton getBtnFavorites() { return btnFavorites; }
    protected MaterialButton getBtnSocial() { return btnSocial; }
}
