package com.fh.anitrack.ui.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.fh.anitrack.R;

/**
 * Profile Favorites Fragment - displays user's favorite anime/manga/characters.
 */
public class ProfileFavorites extends BaseProfileFragment {

    public ProfileFavorites() {
        // Required empty public constructor
    }

    public static ProfileFavorites newInstance() {
        return new ProfileFavorites();
    }

    @Override
    protected void loadContent(View view) {
        // Get the content container
        FrameLayout contentContainer = view.findViewById(R.id.profileContentContainer);
        
        // Inflate the specific content for favorites
        View favoritesContent = LayoutInflater.from(requireContext())
                .inflate(R.layout.profile_content_favorites, contentContainer, false);
        
        // Add to container
        contentContainer.removeAllViews();
        contentContainer.addView(favoritesContent);
        
        // TODO: Setup favorites-specific views and data here
    }

    @Override
    protected void highlightCurrentTab() {
        setTabState(getBtnOverview(), false);
        setTabState(getBtnAnimeList(), false);
        setTabState(getBtnMangaList(), false);
        setTabState(getBtnFavorites(), true);
        setTabState(getBtnSocial(), false);
    }
}