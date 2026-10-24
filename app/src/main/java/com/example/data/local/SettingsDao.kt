package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ShopSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Query("SELECT * FROM shop_settings WHERE id = 'primary_settings' LIMIT 1")
    fun getSettings(): Flow<ShopSettings?>

    @Query("SELECT * FROM shop_settings WHERE id = 'primary_settings' LIMIT 1")
    suspend fun getSettingsSync(): ShopSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: ShopSettings)

    @Update
    suspend fun update(settings: ShopSettings)
}
