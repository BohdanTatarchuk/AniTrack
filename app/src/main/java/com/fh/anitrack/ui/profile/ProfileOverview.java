package com.fh.anitrack.ui.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.fh.anitrack.R;

/**
 * Profile Overview Fragment - displays user's profile overview.
 */
public class ProfileOverview extends BaseProfileFragment {

    public ProfileOverview() {
        // Required empty public constructor
    }

    public static ProfileOverview newInstance() {
        return new ProfileOverview();
    }

    @Override
    protected void loadContent(View view) {
        // Get the content container
        FrameLayout contentContainer = view.findViewById(R.id.profileContentContainer);
        
        // Inflate the specific content for overview
        View overviewContent = LayoutInflater.from(requireContext())
                .inflate(R.layout.profile_content_overview, contentContainer, false);
        
        // Add to container
        contentContainer.removeAllViews();
        contentContainer.addView(overviewContent);
        
        // TODO: Setup overview-specific views and data here
    }

    @Override
    protected void highlightCurrentTab() {
        setTabState(getBtnOverview(), true);
        setTabState(getBtnAnimeList(), false);
        setTabState(getBtnMangaList(), false);
        setTabState(getBtnStats(), false);
        setTabState(getBtnFavorites(), false);
        setTabState(getBtnSocial(), false);
    }
}