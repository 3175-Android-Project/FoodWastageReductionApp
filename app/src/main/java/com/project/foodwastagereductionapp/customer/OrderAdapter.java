package com.project.foodwastagereductionapp.customer;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.project.foodwastagereductionapp.R;

import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private final List<Order> orders;

    public OrderAdapter(List<Order> orders) {
        this.orders = orders;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder h, int position) {
        Order o = orders.get(position);

        h.imgRestaurant.setImageResource(o.imageRes);
        h.txtRestaurantName.setText(o.restaurantName);
        h.txtOrderNumber.setText("Order #" + o.orderNumber);

        String items = o.itemCount + (o.itemCount == 1 ? " item" : " items");
        h.txtDateItems.setText(o.date + " · " + items);
        h.txtTotal.setText(String.format(Locale.CANADA, "$%.2f", o.total));

        // Status text + colour
        h.txtStatus.setText(o.status);
        int colorRes;
        switch (o.status) {
            case "Completed": colorRes = R.color.text_success; break; // green primary
            case "Rejected":  colorRes = R.color.text_warning; break; // red
            default:          colorRes = R.color.text_tertiary; break; // grey (Pending)
        }
        h.txtStatus.setTextColor(ContextCompat.getColor(h.itemView.getContext(), colorRes));

        // Open detail screen
        h.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), CustomerOrderDetailActivity.class);
            intent.putExtra("orderNumber", o.orderNumber);
            intent.putExtra("restaurantName", o.restaurantName);
            intent.putExtra("date", o.date);
            intent.putExtra("status", o.status);
            intent.putExtra("total", o.total);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        ImageView imgRestaurant;
        TextView txtRestaurantName, txtStatus, txtOrderNumber, txtDateItems, txtTotal;

        OrderViewHolder(@NonNull View v) {
            super(v);
            imgRestaurant = v.findViewById(R.id.imgRestaurant);
            txtRestaurantName = v.findViewById(R.id.txtRestaurantName);
            txtStatus = v.findViewById(R.id.txtStatus);
            txtOrderNumber = v.findViewById(R.id.txtOrderNumber);
            txtDateItems = v.findViewById(R.id.txtDateItems);
            txtTotal = v.findViewById(R.id.txtTotal);
        }
    }
}
