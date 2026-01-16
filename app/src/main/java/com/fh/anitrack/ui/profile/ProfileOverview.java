package com.fh.anitrack.ui.profile;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.fh.anitrack.R;
import com.fh.anitrack.api.AniListQueries;
import com.fh.anitrack.api.AniListService;
import com.fh.anitrack.api.GraphQLRequest;
import com.fh.anitrack.api.RequestWrapper;
import com.fh.anitrack.api.RetrofitClient;
import com.fh.anitrack.api.response.ActivityResponse;
import com.fh.anitrack.api.response.UserStatsResponse;
import com.fh.anitrack.ui.home.ActivityAdapter;
import com.google.android.material.button.MaterialButton;

import java.util.HashMap;
import java.util.Map;

import io.noties.markwon.Markwon;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import retrofit2.Call;

public class ProfileOverview extends BaseProfileFragment {

    private View view;
    private ActivityAdapter activityAdapter;
    private Markwon markwon;
    private int activityPage = 1;
    private boolean hasNextPage = false;
    private int currentUserId = -1;

    public ProfileOverview() {
    }

    public static ProfileOverview newInstance() {
        return new ProfileOverview();
    }

    @Override
    protected void loadContent(View view) {
        this.view = view;
        FrameLayout contentContainer = view.findViewById(R.id.profileContentContainer);

        View overviewContent = LayoutInflater.from(requireContext())
                .inflate(R.layout.profile_content_overview, contentContainer, false);

        contentContainer.removeAllViews();
        contentContainer.addView(overviewContent);

        markwon = Markwon.builder(requireContext())
                .usePlugin(StrikethroughPlugin.create())
                .build();

        RecyclerView rv = overviewContent.findViewById(R.id.rvPersonalActivities);
        activityAdapter = new ActivityAdapter();
        activityAdapter.setMarkwon(markwon);
        activityAdapter.setPersonalProfile(true);
        rv.setAdapter(activityAdapter);
        rv.setNestedScrollingEnabled(false);

        MaterialButton btnLoadMore = overviewContent.findViewById(R.id.btnLoadMorePersonal);
        btnLoadMore.setOnClickListener(v -> fetchPersonalActivities());

        fetchStats();
    }

    @Override
    protected void onRefreshTriggered() {
        activityPage = 1;
        hasNextPage = false;

        if (activityAdapter != null) {
            activityAdapter.clearItems();
        }

        fetchStats();
    }

    private void fetchStats() {
        AniListService service = RetrofitClient.getInstance(requireContext()).create(AniListService.class);
        Call<UserStatsResponse> call = service.getUserStats(new GraphQLRequest(AniListQueries.GET_USER_STATS, null));

        RequestWrapper.sendRequest(call, response -> {
            stopRefreshing();

            if (response.isSuccessful() && response.body() != null) {
                if (response.body().data.viewer != null) {
                    UserStatsResponse.Viewer user = response.body().data.viewer;
                    this.currentUserId = user.id;

                    updateUI(user.statistics);
                    fetchPersonalActivities();
                }
            }
        }, requireContext());
    }

    private void fetchPersonalActivities() {
        if (currentUserId == -1) {
            stopRefreshing();
            return;
        }

        Map<String, Object> vars = new HashMap<>();
        vars.put("userId", currentUserId);
        vars.put("page", activityPage);
        vars.put("perPage", 10);

        AniListService service = RetrofitClient.getInstance(requireContext()).create(AniListService.class);
        Call<ActivityResponse> call = service.postQuery(new GraphQLRequest(AniListQueries.GET_USER_ACTIVITIES, vars));

        RequestWrapper.sendRequest(call, response -> {
            stopRefreshing();

            if (response.isSuccessful() && response.body() != null && response.body().data.Page != null) {
                ActivityResponse.Page pageData = response.body().data.Page;

                activityAdapter.addItems(pageData.activities);

                this.hasNextPage = pageData.pageInfo.hasNextPage;
                this.activityPage = pageData.pageInfo.currentPage + 1;

                View loadMoreBtn = view.findViewById(R.id.btnLoadMorePersonal);
                if (loadMoreBtn != null) {
                    loadMoreBtn.setVisibility(hasNextPage ? View.VISIBLE : View.GONE);
                }
            }
        }, requireContext());
    }

    @SuppressLint("DefaultLocale")
    private void updateUI(UserStatsResponse.Statistics stats) {
        if (stats == null || view == null) return;

        setupStatItem(view.findViewById(R.id.statTotalAnime), String.valueOf(stats.anime.count), getString(R.string.total_anime));
        double daysWatched = stats.anime.minutesWatched / 1440.0;
        setupStatItem(view.findViewById(R.id.statDaysWatched), String.format("%.1f", daysWatched), getString(R.string.days_watched));
        setupStatItem(view.findViewById(R.id.statAnimeMeanScore), String.valueOf(stats.anime.meanScore), getString(R.string.mean_score));
        setupProgressBar(view.findViewById(R.id.progressAnime), daysWatched, calculateDynamicMilestones(daysWatched));

        setupStatItem(view.findViewById(R.id.statTotalManga), String.valueOf(stats.manga.count), getString(R.string.total_manga));
        setupStatItem(view.findViewById(R.id.statChaptersRead), String.valueOf(stats.manga.chaptersRead), getString(R.string.chapters_read));
        setupStatItem(view.findViewById(R.id.statMangaMeanScore), String.valueOf(stats.manga.meanScore), getString(R.string.mean_score));
        setupProgressBar(view.findViewById(R.id.progressManga), stats.manga.chaptersRead, calculateDynamicMilestones(stats.manga.chaptersRead));
    }

    private void setupStatItem(View root, String value, String label) {
        if (root == null) return;
        ((TextView) root.findViewById(R.id.tvStatValue)).setText(value);
        ((TextView) root.findViewById(R.id.tvStatLabel)).setText(label);
    }

    private void setupProgressBar(View root, double currentVal, int[] milestones) {
        if (root == null) return;

        int m1 = milestones[0];
        int m2 = milestones[1];
        int m3 = milestones[2];

        ProgressBar pb = root.findViewById(R.id.progressBar);
        TextView tvM1 = root.findViewById(R.id.tvM1);
        TextView tvM2 = root.findViewById(R.id.tvM2);
        TextView tvM3 = root.findViewById(R.id.tvM3);

        tvM1.setText(String.valueOf(m1));
        tvM2.setText(String.valueOf(m2));
        tvM3.setText(String.valueOf(m3));

        int totalSpan = m3 - m1;
        int relativeProgress = (int) (currentVal - m1);

        pb.setMax(totalSpan);
        pb.setProgress(relativeProgress);
    }

    private int[] calculateDynamicMilestones(double value) {
        double step;

        if (value < 20) step = 10;
        else if (value < 100) step = 20;
        else if (value < 500) step = 50;
        else if (value < 2000) step = 500;
        else if (value < 5000) step = 1000;
        else step = 2500;

        int m2 = (int) (Math.ceil(value / step) * step);

        if (m2 - value < (step * 0.1)) {
            m2 += step;
        }

        int m1 = (int) (m2 - step);
        int m3 = (int) (m2 + step);

        return new int[]{m1, m2, m3};
    }

    @Override
    protected void highlightCurrentTab() {
        setTabState(getBtnOverview(), true);
        setTabState(getBtnAnimeList(), false);
        setTabState(getBtnMangaList(), false);
        setTabState(getBtnFavorites(), false);
        setTabState(getBtnSocial(), false);
    }
}