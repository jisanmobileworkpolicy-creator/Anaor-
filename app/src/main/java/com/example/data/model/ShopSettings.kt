package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shop_settings")
data class ShopSettings(
    @PrimaryKey
    val id: String = "primary_settings",
    val shopName: String = "JISAN MOBILE",
    val brandSloganBn: String = "মোবাইল মানেই জিসান মোবাইল",
    val brandSloganEn: String = "Mobile Manei Jisan Mobile",
    val shopAddress: String = "Baromasia Hajir Hut, Sagar Para, Murshidabad, West Bengal, India",
    val primaryPhone: String = "+919382302242",
    val secondaryPhone: String = "+918918933017",
    val whatsappNumber: String = "+919382302242",
    // Configurable Delivery charges based on order amount
    val deliveryFee: Double = 40.0,
    val freeDeliveryAbove: Double = 499.0,
    // Payment Options: COD & Prepaid UPI
    val isCodEnabled: Boolean = true,
    val isPrepaidUpiEnabled: Boolean = true,
    val upiId: String = "9382302242@upi",
    val upiName: String = "JISAN MOBILE",
    // Admin Credentials
    val adminUsername: String = "jisanadmin",
    val adminPassword: String = "jisan786",
    val adminMobile: String = "+919382302242",
    val adminPin: String = "7860",
    // Customer Referral discount
    val referralDiscountAmount: Double = 100.0,
    val announcementBn: String = "সব ধরনের নতুন ও সেকেন্ড-হ্যান্ড মোবাইলে বিশেষ ছাড়!",
    val announcementEn: String = "Special discount on all new & second-hand mobile phones!",
    val returnPolicyBn: String = "৭ দিনের প্রতিস্থাপন বা রিটার্ন গ্যারান্টি (শর্ত প্রযোজ্য)। পণ্য যাচাই করে ডেলিভারি নেওয়া যাবে।",
    val returnPolicyEn: String = "7-day replacement or return guarantee (terms apply). Inspect before accepting delivery."
)
