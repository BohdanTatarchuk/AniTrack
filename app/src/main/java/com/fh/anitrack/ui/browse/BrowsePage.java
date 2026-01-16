package com.fh.anitrack.ui.browse;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fh.anitrack.R;
import com.fh.anitrack.api.AniListQueries;
import com.fh.anitrack.api.AniListService;
import com.fh.anitrack.api.GraphQLRequest;
import com.fh.anitrack.api.RetrofitClient;
import com.fh.anitrack.api.response.FilterOptionsResponse;
import com.fh.anitrack.api.response.MediaSearchResponse;
import com.fh.anitrack.mockData.BrowseMockData;
import com.fh.anitrack.mockData.model.ActiveFilter;
import com.fh.anitrack.mockData.model.AnimeItem;
import com.fh.anitrack.mockData.model.FilterOption;
import com.fh.anitrack.api.response.UserSearchResponse;
import com.fh.anitrack.ui.browse.adapter.AnimeAdapter;
import com.fh.anitrack.ui.browse.adapter.DropdownAdapter;
import com.fh.anitrack.ui.browse.adapter.UserAdapter;
import com.fh.anitrack.ui.browse.widget.LabeledRangeSlider;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Browse page fragment for searching and filtering anime/manga.
 */
public class BrowsePage extends Fragment {

    // Views
    private EditText searchEditText;
    private LinearLayout dropdownMediaType;
    private TextView textMediaType;
    private LinearLayout filtersHeader;
    private LinearLayout filtersContent;
    private ImageView iconFiltersToggle;
    private MaterialButton btnAddFilter;
    private MaterialButton btnMoreOptions;
    private MaterialButton btnLoadMore;
    private View advancedFiltersContainer;
    private ChipGroup activeFiltersChipGroup;
    private RecyclerView animeRecyclerView;
    private RecyclerView usersRecyclerView;
    private View emptyState;
    private View loadingIndicator;

    // Advanced filter views
    private LabeledRangeSlider yearRangeSlider;
    private LabeledRangeSlider episodesRangeSlider;
    private LabeledRangeSlider durationRangeSlider;
    private CheckBox checkboxHideMyAnime;
    private CheckBox checkboxOnlyShowMyAnime;

    // Adapters
    private AnimeAdapter animeAdapter;
    private UserAdapter userAdapter;

    // State
    private static final String TAG = "BrowsePage";
    private static final int MIN_RESULTS_THRESHOLD = 10; // Auto-load more if below this
    private FilterOption selectedMediaType;
    private List<ActiveFilter> activeFilters = new ArrayList<>();
    private boolean isFiltersExpanded = true;
    private boolean isAdvancedFiltersExpanded = false;
    private boolean isAutoLoadingMore = false; // Prevent concurrent auto-loads
    private int rateLimitRemaining = 90; // Track remaining requests
    
    // API filter data
    private List<String> apiGenres = new ArrayList<>();
    private List<FilterOptionsResponse.MediaTag> apiTags = new ArrayList<>();
    private boolean filterOptionsLoaded = false;
    
    // Pagination state
    private int currentPage = 1;
    private boolean hasNextPage = false;
    private String lastSearchQuery = "";
    
    // Debouncing for range sliders
    private final android.os.Handler searchHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable pendingSearchRunnable = null;

    // Initial search query from header
    private static final String ARG_SEARCH_QUERY = "search_query";
    private String initialSearchQuery = null;

    public BrowsePage() {
        // Required empty public constructor
    }

    public static BrowsePage newInstance() {
        return new BrowsePage();
    }

