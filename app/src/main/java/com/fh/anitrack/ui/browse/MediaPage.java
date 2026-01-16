package com.fh.anitrack.ui.browse;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fh.anitrack.R;
import com.fh.anitrack.api.AniListQueries;
import com.fh.anitrack.api.AniListService;
import com.fh.anitrack.api.GraphQLRequest;
import com.fh.anitrack.api.RetrofitClient;
import com.fh.anitrack.api.response.MediaDetailResponse;
import com.fh.anitrack.api.response.SaveMediaListResponse;
import com.fh.anitrack.api.response.ToggleFavouriteResponse;
import com.fh.anitrack.data.MediaDetailMockData;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Media Detail Fragment - displays detailed information about an anime/manga.
 * 
 * By default, all data fields are empty. To populate with test data,
 * set USE_MOCK_DATA = true. For production, integrate with your backend service.
 */
public class MediaPage extends Fragment {

    // Set to true to load mock data for testing, false for empty/production state
    private static final boolean USE_MOCK_DATA = false;
    private static final String TAG = "MediaPage";

    // Argument keys
    private static final String ARG_MEDIA_ID = "media_id";
    private static final String ARG_MEDIA_TYPE = "media_type";

    private RecyclerView relationsRecyclerView;
    private RecyclerView charactersRecyclerView;
    private RecyclerView recommendationsRecyclerView;
    private View loadingIndicator;
    private MaterialButton addToListButton;
    private ImageButton favoriteButton;
    private TextView favoriteCount;

    // Data arrays - empty by default, populated from backend or mock data
    private List<RelationData> relations = new ArrayList<>();
    private List<CharacterData> characters = new ArrayList<>();
    private List<RecommendationData> recommendations = new ArrayList<>();
    
    // Full media data
    private MediaDetailResponse.Media mediaData;

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
        
        // Load data from server or mock
        if (USE_MOCK_DATA) {
            loadMockData(view);
            setupAdapters();
        } else {
            loadMediaDetailsFromServer();
        }
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
        addToListButton = view.findViewById(R.id.addToListButton);
        if (addToListButton != null) {
            addToListButton.setOnClickListener(v -> showAddToListDialog());
        }

        // Favorite button
        favoriteButton = view.findViewById(R.id.favoriteButton);
        favoriteCount = view.findViewById(R.id.favoriteCount);
        if (favoriteButton != null) {
            favoriteButton.setOnClickListener(v -> toggleFavourite());
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

        // Tags expansion toggle
        ImageButton toggleTagsExpansion = view.findViewById(R.id.toggleTagsExpansion);
        ChipGroup tagsChipGroup = view.findViewById(R.id.tagsChipGroup);
        toggleTagsExpansion.setOnClickListener(v -> {
            if (tagsChipGroup.getVisibility() == View.VISIBLE) {
                tagsChipGroup.setVisibility(View.GONE);
                toggleTagsExpansion.setImageResource(R.drawable.ic_expand_more);
            } else {
                tagsChipGroup.setVisibility(View.VISIBLE);
                toggleTagsExpansion.setImageResource(R.drawable.ic_expand_less);
            }
        });

        // Trailer click handled in setupTrailer()
        // (moved to populateMediaInfo after data is loaded)
        
        // Get loading indicator reference
        loadingIndicator = view.findViewById(R.id.loadingIndicator);
    }

