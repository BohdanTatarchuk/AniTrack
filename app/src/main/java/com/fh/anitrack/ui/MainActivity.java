package com.fh.anitrack.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fh.anitrack.R;
import com.fh.anitrack.data.MediaDetailMockData;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.Calendar;

/**
 * Media Detail Activity - displays detailed information about an anime/manga.
 * 
 * By default, all data fields are empty. To populate with test data,
 * set USE_MOCK_DATA = true. For production, integrate with your backend service.
 */
public class MainActivity extends AppCompatActivity {

    // Set to true to load mock data for testing, false for empty/production state
    private static final boolean USE_MOCK_DATA = true;

    private RecyclerView relationsRecyclerView;
    private RecyclerView charactersRecyclerView;
    private RecyclerView recommendationsRecyclerView;

    // Data arrays - empty by default, populated from backend or mock data
    private String[] relationTypes = {};
    private String[] relationTitles = {};
    private String[][] characters = {};
    private String[] recommendationTitles = {};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_media_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.coordinatorLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupToolbar();
        setupViews();
        
        // Load mock data only if enabled
        if (USE_MOCK_DATA) {
            loadMockData();
        }
        
        // Setup adapters (will be empty if no mock data loaded)
        setupAdapters();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        toolbar.setNavigationOnClickListener(v -> finish());

        // Add to List button
        MaterialButton addToListButton = findViewById(R.id.addToListButton);
        addToListButton.setOnClickListener(v -> showAddToListDialog());

