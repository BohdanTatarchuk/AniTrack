package com.fh.anitrack.ui.profile.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fh.anitrack.R;
import com.fh.anitrack.api.response.FavoritesResponse;

import java.util.ArrayList;
import java.util.List;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.ViewHolder> {
    private final List<FavoritesResponse.FavoriteNode> list = new ArrayList<>();

    public void addItems(List<FavoritesResponse.FavoriteNode> items) {
        int start = list.size();
        list.addAll(items);
        notifyItemRangeInserted(start, items.size());
    }

    public void clear() {
        list.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_favorite, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FavoritesResponse.FavoriteNode node = list.get(position);
        String label = "";

        if (node.title != null) {
            label = node.title.userPreferred + (node.format != null ? " (" + node.format + ")" : "");
        } else if (node.name != null) {
            label = node.name.full;
        }

        holder.tvName.setText(label);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;

        ViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvFavoriteName);
        }
    }
}