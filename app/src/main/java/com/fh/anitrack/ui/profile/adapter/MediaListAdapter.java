package com.fh.anitrack.ui.profile.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.fh.anitrack.R;
import com.fh.anitrack.api.response.MediaListResponse;
import java.util.ArrayList;
import java.util.List;

public class MediaListAdapter extends RecyclerView.Adapter<MediaListAdapter.ViewHolder> {
    private final List<MediaListResponse.MediaListEntry> list = new ArrayList<>();

    public void addItems(List<MediaListResponse.MediaListEntry> newItems) {
        int start = list.size();
        list.addAll(newItems);
        notifyItemRangeInserted(start, newItems.size());
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clear() {
        list.clear();
        notifyDataSetChanged();
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int vt) {
        return new ViewHolder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_list_entry, p, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int p) {
        MediaListResponse.MediaListEntry entry = list.get(p);
        MediaListResponse.Media media = entry.media;

        Glide.with(h.itemView).load(media.coverImage.large).into(h.ivCover);
        h.tvTitle.setText(media.title.userPreferred);
        h.tvFormat.setText(media.format != null ? media.format.replace("_", " ") : "");
        h.tvScore.setText("Score " + (entry.score > 0 ? (int)entry.score : "—"));

        String total = (media.episodes != null) ? String.valueOf(media.episodes) : "?";
        if (media.chapters != null) total = String.valueOf(media.chapters);

        h.tvProgress.setText("Progress " + entry.progress + "/" + total);
    }

    @Override public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvTitle, tvScore, tvProgress, tvFormat;
        ViewHolder(View v) {
            super(v);
            ivCover = v.findViewById(R.id.ivMediaCover);
            tvTitle = v.findViewById(R.id.tvMediaTitle);
            tvScore = v.findViewById(R.id.tvScore);
            tvProgress = v.findViewById(R.id.tvProgress);
            tvFormat = v.findViewById(R.id.tvFormat);
        }
    }
}