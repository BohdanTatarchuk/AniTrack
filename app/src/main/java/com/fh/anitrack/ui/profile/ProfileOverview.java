package com.fh.anitrack.ui.profile;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fh.anitrack.R;
import com.fh.anitrack.api.AniListQueries;
import com.fh.anitrack.api.AniListService;
import com.fh.anitrack.api.GraphQLRequest;
import com.fh.anitrack.api.RequestWrapper;
import com.fh.anitrack.api.RetrofitClient;
import com.fh.anitrack.api.response.UserStatsResponse;

import retrofit2.Call;

/**
 * Profile Overview Fragment - displays user's profile overview stats and activity heatmap.
 */
public class ProfileOverview extends BaseProfileFragment {

    private View view;

    public ProfileOverview() {}

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

        fetchStats();
    }

    private void fetchStats() {
        AniListService service = RetrofitClient.getInstance(requireContext()).create(AniListService.class);
        Call<UserStatsResponse> call = service.getUserStats(new GraphQLRequest(AniListQueries.GET_USER_STATS, null));

        RequestWrapper.sendRequest(call, response -> {
            if (response.isSuccessful() && response.body() != null) {
                if (response.body().data.viewer != null) {
                    UserStatsResponse.Statistics stats = response.body().data.viewer.statistics;
                    updateUI(stats);
                }
            }
        }, requireContext());
    }

    @SuppressLint("DefaultLocale")
    private void updateUI(UserStatsResponse.Statistics stats) {
        if (stats == null || view == null) return;

        // anime stats
        setupStatItem(view.findViewById(R.id.statTotalAnime),
                String.valueOf(stats.anime.count),
                getString(R.string.total_anime));

        // convert minutes watched to days
        double daysWatched = stats.anime.minutesWatched / 1440.0;
        setupStatItem(view.findViewById(R.id.statDaysWatched),
                String.format("%.1f", daysWatched),
                getString(R.string.days_watched));

        setupStatItem(view.findViewById(R.id.statAnimeMeanScore),
                String.valueOf(stats.anime.meanScore),
                getString(R.string.mean_score));

        // calc milestones relative to days watched
        int[] animeMilestones = calculateDynamicMilestones(daysWatched);
        setupProgressBar(view.findViewById(R.id.progressAnime), daysWatched, animeMilestones);


        //manga stats
        setupStatItem(view.findViewById(R.id.statTotalManga),
                String.valueOf(stats.manga.count),
                getString(R.string.total_manga));

        setupStatItem(view.findViewById(R.id.statChaptersRead),
                String.valueOf(stats.manga.chaptersRead),
                getString(R.string.chapters_read));

        setupStatItem(view.findViewById(R.id.statMangaMeanScore),
                String.valueOf(stats.manga.meanScore),
                getString(R.string.mean_score));

        // calc milestones relative to chapters read
        int[] mangaMilestones = calculateDynamicMilestones(stats.manga.chaptersRead);
        setupProgressBar(view.findViewById(R.id.progressManga), stats.manga.chaptersRead, mangaMilestones);
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
        setTabState(getBtnStats(), false);
        setTabState(getBtnFavorites(), false);
        setTabState(getBtnSocial(), false);
    }
}