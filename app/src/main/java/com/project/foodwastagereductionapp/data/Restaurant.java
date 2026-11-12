package com.project.foodwastagereductionapp.data;

/** One restaurant per manager, linked by managerId rather than email. */
public final class Restaurant {
    public final long id, managerId;
    public final String name, city, province, postalCode, phone, cuisineType, description;

    public Restaurant(long id, long managerId, String name, String city, String province,
                      String postalCode, String phone, String cuisineType, String description) {
        this.id = id;
        this.managerId = managerId;
        this.name = name;
        this.city = city;
        this.province = province;
        this.postalCode = postalCode;
        this.phone = phone;
        this.cuisineType = cuisineType;
        this.description = description;
    }
}
