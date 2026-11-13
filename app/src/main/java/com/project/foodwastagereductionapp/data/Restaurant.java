package com.project.foodwastagereductionapp.data;

/** One restaurant per manager, linked by managerId rather than email. */
public final class Restaurant {

    public final long id;
    public final long managerId;
    public final String name;
    public final String city;
    public final String province;
    public final String postalCode;
    public final String phone;
    public final String cuisineType;
    public final String description;
    public final String imageUri;

    public Restaurant(
            long id,
            long managerId,
            String name,
            String city,
            String province,
            String postalCode,
            String phone,
            String cuisineType,
            String description,
            String imageUri
    ) {
        this.id = id;
        this.managerId = managerId;
        this.name = name;
        this.city = city;
        this.province = province;
        this.postalCode = postalCode;
        this.phone = phone;
        this.cuisineType = cuisineType;
        this.description = description;
        this.imageUri = imageUri;
    }
}
