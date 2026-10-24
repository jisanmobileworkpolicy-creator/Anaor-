package com.example.util

enum class AppLanguage(val code: String, val displayName: String) {
    BENGALI("bn", "বাংলা"),
    ENGLISH("en", "English")
}

object Strings {
    fun appName(): String = "JISAN MOBILE"

    fun slogan(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "মোবাইল মানেই জিসান মোবাইল"
        AppLanguage.ENGLISH -> "Mobile Manei Jisan Mobile"
    }

    fun shopLocation(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "বারোমাসিয়া হাজির হাট, সাগরপাড়া, মুর্শিদাবাদ"
        AppLanguage.ENGLISH -> "Baromasia Hajir Hut, Sagar Para, Murshidabad"
    }

    fun searchPlaceholder(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "মোবাইল বা অ্যাক্সেসরিজ খুঁজুন..."
        AppLanguage.ENGLISH -> "Search mobiles or accessories..."
    }

    // Tabs
    fun navHome(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "হোম"
        AppLanguage.ENGLISH -> "Home"
    }
    fun navCategories(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "ক্যাটাগরি"
        AppLanguage.ENGLISH -> "Categories"
    }
    fun navCart(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "কার্ট"
        AppLanguage.ENGLISH -> "Cart"
    }
    fun navOrders(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "অর্ডার"
        AppLanguage.ENGLISH -> "Orders"
    }
    fun navAccount(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "অ্যাকাউন্ট"
        AppLanguage.ENGLISH -> "Account"
    }

    // Section Titles
    fun secNewMobiles(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "নতুন স্মার্টফোন"
        AppLanguage.ENGLISH -> "New Smartphones"
    }
    fun secSecondHand(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "সেকেন্ড-হ্যান্ড মোবাইল"
        AppLanguage.ENGLISH -> "Second-Hand Mobiles"
    }
    fun secBestSellers(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "জনপ্রিয় ও বেস্ট সেলার"
        AppLanguage.ENGLISH -> "Best Sellers"
    }
    fun secAccessories(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "মোবাইল অ্যাক্সেসরিজ"
        AppLanguage.ENGLISH -> "Mobile Accessories"
    }
    fun secAllProducts(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "সব পণ্য"
        AppLanguage.ENGLISH -> "All Products"
    }
    fun secViewAll(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "সব দেখুন"
        AppLanguage.ENGLISH -> "View All"
    }

    // Product labels
    fun brandNew(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "ব্র্যান্ড নিউ"
        AppLanguage.ENGLISH -> "Brand New"
    }
    fun secondHand(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "সেকেন্ড-হ্যান্ড"
        AppLanguage.ENGLISH -> "Second-Hand"
    }
    fun inStock(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "স্টকে আছে"
        AppLanguage.ENGLISH -> "In Stock"
    }
    fun outOfStock(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "স্টক শেষ"
        AppLanguage.ENGLISH -> "Out of Stock"
    }
    fun addToCart(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "কার্টে যোগ করুন"
        AppLanguage.ENGLISH -> "Add to Cart"
    }
    fun addedToCart(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "কার্টে যোগ করা হয়েছে"
        AppLanguage.ENGLISH -> "Added to Cart"
    }
    fun buyNow(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "এখনই কিনুন"
        AppLanguage.ENGLISH -> "Buy Now"
    }
    fun batteryHealth(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "ব্যাটারি হেলথ"
        AppLanguage.ENGLISH -> "Battery Health"
    }
    fun cosmeticCondition(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "বাহ্যিক অবস্থা"
        AppLanguage.ENGLISH -> "Cosmetic Condition"
    }
    fun includedAccessories(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "বক্সে যা পাবেন"
        AppLanguage.ENGLISH -> "Included Items"
    }
    fun shopWarranty(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "ওয়ারেন্টি সুবিধা"
        AppLanguage.ENGLISH -> "Warranty Terms"
    }

