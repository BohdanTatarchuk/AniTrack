package com.fh.anitrack.ui.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.fh.anitrack.R;

/**
 * Profile Anime List Fragment - displays user's anime list.
 */
public class ProfileAnimeList extends BaseProfileFragment {

    public ProfileAnimeList() {
        // Required empty public constructor
    }

    public static ProfileAnimeList newInstance() {
        return new ProfileAnimeList();
    }

    @Override
    protected void loadContent(View view) {
        // Get the content container
        FrameLayout contentContainer = view.findViewById(R.id.profileContentContainer);
        
        // Inflate the specific content for anime list
        View animeListContent = LayoutInflater.from(requireContext())
                .inflate(R.layout.profile_content_anime_list, contentContainer, false);
        
        // Add to container
        contentContainer.removeAllViews();
        contentContainer.addView(animeListContent);
        
        // TODO: Setup anime list-specific views and data here
        // TODO: Setup RecyclerView, filters, etc.
    }

    @Override
    protected void highlightCurrentTab() {
        setTabState(getBtnOverview(), false);
        setTabState(getBtnAnimeList(), true);
        setTabState(getBtnMangaList(), false);
        setTabState(getBtnStats(), false);
        setTabState(getBtnFavorites(), false);
        setTabState(getBtnSocial(), false);
    }
}