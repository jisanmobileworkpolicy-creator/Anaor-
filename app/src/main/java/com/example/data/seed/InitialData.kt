package com.example.data.seed

import com.example.data.model.Category
import com.example.data.model.Product
import com.example.data.model.ProductCondition
import com.example.data.model.PromotionalBanner
import com.example.data.model.ShopSettings

object InitialData {

    val categories = listOf(
        Category(
            id = "cat_new_smartphones",
            nameEn = "New Smartphones",
            nameBn = "নতুন স্মার্টফোন",
            iconKey = "phone_android",
            displayOrder = 1,
            isVisible = true
        ),
        Category(
            id = "cat_second_hand",
            nameEn = "Second-Hand Smartphones",
            nameBn = "সেকেন্ড-হ্যান্ড ফোন",
            iconKey = "recycling",
            displayOrder = 2,
            isVisible = true
        ),
        Category(
            id = "cat_feature_phones",
            nameEn = "Feature & Keypad Phones",
            nameBn = "বাটন ও কিপ্যাড ফোন",
            iconKey = "dialpad",
            displayOrder = 3,
            isVisible = true
        ),
        Category(
            id = "cat_chargers",
            nameEn = "Chargers & Adapters",
            nameBn = "চার্জার ও অ্যাডাপ্টার",
            iconKey = "power",
            displayOrder = 4,
            isVisible = true
        ),
        Category(
            id = "cat_cables",
            nameEn = "USB, Type-C & Lightning Cables",
            nameBn = "ক্যাবল ও ডেটা ক্যাবল",
            iconKey = "cable",
            displayOrder = 5,
            isVisible = true
        ),
        Category(
            id = "cat_batteries",
            nameEn = "Batteries",
            nameBn = "মোবাইল ব্যাটারি",
            iconKey = "battery_charging_full",
            displayOrder = 6,
            isVisible = true
        ),
        Category(
            id = "cat_earphones",
            nameEn = "Earphones & Neckbands",
            nameBn = "ইয়ারফোন ও নেকব্যান্ড",
            iconKey = "headphones",
            displayOrder = 7,
            isVisible = true
        ),
        Category(
            id = "cat_bluetooth",
            nameEn = "Bluetooth Earbuds & Speakers",
            nameBn = "ব্লুটুথ ইয়ারবাডস ও স্পিকার",
            iconKey = "speaker",
            displayOrder = 8,
            isVisible = true
        ),
        Category(
            id = "cat_covers",
            nameEn = "Mobile Covers & Cases",
            nameBn = "মোবাইল ব্যাক কভার",
            iconKey = "shield",
            displayOrder = 9,
            isVisible = true
        ),
        Category(
            id = "cat_glass",
            nameEn = "Tempered Glass & Displays",
            nameBn = "টেম্পার্ড গ্লাস ও ডিসপ্লে",
            iconKey = "wallpaper",
            displayOrder = 10,
            isVisible = true
        ),
        Category(
            id = "cat_other",
            nameEn = "Other Mobile Accessories",
            nameBn = "অন্যান্য অ্যাক্সেসরিজ",
            iconKey = "devices_other",
            displayOrder = 11,
            isVisible = true
        )
    )

    val banners = listOf(
        PromotionalBanner(
            id = "ban_1",
            titleEn = "Mobile Manei Jisan Mobile",
            titleBn = "মোবাইল মানেই জিসান মোবাইল",
            subtitleEn = "New & Second-Hand Phones with Shop Guarantee at Baromasia Hajir Hut",
            subtitleBn = "নতুন ও সেকেন্ড-হ্যান্ড ফোন গ্যারান্টি সহ পাওয়া যায় বারোমাসিয়া হাজির হাটে",
            targetCategoryId = "cat_new_smartphones",
            discountTag = "স্পেশাল অফার",
            isActive = true,
            displayOrder = 1
        ),
        PromotionalBanner(
            id = "ban_2",
            titleEn = "Second-Hand Phones Tested & Certified",
            titleBn = "পরীক্ষিত সেকেন্ড-হ্যান্ড ফোন",
            subtitleEn = "3 Months Warranty & Free Inspection on Every Used Phone",
            subtitleBn = "প্রতিটি ব্যবহৃত ফোনে ৩ মাসের শপ ওয়ারেন্টি ও নিখুঁত চেকিং",
            targetCategoryId = "cat_second_hand",
            discountTag = "বেস্ট ডিল",
            isActive = true,
            displayOrder = 2
        ),
        PromotionalBanner(
            id = "ban_3",
            titleEn = "Original Mobile Accessories",
            titleBn = "আসল মোবাইল অ্যাক্সেসরিজ",
            subtitleEn = "Chargers, Cables, Earbuds, Covers & Displays at Wholesale Rates",
            subtitleBn = "চার্জার, ক্যাবল, ইয়ারবাডস ও কভার পাইকারি মূল্যে কিনুন",
            targetCategoryId = "cat_chargers",
            discountTag = "আপটু ৬০% ছাড়",
            isActive = true,
            displayOrder = 3
        )
    )

    val defaultSettings = ShopSettings()
}
