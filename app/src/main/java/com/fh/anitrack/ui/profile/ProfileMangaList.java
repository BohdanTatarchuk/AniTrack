package com.fh.anitrack.ui.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.fh.anitrack.R;

/**
 * Profile Manga List Fragment - displays user's manga list.
 */
public class ProfileMangaList extends BaseProfileFragment {

    public ProfileMangaList() {
        // Required empty public constructor
    }

    public static ProfileMangaList newInstance() {
        return new ProfileMangaList();
    }

    @Override
    protected void loadContent(View view) {
        // Get the content container
        FrameLayout contentContainer = view.findViewById(R.id.profileContentContainer);
        
        // Inflate the specific content for manga list
        View mangaListContent = LayoutInflater.from(requireContext())
                .inflate(R.layout.profile_content_manga_list, contentContainer, false);
        
        // Add to container
        contentContainer.removeAllViews();
        contentContainer.addView(mangaListContent);
        
        // TODO: Setup manga list-specific views and data here
        // TODO: Setup RecyclerView, filters, etc.
    }

    @Override
    protected void highlightCurrentTab() {
        setTabState(getBtnOverview(), false);
        setTabState(getBtnAnimeList(), false);
        setTabState(getBtnMangaList(), true);
        setTabState(getBtnStats(), false);
        setTabState(getBtnFavorites(), false);
        setTabState(getBtnSocial(), false);
    }
}