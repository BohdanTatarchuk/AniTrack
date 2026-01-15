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
import com.fh.anitrack.api.response.MediaListResponse;
import com.fh.anitrack.api.response.UserStatsResponse;
import com.fh.anitrack.ui.profile.adapter.MediaListAdapter;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;

/**
 * Profile Manga List Fragment - displays user's manga list.
 */
public class ProfileMangaList extends BaseProfileFragment {

    private View contentView;
    private int currentUserId = -1;

    private int pagePlanning = 1, pageReading = 1, pageCompleted = 1, pagePaused = 1;
    private MediaListAdapter adapterPlanning, adapterReading, adapterCompleted, adapterPaused;

    @Override
    protected void loadContent(View view) {
        this.contentView = view;
        FrameLayout contentContainer = view.findViewById(R.id.profileContentContainer);
        View content = LayoutInflater.from(requireContext()).inflate(R.layout.profile_content_manga_list, contentContainer, false);
        contentContainer.removeAllViews();
        contentContainer.addView(content);

        adapterPlanning = new MediaListAdapter();
        adapterReading = new MediaListAdapter();
        adapterCompleted = new MediaListAdapter();
        adapterPaused = new MediaListAdapter();

        ((RecyclerView) content.findViewById(R.id.rvMangaPlanning)).setAdapter(adapterPlanning);
        ((RecyclerView) content.findViewById(R.id.rvMangaReading)).setAdapter(adapterReading);
        ((RecyclerView) content.findViewById(R.id.rvMangaCompleted)).setAdapter(adapterCompleted);
        ((RecyclerView) content.findViewById(R.id.rvMangaPaused)).setAdapter(adapterPaused);

        content.findViewById(R.id.btnLoadMoreMangaPlanning).setOnClickListener(v -> fetchList("PLANNING"));
        content.findViewById(R.id.btnLoadMoreMangaReading).setOnClickListener(v -> fetchList("CURRENT"));
        content.findViewById(R.id.btnLoadMoreMangaCompleted).setOnClickListener(v -> fetchList("COMPLETED"));
        content.findViewById(R.id.btnLoadMoreMangaPaused).setOnClickListener(v -> fetchList("PAUSED"));

        fetchStats();
    }

    private void fetchStats() {
        AniListService service = RetrofitClient.getInstance(requireContext()).create(AniListService.class);
        Call<UserStatsResponse> call = service.getUserStats(new GraphQLRequest(AniListQueries.GET_USER_STATS, null));

        RequestWrapper.sendRequest(call, response -> {
            if (response.isSuccessful() && response.body() != null && response.body().data.viewer != null) {
                this.currentUserId = response.body().data.viewer.id;

                fetchList("PLANNING");
                fetchList("CURRENT");
                fetchList("COMPLETED");
                fetchList("PAUSED");
            }
        }, requireContext());
    }

    @Override
    protected void onRefreshTriggered() {
        pagePlanning = 1; pageReading = 1; pageCompleted = 1; pagePaused = 1;
        adapterPlanning.clear();
        adapterReading.clear();
        adapterCompleted.clear();
        adapterPaused.clear();

        contentView.findViewById(R.id.tvHeaderMangaPlanning).setVisibility(View.GONE);
        contentView.findViewById(R.id.tvHeaderMangaReading).setVisibility(View.GONE);
        contentView.findViewById(R.id.tvHeaderMangaCompleted).setVisibility(View.GONE);
        contentView.findViewById(R.id.tvHeaderMangaPaused).setVisibility(View.GONE);

        fetchStats();
    }

    private void fetchList(String status) {
        if (currentUserId == -1) return;

        int page;
        MediaListAdapter adapter;
        View btn;
        View header;

        switch (status) {
            case "PLANNING":
                page = pagePlanning;
                adapter = adapterPlanning;
                btn = contentView.findViewById(R.id.btnLoadMoreMangaPlanning);
                header = contentView.findViewById(R.id.tvHeaderMangaPlanning);
                break;
            case "CURRENT":
                page = pageReading;
                adapter = adapterReading;
                btn = contentView.findViewById(R.id.btnLoadMoreMangaReading);
                header = contentView.findViewById(R.id.tvHeaderMangaReading);
                break;
            case "COMPLETED":
                page = pageCompleted;
                adapter = adapterCompleted;
                btn = contentView.findViewById(R.id.btnLoadMoreMangaCompleted);
                header = contentView.findViewById(R.id.tvHeaderMangaCompleted);
                break;
            case "PAUSED":
                page = pagePaused;
                adapter = adapterPaused;
                btn = contentView.findViewById(R.id.btnLoadMoreMangaPaused);
                header = contentView.findViewById(R.id.tvHeaderMangaPaused);
                break;
            default: return;
        }

        final int requestedPage = page;
        final MediaListAdapter finalAdapter = adapter;
        final View finalBtn = btn;
        final View finalHeader = header;
        final String finalStatus = status;

        Map<String, Object> vars = new HashMap<>();
        vars.put("userId", currentUserId);
        vars.put("type", "MANGA");
        vars.put("status", status);
        vars.put("page", page);

        AniListService service = RetrofitClient.getInstance(requireContext()).create(AniListService.class);
        Call<MediaListResponse> call = service.getUserMediaList(new GraphQLRequest(AniListQueries.GET_USER_MEDIA_LIST, vars));

        RequestWrapper.sendRequest(call, response -> {
            stopRefreshing();
            if (response.isSuccessful() && response.body() != null) {
                MediaListResponse.Page pageData = response.body().data.Page;

                if (requestedPage == 1) {
                    if (pageData.mediaList == null || pageData.mediaList.isEmpty()) {
                        finalHeader.setVisibility(View.GONE);
                    } else {
                        finalHeader.setVisibility(View.VISIBLE);
                    }
                }

                finalAdapter.addItems(pageData.mediaList);

                boolean hasNext = pageData.pageInfo.hasNextPage;
                if (finalBtn != null) finalBtn.setVisibility(hasNext ? View.VISIBLE : View.GONE);

                switch (finalStatus) {
                    case "PLANNING":
                        pagePlanning++;
                        break;
                    case "CURRENT":
                        pageReading++;
                        break;
                    case "COMPLETED":
                        pageCompleted++;
                        break;
                    default:
                        pagePaused++;
                        break;
                }
            }
        }, requireContext());
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