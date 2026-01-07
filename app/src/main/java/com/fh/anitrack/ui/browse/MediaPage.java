package com.fh.anitrack.ui.browse;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fh.anitrack.R;
import com.fh.anitrack.data.MediaDetailMockData;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

/**
 * Media Detail Fragment - displays detailed information about an anime/manga.
 * 
 * By default, all data fields are empty. To populate with test data,
 * set USE_MOCK_DATA = true. For production, integrate with your backend service.
 */
public class MediaPage extends Fragment {

    // Set to true to load mock data for testing, false for empty/production state
    private static final boolean USE_MOCK_DATA = true;

    // Argument keys
    private static final String ARG_MEDIA_ID = "media_id";
    private static final String ARG_MEDIA_TYPE = "media_type";

    private RecyclerView relationsRecyclerView;
    private RecyclerView charactersRecyclerView;
    private RecyclerView recommendationsRecyclerView;

    // Data arrays - empty by default, populated from backend or mock data
    private String[] relationTypes = {};
    private String[] relationTitles = {};
    private String[][] characters = {};
    private String[] recommendationTitles = {};

    private String mediaId;
    private String mediaType;

    public MediaPage() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param mediaId The ID of the media to display.
     * @param mediaType The type of media (ANIME/MANGA).
     * @return A new instance of fragment MediaPage.
     */
    public static MediaPage newInstance(String mediaId, String mediaType) {
        MediaPage fragment = new MediaPage();
        Bundle args = new Bundle();
        args.putString(ARG_MEDIA_ID, mediaId);
        args.putString(ARG_MEDIA_TYPE, mediaType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mediaId = getArguments().getString(ARG_MEDIA_ID);
            mediaType = getArguments().getString(ARG_MEDIA_TYPE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_media_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupToolbar(view);
        setupViews(view);
        
        // Load mock data only if enabled
        if (USE_MOCK_DATA) {
            loadMockData(view);
        }
        
        // Setup adapters (will be empty if no mock data loaded)
        setupAdapters();
    }

    private void setupToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        
        // For fragments, we need to handle back navigation differently
        toolbar.setNavigationOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        // Add to List button
        MaterialButton addToListButton = view.findViewById(R.id.addToListButton);
        if (addToListButton != null) {
            addToListButton.setOnClickListener(v -> showAddToListDialog());
        }

        // Favorite button
        ImageButton favoriteButton = view.findViewById(R.id.favoriteButton);
        if (favoriteButton != null) {
            favoriteButton.setOnClickListener(v -> {
                // Toggle favorite state - the selector drawable will handle icon switching
                v.setSelected(!v.isSelected());
                // Optionally add haptic feedback
                v.performHapticFeedback(android.view.HapticFeedbackConstants.CONTEXT_CLICK);
            });
        }
    }

    private void setupViews(View view) {
        // Setup RecyclerViews with horizontal layout managers
        relationsRecyclerView = view.findViewById(R.id.relationsRecyclerView);
        relationsRecyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

        // Characters is now a horizontal carousel of columns (each column has 2 cards)
        charactersRecyclerView = view.findViewById(R.id.charactersRecyclerView);
        charactersRecyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

        recommendationsRecyclerView = view.findViewById(R.id.recommendationsRecyclerView);
        recommendationsRecyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

        // Setup dynamic scrollbars for carousels
        setupDynamicScrollbar(relationsRecyclerView,
                view.findViewById(R.id.relationsProgressActive),
                view.findViewById(R.id.relationsProgressInactive));
        setupDynamicScrollbar(charactersRecyclerView,
                view.findViewById(R.id.charactersProgressActive),
                view.findViewById(R.id.charactersProgressInactive));
        setupDynamicScrollbar(recommendationsRecyclerView,
                view.findViewById(R.id.recommendationsProgressActive),
                view.findViewById(R.id.recommendationsProgressInactive));

        // Setup chip click listeners
        ChipGroup tabChipGroup = view.findViewById(R.id.tabChipGroup);
        tabChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            // Handle tab selection
            if (!checkedIds.isEmpty()) {
                int checkedId = checkedIds.get(0);
                // You could switch content here based on selected tab
            }
        });

        // Spoiler tags toggle
        ImageButton toggleSpoilerTags = view.findViewById(R.id.toggleSpoilerTags);
        toggleSpoilerTags.setOnClickListener(v -> {
            v.setSelected(!v.isSelected());
            // Toggle spoiler tag visibility
        });