    /**
     * Load media details from AniList API
     */
    private void loadMediaDetailsFromServer() {
        if (mediaId == null) {
            Log.e(TAG, "Media ID is null, cannot load details");
            Toast.makeText(requireContext(), "Error: Invalid media ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading indicator
        if (loadingIndicator != null) loadingIndicator.setVisibility(View.VISIBLE);

        Log.d(TAG, "Loading media details for ID: " + mediaId + ", Type: " + mediaType);

        // Prepare GraphQL variables
        Map<String, Object> variables = new HashMap<>();
        try {
            variables.put("id", Integer.parseInt(mediaId));
            if (mediaType != null && !mediaType.isEmpty()) {
                variables.put("type", mediaType);
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid media ID format: " + mediaId, e);
            Toast.makeText(requireContext(), "Error: Invalid media ID", Toast.LENGTH_SHORT).show();
            if (loadingIndicator != null) loadingIndicator.setVisibility(View.GONE);
            return;
        }

        GraphQLRequest request = new GraphQLRequest(AniListQueries.GET_MEDIA_DETAILS, variables);
        AniListService service = RetrofitClient.getInstance(requireContext()).create(AniListService.class);

        service.getMediaDetails(request).enqueue(new Callback<MediaDetailResponse>() {
            @Override
            public void onResponse(@NonNull Call<MediaDetailResponse> call, @NonNull Response<MediaDetailResponse> response) {
                if (loadingIndicator != null) loadingIndicator.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    mediaData = response.body().data.Media;
                    Log.d(TAG, "Successfully loaded media: " + (mediaData.title != null ? mediaData.title.userPreferred : "Unknown"));
                    populateViewWithData();
                    setupAdapters();
                } else {
                    Log.e(TAG, "Failed to load media details. Code: " + response.code());
                    Toast.makeText(requireContext(), "Failed to load media details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<MediaDetailResponse> call, @NonNull Throwable t) {
                if (loadingIndicator != null) loadingIndicator.setVisibility(View.GONE);
                Log.e(TAG, "Error loading media details", t);
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Populate all views with data from mediaData
     */
    private void populateViewWithData() {
        if (mediaData == null || getView() == null) return;
        
        View view = getView();
        
        // Parse relations
        if (mediaData.relations != null && mediaData.relations.edges != null) {
            for (MediaDetailResponse.RelationEdge edge : mediaData.relations.edges) {
                if (edge.node != null) {
                    relations.add(new RelationData(
                        edge.relationType,
                        edge.node.title != null ? edge.node.title.userPreferred : "Unknown",
                        edge.node.coverImage != null ? edge.node.coverImage.large : null,
                        edge.node.id,
                        edge.node.type
                    ));
                }
            }
        }
        
        // Parse characters
        if (mediaData.characterPreview != null && mediaData.characterPreview.edges != null) {
            for (MediaDetailResponse.CharacterEdge edge : mediaData.characterPreview.edges) {
                if (edge.node != null) {
                    String characterName = edge.node.name != null ? edge.node.name.userPreferred : "Unknown";
                    String characterImage = edge.node.image != null ? edge.node.image.large : null;
                    String role = edge.role != null ? edge.role : "Unknown";
                    
                    String vaName = "N/A";
                    String vaLanguage = "";
                    String vaImage = null;
                    
                    if (edge.voiceActors != null && !edge.voiceActors.isEmpty()) {
                        MediaDetailResponse.VoiceActor va = edge.voiceActors.get(0);
                        vaName = va.name != null ? va.name.userPreferred : "N/A";
                        vaLanguage = va.language != null ? va.language : "";
                        vaImage = va.image != null ? va.image.large : null;
                    }
                    
                    characters.add(new CharacterData(characterName, role, vaName, vaLanguage, characterImage, vaImage));
                }
            }
        }
        
        // Parse recommendations
        if (mediaData.recommendations != null && mediaData.recommendations.nodes != null) {
            for (MediaDetailResponse.RecommendationNode node : mediaData.recommendations.nodes) {
                if (node.mediaRecommendation != null) {
                    recommendations.add(new RecommendationData(
                        node.mediaRecommendation.title != null ? node.mediaRecommendation.title.userPreferred : "Unknown",
                        node.mediaRecommendation.coverImage != null ? node.mediaRecommendation.coverImage.large : null,
                        node.mediaRecommendation.id,
                        node.mediaRecommendation.type
                    ));
                }
            }
        }
        
        // Populate UI elements
        populateMediaInfo(view);
    }
    
    /**
     * Populate the UI with media information
     */
    private void populateMediaInfo(View view) {
        if (mediaData == null) return;
        
        // Load thumbnail image (cover, not banner)
        ImageView mediaThumbnail = view.findViewById(R.id.mediaThumbnail);
        String imageUrl = mediaData.coverImage != null ? mediaData.coverImage.extraLarge : null;
        if (imageUrl != null && mediaThumbnail != null) {
            Glide.with(requireContext())
                .load(imageUrl)
                .placeholder(R.color.darkBlue35op)
                .into(mediaThumbnail);
        }
        
        // Set title
        TextView mediaTitle = view.findViewById(R.id.mediaTitle);
        if (mediaTitle != null && mediaData.title != null) {
            mediaTitle.setText(mediaData.title.userPreferred);
        }
        
        // Set favorite count and state
        if (favoriteCount != null) {
            favoriteCount.setText(formatNumber(mediaData.favourites));
        }
        
        // Set favorite button selected state based on isFavourite
        if (favoriteButton != null) {
            favoriteButton.setSelected(mediaData.isFavourite);
        }
        
        // Update Add to List button text based on media list entry
        updateAddToListButtonText();
        
        // Set stats
        setupStatColumn(view, R.id.statAverageScore, "Average Score", 
            mediaData.averageScore != null ? mediaData.averageScore + "%" : "N/A");
        setupStatColumn(view, R.id.statMeanScore, "Mean Score", 
            mediaData.meanScore != null ? mediaData.meanScore + "%" : "N/A");
        setupStatColumn(view, R.id.statPopularity, "Popularity", "#" + formatNumber(mediaData.popularity));
        setupStatColumn(view, R.id.statFavorites, "Favorites", formatNumber(mediaData.favourites));
        
        // Set studio
        String studioName = "N/A";
        if (mediaData.studios != null && mediaData.studios.edges != null) {
            for (MediaDetailResponse.StudioEdge edge : mediaData.studios.edges) {
                if (edge.isMain && edge.node != null) {
                    studioName = edge.node.name;
                    break;
                }
            }
        }
        setupStatColumn(view, R.id.statStudios, "Studios", studioName);
        
        // Set format row (Type, Format, Episodes/Chapters, Status, Season)
        LinearLayout formatContainer = view.findViewById(R.id.formatContainer);
        if (formatContainer != null) {
            formatContainer.removeAllViews();
            
            if (mediaData.format != null) {
                addFormatItem(formatContainer, mediaData.format);
            }
            
            if ("ANIME".equals(mediaData.type) && mediaData.episodes != null) {
                addFormatItem(formatContainer, mediaData.episodes + " Episodes");
            } else if ("MANGA".equals(mediaData.type) && mediaData.chapters != null) {
                addFormatItem(formatContainer, mediaData.chapters + " Chapters");
            }
            
            if (mediaData.status != null) {
                addFormatItem(formatContainer, mediaData.status.replace("_", " "));
            }
            
            if (mediaData.season != null && mediaData.seasonYear != null) {
                addFormatItem(formatContainer, mediaData.season + " " + mediaData.seasonYear);
            } else if (mediaData.seasonYear != null) {
                addFormatItem(formatContainer, String.valueOf(mediaData.seasonYear));
            }
        }
        
        // Set genres/tags
        ChipGroup tagsChipGroup = view.findViewById(R.id.tagsChipGroup);
        if (tagsChipGroup != null) {
            tagsChipGroup.removeAllViews();
            
            // Add genre chips
            if (mediaData.genres != null) {
                for (String genre : mediaData.genres) {
                    addGenreChip(tagsChipGroup, genre);
                }
            }
            
            // Add tag chips (top ranked tags)
            if (mediaData.tags != null) {
                for (MediaDetailResponse.Tag tag : mediaData.tags) {
                    if (tag.rank >= 60) { // Only show high-ranked tags
                        addTagChip(tagsChipGroup, tag.name, tag.rank);
                    }
                }
            }
        }
        
        // Set description
        TextView descriptionText = view.findViewById(R.id.descriptionText);
        if (descriptionText != null && mediaData.description != null) {
            // Remove HTML tags from description
            String cleanDescription = mediaData.description.replaceAll("<[^>]*>", "");
            descriptionText.setText(cleanDescription);
        }
        
        TextView descriptionSource = view.findViewById(R.id.descriptionSource);
        if (descriptionSource != null && mediaData.source != null) {
            descriptionSource.setText("Source: " + mediaData.source.replace("_", " "));
        }
        
        // Set up trailer
        setupTrailer(view);
        
        // Set up score distribution chart
        setupScoreDistribution(view);
    }
    
    /**
     * Set up trailer section
     */
    private void setupTrailer(View view) {
        View trailerCard = view.findViewById(R.id.trailerCard);
        ImageView trailerThumbnail = view.findViewById(R.id.trailerThumbnail);
        
        if (mediaData != null && mediaData.trailer != null && trailerCard != null) {
            // Load trailer thumbnail
            if (trailerThumbnail != null && mediaData.trailer.thumbnail != null) {
                Glide.with(requireContext())
                    .load(mediaData.trailer.thumbnail)
                    .placeholder(R.color.darkBlue35op)
                    .into(trailerThumbnail);
            }
            
            // Set click listener to open trailer
            trailerCard.setOnClickListener(v -> {
                if (mediaData.trailer.site != null && mediaData.trailer.id != null) {
                    String url = null;
                    if ("youtube".equalsIgnoreCase(mediaData.trailer.site)) {
                        url = "https://www.youtube.com/watch?v=" + mediaData.trailer.id;
                    } else if ("dailymotion".equalsIgnoreCase(mediaData.trailer.site)) {
                        url = "https://www.dailymotion.com/video/" + mediaData.trailer.id;
                    }
                    
                    if (url != null) {
                        try {
                            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
                            intent.setData(android.net.Uri.parse(url));
                            startActivity(intent);
                        } catch (Exception e) {
                            Toast.makeText(requireContext(), "Unable to open trailer", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
            
            trailerCard.setVisibility(View.VISIBLE);
        } else if (trailerCard != null) {
            trailerCard.setVisibility(View.GONE);
        }
    }
    
    /**
     * Set up score distribution chart with actual data
     */
    private void setupScoreDistribution(View view) {
        View scoreChart = view.findViewById(R.id.scoreDistributionChart);
        if (scoreChart == null || mediaData == null || mediaData.stats == null || 
            mediaData.stats.scoreDistribution == null) {
            return;
        }
        
        // Map score ranges to bars (10-30 -> bar1, 40-60 -> bar2, 70-80 -> bar3, 90-100 -> bar4)
        int[] barAmounts = new int[4];
        
        for (MediaDetailResponse.ScoreDistribution dist : mediaData.stats.scoreDistribution) {
            int score = dist.score;
            int amount = dist.amount;
            
            if (score >= 10 && score <= 30) {
                barAmounts[0] += amount;
            } else if (score >= 40 && score <= 60) {
                barAmounts[1] += amount;
            } else if (score >= 70 && score <= 80) {
                barAmounts[2] += amount;
            } else if (score >= 90 && score <= 100) {
                barAmounts[3] += amount;
            }
        }
        
        // Find max for scaling
        int maxAmount = 0;
        for (int amount : barAmounts) {
            if (amount > maxAmount) maxAmount = amount;
        }
        
        if (maxAmount == 0) return; // No data
        
        // Get chart height
        int maxHeight = (int) getResources().getDimension(R.dimen.scoreChartHeight);
        
        // Set bar heights, colors, and counts
        int[] barIds = {R.id.bar1, R.id.bar2, R.id.bar3, R.id.bar4};
        int[] countIds = {R.id.bar1Count, R.id.bar2Count, R.id.bar3Count, R.id.bar4Count};
        int[] barColors = {
            android.graphics.Color.parseColor("#E53935"), // Red for low scores
            android.graphics.Color.parseColor("#FB8C00"), // Orange for medium-low scores
            android.graphics.Color.parseColor("#FFB300"), // Yellow for medium-high scores
            android.graphics.Color.parseColor("#43A047")  // Green for high scores
        };
        
        for (int i = 0; i < 4; i++) {
            View bar = scoreChart.findViewById(barIds[i]);
            TextView countText = scoreChart.findViewById(countIds[i]);
            
            if (bar != null) {
                // Calculate proportional height
                int height = (int) (maxHeight * ((float) barAmounts[i] / maxAmount));
                if (height < 8 && barAmounts[i] > 0) height = 8; // Minimum visible height
                
                android.view.ViewGroup.LayoutParams params = bar.getLayoutParams();
                params.height = height;
                bar.setLayoutParams(params);
                
                // Set bar color
                bar.setBackgroundColor(barColors[i]);
            }
            
            // Always show count label if there's data, even for tallest bars
            if (countText != null && barAmounts[i] > 0) {
                countText.setText(String.valueOf(barAmounts[i]));
                countText.setVisibility(View.VISIBLE);
                
                // Ensure text is always visible above the bar
                android.view.ViewGroup.MarginLayoutParams textParams = 
                    (android.view.ViewGroup.MarginLayoutParams) countText.getLayoutParams();
                if (textParams != null) {
                    textParams.bottomMargin = 4; // Small margin from bar top
                    countText.setLayoutParams(textParams);
                }
            } else if (countText != null) {
                countText.setVisibility(View.GONE);
            }
        }
    }
    
    /**
     * Format large numbers into K format (e.g., 18100 -> 18.1K)
     */
    private String formatNumber(int number) {
        if (number >= 1000) {
            return String.format("%.1fK", number / 1000.0);
        }
        return String.valueOf(number);
    }
    
    /**
     * Add a genre chip (without percentage)
     */
    private void addGenreChip(ChipGroup chipGroup, String genreName) {
        Chip chip = (Chip) LayoutInflater.from(requireContext())
                .inflate(R.layout.media_page_item_tag, chipGroup, false);
        chip.setText(genreName);
        chipGroup.addView(chip);
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

        // Prepare data for adapters
        for (MediaDetailMockData.RelationData r : MediaDetailMockData.RELATIONS) {
            relations.add(new RelationData(r.type, r.title, null, 0, "ANIME"));
        }

        for (MediaDetailMockData.CharacterData c : MediaDetailMockData.CHARACTERS) {
            characters.add(new CharacterData(c.characterName, c.role, c.voiceActorName, c.language, null, null));
        }

        for (MediaDetailMockData.RecommendationData r : MediaDetailMockData.RECOMMENDATIONS) {
            recommendations.add(new RecommendationData(r.title, null, 0, "ANIME"));
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
        startDateValue.setText("--/--/----");
        finishDateValue.setText("--/--/----");
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
                // Collect form data
                String statusDisplay = statusSpinner.getSelectedItem().toString();
                String status = mapStatusToApi(statusDisplay);
                // Convert score from 0-10 to 0-100 for AniList (multiply by 10)
                float score = Float.parseFloat(scoreValue.getText().toString()) * 10;
                int progress = Integer.parseInt(episodeValue.getText().toString());
                int repeat = Integer.parseInt(rewatchesValue.getText().toString());
                
                EditText notesEditText = dialogView.findViewById(R.id.notesEditText);
                String notes = notesEditText != null ? notesEditText.getText().toString() : "";
                
                CheckBox privateCheckbox = dialogView.findViewById(R.id.privateCheckbox);
                boolean isPrivate = privateCheckbox != null && privateCheckbox.isChecked();
                
                // Parse dates
                Map<String, Integer> startDate = parseDateString(startDateValue.getText().toString());
                Map<String, Integer> finishDate = parseDateString(finishDateValue.getText().toString());
                
                // Save to server
                saveMediaListEntry(status, score, progress, repeat, isPrivate, notes, startDate, finishDate);
                
                dialog.dismiss();
            });
        }

        dialog.show();
    }
    
    /**
     * Map display status to AniList API status enum
     */
    private String mapStatusToApi(String displayStatus) {
        switch (displayStatus) {
            case "Watching":
                return "CURRENT";
            case "Planning":
                return "PLANNING";
            case "Completed":
                return "COMPLETED";
            case "Paused":
                return "PAUSED";
            case "Dropped":
                return "DROPPED";
            default:
                return "PLANNING";
        }
    }
    
    /**
     * Parse date string in format MM/DD/YYYY to year, month, day map
     */
    private Map<String, Integer> parseDateString(String dateStr) {
        Map<String, Integer> dateMap = new HashMap<>();
        if (dateStr == null || dateStr.isEmpty() || dateStr.equals("--/--/----")) {
            return dateMap;
        }
        
        try {
            String[] parts = dateStr.split("/");
            if (parts.length == 3) {
                dateMap.put("month", Integer.parseInt(parts[0]));
                dateMap.put("day", Integer.parseInt(parts[1]));
                dateMap.put("year", Integer.parseInt(parts[2]));
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error parsing date: " + dateStr, e);
        }
        
        return dateMap;
    }
    
    /**
     * Save media list entry to AniList
     */
    private void saveMediaListEntry(String status, float score, int progress, int repeat, 
                                    boolean isPrivate, String notes, 
                                    Map<String, Integer> startDate, Map<String, Integer> finishDate) {
        if (mediaId == null) {
            Toast.makeText(requireContext(), "Error: Invalid media ID", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Prepare GraphQL variables
        Map<String, Object> variables = new HashMap<>();
        try {
            variables.put("mediaId", Integer.parseInt(mediaId));
            variables.put("status", status);
            variables.put("score", score);
            variables.put("progress", progress);
            variables.put("repeat", repeat);
            variables.put("private", isPrivate);
            
            if (notes != null && !notes.isEmpty()) {
                variables.put("notes", notes);
            }
            
            if (!startDate.isEmpty()) {
                variables.put("startedAt", startDate);
            }
            
            if (!finishDate.isEmpty()) {
                variables.put("completedAt", finishDate);
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid media ID format: " + mediaId, e);
            Toast.makeText(requireContext(), "Error: Invalid media ID", Toast.LENGTH_SHORT).show();
            return;
        }
        
        GraphQLRequest request = new GraphQLRequest(AniListQueries.SAVE_MEDIA_LIST_ENTRY, variables);
        
        AniListService service = RetrofitClient.getInstance(requireContext()).create(AniListService.class);
        Call<SaveMediaListResponse> call = service.saveMediaListEntry(request);
        
        call.enqueue(new Callback<SaveMediaListResponse>() {
            @Override
            public void onResponse(Call<SaveMediaListResponse> call, Response<SaveMediaListResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    SaveMediaListResponse.SaveMediaListEntry entry = response.body().data.SaveMediaListEntry;
                    if (entry != null) {
                        Log.d(TAG, "Media list entry saved successfully. Entry ID: " + entry.id);
                        Toast.makeText(requireContext(), "Added to list successfully!", Toast.LENGTH_SHORT).show();
                        // Refresh media page to get updated data
                        refreshMediaDetails();
                    } else {
                        Log.e(TAG, "Response data is null");
                        Toast.makeText(requireContext(), "Failed to add to list", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Failed to save media list entry. Response code: " + response.code());
                    try {
                        if (response.errorBody() != null) {
                            Log.e(TAG, "Error body: " + response.errorBody().string());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Toast.makeText(requireContext(), "Failed to add to list. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<SaveMediaListResponse> call, Throwable t) {
                Log.e(TAG, "Network error saving media list entry", t);
                Toast.makeText(requireContext(), "Network error. Please check your connection.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Refresh media details from server to get updated data
     */
    private void refreshMediaDetails() {
        if (!isAdded()) return;
        
        // Clear existing data
        relations.clear();
        characters.clear();
        recommendations.clear();
        
        // Reload from server
        loadMediaDetailsFromServer();
    }
    
    /**
     * Update the Add to List button text based on whether media is in user's list
     */
    private void updateAddToListButtonText() {
        if (addToListButton == null || mediaData == null) return;
        
        if (mediaData.mediaListEntry != null && mediaData.mediaListEntry.status != null) {
            // Media is in user's list - show current status
            String displayStatus = mapApiStatusToDisplay(mediaData.mediaListEntry.status);
            addToListButton.setText(displayStatus);
        } else {
            // Media not in list
            addToListButton.setText(R.string.add_to_list);
        }
    }
    
    /**
     * Map AniList API status to display text
     */
    private String mapApiStatusToDisplay(String apiStatus) {
        switch (apiStatus) {
            case "CURRENT":
                return "ANIME".equals(mediaType) ? "Watching" : "Reading";
            case "PLANNING":
                return "Planning";
            case "COMPLETED":
                return "Completed";
            case "PAUSED":
                return "Paused";
            case "DROPPED":
                return "Dropped";
            case "REPEATING":
                return "Repeating";
            default:
                return apiStatus;
        }
    }
    
    /**
     * Toggle the favourite state of this media
     */
    private void toggleFavourite() {
        if (mediaData == null || mediaId == null) {
            Toast.makeText(requireContext(), "Cannot favorite: media data not loaded", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Optimistically update UI
        boolean newFavoriteState = !mediaData.isFavourite;
        if (favoriteButton != null) {
            favoriteButton.setSelected(newFavoriteState);
            favoriteButton.performHapticFeedback(android.view.HapticFeedbackConstants.CONTEXT_CLICK);
        }
        
        // Update local data optimistically
        int newFavouritesCount = mediaData.favourites + (newFavoriteState ? 1 : -1);
        if (newFavouritesCount < 0) newFavouritesCount = 0;
        
        if (favoriteCount != null) {
            favoriteCount.setText(formatNumber(newFavouritesCount));
        }
        
        // Build variables based on media type
        Map<String, Object> variables = new HashMap<>();
        try {
            int id = Integer.parseInt(mediaId);
            if ("ANIME".equals(mediaType)) {
                variables.put("animeId", id);
            } else {
                variables.put("mangaId", id);
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid media ID", e);
            return;
        }
        
        GraphQLRequest request = new GraphQLRequest(AniListQueries.TOGGLE_FAVOURITE, variables);
        AniListService service = RetrofitClient.getInstance(requireContext()).create(AniListService.class);
        
        final int finalNewCount = newFavouritesCount;
        service.toggleFavourite(request).enqueue(new Callback<ToggleFavouriteResponse>() {
            @Override
            public void onResponse(@NonNull Call<ToggleFavouriteResponse> call,
                                   @NonNull Response<ToggleFavouriteResponse> response) {
                if (!isAdded()) return;
                
                if (response.isSuccessful() && response.body() != null) {
                    // Update local data
                    mediaData.isFavourite = newFavoriteState;
                    mediaData.favourites = finalNewCount;
                    Log.d(TAG, "Toggled favourite: " + newFavoriteState);
                } else {
                    // Revert UI on failure
                    Log.e(TAG, "Failed to toggle favourite. Code: " + response.code());
                    revertFavouriteState();
                    Toast.makeText(requireContext(), "Failed to update favourite", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<ToggleFavouriteResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                
                Log.e(TAG, "Network error toggling favourite", t);
                revertFavouriteState();
                Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Revert favourite button state on API failure
     */
    private void revertFavouriteState() {
        if (favoriteButton != null && mediaData != null) {
            favoriteButton.setSelected(mediaData.isFavourite);
            if (favoriteCount != null) {
                favoriteCount.setText(formatNumber(mediaData.favourites));
            }
        }
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
                    .inflate(R.layout.item_media_card, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if (position < relations.size()) {
                RelationData relation = relations.get(position);
                // Set title with relation type
                holder.title.setText(relation.type + "\n" + relation.title);
                
                // Load image with Glide
                if (relation.imageUrl != null && !relation.imageUrl.isEmpty()) {
                    Glide.with(holder.itemView.getContext())
                        .load(relation.imageUrl)
                        .placeholder(R.color.darkBlue35op)
                        .into(holder.image);
                } else {
                    holder.image.setBackgroundColor(requireContext().getColor(R.color.darkBlue35op));
                }
            }
        }

        @Override
        public int getItemCount() {
            return relations.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView title;
            ImageView image;

            ViewHolder(View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.mediaCardTitle);
                image = itemView.findViewById(R.id.mediaCardImage);
                
                // Set click listener to navigate to related media
                itemView.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && position < relations.size()) {
                        RelationData relation = relations.get(position);
                        navigateToMediaPage(relation.mediaId, relation.mediaType);
                    }
                });
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
            if (index1 < characters.size()) {
                CharacterData char1 = characters.get(index1);
                holder.characterName1.setText(formatNameWithLineBreak(char1.characterName));
                holder.characterRole1.setText(char1.role);
                holder.voiceActorName1.setText(formatNameWithLineBreak(char1.voiceActorName));
                holder.voiceActorLanguage1.setText(char1.voiceActorLanguage);
                
                // Load character image
                if (char1.characterImageUrl != null) {
                    Glide.with(holder.itemView.getContext())
                        .load(char1.characterImageUrl)
                        .placeholder(R.color.darkBlue)
                        .into(holder.characterImage1);
                } else {
                    holder.characterImage1.setBackgroundColor(requireContext().getColor(R.color.darkBlue));
                }
                
                // Load voice actor image
                if (char1.voiceActorImageUrl != null) {
                    Glide.with(holder.itemView.getContext())
                        .load(char1.voiceActorImageUrl)
                        .placeholder(R.color.darkBlue35op)
                        .into(holder.voiceActorImage1);
                } else {
                    holder.voiceActorImage1.setBackgroundColor(requireContext().getColor(R.color.darkBlue35op));
                }
                
                holder.card1.setVisibility(View.VISIBLE);
            } else {
                holder.card1.setVisibility(View.INVISIBLE);
            }

            // Second card in column
            if (index2 < characters.size()) {
                CharacterData char2 = characters.get(index2);
                holder.characterName2.setText(formatNameWithLineBreak(char2.characterName));
                holder.characterRole2.setText(char2.role);
                holder.voiceActorName2.setText(formatNameWithLineBreak(char2.voiceActorName));
                holder.voiceActorLanguage2.setText(char2.voiceActorLanguage);
                
                // Load character image
                if (char2.characterImageUrl != null) {
                    Glide.with(holder.itemView.getContext())
                        .load(char2.characterImageUrl)
                        .placeholder(R.color.darkBlue)
                        .into(holder.characterImage2);
                } else {
                    holder.characterImage2.setBackgroundColor(requireContext().getColor(R.color.darkBlue));
                }
                
                // Load voice actor image
                if (char2.voiceActorImageUrl != null) {
                    Glide.with(holder.itemView.getContext())
                        .load(char2.voiceActorImageUrl)
                        .placeholder(R.color.darkBlue35op)
                        .into(holder.voiceActorImage2);
                } else {
                    holder.voiceActorImage2.setBackgroundColor(requireContext().getColor(R.color.darkBlue35op));
                }
                
                holder.card2.setVisibility(View.VISIBLE);
            } else {
                holder.card2.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public int getItemCount() {
            // Return number of columns (pairs)
            return (characters.size() + 1) / 2;
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
                    .inflate(R.layout.item_media_card, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if (position < recommendations.size()) {
                RecommendationData rec = recommendations.get(position);
                holder.title.setText(rec.title);
                
                // Load image with Glide
                if (rec.imageUrl != null && !rec.imageUrl.isEmpty()) {
                    Glide.with(holder.itemView.getContext())
                        .load(rec.imageUrl)
                        .placeholder(R.color.darkBlue)
                        .into(holder.image);
                } else {
                    holder.image.setBackgroundColor(requireContext().getColor(R.color.darkBlue));
                }
            }
        }

        @Override
        public int getItemCount() {
            return recommendations.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView title;
            ImageView image;

            ViewHolder(View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.mediaCardTitle);
                image = itemView.findViewById(R.id.mediaCardImage);
                
                // Set click listener to navigate to recommended media
                itemView.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && position < recommendations.size()) {
                        RecommendationData rec = recommendations.get(position);
                        navigateToMediaPage(rec.mediaId, rec.mediaType);
                    }
                });
            }
        }
    }
    
    // ==================== Helper Methods ====================
    
    /**
     * Navigate to a media page with the given media ID and type
     */
    private void navigateToMediaPage(int mediaId, String mediaType) {
        Bundle args = new Bundle();
        args.putString(ARG_MEDIA_ID, String.valueOf(mediaId));
        args.putString(ARG_MEDIA_TYPE, mediaType);
        
        MediaPage mediaPage = new MediaPage();
        mediaPage.setArguments(args);
        
        requireActivity().getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.fragment_container, mediaPage)
            .addToBackStack(null)
            .commit();
    }
    
    /**
     * Format a name to display first and last names on separate lines
     */
    private String formatNameWithLineBreak(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return fullName;
        }
        
        // Split on spaces
        String[] parts = fullName.trim().split("\\s+");
        
        if (parts.length <= 1) {
            return fullName; // Single word name, return as is
        }
        
        // If 2 parts, put them on separate lines
        if (parts.length == 2) {
            return parts[0] + "\n" + parts[1];
        }
        
        // If 3+ parts, put first name on one line, rest on second line
        StringBuilder secondLine = new StringBuilder();
        for (int i = 1; i < parts.length; i++) {
            if (i > 1) secondLine.append(" ");
            secondLine.append(parts[i]);
        }
        return parts[0] + "\n" + secondLine.toString();
    }
    
    // ==================== Data Classes ====================
    
    private static class RelationData {
        String type;
        String title;
        String imageUrl;
        int mediaId;
        String mediaType;
        
        RelationData(String type, String title, String imageUrl, int mediaId, String mediaType) {
            this.type = type;
            this.title = title;
            this.imageUrl = imageUrl;
            this.mediaId = mediaId;
            this.mediaType = mediaType;
        }
    }
    
    private static class CharacterData {
        String characterName;
        String role;
        String voiceActorName;
        String voiceActorLanguage;
        String characterImageUrl;
        String voiceActorImageUrl;
        
        CharacterData(String characterName, String role, String voiceActorName, String voiceActorLanguage, String characterImageUrl, String voiceActorImageUrl) {
            this.characterName = characterName;
            this.role = role;
            this.voiceActorName = voiceActorName;
            this.voiceActorLanguage = voiceActorLanguage;
            this.characterImageUrl = characterImageUrl;
            this.voiceActorImageUrl = voiceActorImageUrl;
        }
    }
    
    private static class RecommendationData {
        String title;
        String imageUrl;
        int mediaId;
        String mediaType;
        
        RecommendationData(String title, String imageUrl, int mediaId, String mediaType) {
            this.title = title;
            this.imageUrl = imageUrl;
            this.mediaId = mediaId;
            this.mediaType = mediaType;
        }
    }
}