    public static BrowsePage newInstance(String searchQuery) {
        BrowsePage fragment = new BrowsePage();
        Bundle args = new Bundle();
        args.putString(ARG_SEARCH_QUERY, searchQuery);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            initialSearchQuery = getArguments().getString(ARG_SEARCH_QUERY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_browse_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        setupListeners();
        loadInitialData();
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Cancel any pending searches and auto-loads to prevent memory leaks
        if (pendingSearchRunnable != null) {
            searchHandler.removeCallbacks(pendingSearchRunnable);
            pendingSearchRunnable = null;
        }
        searchHandler.removeCallbacksAndMessages(null);
        isAutoLoadingMore = false;
    }

    private void initViews(View view) {
        // Search header views
        View searchHeader = view.findViewById(R.id.searchHeader);
        searchEditText = searchHeader.findViewById(R.id.searchEditText);
        dropdownMediaType = searchHeader.findViewById(R.id.dropdownMediaType);
        textMediaType = searchHeader.findViewById(R.id.textMediaType);
        filtersHeader = searchHeader.findViewById(R.id.filtersHeader);
        filtersContent = searchHeader.findViewById(R.id.filtersContent);
        iconFiltersToggle = searchHeader.findViewById(R.id.iconFiltersToggle);
        btnAddFilter = searchHeader.findViewById(R.id.btnAddFilter);
        btnMoreOptions = searchHeader.findViewById(R.id.btnMoreOptions);

        // Advanced filters - the include tag's ID replaces the root element's ID
        // So advancedFilters IS the container (not a parent of it)
        advancedFiltersContainer = searchHeader.findViewById(R.id.advancedFilters);
        if (advancedFiltersContainer != null) {
            yearRangeSlider = advancedFiltersContainer.findViewById(R.id.yearRangeSlider);
            episodesRangeSlider = advancedFiltersContainer.findViewById(R.id.episodesRangeSlider);
            durationRangeSlider = advancedFiltersContainer.findViewById(R.id.durationRangeSlider);
            checkboxHideMyAnime = advancedFiltersContainer.findViewById(R.id.checkboxHideMyAnime);
            checkboxOnlyShowMyAnime = advancedFiltersContainer.findViewById(R.id.checkboxOnlyShowMyAnime);
        }

        // Main content views
        activeFiltersChipGroup = view.findViewById(R.id.activeFiltersChipGroup);
        animeRecyclerView = view.findViewById(R.id.animeRecyclerView);
        usersRecyclerView = view.findViewById(R.id.usersRecyclerView);
        emptyState = view.findViewById(R.id.emptyState);
        loadingIndicator = view.findViewById(R.id.loadingIndicator);
        btnLoadMore = view.findViewById(R.id.btnLoadMore);
    }

    private void setupRecyclerView() {
        // Media adapter
        animeAdapter = new AnimeAdapter();
        animeRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        animeRecyclerView.setAdapter(animeAdapter);

        animeAdapter.setOnItemClickListener((item, position) -> {
            // Navigate to media detail page
            navigateToMediaDetail(item);
        });

        // Users adapter (3 columns grid)
        userAdapter = new UserAdapter();
        usersRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        usersRecyclerView.setAdapter(userAdapter);

        userAdapter.setOnUserClickListener((user, position) -> {
            // Navigate to user profile page
            navigateToUserProfile(user);
        });
    }

    private void setupListeners() {
        // Search button click listener
        View searchButton = getView().findViewById(R.id.searchButton);
        if (searchButton != null) {
            searchButton.setOnClickListener(v -> {
                performSearch(searchEditText.getText().toString());
                // Hide keyboard after search
                android.view.inputmethod.InputMethodManager imm = 
                    (android.view.inputmethod.InputMethodManager) requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
                }
            });
        }
        
        // Search submit listener (triggers on keyboard search button or enter)
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH ||
                (event != null && event.getKeyCode() == android.view.KeyEvent.KEYCODE_ENTER)) {
                performSearch(searchEditText.getText().toString());
                // Hide keyboard after search
                android.view.inputmethod.InputMethodManager imm = 
                    (android.view.inputmethod.InputMethodManager) requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                return true;
            }
            return false;
        });

        // Media type dropdown
        dropdownMediaType.setOnClickListener(v -> showMediaTypeDropdown());

        // Filters header toggle
        filtersHeader.setOnClickListener(v -> toggleFiltersSection());

        // Add filter button
        btnAddFilter.setOnClickListener(v -> {
            v.setEnabled(false);
            showAddFilterDialog();
            v.postDelayed(() -> v.setEnabled(true), 500);
        });

        // More options toggle
        btnMoreOptions.setOnClickListener(v -> {
            v.setEnabled(false);
            toggleAdvancedFilters();
            v.postDelayed(() -> v.setEnabled(true), 300);
        });

        // Range slider listeners - debounced to avoid excessive searches
        if (yearRangeSlider != null) {
            yearRangeSlider.setOnRangeChangedListener((min, max) -> {
                debouncedUpdateFilters();
            });
        }

        if (episodesRangeSlider != null) {
            episodesRangeSlider.setOnRangeChangedListener((min, max) -> {
                debouncedUpdateFilters();
            });
        }

        if (durationRangeSlider != null) {
            durationRangeSlider.setOnRangeChangedListener((min, max) -> {
                debouncedUpdateFilters();
            });
        }

        // Checkbox listeners - mutually exclusive
        if (checkboxHideMyAnime != null && checkboxOnlyShowMyAnime != null) {
            checkboxHideMyAnime.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    checkboxOnlyShowMyAnime.setChecked(false);
                }
                updateActiveFiltersFromAdvanced();
            });

            checkboxOnlyShowMyAnime.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    checkboxHideMyAnime.setChecked(false);
                }
                updateActiveFiltersFromAdvanced();
            });
        }
        
        // Load more button
        btnLoadMore.setOnClickListener(v -> loadMoreResults());
    }

    private void loadInitialData() {
        // Set default media type to "All"
        selectedMediaType = new FilterOption("ALL", "All");
        textMediaType.setText(selectedMediaType.getDisplayName());
        textMediaType.setTextColor(requireContext().getColor(R.color.darkBlue));

        // Set year slider max to current year
        if (yearRangeSlider != null) {
            int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
            yearRangeSlider.setRange(1970, currentYear);
            yearRangeSlider.setSelectedRange(1970, currentYear);
        }

        // Load filter options from API
        fetchFilterOptions();

        // Perform initial search - use provided query or empty for trending/popular
        if (initialSearchQuery != null && !initialSearchQuery.isEmpty()) {
            searchEditText.setText(initialSearchQuery);
            performSearch(initialSearchQuery);
        } else {
            performSearch("");
        }
    }

    private void fetchFilterOptions() {
        AniListService service = RetrofitClient.getInstance(requireContext()).create(AniListService.class);
        GraphQLRequest request = new GraphQLRequest(AniListQueries.GET_FILTER_OPTIONS, new HashMap<>());
        
        service.getFilterOptions(request).enqueue(new Callback<FilterOptionsResponse>() {
            @Override
            public void onResponse(@NonNull Call<FilterOptionsResponse> call, 
                                   @NonNull Response<FilterOptionsResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    FilterOptionsResponse.Data data = response.body().data;
                    
                    if (data.genres != null) {
                        apiGenres = data.genres;
                        Log.d(TAG, "Loaded " + apiGenres.size() + " genres from API");
                    }
                    
                    if (data.tags != null) {
                        apiTags = data.tags;
                        Log.d(TAG, "Loaded " + apiTags.size() + " tags from API");
                    }
                    
                    filterOptionsLoaded = true;
                } else {
                    Log.e(TAG, "Failed to load filter options: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<FilterOptionsResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Error fetching filter options", t);
            }
        });
    }

    private void toggleFiltersSection() {
        isFiltersExpanded = !isFiltersExpanded;

        if (isFiltersExpanded) {
            // Expand
            filtersContent.setVisibility(View.VISIBLE);
            iconFiltersToggle.animate().rotation(180).setDuration(200).start();
        } else {
            // Collapse
            filtersContent.setVisibility(View.GONE);
            iconFiltersToggle.animate().rotation(0).setDuration(200).start();
            
            // Also collapse advanced filters if expanded
            if (isAdvancedFiltersExpanded) {
                toggleAdvancedFilters();
            }
        }
    }

    private void showMediaTypeDropdown() {
        // Get media types from data source
        List<FilterOption> mediaTypes = new ArrayList<>(BrowseMockData.getMediaTypes());
        
        // Always ensure "All" option is at the beginning
        boolean hasAll = false;
        for (FilterOption option : mediaTypes) {
            if ("ALL".equals(option.getId())) {
                hasAll = true;
                break;
            }
        }
        
        if (!hasAll) {
            mediaTypes.add(0, new FilterOption("ALL", "All"));
        }
        
        showDropdownDialog(
                mediaTypes,
                "Select Media Type",
                (option, position) -> {
                    selectedMediaType = option;
                    textMediaType.setText(option.getDisplayName());
                    textMediaType.setTextColor(requireContext().getColor(R.color.darkBlue));

                    // Toggle visibility based on media type
                    boolean isUserSearch = "USERS".equals(option.getId());
                    updateViewsForMediaType(isUserSearch);

                    performSearch(searchEditText.getText().toString());
                }
        );
    }

    private void updateViewsForMediaType(boolean isUserSearch) {
        // Show/hide appropriate RecyclerView
        animeRecyclerView.setVisibility(isUserSearch ? View.GONE : View.VISIBLE);
        usersRecyclerView.setVisibility(isUserSearch ? View.VISIBLE : View.GONE);

        // Hide filters section for user search (not applicable)
        if (filtersHeader != null) {
            filtersHeader.setVisibility(isUserSearch ? View.GONE : View.VISIBLE);
        }
        if (filtersContent != null) {
            filtersContent.setVisibility(isUserSearch ? View.GONE : (isFiltersExpanded ? View.VISIBLE : View.GONE));
        }
        if (activeFiltersChipGroup != null) {
            activeFiltersChipGroup.setVisibility(isUserSearch ? View.GONE : View.VISIBLE);
        }

        // Clear adapters when switching
        if (isUserSearch) {
            animeAdapter.setItems(new ArrayList<>());
        } else {
            userAdapter.setUsers(new ArrayList<>());
        }
    }

    private void showAddFilterDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.browse_dialog_add_filter, null);
        dialog.setContentView(dialogView);

        LinearLayout dropdownFilterType = dialogView.findViewById(R.id.dropdownFilterType);
        LinearLayout dropdownFilterValue = dialogView.findViewById(R.id.dropdownFilterValue);
        TextView textFilterType = dialogView.findViewById(R.id.textFilterType);
        TextView textFilterValue = dialogView.findViewById(R.id.textFilterValue);
        MaterialButton btnAddFilterConfirm = dialogView.findViewById(R.id.btnAddFilterConfirm);

        final FilterOption[] selectedType = {null};
        final FilterOption[] selectedValue = {null};

        // Filter type dropdown
        dropdownFilterType.setOnClickListener(v -> {
            showDropdownDialog(
                    getFilterTypes(),
                    "Select Filter Type",
                    (option, position) -> {
                        selectedType[0] = option;
                        textFilterType.setText(option.getDisplayName());
                        textFilterType.setTextColor(requireContext().getColor(R.color.darkBlue));

                        // Enable value dropdown
                        dropdownFilterValue.setEnabled(true);
                        dropdownFilterValue.setAlpha(1.0f);
                        textFilterValue.setText("Select " + option.getDisplayName().toLowerCase() + "...");
                        
                        // Reset selected value
                        selectedValue[0] = null;
                        btnAddFilterConfirm.setEnabled(false);
                    }
            );
        });

        // Filter value dropdown
        dropdownFilterValue.setOnClickListener(v -> {
            if (selectedType[0] == null) return;

            List<FilterOption> options = getOptionsForFilterType(selectedType[0].getId());
            showDropdownDialog(
                    options,
                    "Select " + selectedType[0].getDisplayName(),
                    (option, position) -> {
                        selectedValue[0] = option;
                        textFilterValue.setText(option.getDisplayName());
                        textFilterValue.setTextColor(requireContext().getColor(R.color.darkBlue));
                        btnAddFilterConfirm.setEnabled(true);
                    }
            );
        });

        // Add button
        btnAddFilterConfirm.setOnClickListener(v -> {
            if (selectedType[0] != null && selectedValue[0] != null) {
                // Check if filter already exists
                boolean filterExists = false;
                for (ActiveFilter filter : activeFilters) {
                    if (filter.getFilterType().equals(selectedType[0].getId()) &&
                        filter.getFilterValue().equals(selectedValue[0].getId())) {
                        filterExists = true;
                        break;
                    }
                }
                
                if (filterExists) {
                    // Show feedback that filter already exists
                    Toast.makeText(requireContext(), 
                            "This filter is already active", 
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // Create new filter
                String filterId = UUID.randomUUID().toString();
                ActiveFilter newFilter = new ActiveFilter(
                        filterId,
                        selectedType[0].getId(),
                        selectedType[0].getDisplayName(),
                        selectedValue[0].getId(),
                        selectedValue[0].getDisplayName()
                );

                // Add to active filters
                activeFilters.add(newFilter);
                updateActiveFiltersDisplay();
                performSearch(searchEditText.getText().toString());

                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void showDropdownDialog(List<FilterOption> options, String title,
                                     DropdownAdapter.OnOptionSelectedListener listener) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.browse_dialog_dropdown, null);
        dialog.setContentView(dialogView);

        RecyclerView optionsRecyclerView = dialogView.findViewById(R.id.optionsRecyclerView);
        optionsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        DropdownAdapter adapter = new DropdownAdapter();
        adapter.setOptions(options);
        adapter.setOnOptionSelectedListener((option, position) -> {
            listener.onOptionSelected(option, position);
            dialog.dismiss();
        });

        optionsRecyclerView.setAdapter(adapter);
        dialog.show();
    }

    private void toggleAdvancedFilters() {
        if (advancedFiltersContainer == null) {
            return;
        }

        isAdvancedFiltersExpanded = !isAdvancedFiltersExpanded;

        if (isAdvancedFiltersExpanded) {
            advancedFiltersContainer.setVisibility(View.VISIBLE);
            btnMoreOptions.setText("Hide advanced filters");
            
            // Animate expand
            advancedFiltersContainer.setAlpha(0f);
            advancedFiltersContainer.animate()
                    .alpha(1f)
                    .setDuration(200)
                    .start();
        } else {
            btnMoreOptions.setText(R.string.more_filter_options);
            
            // Animate collapse
            advancedFiltersContainer.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction(() -> advancedFiltersContainer.setVisibility(View.GONE))
                    .start();
        }
    }

    private void updateActiveFiltersDisplay() {
        activeFiltersChipGroup.removeAllViews();

        // Add chips for standard filters
        for (ActiveFilter filter : activeFilters) {
            addFilterChip(filter.getDisplayText(), () -> {
                activeFilters.remove(filter);
                updateActiveFiltersDisplay();
                performSearch(searchEditText.getText().toString());
            });
        }

        // Add chips for advanced filters
        addAdvancedFilterChips();

        // Show/hide chip group
        boolean hasActiveFilters = activeFiltersChipGroup.getChildCount() > 0;
        activeFiltersChipGroup.setVisibility(hasActiveFilters ? View.VISIBLE : View.GONE);
    }

    /**
     * Debounced update for range sliders to avoid excessive API calls.
     * Updates display immediately but delays search by 500ms.
     */
    private void debouncedUpdateFilters() {
        // Update chips immediately for visual feedback
        updateActiveFiltersDisplay();
        
        // Cancel any pending search
        if (pendingSearchRunnable != null) {
            searchHandler.removeCallbacks(pendingSearchRunnable);
        }
        
        // Schedule new search after delay
        pendingSearchRunnable = () -> {
            performSearch(searchEditText.getText().toString());
            pendingSearchRunnable = null;
        };
        searchHandler.postDelayed(pendingSearchRunnable, 500);
    }
    
    private void updateActiveFiltersFromAdvanced() {
        updateActiveFiltersDisplay();
        performSearch(searchEditText.getText().toString());
    }

    private void addAdvancedFilterChips() {
        // Year range chip
        if (yearRangeSlider != null && 
                (yearRangeSlider.getMinSelectedValue() != yearRangeSlider.getMinValue() ||
                yearRangeSlider.getMaxSelectedValue() != yearRangeSlider.getMaxValue())) {
            String yearText = (int) yearRangeSlider.getMinSelectedValue() + " - " +
                    (int) yearRangeSlider.getMaxSelectedValue();
            addFilterChip("Year: " + yearText, () -> {
                if (yearRangeSlider != null) {
                    yearRangeSlider.setSelectedRange(
                            yearRangeSlider.getMinValue(),
                            yearRangeSlider.getMaxValue()
                    );
                    updateActiveFiltersDisplay();
                    performSearch(searchEditText.getText().toString());
                }
            });
        }

        // Episodes range chip
        if (episodesRangeSlider != null &&
                (episodesRangeSlider.getMinSelectedValue() != episodesRangeSlider.getMinValue() ||
                episodesRangeSlider.getMaxSelectedValue() != episodesRangeSlider.getMaxValue())) {
            String epText = (int) episodesRangeSlider.getMinSelectedValue() + " - " +
                    (int) episodesRangeSlider.getMaxSelectedValue();
            addFilterChip("Episodes: " + epText, () -> {
                if (episodesRangeSlider != null) {
                    episodesRangeSlider.setSelectedRange(
                            episodesRangeSlider.getMinValue(),
                            episodesRangeSlider.getMaxValue()
                    );
                    updateActiveFiltersDisplay();
                    performSearch(searchEditText.getText().toString());
                }
            });
        }

        // Duration range chip
        if (durationRangeSlider != null &&
                (durationRangeSlider.getMinSelectedValue() != durationRangeSlider.getMinValue() ||
                durationRangeSlider.getMaxSelectedValue() != durationRangeSlider.getMaxValue())) {
            String durText = (int) durationRangeSlider.getMinSelectedValue() + " - " +
                    (int) durationRangeSlider.getMaxSelectedValue();
            addFilterChip("Duration: " + durText, () -> {
                if (durationRangeSlider != null) {
                    durationRangeSlider.setSelectedRange(
                            durationRangeSlider.getMinValue(),
                            durationRangeSlider.getMaxValue()
                    );
                    updateActiveFiltersDisplay();
                    performSearch(searchEditText.getText().toString());
                }
            });
        }

        // Checkbox filters
        if (checkboxHideMyAnime != null && checkboxHideMyAnime.isChecked()) {
            addFilterChip("Hide My Anime", () -> {
                if (checkboxHideMyAnime != null) {
                    checkboxHideMyAnime.setChecked(false);
                    updateActiveFiltersDisplay();
                    performSearch(searchEditText.getText().toString());
                }
            });
        }

        if (checkboxOnlyShowMyAnime != null && checkboxOnlyShowMyAnime.isChecked()) {
            addFilterChip("Only My Anime", () -> {
                if (checkboxOnlyShowMyAnime != null) {
                    checkboxOnlyShowMyAnime.setChecked(false);
                    updateActiveFiltersDisplay();
                    performSearch(searchEditText.getText().toString());
                }
            });
        }
    }

    private void addFilterChip(String text, Runnable onClose) {
        Chip chip = (Chip) LayoutInflater.from(requireContext())
                .inflate(R.layout.browse_item_filter_chip, activeFiltersChipGroup, false);
        chip.setText(text);
        chip.setOnCloseIconClickListener(v -> onClose.run());
        activeFiltersChipGroup.addView(chip);
    }

    private void performSearch(String query) {
        // Check if searching for users
        if (selectedMediaType != null && "USERS".equals(selectedMediaType.getId())) {
            performUserSearch(query);
            return;
        }

        // Reset pagination for new search
        currentPage = 1;
        lastSearchQuery = query;
        isAutoLoadingMore = false; // Reset auto-loading flag
        
        // Cancel any pending auto-loads to prevent concurrent requests
        searchHandler.removeCallbacksAndMessages(null);
        
        // Show loading
        loadingIndicator.setVisibility(View.VISIBLE);
        btnLoadMore.setVisibility(View.GONE);
        
        // Build search parameters from filter chips
        Map<String, Object> variables = buildSearchVariables(query, currentPage);
        
        AniListService service = RetrofitClient.getInstance(requireContext()).create(AniListService.class);
        GraphQLRequest request = new GraphQLRequest(AniListQueries.SEARCH_MEDIA, variables);
        
        service.searchMedia(request).enqueue(new Callback<MediaSearchResponse>() {
            @Override
            public void onResponse(@NonNull Call<MediaSearchResponse> call, 
                                   @NonNull Response<MediaSearchResponse> response) {
                if (!isAdded()) return; // Fragment not attached
                
                loadingIndicator.setVisibility(View.GONE);
                
                // Extract rate limit headers
                String rateLimitStr = response.headers().get("X-RateLimit-Remaining");
                if (rateLimitStr != null) {
                    try {
                        rateLimitRemaining = Integer.parseInt(rateLimitStr);
                        Log.d(TAG, "Rate limit remaining: " + rateLimitRemaining + "/90");
                    } catch (NumberFormatException e) {
                        Log.w(TAG, "Failed to parse rate limit header");
                    }
                }
                
                if (response.isSuccessful() && response.body() != null 
                        && response.body().data != null 
                        && response.body().data.page != null) {
                    
                    MediaSearchResponse.Page page = response.body().data.page;
                    List<MediaSearchResponse.Media> mediaList = page.media;
                    
                    // Log server response details
                    Log.d(TAG, "=== SERVER RESPONSE (Search) ===");
                    Log.d(TAG, "Raw items from server: " + (mediaList != null ? mediaList.size() : 0));
                    if (page.pageInfo != null) {
                        Log.d(TAG, "PageInfo - current: " + page.pageInfo.currentPage 
                                + ", total: " + page.pageInfo.total
                                + ", perPage: " + page.pageInfo.perPage
                                + ", hasNext: " + page.pageInfo.hasNextPage);
                    }
                    
                    List<AnimeItem> results = convertToAnimeItems(mediaList);
                    Log.d(TAG, "Items after conversion: " + results.size());
                    
                    // Apply client-side filters (advanced options)
                    results = applyClientSideFilters(results);
                    Log.d(TAG, "Items after client filtering: " + results.size());
                    Log.d(TAG, "================================");
                    
                    animeAdapter.setItems(results);
                    updateEmptyState(results.isEmpty());
                    
                    // Update pagination state
                    hasNextPage = page.pageInfo != null && page.pageInfo.hasNextPage;
                    btnLoadMore.setVisibility(hasNextPage ? View.VISIBLE : View.GONE);
                    
                    Log.d(TAG, "Search returned " + results.size() + " results after filtering, hasNextPage: " + hasNextPage);
                    
                    // Auto-load more pages if filtered results are below threshold
                    checkAndAutoLoadMore();
                } else {
                    Log.e(TAG, "Search failed: " + response.code());
                    animeAdapter.setItems(new ArrayList<>());
                    updateEmptyState(true);
                    hasNextPage = false;
                    btnLoadMore.setVisibility(View.GONE);
                    isAutoLoadingMore = false;
                }
            }

            @Override
            public void onFailure(@NonNull Call<MediaSearchResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                
                loadingIndicator.setVisibility(View.GONE);
                Log.e(TAG, "Search error", t);
                Toast.makeText(requireContext(), "Search failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                animeAdapter.setItems(new ArrayList<>());
                updateEmptyState(true);
                hasNextPage = false;
                btnLoadMore.setVisibility(View.GONE);
                isAutoLoadingMore = false;
            }
        });
    }

    /**
     * Load more results for pagination.
     */
    private void loadMoreResults() {
        if (!hasNextPage) {
            Log.d(TAG, "Load more clicked but hasNextPage=false. No more pages available.");
            Toast.makeText(requireContext(), "No more results available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if searching for users
        if (selectedMediaType != null && "USERS".equals(selectedMediaType.getId())) {
            loadMoreUsers();
            return;
        }

        Log.d(TAG, "Load more clicked - loading page " + (currentPage + 1));
        currentPage++;
        
        // Show loading
        loadingIndicator.setVisibility(View.VISIBLE);
        btnLoadMore.setEnabled(false);
        
        // Build search parameters with current page
        Map<String, Object> variables = buildSearchVariables(lastSearchQuery, currentPage);
        
        AniListService service = RetrofitClient.getInstance(requireContext()).create(AniListService.class);
        GraphQLRequest request = new GraphQLRequest(AniListQueries.SEARCH_MEDIA, variables);
        
        service.searchMedia(request).enqueue(new Callback<MediaSearchResponse>() {
            @Override
            public void onResponse(@NonNull Call<MediaSearchResponse> call, 
                                   @NonNull Response<MediaSearchResponse> response) {
                if (!isAdded()) return;
                
                loadingIndicator.setVisibility(View.GONE);
                btnLoadMore.setEnabled(true);
                
                // Extract rate limit headers
                String rateLimitStr = response.headers().get("X-RateLimit-Remaining");
                if (rateLimitStr != null) {
                    try {
                        rateLimitRemaining = Integer.parseInt(rateLimitStr);
                        Log.d(TAG, "Rate limit remaining: " + rateLimitRemaining + "/90");
                    } catch (NumberFormatException e) {
                        Log.w(TAG, "Failed to parse rate limit header");
                    }
                }
                
                if (response.isSuccessful() && response.body() != null 
                        && response.body().data != null 
                        && response.body().data.page != null) {
                    
                    MediaSearchResponse.Page page = response.body().data.page;
                    List<MediaSearchResponse.Media> mediaList = page.media;
                    
                    // Log server response details
                    Log.d(TAG, "=== SERVER RESPONSE (Page " + currentPage + ") ===");
                    Log.d(TAG, "Raw items from server: " + (mediaList != null ? mediaList.size() : 0));
                    if (page.pageInfo != null) {
                        Log.d(TAG, "PageInfo - current: " + page.pageInfo.currentPage 
                                + ", total: " + page.pageInfo.total
                                + ", perPage: " + page.pageInfo.perPage
                                + ", hasNext: " + page.pageInfo.hasNextPage);
                    }
                    
                    List<AnimeItem> results = convertToAnimeItems(mediaList);
                    Log.d(TAG, "Items after conversion: " + results.size());
                    
                    // Apply client-side filters (advanced options)
                    results = applyClientSideFilters(results);
                    Log.d(TAG, "Items after client filtering: " + results.size());
                    Log.d(TAG, "=================================");
                    
                    // Append to existing items
                    animeAdapter.addItems(results);
                    
                    // Update pagination state
                    hasNextPage = page.pageInfo != null && page.pageInfo.hasNextPage;
                    btnLoadMore.setVisibility(hasNextPage ? View.VISIBLE : View.GONE);
                    
                    Log.d(TAG, "Loaded " + results.size() + " more results (page " + currentPage + "), total now: " + animeAdapter.getItemCount() + ", hasNextPage: " + hasNextPage);
                    
                    // Always check if we need to auto-load more (works for both auto and manual loads)
                    // Reset flag before checking to allow fresh evaluation
                    isAutoLoadingMore = false;
                    checkAndAutoLoadMore();
                } else {
                    // Re-enable button for retry
                    btnLoadMore.setEnabled(true);
                    
                    Log.e(TAG, "Load more failed: " + response.code());
                    if (response.code() == 429) {
                        // Extract Retry-After header
                        String retryAfter = response.headers().get("Retry-After");
                        int retrySeconds = 60; // Default to 1 minute
                        if (retryAfter != null) {
                            try {
                                retrySeconds = Integer.parseInt(retryAfter);
                            } catch (NumberFormatException e) {
                                Log.w(TAG, "Failed to parse Retry-After header");
                            }
                        }
                        Log.e(TAG, "Rate limited! Retry after " + retrySeconds + " seconds.");
                        Toast.makeText(requireContext(), "Rate limited. Wait " + retrySeconds + "s", Toast.LENGTH_SHORT).show();
                        
                        // Stop auto-loading
                        isAutoLoadingMore = false;
                        searchHandler.removeCallbacksAndMessages(null);
                    } else {
                        // Other error codes
                        Toast.makeText(requireContext(), "Failed to load more (Error " + response.code() + ")", Toast.LENGTH_SHORT).show();
                    }
                    currentPage--; // Revert page increment
                    isAutoLoadingMore = false;
                }
            }

            @Override
            public void onFailure(@NonNull Call<MediaSearchResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                
                loadingIndicator.setVisibility(View.GONE);
                btnLoadMore.setEnabled(true);
                Log.e(TAG, "Load more error", t);
                Toast.makeText(requireContext(), "Failed to load more: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                currentPage--; // Revert page increment
                isAutoLoadingMore = false;
            }
        });
    }

    /**
     * Check if we have enough results after filtering, and auto-load more if needed.
     * This prevents the user from having to click "Load More" multiple times
     * when client-side filters reduce results significantly.
     */
    private void checkAndAutoLoadMore() {
        // Don't auto-load if no more pages available
        if (!hasNextPage) {
            isAutoLoadingMore = false;
            return;
        }
        
        // Don't start a new auto-load cycle if one is already queued
        if (isAutoLoadingMore) {
            return;
        }
        
        int currentItemCount = animeAdapter.getItemCount();
        
        // If we have fewer than threshold items, automatically load more
        if (currentItemCount < MIN_RESULTS_THRESHOLD) {
            Log.d(TAG, "Auto-loading more: only " + currentItemCount + " items after filtering");
            isAutoLoadingMore = true;
            
            // Post to handler to avoid blocking the current callback
            searchHandler.postDelayed(() -> {
                if (isAdded() && hasNextPage) {
                    loadMoreResults();
                } else {
                    isAutoLoadingMore = false;
                }
            }, 2000); // 2s delay for degraded rate limit (30 req/min)
        } else {
            Log.d(TAG, "Auto-load check: " + currentItemCount + " items, threshold reached");
            isAutoLoadingMore = false;
        }
    }
    
    /**
     * Build search variables map based on current filters.
     * Only chip-based filters are sent to the server.
     * Advanced filters (year range, episodes, duration, checkboxes) are applied client-side.
     */
    private Map<String, Object> buildSearchVariables(String searchQuery, int page) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("page", page);
        
        // Add search query if present
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            variables.put("search", searchQuery.trim());
        }
        
        // Add media type filter (ANIME or MANGA)
        if (selectedMediaType != null && !"ALL".equals(selectedMediaType.getId())) {
            String typeId = selectedMediaType.getId();
            if ("ANIME".equals(typeId) || "MANGA".equals(typeId)) {
                variables.put("type", typeId);
            }
        }
        
        // Process active filter chips - collect all values by type
        List<String> genres = new ArrayList<>();
        List<String> tags = new ArrayList<>();
        List<String> formats = new ArrayList<>();
        
        for (ActiveFilter filter : activeFilters) {
            switch (filter.getFilterType()) {
                case "GENRES":
                    genres.add(filter.getFilterValue());
                    break;
                case "TAGS":
                    tags.add(filter.getFilterValue());
                    break;
                case "YEAR":
                    // Year from chip filter (single year selection)
                    variables.put("seasonYear", Integer.parseInt(filter.getFilterValue()));
                    break;
                case "SEASON":
                    variables.put("season", filter.getFilterValue());
                    break;
                case "FORMAT":
                    formats.add(filter.getFilterValue());
                    break;
                case "AIRING_STATUS":
                    variables.put("status", filter.getFilterValue());
                    break;
                case "COUNTRY":
                    variables.put("countryOfOrigin", filter.getFilterValue());
                    break;
                case "SOURCE":
                    variables.put("source", filter.getFilterValue());
                    break;
            }
        }
        
        // Add collected arrays to variables
        if (!genres.isEmpty()) {
            variables.put("genres", genres);
        }
        if (!tags.isEmpty()) {
            variables.put("tags", tags);
        }
        if (!formats.isEmpty()) {
            variables.put("format", formats);
        }
        
        // Note: Advanced filters (year range, episodes, duration, onList checkboxes)
        // are NOT sent to the server - they are applied client-side in applyClientSideFilters()
        
        return variables;
    }

    /**
     * Apply client-side filters (advanced options) to the results.
     * These filters are not sent to the GraphQL server.
     */
    private List<AnimeItem> applyClientSideFilters(List<AnimeItem> items) {
        if (items == null || items.isEmpty()) {
            return items;
        }
        
        List<AnimeItem> filtered = new ArrayList<>(items);
        
        // Year range filter
        if (yearRangeSlider != null) {
            int minYear = (int) yearRangeSlider.getMinSelectedValue();
            int maxYear = (int) yearRangeSlider.getMaxSelectedValue();
            int sliderMin = (int) yearRangeSlider.getMinValue();
            int sliderMax = (int) yearRangeSlider.getMaxValue();
            
            // Only filter if not at default values
            if (minYear > sliderMin || maxYear < sliderMax) {
                filtered.removeIf(item -> {
                    int year = parseYearFromReleaseInfo(item.getReleaseInfo());
                    if (year == 0) return false; // Keep items without year info
                    return year < minYear || year > maxYear;
                });
            }
        }
        
        // Episodes range filter
        if (episodesRangeSlider != null) {
            int minEp = (int) episodesRangeSlider.getMinSelectedValue();
            int maxEp = (int) episodesRangeSlider.getMaxSelectedValue();
            int sliderMin = (int) episodesRangeSlider.getMinValue();
            int sliderMax = (int) episodesRangeSlider.getMaxValue();
            
            if (minEp > sliderMin || maxEp < sliderMax) {
                filtered.removeIf(item -> {
                    int episodes = item.getEpisodes();
                    if (episodes == 0) return false; // Keep items without episode info
                    return episodes < minEp || episodes > maxEp;
                });
            }
        }
        
        // Duration range filter
        if (durationRangeSlider != null) {
            int minDur = (int) durationRangeSlider.getMinSelectedValue();
            int maxDur = (int) durationRangeSlider.getMaxSelectedValue();
            int sliderMin = (int) durationRangeSlider.getMinValue();
            int sliderMax = (int) durationRangeSlider.getMaxValue();
            
            if (minDur > sliderMin || maxDur < sliderMax) {
                filtered.removeIf(item -> {
                    int duration = item.getDuration();
                    if (duration == 0) return false; // Keep items without duration info
                    return duration < minDur || duration > maxDur;
                });
            }
        }
        
        // Note: "Hide my anime" / "Only show my anime" would require user list data
        // which we don't have client-side, so those filters are skipped for now
        
        return filtered;
    }

    /**
     * Parse year from release info string (e.g., "Spring 2024" -> 2024, "2024" -> 2024)
     */
    private int parseYearFromReleaseInfo(String releaseInfo) {
        if (releaseInfo == null || releaseInfo.isEmpty()) {
            return 0;
        }
        
        // Try to find a 4-digit year in the string
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\b(19|20)\\d{2}\\b");
        java.util.regex.Matcher matcher = pattern.matcher(releaseInfo);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group());
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    /**
     * Convert API response media items to AnimeItem model objects.
     */
    private List<AnimeItem> convertToAnimeItems(List<MediaSearchResponse.Media> mediaList) {
        List<AnimeItem> items = new ArrayList<>();
        
        if (mediaList == null) return items;
        
        for (MediaSearchResponse.Media media : mediaList) {
            AnimeItem item = new AnimeItem();
            item.setId(media.id);
            
            if (media.title != null) {
                item.setTitle(media.title.userPreferred);
            }
            
            if (media.coverImage != null) {
                // Prefer large over extraLarge for list view
                item.setCoverImageUrl(media.coverImage.large != null ? 
                        media.coverImage.large : media.coverImage.extraLarge);
            }
            
            item.setDescription(media.description);
            item.setMediaType(media.type);
            item.setFormat(media.format);
            item.setStatus(media.status);
            item.setEpisodes(media.episodes != null ? media.episodes : 0);
            item.setDuration(media.duration != null ? media.duration : 0);
            item.setScore(media.averageScore != null ? media.averageScore : 0);
            
            // Build release info string
            item.setReleaseInfo(buildReleaseInfo(media));
            
            // Build next episode info
            if (media.nextAiringEpisode != null) {
                item.setNextEpisodeInfo(buildNextEpisodeInfo(media.nextAiringEpisode));
            }
            
            // Get studio name
            if (media.studios != null && media.studios.edges != null && !media.studios.edges.isEmpty()) {
                for (MediaSearchResponse.StudioEdge edge : media.studios.edges) {
                    if (edge.isMain && edge.node != null) {
                        item.setStudio(edge.node.name);
                        break;
                    }
                }
            }
            
            items.add(item);
        }
        
        return items;
    }

    /**
     * Build release info string from media data.
     */
    private String buildReleaseInfo(MediaSearchResponse.Media media) {
        StringBuilder sb = new StringBuilder();
        
        // Add season and year
        if (media.season != null && media.seasonYear != null) {
            sb.append(capitalizeFirst(media.season.toLowerCase()));
            sb.append(" ");
            sb.append(media.seasonYear);
        } else if (media.seasonYear != null) {
            sb.append(media.seasonYear);
        } else if (media.startDate != null && media.startDate.year != null) {
            sb.append(media.startDate.year);
        }
        
        return sb.toString();
    }

    /**
     * Build next episode info string.
     */
    private String buildNextEpisodeInfo(MediaSearchResponse.NextAiringEpisode nextEp) {
        long seconds = nextEp.timeUntilAiring;
        long days = seconds / (24 * 3600);
        long hours = (seconds % (24 * 3600)) / 3600;
        
        StringBuilder sb = new StringBuilder();
        sb.append("Ep ").append(nextEp.episode).append(" airing in ");
        
        if (days > 0) {
            sb.append(days).append(" day").append(days > 1 ? "s" : "");
            if (hours > 0) {
                sb.append(", ").append(hours).append(" hour").append(hours > 1 ? "s" : "");
            }
        } else if (hours > 0) {
            sb.append(hours).append(" hour").append(hours > 1 ? "s" : "");
        } else {
            long minutes = seconds / 60;
            sb.append(minutes).append(" minute").append(minutes > 1 ? "s" : "");
        }
        
        return sb.toString();
    }

    /**
     * Capitalize the first letter of a string.
     */
    private String capitalizeFirst(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private void updateEmptyState(boolean isEmpty) {
        emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        animeRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void navigateToMediaDetail(AnimeItem item) {
        // Navigate to MediaPage fragment
        MediaPage mediaPage = MediaPage.newInstance(
                String.valueOf(item.getId()),
                item.getMediaType() != null ? item.getMediaType() : "ANIME"
        );

        getParentFragmentManager().beginTransaction()
                .setCustomAnimations(
                        android.R.anim.fade_in,
                        android.R.anim.fade_out,
                        android.R.anim.fade_in,
                        android.R.anim.fade_out
                )
                .replace(R.id.fragment_container, mediaPage)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Search for users by username.
     */
    private void performUserSearch(String query) {
        // Reset pagination for new search
        currentPage = 1;
        lastSearchQuery = query;
        isAutoLoadingMore = false;

        // Cancel any pending auto-loads
        searchHandler.removeCallbacksAndMessages(null);

        // Show loading
        loadingIndicator.setVisibility(View.VISIBLE);
        btnLoadMore.setVisibility(View.GONE);

        // Build search parameters
        Map<String, Object> variables = new HashMap<>();
        variables.put("page", currentPage);
        if (query != null && !query.trim().isEmpty()) {
            variables.put("search", query.trim());
        }

        AniListService service = RetrofitClient.getInstance(requireContext()).create(AniListService.class);
        GraphQLRequest request = new GraphQLRequest(AniListQueries.SEARCH_USERS, variables);

        service.searchUsers(request).enqueue(new Callback<UserSearchResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserSearchResponse> call,
                                   @NonNull Response<UserSearchResponse> response) {
                if (!isAdded()) return;

                loadingIndicator.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null
                        && response.body().data != null
                        && response.body().data.page != null) {

                    UserSearchResponse.Page page = response.body().data.page;
                    List<UserSearchResponse.User> users = page.users;

                    Log.d(TAG, "User search returned " + (users != null ? users.size() : 0) + " results");

                    userAdapter.setUsers(users != null ? users : new ArrayList<>());
                    updateUserEmptyState(users == null || users.isEmpty());

                    // Update pagination state
                    hasNextPage = page.pageInfo != null && page.pageInfo.hasNextPage;
                    btnLoadMore.setVisibility(hasNextPage ? View.VISIBLE : View.GONE);
                } else {
                    Log.e(TAG, "User search failed: " + response.code());
                    userAdapter.setUsers(new ArrayList<>());
                    updateUserEmptyState(true);
                    hasNextPage = false;
                    btnLoadMore.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserSearchResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;

                loadingIndicator.setVisibility(View.GONE);
                Log.e(TAG, "User search error", t);
                Toast.makeText(requireContext(), "User search failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                userAdapter.setUsers(new ArrayList<>());
                updateUserEmptyState(true);
                hasNextPage = false;
                btnLoadMore.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Load more users for pagination.
     */
    private void loadMoreUsers() {
        Log.d(TAG, "Load more users - loading page " + (currentPage + 1));
        currentPage++;

        // Show loading
        loadingIndicator.setVisibility(View.VISIBLE);
        btnLoadMore.setEnabled(false);

        // Build search parameters
        Map<String, Object> variables = new HashMap<>();
        variables.put("page", currentPage);
        if (lastSearchQuery != null && !lastSearchQuery.trim().isEmpty()) {
            variables.put("search", lastSearchQuery.trim());
        }

        AniListService service = RetrofitClient.getInstance(requireContext()).create(AniListService.class);
        GraphQLRequest request = new GraphQLRequest(AniListQueries.SEARCH_USERS, variables);

        service.searchUsers(request).enqueue(new Callback<UserSearchResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserSearchResponse> call,
                                   @NonNull Response<UserSearchResponse> response) {
                if (!isAdded()) return;

                loadingIndicator.setVisibility(View.GONE);
                btnLoadMore.setEnabled(true);

                if (response.isSuccessful() && response.body() != null
                        && response.body().data != null
                        && response.body().data.page != null) {

                    UserSearchResponse.Page page = response.body().data.page;
                    List<UserSearchResponse.User> users = page.users;

                    Log.d(TAG, "Load more users returned " + (users != null ? users.size() : 0) + " results");

                    // Append to existing users
                    userAdapter.addUsers(users != null ? users : new ArrayList<>());

                    // Update pagination state
                    hasNextPage = page.pageInfo != null && page.pageInfo.hasNextPage;
                    btnLoadMore.setVisibility(hasNextPage ? View.VISIBLE : View.GONE);
                } else {
                    Log.e(TAG, "Load more users failed: " + response.code());
                    btnLoadMore.setEnabled(true);
                    currentPage--; // Revert page increment
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserSearchResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;

                loadingIndicator.setVisibility(View.GONE);
                btnLoadMore.setEnabled(true);
                Log.e(TAG, "Load more users error", t);
                Toast.makeText(requireContext(), "Failed to load more users: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                currentPage--; // Revert page increment
            }
        });
    }

    private void updateUserEmptyState(boolean isEmpty) {
        emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        usersRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void navigateToUserProfile(UserSearchResponse.User user) {
        // TODO: Navigate to user profile page when implemented
        Toast.makeText(requireContext(), "User: " + user.name, Toast.LENGTH_SHORT).show();
    }

    /**
     * Get filter types, combining API data with static options.
     * If API genres/tags are loaded, include "Genre" and "Tags" options.
     * Other filter types (Year, Season, Format, etc.) remain static.
     */
    private List<FilterOption> getFilterTypes() {
        List<FilterOption> types = new ArrayList<>();
        
        // Add Genre if API data is available (or fallback to mock)
        types.add(new FilterOption("GENRES", "Genres"));
        
        // Add Tags option if API tags are available
        if (!apiTags.isEmpty()) {
            types.add(new FilterOption("TAGS", "Tags"));
        }
        
        // Static filter types from mock data
        types.add(new FilterOption("YEAR", "Year"));
        types.add(new FilterOption("SEASON", "Season"));
        types.add(new FilterOption("FORMAT", "Format"));
        types.add(new FilterOption("AIRING_STATUS", "Airing Status"));
        types.add(new FilterOption("STREAMING_ON", "Streaming On"));
        types.add(new FilterOption("COUNTRY", "Country of Origin"));
        types.add(new FilterOption("SOURCE", "Source Material"));
        
        return types;
    }

    /**
     * Get filter options based on filter type selection.
     * Uses API data for genres and tags, mock data for other filter types.
     */
    private List<FilterOption> getOptionsForFilterType(String filterTypeId) {
        if (filterTypeId == null) return new ArrayList<>();

        switch (filterTypeId) {
            case "GENRES":
                // Use API genres if available, otherwise fallback to mock
                if (!apiGenres.isEmpty()) {
                    List<FilterOption> genreOptions = new ArrayList<>();
                    for (String genre : apiGenres) {
                        genreOptions.add(new FilterOption(genre, genre));
                    }
                    return genreOptions;
                }
                return BrowseMockData.getGenres();
                
            case "TAGS":
                // Use API tags (filter out adult tags if needed)
                List<FilterOption> tagOptions = new ArrayList<>();
                for (FilterOptionsResponse.MediaTag tag : apiTags) {
                    // Optionally filter adult tags: if (!tag.isAdult)
                    tagOptions.add(new FilterOption(tag.name, tag.name));
                }
                return tagOptions;
                
            // Static options from mock data
            case "YEAR":
                return BrowseMockData.getYears();
            case "SEASON":
                return BrowseMockData.getSeasons();
            case "FORMAT":
                return BrowseMockData.getFormats();
            case "AIRING_STATUS":
                return BrowseMockData.getAiringStatuses();
            case "STREAMING_ON":
                return BrowseMockData.getStreamingPlatforms();
            case "COUNTRY":
                return BrowseMockData.getCountries();
            case "SOURCE":
                return BrowseMockData.getSourceMaterials();
            default:
                return new ArrayList<>();
        }
    }
}