package com.project.foodwastagereductionapp.customer;

import com.project.foodwastagereductionapp.data.FoodItem;
import com.project.foodwastagereductionapp.data.CartItem;
import com.project.foodwastagereductionapp.data.Order;
import com.project.foodwastagereductionapp.data.OrderStatus;
import com.project.foodwastagereductionapp.data.AppNotification;


import android.content.Context;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private final List<AppNotification> notifications;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("MMM d, h:mm a", Locale.US);

    public NotificationAdapter(List<AppNotification> notifications) {
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

        h.txtTitle.setText(titleFor(n.status));

        // "Your order is ready!" doubles as the pickup reminder, so it also says when to collect
        if (OrderStatus.READY.equals(n.status)) {
            h.txtMessage.setText(context.getString(R.string.fmtNotificationReady,
                    n.restaurantName, n.pickupTime));
        } else {
            h.txtMessage.setText(n.restaurantName);
        }
        h.txtTime.setText(timeFormat.format(new Date(n.timeMillis)));

        // Rejected is shown in red, everything else in the app's green
        boolean negative = OrderStatus.isNegative(n.status);
        h.imgIcon.setImageResource(iconFor(n.status));
        h.imgIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context,
                negative ? R.color.text_warning : R.color.background_accent1)));
        h.imgIcon.setBackgroundTintList(ContextCompat.getColorStateList(context,
                negative ? R.color.background_warning : R.color.background_accent2));

        h.itemView.setOnClickListener(v -> OrderNavigator.open(v.getContext(), n.orderId, n.orderStatus));
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    private static int titleFor(String status) {
        switch (status) {
            case OrderStatus.ACCEPTED:  return R.string.notifOrderAccepted;
            case OrderStatus.PREPARING: return R.string.notifOrderPreparing;
            case OrderStatus.READY:     return R.string.notifOrderReady;
            case OrderStatus.REJECTED:  return R.string.notifOrderRejected;
            default:                    return R.string.notifOrderCompleted;
        }
    }

    private static int iconFor(String status) {
        switch (status) {
            case OrderStatus.ACCEPTED: return R.drawable.icon_check;
            case OrderStatus.REJECTED: return R.drawable.icon_warning;
            default:                   return R.drawable.icon_package;
        }
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
