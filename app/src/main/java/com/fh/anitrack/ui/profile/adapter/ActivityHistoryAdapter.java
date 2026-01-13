package com.fh.anitrack.ui.profile.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fh.anitrack.R;
import com.fh.anitrack.api.response.UserStatsResponse;

import java.util.List;

public class ActivityHistoryAdapter extends RecyclerView.Adapter<ActivityHistoryAdapter.ViewHolder> {

    private final List<UserStatsResponse.HistoryItem> dayList;

    public ActivityHistoryAdapter(List<UserStatsResponse.HistoryItem> dayList) {
        this.dayList = dayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_activity_square, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int amount = dayList.get(position).amount;
        int color;

        if (amount == 0) color = Color.parseColor("#FFFFFF");
        else if (amount < 3) color = Color.parseColor("#9915185F");
        else if (amount < 6) color = Color.parseColor("#CC15185F");
        else color = Color.parseColor("#15185F");

        holder.square.getBackground().setTint(color);
    }

    @Override
    public int getItemCount() { return dayList.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View square;
        ViewHolder(View v) { super(v); square = v.findViewById(R.id.activitySquare); }
    }
}