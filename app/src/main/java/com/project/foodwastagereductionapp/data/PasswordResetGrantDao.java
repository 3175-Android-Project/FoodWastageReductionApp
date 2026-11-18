package com.project.foodwastagereductionapp.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
interface PasswordResetGrantDao {
    @Insert void insert(PasswordResetGrantEntity grant);
    @Query("SELECT * FROM password_reset_grants WHERE token_hash = :hash AND user_id = :userId "
            + "AND issued_at <= :now AND expires_at > :now LIMIT 1")
    PasswordResetGrantEntity findValid(String hash, long userId, long now);
    @Query("DELETE FROM password_reset_grants WHERE user_id = :userId")
    void deleteForUser(long userId);
    @Query("DELETE FROM password_reset_grants WHERE expires_at <= :now")
    void deleteExpired(long now);
}
