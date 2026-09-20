package com.example.weighttracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Binds each Weight to a row in the grid. A tap on the row edits it and the
 * trash button deletes it, both handled by the activity through RowListener.
 */
public class WeightAdapter extends RecyclerView.Adapter<WeightAdapter.WeightViewHolder> {

    // Lets the activity respond to row edit and delete actions
    public interface RowListener {
        void onEdit(Weight weight);
        void onDelete(Weight weight);
    }

    private final List<Weight> weights;
    private final RowListener listener;

    public WeightAdapter(List<Weight> weights, RowListener listener) {
        this.weights = weights;
        this.listener = listener;
    }

    @NonNull
    @Override
    public WeightViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_weight, parent, false);
        return new WeightViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull WeightViewHolder holder, int position) {
        Weight weight = weights.get(position);
        holder.date.setText(weight.getDate());
        holder.value.setText(String.valueOf(weight.getValue()));

        holder.itemView.setOnClickListener(v -> listener.onEdit(weight));
        holder.delete.setOnClickListener(v -> listener.onDelete(weight));
    }

    @Override
    public int getItemCount() {
        return weights.size();
    }

    static class WeightViewHolder extends RecyclerView.ViewHolder {
        TextView date;
        TextView value;
        ImageButton delete;

        WeightViewHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.rowDate);
            value = itemView.findViewById(R.id.rowWeight);
            delete = itemView.findViewById(R.id.rowDelete);
        }
    }
}
