package com.project.foodwastagereductionapp.manager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.foodwastagereductionapp.R;
import com.project.foodwastagereductionapp.data.OrderEntity;

import java.util.ArrayList;
import java.util.List;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder> {

    private final List<OrderEntity> rows = new ArrayList<>();

    public void setRows(List<OrderEntity> newRows) {
        rows.clear();

        rows.addAll(newRows);

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        return new OrderViewHolder(inflater.inflate(R.layout.item_order_history, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderEntity order = rows.get(position);

        Context context = holder.itemView.getContext();

        holder.txtOrderNumber.setText("Order #" + order.orderNumber);

        holder.txtDateItems.setText(order.date + " • " + order.itemCount + " item(s)");

        holder.txtTotal.setText("$" + order.total);

        holder.txtStatus.setText(order.status);

        if ("Picked up".equals(order.status) || "Delivered".equals(order.status)) {

            holder.txtStatus.setTextColor(Color.parseColor("#4CAF50"));

        } else if ("Rejected".equals(order.status) || "Cancelled".equals(order.status)) {

            holder.txtStatus.setTextColor(Color.RED);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, OrderHistoryDetailActivity.class);
            intent.putExtra("orderId", order.id);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        final TextView txtOrderNumber;
        final TextView txtDateItems;
        final TextView txtTotal;
        final TextView txtStatus;

        OrderViewHolder(@NonNull View v) {
            super(v);
            txtOrderNumber = v.findViewById(R.id.txtOrderNumber);

            txtDateItems = v.findViewById(R.id.txtDateItems);

            txtTotal = v.findViewById(R.id.txtTotal);

            txtStatus = v.findViewById(R.id.txtStatus);
        }
    }
}
