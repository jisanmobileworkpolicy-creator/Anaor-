package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class OrderStatus(val code: String, val enLabel: String, val bnLabel: String) {
    PENDING("PENDING", "Pending", "অপেক্ষারত"),
    CONFIRMED("CONFIRMED", "Confirmed", "নিশ্চিত করা হয়েছে"),
    PROCESSING("PROCESSING", "Processing", "প্রস্তুত হচ্ছে"),
    SHIPPED("SHIPPED", "Shipped", "পাঠানো হয়েছে"),
    OUT_FOR_DELIVERY("OUT_FOR_DELIVERY", "Out for Delivery", "ডেলিভারির জন্য বের হয়েছে"),
    DELIVERED("DELIVERED", "Delivered", "ডেলিভারি সম্পন্ন"),
    CANCELLED("CANCELLED", "Cancelled", "বাতিল করা হয়েছে");

    companion object {
        fun fromCode(code: String): OrderStatus = entries.firstOrNull { it.code == code } ?: PENDING
    }
}

data class OrderItemSnapshot(
    val productId: String,
    val nameEn: String,
    val nameBn: String,
    val unitPrice: Double,
    val quantity: Int,
    val selectedColor: String = "",
    val selectedStorage: String = "",
    val condition: String = "NEW"
) {
    val total: Double get() = unitPrice * quantity
}

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey
    val orderNumber: String,
    val customerName: String,
    val customerPhone: String,
    val addressLine: String,
    val locality: String,
    val district: String = "Murshidabad",
    val state: String = "West Bengal",
    val pinCode: String,
    val itemsJson: String,
    val subtotalAmount: Double,
    val deliveryFee: Double,
    val discountAmount: Double = 0.0,
    val totalAmount: Double,
    val paymentMethod: String = "Cash on Delivery (COD)", // "Cash on Delivery (COD)" or "Prepaid UPI"
    val transactionRef: String = "", // UPI Reference UTR
    val appliedReferralCode: String = "",
    val status: String = OrderStatus.PENDING.code,
    val trackingNotes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val orderStatus: OrderStatus
        get() = OrderStatus.fromCode(status)

    val canBeCancelled: Boolean
        get() = status == OrderStatus.PENDING.code || status == OrderStatus.CONFIRMED.code
}
