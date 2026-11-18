package com.project.foodwastagereductionapp.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/** Only a hash of the short-lived reset token is persisted. */
@Entity(tableName = "password_reset_grants", foreignKeys = @ForeignKey(
        entity = UserEntity.class, parentColumns = "id", childColumns = "user_id",
        onDelete = ForeignKey.CASCADE), indices = @Index(value = "user_id", unique = true))
final class PasswordResetGrantEntity {
    @PrimaryKey @NonNull @ColumnInfo(name = "token_hash")
    String tokenHash = "";
    @ColumnInfo(name = "user_id") long userId;
    @ColumnInfo(name = "issued_at") long issuedAt;
    @ColumnInfo(name = "expires_at") long expiresAt;
}
