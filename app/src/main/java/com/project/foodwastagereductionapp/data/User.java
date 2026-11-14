package com.project.foodwastagereductionapp.data;

/** Profile data returned to screens does not include password hashes. */
public final class User {
    public static final String CUSTOMER = "customer";
    public static final String MANAGER = "manager";
    public final long id;
    public final String email, role, fullName, city, province, postalCode;
    public final boolean emailVerified;
    public final String address, phone; // may be null or empty

    public User(long id, String email, String role, String fullName, String city,
                String province, String postalCode, boolean emailVerified,
                String address, String phone) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.fullName = fullName;
        this.city = city;
        this.province = province;
        this.postalCode = postalCode;
        this.emailVerified = emailVerified;
        this.address = address;
        this.phone = phone;
    }

    /** Old constructor kept so existing code still compiles (no address / phone). */
    public User(long id, String email, String role, String fullName, String city,
                String province, String postalCode, boolean emailVerified) {
        this(id, email, role, fullName, city, province, postalCode, emailVerified, null, null);
    }

    public boolean hasAddress() {
        return address != null && !address.trim().isEmpty();
    }

    /** "405 Main Street, Vancouver" - used as the delivery address. */
    public String getAddressLine() {
        if (!hasAddress()) return city;
        return address.trim() + ", " + city;
    }
}