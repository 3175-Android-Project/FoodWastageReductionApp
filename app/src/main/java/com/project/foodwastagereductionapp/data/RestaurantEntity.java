package com.project.foodwastagereductionapp.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "restaurants",
        foreignKeys = @ForeignKey(
                entity = UserEntity.class,
                parentColumns = "id",
                childColumns = "manager_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index(value = {"manager_id"}, unique = true)}
)
final class RestaurantEntity {
    @PrimaryKey(autoGenerate = true)
    long id;
    @ColumnInfo(name = "manager_id")
    long managerId;
    String name;
    String city;
    String province;
    @ColumnInfo(name = "postal_code")
    String postalCode;
    String phone;
    @ColumnInfo(name = "cuisine_type")
    String cuisineType;
    String description;
    @ColumnInfo(name = "image_uri")
    String imageUri;
}
