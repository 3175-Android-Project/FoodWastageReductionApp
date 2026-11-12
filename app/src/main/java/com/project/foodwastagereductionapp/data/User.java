package com.project.foodwastagereductionapp.data;

/** Profile data returned to screens does not include password hashes. */
public final class User {
    public static final String CUSTOMER = "customer";
    public static final String MANAGER = "manager";
    public final long id;
    public final String email, role, fullName, city, province, postalCode;
    public final boolean emailVerified;

    public User(long id, String email, String role, String fullName, String city,
                String province, String postalCode, boolean emailVerified) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.fullName = fullName;
        this.city = city;
        this.province = province;
        this.postalCode = postalCode;
        this.emailVerified = emailVerified;
    }
}
