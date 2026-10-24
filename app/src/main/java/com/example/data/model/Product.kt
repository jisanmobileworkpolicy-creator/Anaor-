package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ProductCondition {
    NEW,
    SECOND_HAND
}

@Entity(tableName = "products")
data class Product(
    @PrimaryKey
    val id: String,
    val nameEn: String,
    val nameBn: String,
    val categoryId: String,
    val brand: String,
    val model: String,
    val condition: String = ProductCondition.NEW.name, // "NEW" or "SECOND_HAND"
    val price: Double,
    val mrp: Double,
    val stock: Int = 1,
    val imageUrl: String = "",
    val isAvailable: Boolean = true,
    val isBestSeller: Boolean = false,
    val isNewArrival: Boolean = false,
    val descriptionEn: String = "",
    val descriptionBn: String = "",
    // Second-hand specific inspection details
    val batteryHealth: Int? = null,
    val cosmeticCondition: String? = null,
    val includedAccessories: String? = null,
    val warrantyMonths: Int? = null,
    // Variants & Specs
    val colorVariants: String = "",
    val storageVariants: String = "",
    val warrantyInfo: String = "",
    val returnPolicy: String = "",
    val isDemoProduct: Boolean = false
) {
    val isSecondHand: Boolean
        get() = condition == ProductCondition.SECOND_HAND.name

    val discountPercentage: Int
        get() = if (mrp > price && mrp > 0) {
            (((mrp - price) / mrp) * 100).toInt()
        } else 0

    val isInStock: Boolean
        get() = isAvailable && stock > 0

    val parsedColors: List<String>
        get() = colorVariants.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    val parsedStorages: List<String>
        get() = storageVariants.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    val displayImageUrl: String
        get() {
            val backend = com.example.data.remote.JisanApiService.getActiveBackendUrl()
            return when {
                imageUrl.isBlank() -> ""
                imageUrl.startsWith("http://") || imageUrl.startsWith("https://") -> {
                    if (imageUrl.contains("ais-dev-") && backend.isNotBlank() && !backend.contains("ais-dev-")) {
                        val path = imageUrl.substringAfter(".app")
                        "$backend$path"
                    } else {
                        imageUrl
                    }
                }
                backend.isNotBlank() -> {
                    if (imageUrl.startsWith("/")) "$backend$imageUrl" else "$backend/$imageUrl"
                }
                else -> imageUrl
            }
        }
}
