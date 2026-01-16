package com.fh.anitrack.ui.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.recyclerview.widget.RecyclerView;

import com.fh.anitrack.R;
import com.fh.anitrack.api.AniListQueries;
import com.fh.anitrack.api.AniListService;
import com.fh.anitrack.api.GraphQLRequest;
import com.fh.anitrack.api.RequestWrapper;
import com.fh.anitrack.api.RetrofitClient;
import com.fh.anitrack.api.response.FavoritesResponse;
import com.fh.anitrack.api.response.UserStatsResponse;
import com.fh.anitrack.ui.profile.adapter.FavoriteAdapter;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;

public class ProfileFavorites extends BaseProfileFragment {
    private View content;
    private FavoriteAdapter adapterAnime, adapterManga, adapterStaff, adapterCharacters;
    private int currentId = -1;

    public static ProfileFavorites newInstance() {
        return new ProfileFavorites();
    }

    @Override
    protected void loadContent(View view) {
        FrameLayout container = view.findViewById(R.id.profileContentContainer);
        content = LayoutInflater.from(requireContext()).inflate(R.layout.profile_content_favorites, container, false);
        container.removeAllViews();
        container.addView(content);

        adapterAnime = new FavoriteAdapter();
        adapterManga = new FavoriteAdapter();
        adapterStaff = new FavoriteAdapter();
        adapterCharacters = new FavoriteAdapter();

        ((RecyclerView) content.findViewById(R.id.rvFavAnime)).setAdapter(adapterAnime);
        ((RecyclerView) content.findViewById(R.id.rvFavManga)).setAdapter(adapterManga);
        ((RecyclerView) content.findViewById(R.id.rvFavStaff)).setAdapter(adapterStaff);
        ((RecyclerView) content.findViewById(R.id.rvFavCharacters)).setAdapter(adapterCharacters);

        content.findViewById(R.id.btnLoadAnime).setOnClickListener(v -> fetchFavorites());
        content.findViewById(R.id.btnLoadManga).setOnClickListener(v -> fetchFavorites());
        content.findViewById(R.id.btnLoadStaff).setOnClickListener(v -> fetchFavorites());
        content.findViewById(R.id.btnLoadCharacters).setOnClickListener(v -> fetchFavorites());

        fetchInitialData();
    }

    private void fetchInitialData() {
        AniListService service = RetrofitClient.getInstance(requireContext()).create(AniListService.class);
        Call<UserStatsResponse> call = service.getUserStats(new GraphQLRequest(AniListQueries.GET_USER_STATS, null));
        RequestWrapper.sendRequest(call, response -> {
            if (response.isSuccessful() && response.body() != null) {
                currentId = response.body().data.viewer.id;
                fetchFavorites();
            }
        }, requireContext());
    }

    private void fetchFavorites() {
        Map<String, Object> vars = new HashMap<>();
        vars.put("userId", currentId);
        vars.put("page", 1);

        AniListService service = RetrofitClient.getInstance(requireContext()).create(AniListService.class);
        Call<FavoritesResponse> call = service.getFavorites(new GraphQLRequest(AniListQueries.GET_USER_FAVORITES, vars));

        RequestWrapper.sendRequest(call, response -> {
            stopRefreshing();
            if (response.isSuccessful() && response.body() != null) {
                FavoritesResponse.Favourites favs = response.body().data.user.favourites;
                handleCategory(favs.anime, adapterAnime, R.id.headerAnime, R.id.btnLoadAnime);
                handleCategory(favs.manga, adapterManga, R.id.headerManga, R.id.btnLoadManga);
                handleCategory(favs.staff, adapterStaff, R.id.headerStaff, R.id.btnLoadStaff);
                handleCategory(favs.characters, adapterCharacters, R.id.headerCharacters, R.id.btnLoadCharacters);
            }
        }, requireContext());
    }

    private void handleCategory(FavoritesResponse.Connection conn, FavoriteAdapter adapter, int headerId, int btnId) {
        if (conn != null && conn.nodes != null && !conn.nodes.isEmpty()) {
            content.findViewById(headerId).setVisibility(View.VISIBLE);
            adapter.addItems(conn.nodes);
            content.findViewById(btnId).setVisibility(conn.pageInfo.hasNextPage ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    protected void onRefreshTriggered() {
        adapterAnime.clear();
        adapterManga.clear();
        adapterStaff.clear();
        adapterCharacters.clear();
        fetchInitialData();
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