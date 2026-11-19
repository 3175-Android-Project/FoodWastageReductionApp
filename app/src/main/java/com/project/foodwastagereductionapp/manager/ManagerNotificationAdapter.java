package com.project.foodwastagereductionapp.manager;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.project.foodwastagereductionapp.R;
import com.project.foodwastagereductionapp.data.AppNotification;
import com.project.foodwastagereductionapp.data.OrderStatus;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ManagerNotificationAdapter extends RecyclerView.Adapter<ManagerNotificationAdapter.ViewHolder> {

    private final List<AppNotification> notifications;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("MMM d, h:mm a", Locale.US);

    public ManagerNotificationAdapter(List<AppNotification> notifications) {
        this.notifications = notifications;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        AppNotification n = notifications.get(position);
        Context context = h.itemView.getContext();

        h.txtTitle.setText(n.status);
        h.txtMessage.setText("Order #" + n.orderId + " · " + n.restaurantName);
        h.txtTime.setText(timeFormat.format(new Date(n.timeMillis)));

        boolean negative = OrderStatus.isNegative(n.orderStatus);
        h.imgIcon.setImageResource(R.drawable.icon_receipt);
        h.imgIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context,
                negative ? R.color.text_warning : R.color.background_accent1)));
        h.imgIcon.setBackgroundTintList(ContextCompat.getColorStateList(context,
                negative ? R.color.background_warning : R.color.background_accent2));

        h.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, OrderHistoryDetailActivity.class);
            intent.putExtra("orderId", n.orderId);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView imgIcon;
        final TextView txtTitle, txtMessage, txtTime;

        ViewHolder(@NonNull View v) {
            super(v);
            imgIcon = v.findViewById(R.id.imgNotificationIcon);
            txtTitle = v.findViewById(R.id.txtNotificationTitle);
            txtMessage = v.findViewById(R.id.txtNotificationMessage);
            txtTime = v.findViewById(R.id.txtNotificationTime);
        }
    }
}
