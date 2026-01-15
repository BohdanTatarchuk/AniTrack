package com.fh.anitrack.ui.home;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fh.anitrack.R;
import com.fh.anitrack.api.AniListQueries;
import com.fh.anitrack.api.AniListService;
import com.fh.anitrack.api.AuthRepository;
import com.fh.anitrack.api.GraphQLRequest;
import com.fh.anitrack.api.RequestWrapper;
import com.fh.anitrack.api.RetrofitClient;
import com.fh.anitrack.api.response.ActivityResponse;
import com.fh.anitrack.api.response.SaveActivityResponse;
import com.fh.anitrack.ui.MainActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.HashMap;
import java.util.Map;

import io.noties.markwon.Markwon;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import retrofit2.Call;

public class HomeFragment extends Fragment {

    //for the formatting
    private Markwon markwon;

    EditText etStatus;

    //adapters are used for pagination ("load more" functionality to be precise)
    private ActivityAdapter activityAdapter;
    private TrendingAdapter trendingAdapter;

    //counter variables for pagination. they will be increased if user presses on "load more"
    private int activityPage = 1;
    private int trendingPage = 1;

    public HomeFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Markwon init
        markwon = Markwon.builder(requireContext())
                .usePlugin(StrikethroughPlugin.create())
                .build();

        // ui elements binding
        RecyclerView rvFeed = view.findViewById(R.id.rvFeed);
        RecyclerView rvTrending = view.findViewById(R.id.rvTrending);
        MaterialButton btnLoadMoreFeed = view.findViewById(R.id.btnLoadMoreFeed);
        MaterialButton btnLoadMoreTitles = view.findViewById(R.id.btnLoadMoreTitles);
        etStatus = view.findViewById(R.id.etStatus);
        TextView tvPreviewContent = view.findViewById(R.id.tvPreviewStatusContent);
        TextView tvPreviewUsername = view.findViewById(R.id.tvPreviewUsername);
        MaterialButton btnPublish = view.findViewById(R.id.btnPublish);
        MaterialButton btnCancel = view.findViewById(R.id.btnCancel);

        View previewAvatarCard = view.findViewById(R.id.previewAvatarCard);
        ImageView previewAvatar = previewAvatarCard.findViewById(R.id.userAvatar);

        btnCancel.setOnClickListener(v -> handleCancelAction());
        btnPublish.setOnClickListener(v -> handlePublishAction());

        //setting image and username for status edit text
        AuthRepository authRepo = AuthRepository.getInstance(requireContext());
        tvPreviewUsername.setText(authRepo.getUsername());
        Glide.with(this)
                .load(authRepo.getAvatarUrl())
                .circleCrop()
                .placeholder(R.drawable.profile_picture)
                .into(previewAvatar);

