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
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fh.anitrack.R;
import com.fh.anitrack.data.BrowseMockData;
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

/**
 * Browse page fragment for searching and filtering anime/manga.
 */
public class BrowsePage extends Fragment {

    // Views
    private EditText searchEditText;
    private LinearLayout dropdownMediaType;
    private LinearLayout dropdownFilterType;
    private LinearLayout dropdownTypeOption;
    private TextView textMediaType;
    private TextView textFilterType;
    private TextView textTypeOption;
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
    private FilterOption selectedFilterType;
    private List<FilterOption> selectedTypeOptions = new ArrayList<>();
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
        dropdownFilterType = searchHeader.findViewById(R.id.dropdownFilterType);
        dropdownTypeOption = searchHeader.findViewById(R.id.dropdownTypeOption);
        textMediaType = searchHeader.findViewById(R.id.textMediaType);
        textFilterType = searchHeader.findViewById(R.id.textFilterType);
        textTypeOption = searchHeader.findViewById(R.id.textTypeOption);
        btnMoreOptions = searchHeader.findViewById(R.id.btnMoreOptions);

        // Advanced filters views
        advancedFiltersContainer = view.findViewById(R.id.advancedFiltersContainer);
        yearRangeSlider = view.findViewById(R.id.yearRangeSlider);
        episodesRangeSlider = view.findViewById(R.id.episodesRangeSlider);
        durationRangeSlider = view.findViewById(R.id.durationRangeSlider);
        checkboxHideMyAnime = view.findViewById(R.id.checkboxHideMyAnime);
        checkboxOnlyShowMyAnime = view.findViewById(R.id.checkboxOnlyShowMyAnime);

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

        // Dropdown click listeners
        dropdownMediaType.setOnClickListener(v -> showMediaTypeDropdown());
        dropdownFilterType.setOnClickListener(v -> showFilterTypeDropdown());
        dropdownTypeOption.setOnClickListener(v -> showTypeOptionDropdown());

        // More options toggle
        btnMoreOptions.setOnClickListener(v -> toggleAdvancedFilters());

        // Range slider listeners
        yearRangeSlider.setOnRangeChangedListener((min, max) -> {
            updateActiveFilters();
        });

        episodesRangeSlider.setOnRangeChangedListener((min, max) -> {
            updateActiveFilters();
        });

        durationRangeSlider.setOnRangeChangedListener((min, max) -> {
            updateActiveFilters();
        });

