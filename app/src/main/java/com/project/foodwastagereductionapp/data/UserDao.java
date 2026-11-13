package com.project.foodwastagereductionapp.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(UserEntity user);

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    UserEntity findById(long id);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    UserEntity findByEmail(String email);

    @Query("UPDATE users SET email_verified = 1 WHERE id = :id")
    int confirmEmail(long id);

    @Query("UPDATE users SET password_hash = :passwordHash WHERE email = :email")
    int updatePassword(String email, String passwordHash);

    @Query("DELETE FROM users WHERE id = :id")
    int deleteById(long id);

    @Query("SELECT COUNT(*) FROM users")
    int count();
}
