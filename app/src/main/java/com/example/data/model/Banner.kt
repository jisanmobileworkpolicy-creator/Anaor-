package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "promotional_banners")
data class PromotionalBanner(
    @PrimaryKey
    val id: String,
    val titleEn: String,
    val titleBn: String,
    val subtitleEn: String,
    val subtitleBn: String,
    val targetCategoryId: String? = null,
    val discountTag: String = "",
    val isActive: Boolean = true,
    val displayOrder: Int = 0
)
