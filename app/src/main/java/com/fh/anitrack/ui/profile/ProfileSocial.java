package com.fh.anitrack.ui.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.fh.anitrack.R;

/**
 * Profile Social Fragment - displays user's social connections and activity.
 */
public class ProfileSocial extends BaseProfileFragment {

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
        
        // TODO: Setup social-specific views and data here
    }

    @Override
    protected void highlightCurrentTab() {
        setTabState(getBtnOverview(), false);
        setTabState(getBtnAnimeList(), false);
        setTabState(getBtnMangaList(), false);
        setTabState(getBtnFavorites(), false);
        setTabState(getBtnSocial(), true);
    }
}