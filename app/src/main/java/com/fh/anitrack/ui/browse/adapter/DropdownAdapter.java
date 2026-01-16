package com.fh.anitrack.ui.browse.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fh.anitrack.R;
import com.fh.anitrack.mockData.model.FilterOption;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying filter options in dropdown dialogs.
 */
public class DropdownAdapter extends RecyclerView.Adapter<DropdownAdapter.ViewHolder> {

    private List<FilterOption> options = new ArrayList<>();
    private OnOptionSelectedListener listener;

    public interface OnOptionSelectedListener {
        void onOptionSelected(FilterOption option, int position);
    }

    public void setOnOptionSelectedListener(OnOptionSelectedListener listener) {
        this.listener = listener;
    }

    public void setOptions(List<FilterOption> options) {
        this.options = options != null ? options : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.browse_item_dropdown_option, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FilterOption option = options.get(position);
        holder.bind(option);
    }

    @Override
    public int getItemCount() {
        return options.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView optionText;

        ViewHolder(View itemView) {
            super(itemView);
            optionText = itemView.findViewById(R.id.optionText);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        listener.onOptionSelected(options.get(pos), pos);
                    }
                }
            });
        }

        void bind(FilterOption option) {
            optionText.setText(option.getDisplayName());
        }
    }
}