        // Trailer click
        view.findViewById(R.id.trailerCard).setOnClickListener(v -> {
            // Open trailer (YouTube intent, etc.)
        });
    }

    /**
     * Loads mock data from MediaDetailMockData class.
     * This method should only be called for testing purposes.
     * In production, replace this with actual backend data loading.
     */
    private void loadMockData(View view) {
        // Thumbnail image for testing
        ImageView mediaThumbnail = view.findViewById(R.id.mediaThumbnail);
        mediaThumbnail.setImageResource(R.drawable.test_thumbnail_jjk);

        // Title
        TextView mediaTitle = view.findViewById(R.id.mediaTitle);
        mediaTitle.setText(MediaDetailMockData.TITLE);

        // Badges
        TextView badgeHighestRated = view.findViewById(R.id.badgeHighestRated);
        badgeHighestRated.setText(MediaDetailMockData.BADGE_HIGHEST_RATED);
        badgeHighestRated.setVisibility(View.VISIBLE);

        TextView badgeMostPopular = view.findViewById(R.id.badgeMostPopular);
        badgeMostPopular.setText(MediaDetailMockData.BADGE_MOST_POPULAR);
        badgeMostPopular.setVisibility(View.VISIBLE);

        // Favorite count
        TextView favoriteCount = view.findViewById(R.id.favoriteCount);
        favoriteCount.setText(MediaDetailMockData.FAVORITE_COUNT);

        // Stats
        setupStatColumn(view, R.id.statAverageScore, "Average Score", "85%");
        setupStatColumn(view, R.id.statMeanScore, "Mean Score", "84%");
        setupStatColumn(view, R.id.statPopularity, "Popularity", "#25");
        setupStatColumn(view, R.id.statFavorites, "Favorites", "18.1k");
        setupStatColumn(view, R.id.statStudios, "Studios", "MAPPA");

        // Format row
        LinearLayout formatContainer = view.findViewById(R.id.formatContainer);
        for (String format : MediaDetailMockData.FORMAT_ITEMS) {
            addFormatItem(formatContainer, format);
        }

        // Tags
        ChipGroup tagsChipGroup = view.findViewById(R.id.tagsChipGroup);
        for (MediaDetailMockData.TagData tag : MediaDetailMockData.TAGS) {
            addTagChip(tagsChipGroup, tag.name, tag.percentage);
        }

        // Description
        TextView descriptionText = view.findViewById(R.id.descriptionText);
        descriptionText.setText(MediaDetailMockData.DESCRIPTION);

        TextView descriptionSource = view.findViewById(R.id.descriptionSource);
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

    private void setupStatColumn(View view, int viewId, String label, String value) {
        View statView = view.findViewById(viewId);
        TextView statLabel = statView.findViewById(R.id.statLabel);
        TextView statValue = statView.findViewById(R.id.statValue);
        statLabel.setText(label);
        statValue.setText(value);
    }

    private void addFormatItem(LinearLayout container, String format) {
        TextView formatText = new TextView(requireContext());
        formatText.setText(format);
        formatText.setTextColor(requireContext().getColor(R.color.darkGrey));
        formatText.setTextSize(12);
        formatText.setPadding(0, 0, 32, 0);
        container.addView(formatText);
    }

    private void addTagChip(ChipGroup chipGroup, String tagName, int percentage) {
        Chip chip = (Chip) LayoutInflater.from(requireContext())
                .inflate(R.layout.media_page_item_tag, chipGroup, false);
        chip.setText(tagName + " (" + percentage + "%)");
        chipGroup.addView(chip);
    }

    private void showAddToListDialog() {
        if (getContext() == null) {
            return;
        }
        
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.media_page_dialog_add_to_list, null);
        dialog.setContentView(dialogView);

        // Setup Spinner
        android.widget.Spinner statusSpinner = dialogView.findViewById(R.id.statusSpinner);
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"Planning", "Watching", "Completed", "Paused", "Dropped"}
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(adapter);

        // Setup Score controls
        TextView scoreValue = dialogView.findViewById(R.id.scoreValue);
        ImageView scoreUpDown = dialogView.findViewById(R.id.scoreUpDown);
        scoreValue.setText("0");
        scoreValue.setOnClickListener(v -> showScorePicker(scoreValue));
        scoreUpDown.setOnClickListener(v -> showScorePicker(scoreValue));

        // Setup Episode Progress controls
        TextView episodeValue = dialogView.findViewById(R.id.episodeProgressValue);
        ImageView episodeUpDown = dialogView.findViewById(R.id.episodeUpDown);
        episodeValue.setText("0");
        episodeValue.setOnClickListener(v -> showNumberPicker(episodeValue, "Episode Progress", 0, 1000));
        episodeUpDown.setOnClickListener(v -> showNumberPicker(episodeValue, "Episode Progress", 0, 1000));

        // Setup Date controls
        TextView startDateValue = dialogView.findViewById(R.id.startDateValue);
        TextView finishDateValue = dialogView.findViewById(R.id.finishDateValue);
        startDateValue.setOnClickListener(v -> showDatePicker(startDateValue));
        finishDateValue.setOnClickListener(v -> showDatePicker(finishDateValue));

        // Setup Rewatches controls
        TextView rewatchesValue = dialogView.findViewById(R.id.rewatchesValue);
        ImageView rewatchesUpDown = dialogView.findViewById(R.id.rewatchesUpDown);
        rewatchesValue.setText("0");
        rewatchesValue.setOnClickListener(v -> showNumberPicker(rewatchesValue, "Total Rewatches", 0, 100));
        rewatchesUpDown.setOnClickListener(v -> showNumberPicker(rewatchesValue, "Total Rewatches", 0, 100));

        // Notes EditText is already interactive by default
        
        // Private checkbox is already interactive by default

        // Setup Save button
        MaterialButton saveButton = dialogView.findViewById(R.id.saveButton);
        if (saveButton != null) {
            saveButton.setOnClickListener(v -> {
                // Save list entry
                dialog.dismiss();
            });
        }

        dialog.show();
    }

    private void showScorePicker(TextView scoreView) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Select Score");
        
        String[] scores = new String[11];
        for (int i = 0; i <= 10; i++) {
            scores[i] = String.valueOf(i);
        }
        
        builder.setItems(scores, (dialog, which) -> {
            scoreView.setText(scores[which]);
            dialog.dismiss();
        });
        
        builder.show();
    }

    private void showNumberPicker(TextView targetView, String title, int minValue, int maxValue) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        builder.setTitle(title);
        
        android.widget.NumberPicker numberPicker = new android.widget.NumberPicker(requireContext());
        numberPicker.setMinValue(minValue);
        numberPicker.setMaxValue(maxValue);
        
        try {
            int currentValue = Integer.parseInt(targetView.getText().toString());
            numberPicker.setValue(currentValue);
        } catch (NumberFormatException e) {
            numberPicker.setValue(minValue);
        }
        
        builder.setView(numberPicker);
        builder.setPositiveButton("OK", (dialog, which) -> {
            targetView.setText(String.valueOf(numberPicker.getValue()));
            dialog.dismiss();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        
        builder.show();
    }

    private void showDatePicker(TextView dateView) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int year = calendar.get(java.util.Calendar.YEAR);
        int month = calendar.get(java.util.Calendar.MONTH);
        int day = calendar.get(java.util.Calendar.DAY_OF_MONTH);
        
        android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(
                requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String date = String.format("%02d/%02d/%d", selectedMonth + 1, selectedDay, selectedYear);
                    dateView.setText(date);
                },
                year, month, day
        );
        
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
        if (layoutManager == null) return;

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
            }
            // Set a placeholder color for the image
            holder.relationImage.setBackgroundColor(requireContext().getColor(R.color.darkBlue35op));
        }

        @Override
        public int getItemCount() {
            return relationTypes.length;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView relationType;
            ImageView relationImage;

            ViewHolder(View itemView) {
                super(itemView);
                relationType = itemView.findViewById(R.id.relationType);
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
                holder.characterImage1.setBackgroundColor(requireContext().getColor(R.color.darkBlue));
                holder.voiceActorImage1.setBackgroundColor(requireContext().getColor(R.color.darkBlue35op));
                holder.card1.setVisibility(View.VISIBLE);
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
                holder.characterImage2.setBackgroundColor(requireContext().getColor(R.color.darkBlue));
                holder.voiceActorImage2.setBackgroundColor(requireContext().getColor(R.color.darkBlue35op));
                holder.card2.setVisibility(View.VISIBLE);
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
            holder.recommendationImage.setBackgroundColor(requireContext().getColor(R.color.darkBlue));
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