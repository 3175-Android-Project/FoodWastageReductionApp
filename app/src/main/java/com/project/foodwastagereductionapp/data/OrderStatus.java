package com.project.foodwastagereductionapp.data;

import com.project.foodwastagereductionapp.R;

/**
 * Customer Orders feature
 *
 * Single place that defines the order statuses, their order in the flow
 * (Pending -> Accepted -> Preparing -> Ready) and how each one is coloured.
 */
public final class OrderStatus {

    public static final String PENDING = "Pending";
    public static final String ACCEPTED = "Accepted";
    public static final String PREPARING = "Preparing";
    public static final String READY = "Ready";
    public static final String COMPLETED = "Completed";
    public static final String REJECTED = "Rejected";
    public static final String CANCELLED = "Cancelled";

    private static final String[] STEPS = {PENDING, ACCEPTED, PREPARING, READY};

    private OrderStatus() { }

    /** The four steps shown on the Order Status screen, in order. */
    public static String[] steps() {
        return STEPS.clone();
    }

    /** True while the order is still in progress (shown under "Current orders"). */
    public static boolean isActive(String status) {
        return indexInFlow(status) >= 0;
    }

    /** Rejected or cancelled orders. */
    public static boolean isNegative(String status) {
        return REJECTED.equals(status) || CANCELLED.equals(status);
    }

    /** A customer can only cancel before the restaurant accepts the order. */
    public static boolean canCancel(String status) {
        return PENDING.equals(status);
    }

    /** Index of the status in the 4-step flow (a completed order counts as the last step). */
    public static int stepIndex(String status) {
        if (COMPLETED.equals(status)) return STEPS.length - 1;
        return Math.max(indexInFlow(status), 0);
    }

    public static int textColor(String status) {
        return isNegative(status) ? R.color.text_warning : R.color.text_success;
    }

    public static int pillBackground(String status) {
        return isNegative(status) ? R.color.pill_red_bg : R.color.pill_green_bg;
    }

    private static int indexInFlow(String status) {
        for (int i = 0; i < STEPS.length; i++) {
            if (STEPS[i].equals(status)) return i;
        }
        return -1;
    }
}