    // Cart & Checkout
    fun cartEmptyTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "আপনার কার্ট খালি"
        AppLanguage.ENGLISH -> "Your Cart is Empty"
    }
    fun cartEmptyDesc(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "আপনার পছন্দের মোবাইল বা গ্যাজেট কার্টে যোগ করুন।"
        AppLanguage.ENGLISH -> "Explore our collection and add items to your cart."
    }
    fun startShopping(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "কেনাকাটা শুরু করুন"
        AppLanguage.ENGLISH -> "Start Shopping"
    }
    fun orderSummary(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "অর্ডার সারাংশ"
        AppLanguage.ENGLISH -> "Order Summary"
    }
    fun itemsSubtotal(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "পণ্যের মোট মূল্য"
        AppLanguage.ENGLISH -> "Items Subtotal"
    }
    fun deliveryFee(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "ডেলিভারি চার্জ"
        AppLanguage.ENGLISH -> "Delivery Fee"
    }
    fun freeDelivery(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "ফ্রি ডেলিভারি"
        AppLanguage.ENGLISH -> "FREE Delivery"
    }
    fun grandTotal(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "সর্বমোট মূল্য"
        AppLanguage.ENGLISH -> "Grand Total"
    }
    fun proceedToCheckout(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "অর্ডার এগিয়ে নিন"
        AppLanguage.ENGLISH -> "Proceed to Checkout"
    }
    fun codPayment(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "ক্যাশ অন ডেলিভারি (হাতে পেয়ে টাকা দিন)"
        AppLanguage.ENGLISH -> "Cash on Delivery (COD)"
    }
    fun confirmOrder(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "অর্ডার নিশ্চিত করুন"
        AppLanguage.ENGLISH -> "Confirm Order"
    }

    // Checkout Form
    fun customerName(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "আপনার সম্পূর্ণ নাম"
        AppLanguage.ENGLISH -> "Full Name"
    }
    fun mobileNumber(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "১০ ডিজিটের মোবাইল নম্বর"
        AppLanguage.ENGLISH -> "10-digit Mobile Number"
    }
    fun address(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "গ্রাম / রাস্তা / বাড়ির ঠিকানা"
        AppLanguage.ENGLISH -> "Village / Street / House Address"
    }
    fun locality(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "এলাকা / ল্যান্ডমার্ক / ডাকঘর"
        AppLanguage.ENGLISH -> "Locality / Landmark / Post Office"
    }
    fun pinCode(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "পিন কোড (PIN Code)"
        AppLanguage.ENGLISH -> "PIN Code"
    }
    fun district(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "জেলা (যেমন: মুর্শিদাবাদ)"
        AppLanguage.ENGLISH -> "District (e.g. Murshidabad)"
    }

    // Orders & Tracking
    fun noOrders(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "এখনও কোনো অর্ডার করেননি"
        AppLanguage.ENGLISH -> "No Orders Found"
    }
    fun orderSuccess(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "অর্ডার সফলভাবে সম্পন্ন হয়েছে!"
        AppLanguage.ENGLISH -> "Order Placed Successfully!"
    }
    fun orderNumber(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "অর্ডার নম্বর"
        AppLanguage.ENGLISH -> "Order Number"
    }
    fun shareOnWhatsApp(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "হোয়াটসঅ্যাপে অর্ডার রসিদ পাঠান"
        AppLanguage.ENGLISH -> "Share Order on WhatsApp"
    }
    fun cancelOrder(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "অর্ডার বাতিল করুন"
        AppLanguage.ENGLISH -> "Cancel Order"
    }

    // Admin & Shop
    fun adminPortal(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "দোকানদার / অ্যাডমিন প্যানেল"
        AppLanguage.ENGLISH -> "Shop Owner / Admin Panel"
    }
    fun adminPinPrompt(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "অ্যাডমিন পিন কোড লিখুন"
        AppLanguage.ENGLISH -> "Enter Admin Security PIN"
    }
    fun contactShop(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "সরাসরি দোকানে কল করুন"
        AppLanguage.ENGLISH -> "Call Shop Directly"
    }
    fun whatsappSupport(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> "হোয়াটসঅ্যাপ সহায়তা"
        AppLanguage.ENGLISH -> "WhatsApp Support"
    }
}
