package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CustomerProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customer_profile WHERE id = 'active_user' LIMIT 1")
    fun getProfile(): Flow<CustomerProfile?>

    @Query("SELECT * FROM customer_profile WHERE id = 'active_user' LIMIT 1")
    suspend fun getProfileSync(): CustomerProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: CustomerProfile)

    @Update
    suspend fun updateProfile(profile: CustomerProfile)

    @Query("UPDATE customer_profile SET isLoggedIn = 0 WHERE id = 'active_user'")
    suspend fun logout()
}
