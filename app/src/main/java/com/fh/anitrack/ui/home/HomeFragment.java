package com.fh.anitrack.ui.home;

import static com.fh.anitrack.api.RequestWrapper.sendRequest;

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
import com.fh.anitrack.api.RetrofitClient;
import com.fh.anitrack.api.response.SaveActivityResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    EditText etStatus;

    //adapters are used for pagination (load more functionality to be precise)
    private ActivityAdapter activityAdapter;
    private TrendingAdapter trendingAdapter;

    //counter variables for pagination. they will be increased if user presses on "load more"
    private int activityPage = 1;
    private int trendingPage = 1;

    public HomeFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

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
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvPreviewContent.setText(s.length() > 0 ? s.toString() : getString(R.string.your_status_will_appear_here));
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        //adapters init
        activityAdapter = new ActivityAdapter();
        rvFeed.setAdapter(activityAdapter);
        rvFeed.setNestedScrollingEnabled(false); // for better performance
        trendingAdapter = new TrendingAdapter();
        rvTrending.setAdapter(trendingAdapter);
        rvTrending.setNestedScrollingEnabled(false); //for better performance

        //listeners for load more buttons
        btnLoadMoreFeed.setOnClickListener(v -> fetchFeed(++activityPage));
        btnLoadMoreTitles.setOnClickListener(v -> fetchTrending(++trendingPage));

        //initial data loading
        fetchFeed(1);
        fetchTrending(1);

        return view;
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

        GraphQLRequest request = new GraphQLRequest(AniListQueries.SAVE_TEXT_ACTIVITY, vars);
        AniListService service = RetrofitClient.getInstance(requireContext()).create(AniListService.class);

        service.saveTextActivity(request).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<SaveActivityResponse> call, @NonNull Response<SaveActivityResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), R.string.published_successfully, Toast.LENGTH_SHORT).show();
                    clearStatusInput();
                    hideKeyboard();
                } else {
                    Toast.makeText(getContext(), "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<SaveActivityResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), R.string.network_failure, Toast.LENGTH_SHORT).show();
            }
        });
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

        sendRequest(AniListQueries.GET_ACTIVITIES, vars, response -> {
            if (response.body() != null && response.body().data.Page != null) {
                activityAdapter.addItems(response.body().data.Page.activities);
            }
        }, requireContext());
    }

    private void fetchTrending(int page) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("page", page);
        vars.put("perPage", 6);

        sendRequest(AniListQueries.GET_TRENDING, vars, response -> {
            if (response.body() != null && response.body().data.Page != null) {
                trendingAdapter.addItems(response.body().data.Page.media);
            }
        }, requireContext());
    }
}