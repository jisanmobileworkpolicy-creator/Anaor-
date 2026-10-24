package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PromotionalBanner
import kotlinx.coroutines.flow.Flow

@Dao
interface BannerDao {
    @Query("SELECT * FROM promotional_banners WHERE isActive = 1 ORDER BY displayOrder ASC")
    fun getActiveBanners(): Flow<List<PromotionalBanner>>

    @Query("SELECT * FROM promotional_banners ORDER BY displayOrder ASC")
    fun getAllBanners(): Flow<List<PromotionalBanner>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBanner(banner: PromotionalBanner)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBanners(banners: List<PromotionalBanner>)

    @Update
    suspend fun updateBanner(banner: PromotionalBanner)

    @Delete
    suspend fun deleteBanner(banner: PromotionalBanner)

    @Query("SELECT COUNT(*) FROM promotional_banners")
    suspend fun getBannerCount(): Int
}
