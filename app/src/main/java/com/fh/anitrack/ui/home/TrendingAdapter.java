package com.fh.anitrack.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.fh.anitrack.R;
import com.fh.anitrack.api.response.ActivityResponse;

import java.util.ArrayList;
import java.util.List;

public class TrendingAdapter extends RecyclerView.Adapter<TrendingAdapter.ViewHolder> {
    private final List<ActivityResponse.Media> mediaList = new ArrayList<>();

    // used for pagination to add new items to the bottom of the list
    public void addItems(List<ActivityResponse.Media> newItems) {
        int start = mediaList.size();
        mediaList.addAll(newItems);
        notifyItemRangeInserted(start, newItems.size());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trending, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ActivityResponse.Media media = mediaList.get(position);
        holder.title.setText(media.title.userPreferred);
        Glide.with(holder.itemView).load(media.coverImage.large).into(holder.image);
    }

    @Override
    public int getItemCount() { return mediaList.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;
        ViewHolder(View v) {
            super(v);
            image = v.findViewById(R.id.trendingImage);
            title = v.findViewById(R.id.trendingTitle);
        }
    }
}