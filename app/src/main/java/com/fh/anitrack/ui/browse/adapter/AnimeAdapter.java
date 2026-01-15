package com.fh.anitrack.ui.browse.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.fh.anitrack.R;
import com.fh.anitrack.mockData.model.AnimeItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying anime/manga items in the browse list.
 */
public class AnimeAdapter extends RecyclerView.Adapter<AnimeAdapter.ViewHolder> {

    private List<AnimeItem> items = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(AnimeItem item, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<AnimeItem> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addItems(List<AnimeItem> newItems) {
        if (newItems != null && !newItems.isEmpty()) {
            int startPos = items.size();
            items.addAll(newItems);
            notifyItemRangeInserted(startPos, newItems.size());
        }
    }

    public void clearItems() {
        items.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.browse_item_anime_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AnimeItem item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView posterImage;
        private final TextView posterTitle;
        private final TextView releaseInfo;
        private final ImageView scoreIcon;
        private final TextView scoreText;
        private final LinearLayout scoreBadge;
        private final TextView nextEpisodeInfo;
        private final TextView tagline;
        private final TextView description;
        private final TextView footer;

        ViewHolder(View itemView) {
            super(itemView);
            posterImage = itemView.findViewById(R.id.posterImage);
            posterTitle = itemView.findViewById(R.id.posterTitle);
            releaseInfo = itemView.findViewById(R.id.releaseInfo);
            scoreIcon = itemView.findViewById(R.id.scoreIcon);
            scoreText = itemView.findViewById(R.id.scoreText);
            scoreBadge = itemView.findViewById(R.id.scoreBadge);
            nextEpisodeInfo = itemView.findViewById(R.id.nextEpisodeInfo);
            tagline = itemView.findViewById(R.id.tagline);
            description = itemView.findViewById(R.id.description);
            footer = itemView.findViewById(R.id.footer);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        listener.onItemClick(items.get(pos), pos);
                    }
                }
            });
        }

        void bind(AnimeItem item) {
            Context context = itemView.getContext();

            // Set poster title
            posterTitle.setText(item.getTitle());

            // Set release info
            if (item.getReleaseInfo() != null && !item.getReleaseInfo().isEmpty()) {
                releaseInfo.setText(item.getReleaseInfo());
                releaseInfo.setVisibility(View.VISIBLE);
            } else {
                releaseInfo.setVisibility(View.GONE);
            }

            // Set score badge
            if (item.getScore() > 0) {
                scoreText.setText(item.getScore() + "%");
                scoreBadge.setVisibility(View.VISIBLE);

                // Set score icon based on category
                switch (item.getScoreCategory()) {
                    case POSITIVE:
                        scoreIcon.setImageResource(R.drawable.ic_score_positive);
                        scoreText.setTextColor(ContextCompat.getColor(context, R.color.accentGreen));
                        break;
                    case NEUTRAL:
                        scoreIcon.setImageResource(R.drawable.ic_score_neutral);
                        scoreText.setTextColor(ContextCompat.getColor(context, R.color.gold));
                        break;
                    case NEGATIVE:
                        scoreIcon.setImageResource(R.drawable.ic_score_negative);
                        scoreText.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_light));
                        break;
                }
            } else {
                scoreBadge.setVisibility(View.GONE);
            }

            // Set next episode info (for currently airing)
            if (item.getNextEpisodeInfo() != null && !item.getNextEpisodeInfo().isEmpty()) {
                nextEpisodeInfo.setText(item.getNextEpisodeInfo());
                nextEpisodeInfo.setVisibility(View.VISIBLE);
            } else {
                nextEpisodeInfo.setVisibility(View.GONE);
            }

            // Set tagline (first line of description or custom tagline)
            String desc = item.getDescription();
            if (desc != null && !desc.isEmpty()) {
                // Extract first sentence as tagline
                int firstPeriod = desc.indexOf('.');
                if (firstPeriod > 0 && firstPeriod < 100) {
                    tagline.setText(desc.substring(0, firstPeriod + 1));
                    description.setText(desc.substring(firstPeriod + 1).trim());
                } else {
                    tagline.setVisibility(View.GONE);
                    description.setText(desc);
                }
                description.setVisibility(View.VISIBLE);
            } else {
                tagline.setVisibility(View.GONE);
                description.setVisibility(View.GONE);
            }

            // Set footer (studio info, etc.)
            if (item.getStudio() != null && !item.getStudio().isEmpty()) {
                footer.setText(item.getStudio());
                footer.setVisibility(View.VISIBLE);
            } else {
                footer.setVisibility(View.GONE);
            }

            // Load poster image with Glide
            if (item.getCoverImageUrl() != null && !item.getCoverImageUrl().isEmpty()) {
                Glide.with(context)
                        .load(item.getCoverImageUrl())
                        .placeholder(R.color.darkBlue35op)
                        .error(R.color.darkBlue35op)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(posterImage);
            } else {
                // Fallback to colored background if no image URL
                posterImage.setImageDrawable(null);
                posterImage.setBackgroundColor(ContextCompat.getColor(context, R.color.darkBlue35op));
            }
        }
    }
}
