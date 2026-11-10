package com.project.foodwastagereductionapp.customer;

/**
 * Customer Profile feature
 *
 * In-memory profile of the logged-in customer (replace with the real account / database later).
 * Shown on the Profile and Order Detail screens and edited on the Edit Profile screen.
 */
public final class CustomerProfile {

    // Sample data until real accounts exist
    public static String fullName = "Customer Park";
    public static String email = "customer@email.com";
    public static String address = "405 Main Street";
    public static String city = "Vancouver";
    public static String province = "BC";
    public static String postalCode = "V6B 1A1";
    public static String phone = "604-555-0123";

    private CustomerProfile() { }

    /** "405 Main Street, Vancouver" - empty parts are skipped. */
    public static String getAddressLine() {
        StringBuilder sb = new StringBuilder();
        appendPart(sb, address);
        appendPart(sb, city);
        return sb.toString();
    }

    private static void appendPart(StringBuilder sb, String part) {
        if (part == null || part.trim().isEmpty()) return;
        if (sb.length() > 0) sb.append(", ");
        sb.append(part.trim());
    }
}
