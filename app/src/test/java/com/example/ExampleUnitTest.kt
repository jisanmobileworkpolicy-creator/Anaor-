package com.example

import com.example.data.model.OrderEntity
import com.example.data.model.OrderStatus
import com.example.data.model.Product
import com.example.data.model.ShopSettings
import com.example.util.AppLanguage
import com.example.util.Formatters
import com.example.util.Strings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testCurrencyFormatting() {
        val formatted = Formatters.formatPrice(14999.0)
        assertTrue(formatted.contains("14,999") || formatted.contains("14999"))
        assertTrue(formatted.startsWith("₹"))
    }

    @Test
    fun testProductDiscountCalculation() {
        val product = Product(
            id = "test_1",
            nameEn = "Test Phone",
            nameBn = "টেস্ট ফোন",
            categoryId = "cat_new_smartphones",
            brand = "TestBrand",
            model = "M1",
            price = 8000.0,
            mrp = 10000.0,
            stock = 5
        )
        assertEquals(20, product.discountPercentage)
        assertTrue(product.isInStock)
    }

    @Test
    fun testLanguageStrings() {
        assertEquals("মোবাইল মানেই জিসান মোবাইল", Strings.slogan(AppLanguage.BENGALI))
        assertEquals("Mobile Manei Jisan Mobile", Strings.slogan(AppLanguage.ENGLISH))
        assertEquals("কার্ট", Strings.navCart(AppLanguage.BENGALI))
    }

    @Test
    fun testOrderWhatsAppMessageFormatting() {
        val order = OrderEntity(
            orderNumber = "JM-20261021-1234",
            customerName = "Rahul Roy",
            customerPhone = "9876543210",
            addressLine = "Near Bus Stand",
            locality = "Sagar Para",
            district = "Murshidabad",
            state = "West Bengal",
            pinCode = "742149",
            itemsJson = "5G Smartphone x 1 @ ₹14999",
            subtotalAmount = 14999.0,
            deliveryFee = 0.0,
            totalAmount = 14999.0,
            paymentMethod = "Cash on Delivery (COD)",
            transactionRef = "",
            appliedReferralCode = "JMREF-100",
            status = OrderStatus.PENDING.code
        )
        val message = Formatters.buildOrderWhatsAppMessage(order)
        assertTrue(message.contains("JM-20261021-1234"))
        assertTrue(message.contains("Rahul Roy"))
        assertTrue(message.contains("Murshidabad"))
    }

    @Test
    fun testAdminDefaultCredentials() {
        val settings = ShopSettings()
        assertEquals("jisanadmin", settings.adminUsername)
        assertEquals("jisan786", settings.adminPassword)
        assertEquals("+919382302242", settings.adminMobile)
        assertEquals("9382302242@upi", settings.upiId)
    }
}
