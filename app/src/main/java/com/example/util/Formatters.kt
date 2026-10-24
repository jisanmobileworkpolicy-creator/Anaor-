package com.example.util

import com.example.data.model.OrderEntity
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {
    private val indianCurrencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = 0
    }

    fun formatPrice(amount: Double): String {
        return try {
            val formatted = indianCurrencyFormat.format(amount)
            // Ensure ₹ symbol is cleanly visible
            if (!formatted.startsWith("₹")) "₹" + formatted.replace("[^0-9,.]".toRegex(), "") else formatted
        } catch (_: Exception) {
            "₹${amount.toInt()}"
        }
    }

    fun formatDate(timestamp: Long): String {
        return try {
            val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            sdf.format(Date(timestamp))
        } catch (_: Exception) {
            ""
        }
    }

    fun buildWhatsAppUrl(phoneNumber: String, message: String): String {
        val cleanNumber = phoneNumber.replace("[^0-9]".toRegex(), "")
        val encodedMessage = try {
            URLEncoder.encode(message, StandardCharsets.UTF_8.toString())
        } catch (_: Exception) {
            message
        }
        return "https://wa.me/$cleanNumber?text=$encodedMessage"
    }

    fun buildOrderWhatsAppMessage(order: OrderEntity): String {
        val sb = StringBuilder()
        sb.append("🛍️ *নতুন অর্ডার - JISAN MOBILE*\n")
        sb.append("━━━━━━━━━━━━━━━━━━\n")
        sb.append("📌 *অর্ডার নম্বর:* ${order.orderNumber}\n")
        sb.append("👤 *গ্রাহক:* ${order.customerName}\n")
        sb.append("📞 *ফোন:* ${order.customerPhone}\n")
        sb.append("📍 *ঠিকানা:* ${order.addressLine}, ${order.locality}, ${order.district} - ${order.pinCode}\n")
        sb.append("━━━━━━━━━━━━━━━━━━\n")
        sb.append("📦 *পণ্য তালিকা:*\n")
        sb.append(order.itemsJson.replace(" | ", "\n• ").replace(";", "\n• "))
        sb.append("\n━━━━━━━━━━━━━━━━━━\n")
        sb.append("💰 *পণ্য মূল্য:* ${formatPrice(order.subtotalAmount)}\n")
        sb.append("🚚 *ডেলিভারি চার্জ:* ${if (order.deliveryFee > 0) formatPrice(order.deliveryFee) else "ফ্রি (FREE)"}\n")
        sb.append("💵 *সর্বমোট প্রদেয়:* *${formatPrice(order.totalAmount)}*\n")
        sb.append("💳 *পেমেন্ট পদ্ধতি:* ${order.paymentMethod}\n")
        sb.append("━━━━━━━━━━━━━━━━━━\n")
        sb.append("দয়া করে অর্ডারটি নিশ্চিত করুন। ধন্যবাদ!\n")
        sb.append("দোকান: বারোমাসিয়া হাজির হাট, সাগরপাড়া, মুর্শিদাবাদ।")
        return sb.toString()
    }
}
