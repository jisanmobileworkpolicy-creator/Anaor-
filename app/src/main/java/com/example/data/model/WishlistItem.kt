package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wishlist_items")
data class WishlistItem(
    @PrimaryKey
    val productId: String,
    val addedAt: Long = System.currentTimeMillis()
)
