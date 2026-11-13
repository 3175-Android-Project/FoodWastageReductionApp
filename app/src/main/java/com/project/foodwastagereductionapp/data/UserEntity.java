package com.project.foodwastagereductionapp.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "users",
        indices = {@Index(value = {"email"}, unique = true)}
)
final class UserEntity {
    @PrimaryKey(autoGenerate = true)
    long id;
    @ColumnInfo(name = "email")
    String email;
    @ColumnInfo(name = "password_hash")
    String passwordHash;
    String role;
    @ColumnInfo(name = "full_name")
    String fullName;
    String city;
    String province;
    @ColumnInfo(name = "postal_code")
    String postalCode;
    @ColumnInfo(name = "email_verified")
    boolean emailVerified;
}
