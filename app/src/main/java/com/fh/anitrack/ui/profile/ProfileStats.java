package com.fh.anitrack.ui.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.fh.anitrack.R;

/**
 * Profile Stats Fragment - displays user's statistics.
 */
public class ProfileStats extends BaseProfileFragment {

    public ProfileStats() {
        // Required empty public constructor
    }

    public static ProfileStats newInstance() {
        return new ProfileStats();
    }

    @Override
    protected void loadContent(View view) {
        // Get the content container
        FrameLayout contentContainer = view.findViewById(R.id.profileContentContainer);
        
        // Inflate the specific content for stats
        View statsContent = LayoutInflater.from(requireContext())
                .inflate(R.layout.profile_content_stats, contentContainer, false);
        
        // Add to container
        contentContainer.removeAllViews();
        contentContainer.addView(statsContent);
        
        // TODO: Setup stats-specific views and data here
    }

    @Override
    protected void highlightCurrentTab() {
        setTabState(getBtnOverview(), false);
        setTabState(getBtnAnimeList(), false);
        setTabState(getBtnMangaList(), false);
        setTabState(getBtnStats(), true);
        setTabState(getBtnFavorites(), false);
        setTabState(getBtnSocial(), false);
    }
}