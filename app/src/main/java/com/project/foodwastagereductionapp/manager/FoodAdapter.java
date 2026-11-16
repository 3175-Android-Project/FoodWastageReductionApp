package com.project.foodwastagereductionapp.manager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.foodwastagereductionapp.R;
import com.project.foodwastagereductionapp.data.FoodItemEntity;

import java.util.ArrayList;
import java.util.List;

public class FoodAdapter
        extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

    public interface OnEditClick {
        void onEdit(FoodItemEntity item);
    }

    public interface OnDeleteClick {
        void onDelete(FoodItemEntity item);
    }

    public interface OnAvailabilityChange {
        void onAvailabilityChanged(
                FoodItemEntity item,
                boolean available
        );
    }

    private final List<FoodItemEntity> rows = new ArrayList<>();
    private final OnDeleteClick onDeleteClick;
    private final OnAvailabilityChange onAvailabilityChange;

    public FoodAdapter(
            OnDeleteClick onDeleteClick,
            OnAvailabilityChange onAvailabilityChange) {

        this.onDeleteClick = onDeleteClick;
        this.onAvailabilityChange = onAvailabilityChange;
    }

    public void setRows(
            List<FoodItemEntity> newRows) {
        rows.clear();
        rows.addAll(newRows);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new FoodViewHolder(inflater.inflate(R.layout.item_food, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull FoodViewHolder h,
            int position) {
        FoodItemEntity food = rows.get(position);
        h.txtFoodName.setText(food.name);
        h.txtFoodPrice.setText("$" + food.discountedPrice);
        h.txtQuantity.setText("Qty Left: " + food.quantityLeft);
        h.switchAvailable.setChecked(food.available);
        h.btnEdit.setOnClickListener(v ->
                onEditClick.onEdit(food));
        h.btnDelete.setOnClickListener(v ->
                onDeleteClick.onDelete(food));
        h.switchAvailable.setOnCheckedChangeListener((buttonView, isChecked) ->
                                onAvailabilityChange.onAvailabilityChanged(food, isChecked));
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    static class FoodViewHolder
            extends RecyclerView.ViewHolder {
        final TextView txtFoodName;
        final TextView txtFoodPrice;
        final TextView txtQuantity;
        final Switch switchAvailable;
        final ImageView btnEdit;
        final ImageView btnDelete;

        FoodViewHolder(
                @NonNull View v) {
            super(v);

            txtFoodName = v.findViewById(R.id.txtFoodName);
            txtFoodPrice = v.findViewById(R.id.txtFoodPrice);
            txtQuantity = v.findViewById(R.id.txtQuantity);
            switchAvailable = v.findViewById(R.id.switchAvailable);
            btnEdit = v.findViewById(R.id.btnEdit);
            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }
}