        // Favorite button
        ImageButton favoriteButton = findViewById(R.id.favoriteButton);
        favoriteButton.setOnClickListener(v -> {
            // Toggle favorite state
            v.setSelected(!v.isSelected());
        });
    }

    private void setupViews() {
        // Setup RecyclerViews with horizontal layout managers
        relationsRecyclerView = findViewById(R.id.relationsRecyclerView);
        relationsRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Characters is now a horizontal carousel of columns (each column has 2 cards)
        charactersRecyclerView = findViewById(R.id.charactersRecyclerView);
        charactersRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        recommendationsRecyclerView = findViewById(R.id.recommendationsRecyclerView);
        recommendationsRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Setup dynamic scrollbars for carousels
        setupDynamicScrollbar(relationsRecyclerView,
                findViewById(R.id.relationsProgressActive),
                findViewById(R.id.relationsProgressInactive));
        setupDynamicScrollbar(charactersRecyclerView,
                findViewById(R.id.charactersProgressActive),
                findViewById(R.id.charactersProgressInactive));
        setupDynamicScrollbar(recommendationsRecyclerView,
                findViewById(R.id.recommendationsProgressActive),
                findViewById(R.id.recommendationsProgressInactive));

        // Setup chip click listeners
        ChipGroup tabChipGroup = findViewById(R.id.tabChipGroup);
        tabChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            // Handle tab selection
            if (!checkedIds.isEmpty()) {
                int checkedId = checkedIds.get(0);
                // You could switch content here based on selected tab
            }
        });

        // Spoiler tags toggle
        ImageButton toggleSpoilerTags = findViewById(R.id.toggleSpoilerTags);
        toggleSpoilerTags.setOnClickListener(v -> {
            v.setSelected(!v.isSelected());
            // Toggle spoiler tag visibility
        });

        // Trailer click
        findViewById(R.id.trailerCard).setOnClickListener(v -> {
            // Open trailer (YouTube intent, etc.)
        });

        // Setup toolbar hide/show behavior after thumbnail collapses
        setupToolbarScrollBehavior();
    }

    private void setupToolbarScrollBehavior() {
        AppBarLayout appBarLayout = findViewById(R.id.appBarLayout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        NestedScrollView nestedScrollView = findViewById(R.id.nestedScrollView);

        // Track if thumbnail is fully collapsed
        final boolean[] isCollapsed = {false};
        final int[] lastScrollY = {0};
        final boolean[] toolbarHidden = {false};

        // Listen to AppBar offset to know when thumbnail is collapsed
        appBarLayout.addOnOffsetChangedListener((appBar, verticalOffset) -> {
            int totalScrollRange = appBar.getTotalScrollRange();
            // Thumbnail is fully collapsed when offset equals negative total range
            isCollapsed[0] = Math.abs(verticalOffset) >= totalScrollRange;
            
            // If thumbnail expanded again, restore toolbar
            if (!isCollapsed[0] && toolbarHidden[0]) {
                toolbar.animate()
                        .translationY(0)
                        .setDuration(200)
                        .start();
                nestedScrollView.animate()
                        .translationY(0)
                        .setDuration(200)
                        .start();
                toolbarHidden[0] = false;
            }
        });

        // Listen to scroll to hide/show toolbar after thumbnail is collapsed
        nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (!isCollapsed[0]) {
                lastScrollY[0] = scrollY;
                return;
            }

            // Thumbnail is collapsed, now handle toolbar visibility
            int deltaY = scrollY - lastScrollY[0];
            int toolbarHeight = toolbar.getHeight();
            
            if (deltaY > 5 && !toolbarHidden[0]) {
                // Scrolling down - hide toolbar and move content up
                toolbar.animate()
                        .translationY(-toolbarHeight)
                        .setDuration(200)
                        .start();
                nestedScrollView.animate()
                        .translationY(-toolbarHeight)
                        .setDuration(200)
                        .start();
                toolbarHidden[0] = true;
            } else if (deltaY < -5 && toolbarHidden[0]) {
                // Scrolling up - show toolbar and restore content
                toolbar.animate()
                        .translationY(0)
                        .setDuration(200)
                        .start();
                nestedScrollView.animate()
                        .translationY(0)
                        .setDuration(200)
                        .start();
                toolbarHidden[0] = false;
            }
            
            lastScrollY[0] = scrollY;
        });
    }

    /**
     * Loads mock data from MediaDetailMockData class.
     * This method should only be called for testing purposes.
     * In production, replace this with actual backend data loading.
     */
    private void loadMockData() {
        // Thumbnail image for testing
        ImageView mediaThumbnail = findViewById(R.id.mediaThumbnail);
        mediaThumbnail.setImageResource(R.drawable.test_thumbnail_jjk);

        // Title
        TextView mediaTitle = findViewById(R.id.mediaTitle);
        mediaTitle.setText(MediaDetailMockData.TITLE);

        // Badges
        TextView badgeHighestRated = findViewById(R.id.badgeHighestRated);
        badgeHighestRated.setText(MediaDetailMockData.BADGE_HIGHEST_RATED);
        badgeHighestRated.setVisibility(View.VISIBLE);

        TextView badgeMostPopular = findViewById(R.id.badgeMostPopular);
        badgeMostPopular.setText(MediaDetailMockData.BADGE_MOST_POPULAR);
        badgeMostPopular.setVisibility(View.VISIBLE);

        // Favorite count
        TextView favoriteCount = findViewById(R.id.favoriteCount);
        favoriteCount.setText(MediaDetailMockData.FAVORITE_COUNT);

        // Stats
        setupStatColumn(R.id.statAverageScore, "Average Score", MediaDetailMockData.STAT_AVERAGE_SCORE);
        setupStatColumn(R.id.statMeanScore, "Mean Score", MediaDetailMockData.STAT_MEAN_SCORE);
        setupStatColumn(R.id.statPopularity, "Popularity", MediaDetailMockData.STAT_POPULARITY);
        setupStatColumn(R.id.statFavorites, "Favorites", MediaDetailMockData.STAT_FAVORITES);
        setupStatColumn(R.id.statStudios, "Studios", MediaDetailMockData.STAT_STUDIOS);

        // Format row
        LinearLayout formatContainer = findViewById(R.id.formatContainer);
        for (String format : MediaDetailMockData.FORMAT_ITEMS) {
            addFormatItem(formatContainer, format);
        }

        // Tags
        ChipGroup tagsChipGroup = findViewById(R.id.tagsChipGroup);
        for (MediaDetailMockData.TagData tag : MediaDetailMockData.TAGS) {
            addTagChip(tagsChipGroup, tag.name, tag.percentage);
        }

        // Description
        TextView descriptionText = findViewById(R.id.descriptionText);
        descriptionText.setText(MediaDetailMockData.DESCRIPTION);

        TextView descriptionSource = findViewById(R.id.descriptionSource);
        descriptionSource.setText(MediaDetailMockData.DESCRIPTION_SOURCE);

        // Prepare data arrays for adapters
        relationTypes = new String[MediaDetailMockData.RELATIONS.length];
        relationTitles = new String[MediaDetailMockData.RELATIONS.length];
        for (int i = 0; i < MediaDetailMockData.RELATIONS.length; i++) {
            relationTypes[i] = MediaDetailMockData.RELATIONS[i].type;
            relationTitles[i] = MediaDetailMockData.RELATIONS[i].title;
        }

        characters = new String[MediaDetailMockData.CHARACTERS.length][4];
        for (int i = 0; i < MediaDetailMockData.CHARACTERS.length; i++) {
            MediaDetailMockData.CharacterData c = MediaDetailMockData.CHARACTERS[i];
            characters[i] = new String[]{c.characterName, c.role, c.voiceActorName, c.language};
        }

        recommendationTitles = new String[MediaDetailMockData.RECOMMENDATIONS.length];
        for (int i = 0; i < MediaDetailMockData.RECOMMENDATIONS.length; i++) {
            recommendationTitles[i] = MediaDetailMockData.RECOMMENDATIONS[i].title;
        }
    }

    private void setupAdapters() {
        relationsRecyclerView.setAdapter(new RelationsAdapter());
        charactersRecyclerView.setAdapter(new CharactersAdapter());
        recommendationsRecyclerView.setAdapter(new RecommendationsAdapter());
    }

    private void setupStatColumn(int viewId, String label, String value) {
        View statView = findViewById(viewId);
        TextView statLabel = statView.findViewById(R.id.statLabel);
        TextView statValue = statView.findViewById(R.id.statValue);
        statLabel.setText(label);
        statValue.setText(value);
    }

    private void addFormatItem(LinearLayout container, String format) {
        TextView formatText = new TextView(this);
        formatText.setText(format);
        formatText.setTextColor(getColor(R.color.darkGrey));
        formatText.setTextSize(12);
        formatText.setPadding(0, 0, 32, 0);
        container.addView(formatText);
    }

    private void addTagChip(ChipGroup chipGroup, String tagName, int percentage) {
        Chip chip = (Chip) LayoutInflater.from(this)
                .inflate(R.layout.media_page_item_tag, chipGroup, false);
        chip.setText(tagName + " (" + percentage + "%)");
        chipGroup.addView(chip);
    }

    private void showAddToListDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.media_page_dialog_add_to_list, null);
        dialog.setContentView(dialogView);

        // Setup status spinner
        Spinner statusSpinner = dialogView.findViewById(R.id.statusSpinner);
        String[] statusOptions = getResources().getStringArray(R.array.anime_status);
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, statusOptions);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(statusAdapter);

        // Setup date pickers
        EditText startDateValue = dialogView.findViewById(R.id.startDateValue);
        EditText finishDateValue = dialogView.findViewById(R.id.finishDateValue);

        startDateValue.setFocusable(false);
        startDateValue.setClickable(true);
        startDateValue.setOnClickListener(v -> showDatePicker(startDateValue));

        finishDateValue.setFocusable(false);
        finishDateValue.setClickable(true);
        finishDateValue.setOnClickListener(v -> showDatePicker(finishDateValue));

        // Setup increment/decrement buttons
        EditText scoreValue = dialogView.findViewById(R.id.scoreValue);
        EditText episodeProgressValue = dialogView.findViewById(R.id.episodeProgressValue);
        EditText rewatchesValue = dialogView.findViewById(R.id.rewatchesValue);

        ImageView scoreUpDown = dialogView.findViewById(R.id.scoreUpDown);
        ImageView episodeUpDown = dialogView.findViewById(R.id.episodeUpDown);
        ImageView rewatchesUpDown = dialogView.findViewById(R.id.rewatchesUpDown);

        // Score increment (on click cycles: increment, or you could add long press for decrement)
        scoreUpDown.setOnClickListener(v -> incrementValue(scoreValue, 0, 10, 0.5));

        // Episode progress increment
        episodeUpDown.setOnClickListener(v -> incrementValue(episodeProgressValue, 0, Integer.MAX_VALUE, 1));

        // Rewatches increment
        rewatchesUpDown.setOnClickListener(v -> incrementValue(rewatchesValue, 0, Integer.MAX_VALUE, 1));

        // Setup dialog views
        MaterialButton saveButton = dialogView.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v -> {
            // Save list entry
            dialog.dismiss();
        });

        dialog.show();
    }

    private void incrementValue(EditText field, double min, double max, double step) {
        String currentText = field.getText().toString().trim();
        double currentValue = 0;
        
        if (!currentText.isEmpty()) {
            try {
                currentValue = Double.parseDouble(currentText);
            } catch (NumberFormatException e) {
                currentValue = min;
            }
        }

        currentValue += step;
        if (currentValue > max) {
            currentValue = min; // Wrap around to minimum
        }

        // Format the value (show decimal only if step has decimal)
        if (step % 1 == 0) {
            field.setText(String.valueOf((int) currentValue));
        } else {
            field.setText(String.format("%.1f", currentValue));
        }
    }

    private void showDatePicker(EditText dateField) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Format the date as YYYY-MM-DD
                    String formattedDate = String.format("%04d-%02d-%02d",
                            selectedYear, selectedMonth + 1, selectedDay);
                    dateField.setText(formattedDate);
                }, year, month, day);

        datePickerDialog.show();
    }

    private void setupDynamicScrollbar(RecyclerView recyclerView, View activeIndicator, View inactiveIndicator) {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                super.onScrolled(rv, dx, dy);
                updateScrollIndicator(rv, activeIndicator, inactiveIndicator);
            }
        });

        // Initial update after layout
        recyclerView.post(() -> updateScrollIndicator(recyclerView, activeIndicator, inactiveIndicator));
    }

    private void updateScrollIndicator(RecyclerView recyclerView, View activeIndicator, View inactiveIndicator) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        if (layoutManager == null || activeIndicator == null || inactiveIndicator == null) return;

        int totalItemCount = layoutManager.getItemCount();
        if (totalItemCount == 0) return;

        // Calculate the progress ratio based on scroll position
        int scrollRange = recyclerView.computeHorizontalScrollRange();
        int scrollOffset = recyclerView.computeHorizontalScrollOffset();
        int scrollExtent = recyclerView.computeHorizontalScrollExtent();

        if (scrollRange <= scrollExtent) {
            // All items visible, hide inactive indicator
            activeIndicator.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    getResources().getDimensionPixelSize(R.dimen.progressBarHeight)));
            inactiveIndicator.setVisibility(View.GONE);
        } else {
            inactiveIndicator.setVisibility(View.VISIBLE);

            // Calculate the width of active indicator based on scroll position
            float scrollProgress = (float) scrollOffset / (scrollRange - scrollExtent);
            int totalWidth = activeIndicator.getWidth() + inactiveIndicator.getWidth();
            if (totalWidth == 0) {
                totalWidth = ((View) activeIndicator.getParent()).getWidth();
            }

            // Calculate width based on visible portion and scroll position
            float visibleRatio = (float) scrollExtent / scrollRange;
            int indicatorWidth = (int) (totalWidth * visibleRatio);
            int maxOffset = totalWidth - indicatorWidth;
            int indicatorOffset = (int) (maxOffset * scrollProgress);

            // Update the active indicator width using weight
            LinearLayout.LayoutParams activeParams = new LinearLayout.LayoutParams(
                    indicatorWidth + indicatorOffset,
                    getResources().getDimensionPixelSize(R.dimen.progressBarHeight));
            activeIndicator.setLayoutParams(activeParams);

            LinearLayout.LayoutParams inactiveParams = new LinearLayout.LayoutParams(
                    0, getResources().getDimensionPixelSize(R.dimen.progressBarHeight), 1f);
            inactiveIndicator.setLayoutParams(inactiveParams);
        }
    }

    // ==================== Adapters ====================

    private class RelationsAdapter extends RecyclerView.Adapter<RelationsAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.media_page_item_relation, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if (position < relationTypes.length) {
                holder.relationType.setText(relationTypes[position]);
                holder.relationTitle.setText(relationTitles[position]);
            }
            // Set a placeholder color for the image
            holder.relationImage.setBackgroundColor(getColor(R.color.darkBlue35op));

            // Set click listener for the entire card
            holder.itemView.setOnClickListener(v -> {
                if (position < relationTitles.length) {
                    // TODO: Open related media detail page
                    // For now, show a toast or log
                }
            });
        }

        @Override
        public int getItemCount() {
            return relationTypes.length;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView relationType;
            TextView relationTitle;
            ImageView relationImage;

            ViewHolder(View itemView) {
                super(itemView);
                relationType = itemView.findViewById(R.id.relationType);
                relationTitle = itemView.findViewById(R.id.relationTitle);
                relationImage = itemView.findViewById(R.id.relationImage);
            }
        }
    }

    // Character column adapter - each item shows 2 character cards stacked vertically
    private class CharactersAdapter extends RecyclerView.Adapter<CharactersAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.media_page_item_character_column, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            int index1 = position * 2;
            int index2 = position * 2 + 1;

            // First card in column
            if (index1 < characters.length) {
                String[] character1 = characters[index1];
                holder.characterName1.setText(character1[0]);
                holder.characterRole1.setText(character1[1]);
                holder.voiceActorName1.setText(character1[2]);
                holder.voiceActorLanguage1.setText(character1[3]);
                holder.characterImage1.setBackgroundColor(getColor(R.color.darkBlue));
                holder.voiceActorImage1.setBackgroundColor(getColor(R.color.darkBlue35op));
                holder.card1.setVisibility(View.VISIBLE);

                // Click listener for character avatar
                holder.characterImage1.setOnClickListener(v -> {
                    // TODO: Open character detail page
                });

                // Click listener for voice actor avatar
                holder.voiceActorImage1.setOnClickListener(v -> {
                    // TODO: Open voice actor/staff detail page
                });
            } else {
                holder.card1.setVisibility(View.INVISIBLE);
            }

            // Second card in column
            if (index2 < characters.length) {
                String[] character2 = characters[index2];
                holder.characterName2.setText(character2[0]);
                holder.characterRole2.setText(character2[1]);
                holder.voiceActorName2.setText(character2[2]);
                holder.voiceActorLanguage2.setText(character2[3]);
                holder.characterImage2.setBackgroundColor(getColor(R.color.darkBlue));
                holder.voiceActorImage2.setBackgroundColor(getColor(R.color.darkBlue35op));
                holder.card2.setVisibility(View.VISIBLE);

                // Click listener for character avatar
                holder.characterImage2.setOnClickListener(v -> {
                    // TODO: Open character detail page
                });

                // Click listener for voice actor avatar
                holder.voiceActorImage2.setOnClickListener(v -> {
                    // TODO: Open voice actor/staff detail page
                });
            } else {
                holder.card2.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public int getItemCount() {
            // Return number of columns (pairs)
            return (characters.length + 1) / 2;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            View card1, card2;
            TextView characterName1, characterRole1, voiceActorName1, voiceActorLanguage1;
            TextView characterName2, characterRole2, voiceActorName2, voiceActorLanguage2;
            ImageView characterImage1, voiceActorImage1, characterImage2, voiceActorImage2;

            ViewHolder(View itemView) {
                super(itemView);
                // First card
                card1 = itemView.findViewById(R.id.characterCard1);
                characterName1 = card1.findViewById(R.id.characterName);
                characterRole1 = card1.findViewById(R.id.characterRole);
                voiceActorName1 = card1.findViewById(R.id.voiceActorName);
                voiceActorLanguage1 = card1.findViewById(R.id.voiceActorLanguage);
                characterImage1 = card1.findViewById(R.id.characterImage);
                voiceActorImage1 = card1.findViewById(R.id.voiceActorImage);

                // Second card
                card2 = itemView.findViewById(R.id.characterCard2);
                characterName2 = card2.findViewById(R.id.characterName);
                characterRole2 = card2.findViewById(R.id.characterRole);
                voiceActorName2 = card2.findViewById(R.id.voiceActorName);
                voiceActorLanguage2 = card2.findViewById(R.id.voiceActorLanguage);
                characterImage2 = card2.findViewById(R.id.characterImage);
                voiceActorImage2 = card2.findViewById(R.id.voiceActorImage);
            }
        }
    }

    private class RecommendationsAdapter extends RecyclerView.Adapter<RecommendationsAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.media_page_item_recommendation, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if (position < recommendationTitles.length) {
                holder.recommendationTitle.setText(recommendationTitles[position]);
            }
            // Set a placeholder color for the image
            holder.recommendationImage.setBackgroundColor(getColor(R.color.darkBlue));

            // Set click listener for the entire card
            holder.itemView.setOnClickListener(v -> {
                if (position < recommendationTitles.length) {
                    // TODO: Open recommended media detail page
                }
            });
        }

        @Override
        public int getItemCount() {
            return recommendationTitles.length;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView recommendationTitle;
            ImageView recommendationImage;

            ViewHolder(View itemView) {
                super(itemView);
                recommendationTitle = itemView.findViewById(R.id.recommendationTitle);
                recommendationImage = itemView.findViewById(R.id.recommendationImage);
            }
        }
    }
}
