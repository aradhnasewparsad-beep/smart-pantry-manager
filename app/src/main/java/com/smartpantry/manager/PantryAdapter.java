package com.smartpantry.manager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

    public interface OnItemActionListener {
        void onEdit(PantryItem item);
        void onDelete(PantryItem item);
    }

    private List<PantryItem> items;
    private OnItemActionListener listener;

    public PantryAdapter(List<PantryItem> items, OnItemActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void setItems(List<PantryItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);
        return new PantryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        PantryItem item = items.get(position);
        holder.tvItemName.setText(item.getName());

        String details = "Qty: " + item.getQuantity() + " " + item.getUnit();
        if (item.getExpiryDate() != null && !item.getExpiryDate().isEmpty()) {
            details += " • Expires: " + item.getExpiryDate();
        }
        holder.tvItemDetails.setText(details);

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(item));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class PantryViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemName, tvItemDetails;
        ImageButton btnEdit, btnDelete;

        public PantryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvItemDetails = itemView.findViewById(R.id.tvItemDetails);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}