        // Checkbox listeners - mutually exclusive
        checkboxHideMyAnime.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkboxOnlyShowMyAnime.setChecked(false);
            }
            updateActiveFilters();
        });

        checkboxOnlyShowMyAnime.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkboxHideMyAnime.setChecked(false);
            }
            updateActiveFilters();
        });
    }

    private void loadInitialData() {
        // Set default media type
        selectedMediaType = new FilterOption("ANIME", "Anime");
        textMediaType.setText(selectedMediaType.getDisplayName());
        textMediaType.setTextColor(requireContext().getColor(R.color.darkBlue));

        // Load mock data
        List<AnimeItem> mockItems = BrowseMockData.getMockAnimeList();
        animeAdapter.setItems(mockItems);

        updateEmptyState(mockItems.isEmpty());
    }

    private void showMediaTypeDropdown() {
        showDropdownDialog(
                BrowseMockData.getMediaTypes(),
                "Select Media Type",
                (option, position) -> {
                    selectedMediaType = option;
                    textMediaType.setText(option.getDisplayName());
                    textMediaType.setTextColor(requireContext().getColor(R.color.darkBlue));
                    updateActiveFilters();
                    performSearch(searchEditText.getText().toString());
                }
        );
    }

    private void showFilterTypeDropdown() {
        showDropdownDialog(
                BrowseMockData.getFilterTypes(),
                "Select Filter Type",
                (option, position) -> {
                    selectedFilterType = option;
                    textFilterType.setText(option.getDisplayName());
                    textFilterType.setTextColor(requireContext().getColor(R.color.darkBlue));

                    // Reset type option when filter type changes
                    selectedTypeOptions.clear();
                    textTypeOption.setText(R.string.type_option);
                    textTypeOption.setTextColor(requireContext().getColor(R.color.darkGrey));
                }
        );
    }

    private void showTypeOptionDropdown() {
        if (selectedFilterType == null) {
            // Show filter type dropdown first
            showFilterTypeDropdown();
            return;
        }

        List<FilterOption> options = BrowseMockData.getOptionsForFilterType(selectedFilterType.getId());

        showDropdownDialog(
                options,
                "Select " + selectedFilterType.getDisplayName(),
                (option, position) -> {
                    // Add to selected options if not already present
                    if (!selectedTypeOptions.contains(option)) {
                        selectedTypeOptions.add(option);
                    }

                    // Update display text
                    if (selectedTypeOptions.size() == 1) {
                        textTypeOption.setText(option.getDisplayName());
                    } else {
                        textTypeOption.setText(selectedTypeOptions.size() + " selected");
                    }
                    textTypeOption.setTextColor(requireContext().getColor(R.color.darkBlue));

                    updateActiveFilters();
                    performSearch(searchEditText.getText().toString());
                }
        );
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
        isAdvancedFiltersExpanded = !isAdvancedFiltersExpanded;

        if (isAdvancedFiltersExpanded) {
            advancedFiltersContainer.setVisibility(View.VISIBLE);
            // Animate expand
            advancedFiltersContainer.setAlpha(0f);
            advancedFiltersContainer.animate()
                    .alpha(1f)
                    .setDuration(200)
                    .start();
        } else {
            // Animate collapse
            advancedFiltersContainer.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction(() -> advancedFiltersContainer.setVisibility(View.GONE))
                    .start();
        }
    }

    private void updateActiveFilters() {
        activeFiltersChipGroup.removeAllViews();
        boolean hasActiveFilters = false;

        // Add selected type options as chips
        for (FilterOption option : selectedTypeOptions) {
            addFilterChip(option.getDisplayName(), () -> {
                selectedTypeOptions.remove(option);
                updateTypeOptionText();
                updateActiveFilters();
                performSearch(searchEditText.getText().toString());
            });
            hasActiveFilters = true;
        }

        // Add range filter chips if modified from default
        if (yearRangeSlider.getMinSelectedValue() != yearRangeSlider.getMinValue() ||
                yearRangeSlider.getMaxSelectedValue() != yearRangeSlider.getMaxValue()) {
            String yearText = (int) yearRangeSlider.getMinSelectedValue() + " - " +
                    (int) yearRangeSlider.getMaxSelectedValue();
            addFilterChip("Year: " + yearText, () -> {
                yearRangeSlider.setSelectedRange(
                        yearRangeSlider.getMinValue(),
                        yearRangeSlider.getMaxValue()
                );
                updateActiveFilters();
            });
            hasActiveFilters = true;
        }

        if (episodesRangeSlider.getMinSelectedValue() != episodesRangeSlider.getMinValue() ||
                episodesRangeSlider.getMaxSelectedValue() != episodesRangeSlider.getMaxValue()) {
            String epText = (int) episodesRangeSlider.getMinSelectedValue() + " - " +
                    (int) episodesRangeSlider.getMaxSelectedValue();
            addFilterChip("Episodes: " + epText, () -> {
                episodesRangeSlider.setSelectedRange(
                        episodesRangeSlider.getMinValue(),
                        episodesRangeSlider.getMaxValue()
                );
                updateActiveFilters();
            });
            hasActiveFilters = true;
        }

        // Checkbox filters
        if (checkboxHideMyAnime.isChecked()) {
            addFilterChip("Hide My Anime", () -> {
                checkboxHideMyAnime.setChecked(false);
                updateActiveFilters();
            });
            hasActiveFilters = true;
        }

        if (checkboxOnlyShowMyAnime.isChecked()) {
            addFilterChip("Only My Anime", () -> {
                checkboxOnlyShowMyAnime.setChecked(false);
                updateActiveFilters();
            });
            hasActiveFilters = true;
        }

        activeFiltersChipGroup.setVisibility(hasActiveFilters ? View.VISIBLE : View.GONE);
    }

    private void addFilterChip(String text, Runnable onClose) {
        Chip chip = (Chip) LayoutInflater.from(requireContext())
                .inflate(R.layout.browse_item_filter_chip, activeFiltersChipGroup, false);
        chip.setText(text);
        chip.setOnCloseIconClickListener(v -> onClose.run());
        activeFiltersChipGroup.addView(chip);
    }

    private void updateTypeOptionText() {
        if (selectedTypeOptions.isEmpty()) {
            textTypeOption.setText(R.string.type_option);
            textTypeOption.setTextColor(requireContext().getColor(R.color.darkGrey));
        } else if (selectedTypeOptions.size() == 1) {
            textTypeOption.setText(selectedTypeOptions.get(0).getDisplayName());
            textTypeOption.setTextColor(requireContext().getColor(R.color.darkBlue));
        } else {
            textTypeOption.setText(selectedTypeOptions.size() + " selected");
            textTypeOption.setTextColor(requireContext().getColor(R.color.darkBlue));
        }
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