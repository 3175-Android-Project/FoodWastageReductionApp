package com.project.foodwastagereductionapp.manager;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.project.foodwastagereductionapp.R;
import com.project.foodwastagereductionapp.data.FoodItemEntity;
import com.project.foodwastagereductionapp.ui.ConfirmationDialog;

import java.util.ArrayList;
import java.util.List;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

    public interface OnItemClick {
        void onClick(FoodItemEntity item);
    }

    public interface OnDeleteClick {
        void onDelete(FoodItemEntity item);
    }

    public interface OnAvailabilityChange {
        void onAvailabilityChanged(FoodItemEntity item, boolean available);
    }

    private final List<FoodItemEntity> rows = new ArrayList<>();
    private final OnItemClick onItemClick;
    private final OnDeleteClick onDeleteClick;
    private final OnAvailabilityChange onAvailabilityChange;

    public FoodAdapter(OnItemClick onItemClick, OnDeleteClick onDeleteClick, OnAvailabilityChange onAvailabilityChange) {
        this.onItemClick = onItemClick;
        this.onDeleteClick = onDeleteClick;
        this.onAvailabilityChange = onAvailabilityChange;
    }

    public void setRows(List<FoodItemEntity> newRows) {
        rows.clear();
        rows.addAll(newRows);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new FoodViewHolder(inflater.inflate(R.layout.item_food_manager, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        FoodItemEntity food = rows.get(position);
        Context context = holder.itemView.getContext();

        holder.txtFoodName.setText(food.name);
        holder.txtFoodPrice.setText("$" + food.discountedPrice + " · " + food.quantityLeft + " left");

        if (food.quantityLeft <= 0) {
            holder.txtStatus.setText("Out of stock");
            holder.txtStatus.setTextColor(Color.parseColor("#C62828"));
            holder.txtStatus.setBackgroundResource(R.drawable.bg_status_badge);
            holder.txtStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFEBEE")));
            holder.txtStatus.setPadding(24, 6, 24, 6);
            if (food.available) {
                food.available = false;
                onAvailabilityChange.onAvailabilityChanged(food, false);
            }
        } else if (!food.available) {
            holder.txtStatus.setText("Unavailable");
            holder.txtStatus.setTextColor(Color.parseColor("#64748B"));
            holder.txtStatus.setBackgroundResource(R.drawable.bg_status_badge);
            holder.txtStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F1F5F9")));
            holder.txtStatus.setPadding(24, 6, 24, 6);
        } else {
            holder.txtStatus.setText("Available");
            holder.txtStatus.setTextColor(Color.parseColor("#2E7D32"));
            holder.txtStatus.setBackgroundResource(R.drawable.bg_status_badge);
            holder.txtStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E8F5E9")));
            holder.txtStatus.setPadding(24, 6, 24, 6);
        }

        if (food.imageUri != null && !food.imageUri.isEmpty()) {
            holder.imgFood.setImageURI(Uri.parse(food.imageUri));
            holder.imgFood.setImageTintList(null);
        } else {
            holder.imgFood.setImageResource(R.drawable.icon_food);
            holder.imgFood.setImageTintList(ColorStateList.valueOf(Color.parseColor("#94A3B8")));
        }

        float maxTx = 20 * context.getResources().getDisplayMetrics().density;
        updateToggleUI(holder.switchContainer, holder.switchThumb, food.available, maxTx, false);

        holder.switchContainer.setOnClickListener(v -> {
            if (food.quantityLeft <= 0) {
                ConfirmationDialog.show(
                        (Activity) context,
                        "Out of stock",
                        "This item has 0 quantity left. Please update the quantity before making it available.",
                        "OK",
                        null,
                        () -> {}
                );
                return;
            }

            boolean newState = !food.available;
            food.available = newState;
            updateToggleUI(holder.switchContainer, holder.switchThumb, newState, maxTx, true);

            if (!newState) {
                holder.txtStatus.setText("Unavailable");
                holder.txtStatus.setTextColor(Color.parseColor("#64748B"));
                holder.txtStatus.setBackgroundResource(R.drawable.bg_status_badge);
                holder.txtStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F1F5F9")));
                holder.txtStatus.setPadding(24, 6, 24, 6);
            } else {
                holder.txtStatus.setText("Available");
                holder.txtStatus.setTextColor(Color.parseColor("#2E7D32"));
                holder.txtStatus.setBackgroundResource(R.drawable.bg_status_badge);
                holder.txtStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E8F5E9")));
                holder.txtStatus.setPadding(24, 6, 24, 6);
            }

            onAvailabilityChange.onAvailabilityChanged(food, newState);
        });

        holder.itemView.setOnClickListener(v -> onItemClick.onClick(food));
        holder.btnDelete.setOnClickListener(v -> onDeleteClick.onDelete(food));
    }

    private void updateToggleUI(FrameLayout container, View thumb, boolean checked, float maxTx, boolean animate) {
        if (checked) {
            container.setBackgroundResource(R.drawable.bg_switch_track_on);
            if (animate) {
                thumb.animate().translationX(maxTx).setDuration(150).start();
            } else {
                thumb.setTranslationX(maxTx);
            }
        } else {
            container.setBackgroundResource(R.drawable.bg_switch_track_off);
            if (animate) {
                thumb.animate().translationX(0).setDuration(150).start();
            } else {
                thumb.setTranslationX(0);
            }
        }
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    static class FoodViewHolder extends RecyclerView.ViewHolder {
        final ShapeableImageView imgFood;
        final TextView txtFoodName;
        final TextView txtFoodPrice;
        final TextView txtStatus;
        final FrameLayout switchContainer;
        final View switchThumb;
        final ImageView btnDelete;

        FoodViewHolder(@NonNull View v) {
            super(v);
            imgFood = v.findViewById(R.id.imgFood);
            txtFoodName = v.findViewById(R.id.txtFoodName);
            txtFoodPrice = v.findViewById(R.id.txtFoodPrice);
            txtStatus = v.findViewById(R.id.txtStatus);
            switchContainer = v.findViewById(R.id.switchContainer);
            switchThumb = v.findViewById(R.id.switchThumb);
            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }
}
