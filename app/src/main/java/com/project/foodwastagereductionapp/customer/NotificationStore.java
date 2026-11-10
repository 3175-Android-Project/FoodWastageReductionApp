package com.project.foodwastagereductionapp.customer;

import java.util.ArrayList;
import java.util.List;

/**
 * Customer Orders feature
 *
 * In-memory list of notifications, newest first (replace with database later).
 * OrderStore adds an entry whenever an order's status changes.
 */
public final class NotificationStore {

    private static final List<AppNotification> NOTIFICATIONS = new ArrayList<>();

    private NotificationStore() { }

    public static void add(AppNotification notification) {
        NOTIFICATIONS.add(0, notification);
    }

    public static List<AppNotification> getAll() {
        return new ArrayList<>(NOTIFICATIONS);
    }
}