        etStatus.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    markwon.setMarkdown(tvPreviewContent, s.toString());
                } else {
                    tvPreviewContent.setText(getString(R.string.your_status_will_appear_here));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        //adapters init
        activityAdapter = new ActivityAdapter();
        activityAdapter.setMarkwon(markwon);
        rvFeed.setAdapter(activityAdapter);
        rvFeed.setNestedScrollingEnabled(false); // for better performance
        trendingAdapter = new TrendingAdapter();
        rvTrending.setAdapter(trendingAdapter);
        rvTrending.setNestedScrollingEnabled(false); //for better performance

        //listeners for load more buttons
        btnLoadMoreFeed.setOnClickListener(v -> fetchFeed(++activityPage));
        btnLoadMoreTitles.setOnClickListener(v -> fetchTrending(++trendingPage));

        //initialize formatting buttons
        bindFormattingListeners(view);

        //initial data loading
        fetchFeed(1);
        fetchTrending(1);

        androidx.core.widget.NestedScrollView scrollView = view.findViewById(R.id.homeScrollView);
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setupScrollToTop(scrollView);
        }

        return view;
    }

    private void applyFormatting(String startTag, String endTag) {
        int start = etStatus.getSelectionStart();
        int end = etStatus.getSelectionEnd();
        Editable text = etStatus.getText();

        if (start != -1 && end != -1) {
            if (start != end) {
                // user selected text: wrap it -> [start]Selected Text[end]
                text.insert(start, startTag);
                text.insert(end + startTag.length(), endTag);
            } else {
                // no selection: we just insert tags and place cursor in middle
                text.insert(start, startTag + endTag);
                etStatus.setSelection(start + startTag.length());
            }
        }
        etStatus.requestFocus();
    }

    private void bindFormattingListeners(View root) {
        // Row 1
        root.findViewById(R.id.btnBold).setOnClickListener(v -> applyFormatting("__", "__"));
        root.findViewById(R.id.btnItalic).setOnClickListener(v -> applyFormatting("_", "_"));
        root.findViewById(R.id.btnStrikethrough).setOnClickListener(v -> applyFormatting("~~", "~~"));
        root.findViewById(R.id.btnSpoiler).setOnClickListener(v -> applyFormatting("~! ", " !~"));
        root.findViewById(R.id.btnLink).setOnClickListener(v -> applyFormatting("[", "](url)"));
        root.findViewById(R.id.btnImage).setOnClickListener(v -> applyFormatting("img###(", ")"));
        root.findViewById(R.id.btnYoutube).setOnClickListener(v -> applyFormatting("youtube(", ")"));

        // Row 2
        root.findViewById(R.id.btnVideo).setOnClickListener(v -> applyFormatting("webm(", ")"));
        root.findViewById(R.id.btnOrderedList).setOnClickListener(v -> applyFormatting("1. ", ""));
        root.findViewById(R.id.btnUnorderedList).setOnClickListener(v -> applyFormatting("- ", ""));
        root.findViewById(R.id.btnHeading).setOnClickListener(v -> applyFormatting("### ", ""));
        root.findViewById(R.id.btnCenter).setOnClickListener(v -> applyFormatting("center~", "~center"));
        root.findViewById(R.id.btnQuote).setOnClickListener(v -> applyFormatting("> ", ""));
        root.findViewById(R.id.btnHtml).setOnClickListener(v -> applyFormatting("<code>", "</code>"));
    }

    private void handleCancelAction() {
        String content = etStatus.getText().toString().trim();

        if (content.isEmpty()) {
            // Nothing typed, just close keyboard
            hideKeyboard();
        } else {
            // Content exists, show double-check dialog
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.cancel_post)
                    .setMessage(R.string.discard_changes_confirm)
                    .setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss())
                    .setPositiveButton(R.string.yes, (dialog, which) -> {
                        clearStatusInput();
                        hideKeyboard();
                    })
                    .show();
        }
    }

    private void handlePublishAction() {
        String content = etStatus.getText().toString().trim();

        if (content.isEmpty()) {
            Toast.makeText(getContext(), R.string.status_cannot_be_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        // Show double-check dialog before posting
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.publish_status)
                .setMessage(R.string.publish_confirm_msg)
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .setPositiveButton(R.string.publish, (dialog, which) -> {
                    publishStatus(content);
                })
                .show();
    }

    private void publishStatus(String content) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("text", content);

        AniListService service = RetrofitClient.getInstance(requireContext()).create(AniListService.class);
        Call<SaveActivityResponse> call = service.saveTextActivity(new GraphQLRequest(AniListQueries.SAVE_TEXT_ACTIVITY, vars));

        RequestWrapper.sendRequest(call, response -> {
            if (response.isSuccessful()) {
                Toast.makeText(getContext(), R.string.published_successfully, Toast.LENGTH_SHORT).show();

                //clear input and hide keyboard
                clearStatusInput();
                hideKeyboard();

                //refresh the feed
                activityPage = 1; // reset page counter to 1
                activityAdapter.clearItems(); // clear current items in RecyclerView
                fetchFeed(1); // fetch the first page again

            } else {
                Toast.makeText(getContext(), "Error: " + response.code(), Toast.LENGTH_SHORT).show();
            }
        }, t -> {
            Toast.makeText(getContext(), R.string.network_failure, Toast.LENGTH_SHORT).show();
        }, requireContext());
    }
    private void clearStatusInput() {
        etStatus.setText("");
    }

    private void hideKeyboard() {
        View view = this.getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void fetchFeed(int page) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("page", page);
        vars.put("perPage", 10);

        AniListService service = RetrofitClient.getInstance(requireContext()).create(AniListService.class);
        Call<ActivityResponse> call = service.postQuery(new GraphQLRequest(AniListQueries.GET_ACTIVITIES, vars));

        RequestWrapper.sendRequest(call, response -> {
            if (response.isSuccessful() && response.body() != null && response.body().data.Page != null) {
                activityAdapter.addItems(response.body().data.Page.activities);
            }
        }, requireContext());
    }

    private void fetchTrending(int page) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("page", page);
        vars.put("perPage", 6);

        AniListService service = RetrofitClient.getInstance(requireContext()).create(AniListService.class);
        Call<ActivityResponse> call = service.postQuery(new GraphQLRequest(AniListQueries.GET_TRENDING, vars));

        RequestWrapper.sendRequest(call, response -> {
            if (response.isSuccessful() && response.body() != null && response.body().data.Page != null) {
                trendingAdapter.addItems(response.body().data.Page.media);
            }
        }, requireContext());
    }
}