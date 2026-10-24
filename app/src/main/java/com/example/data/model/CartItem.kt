package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey
    val productId: String,
    val quantity: Int = 1,
    val selectedColor: String = "",
    val selectedStorage: String = "",
    val addedAt: Long = System.currentTimeMillis()
)

data class CartItemDetail(
    val cartItem: CartItem,
    val product: Product
) {
    val itemTotal: Double
        get() = product.price * cartItem.quantity
}
