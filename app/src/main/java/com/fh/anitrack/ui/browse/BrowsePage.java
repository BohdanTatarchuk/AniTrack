package com.fh.anitrack.ui.browse;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fh.anitrack.R;
import com.fh.anitrack.data.BrowseMockData;
import com.fh.anitrack.data.model.ActiveFilter;
import com.fh.anitrack.data.model.AnimeItem;
import com.fh.anitrack.data.model.FilterOption;
import com.fh.anitrack.ui.browse.adapter.AnimeAdapter;
import com.fh.anitrack.ui.browse.adapter.DropdownAdapter;
import com.fh.anitrack.ui.browse.widget.LabeledRangeSlider;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
    private View advancedFiltersContainer;
    private ChipGroup activeFiltersChipGroup;
    private RecyclerView animeRecyclerView;
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

    // State
    private FilterOption selectedMediaType;
    private List<ActiveFilter> activeFilters = new ArrayList<>();
    private boolean isFiltersExpanded = true;
    private boolean isAdvancedFiltersExpanded = false;

    public BrowsePage() {
        // Required empty public constructor
    }

    public static BrowsePage newInstance() {
        return new BrowsePage();
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
        emptyState = view.findViewById(R.id.emptyState);
        loadingIndicator = view.findViewById(R.id.loadingIndicator);
    }

    private void setupRecyclerView() {
        animeAdapter = new AnimeAdapter();
        animeRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        animeRecyclerView.setAdapter(animeAdapter);

        animeAdapter.setOnItemClickListener((item, position) -> {
            // Navigate to media detail page
            navigateToMediaDetail(item);
        });
    }

    private void setupListeners() {
        // Search text listener
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                performSearch(s.toString());
            }
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

        // Range slider listeners
        if (yearRangeSlider != null) {
            yearRangeSlider.setOnRangeChangedListener((min, max) -> {
                updateActiveFiltersFromAdvanced();
            });
        }

        if (episodesRangeSlider != null) {
            episodesRangeSlider.setOnRangeChangedListener((min, max) -> {
                updateActiveFiltersFromAdvanced();
            });
        }

        if (durationRangeSlider != null) {
            durationRangeSlider.setOnRangeChangedListener((min, max) -> {
                updateActiveFiltersFromAdvanced();
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
    }

    private void loadInitialData() {
        // Set default media type
        selectedMediaType = new FilterOption("ANIME", "Anime");
        textMediaType.setText(selectedMediaType.getDisplayName());
        textMediaType.setTextColor(requireContext().getColor(R.color.darkBlue));

        // Set year slider max to current year
        if (yearRangeSlider != null) {
            int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
            yearRangeSlider.setRange(1970, currentYear);
            yearRangeSlider.setSelectedRange(1976, currentYear);
        }

        // Load mock data
        List<AnimeItem> mockItems = BrowseMockData.getMockAnimeList();
        animeAdapter.setItems(mockItems);

        updateEmptyState(mockItems.isEmpty());
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
        showDropdownDialog(
                BrowseMockData.getMediaTypes(),
                "Select Media Type",
                (option, position) -> {
                    selectedMediaType = option;
                    textMediaType.setText(option.getDisplayName());
                    textMediaType.setTextColor(requireContext().getColor(R.color.darkBlue));
                    performSearch(searchEditText.getText().toString());
                }
        );
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
                    BrowseMockData.getFilterTypes(),
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

            List<FilterOption> options = BrowseMockData.getOptionsForFilterType(selectedType[0].getId());
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
        // Show loading
        loadingIndicator.setVisibility(View.VISIBLE);

        // In production, this would be an API call with all filter parameters
        // For now, just show mock data after a brief delay
        animeRecyclerView.postDelayed(() -> {
            loadingIndicator.setVisibility(View.GONE);
            List<AnimeItem> results = BrowseMockData.getMockAnimeList();
            animeAdapter.setItems(results);
            updateEmptyState(results.isEmpty());
        }, 300);
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
}