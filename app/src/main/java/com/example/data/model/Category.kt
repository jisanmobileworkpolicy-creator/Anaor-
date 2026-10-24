package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey
    val id: String,
    val nameEn: String,
    val nameBn: String,
    val iconKey: String = "phone", // identifier for local icon rendering
    val displayOrder: Int = 0,
    val isVisible: Boolean = true
)
