package com.project.foodwastagereductionapp.data;

/** One tax rule for Checkout and for every saved order, so the screens always agree. */
public final class OrderPricing {
    /** GST on restaurant food in BC. */
    public static final double TAX_RATE = 0.05;

    private OrderPricing() { }

    public static double roundToCents(double value) {
        return Math.round(value * 100) / 100.0;
    }

    public static double taxOf(double subtotal) {
        return roundToCents(subtotal * TAX_RATE);
    }

    public static double totalWithTax(double subtotal) {
        return roundToCents(subtotal + taxOf(subtotal));
    }
}
