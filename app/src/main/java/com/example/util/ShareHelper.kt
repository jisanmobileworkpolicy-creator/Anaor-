package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object ShareHelper {
    // Dynamic public download URL set by admin or verified persistent host
    const val DEFAULT_DOWNLOAD_URL = ""

    fun getShareMessage(referralCode: String, downloadUrl: String = ""): String {
        val code = referralCode.ifBlank { "JMREF-786" }
        val activeUrl = downloadUrl.trim().ifEmpty { DEFAULT_DOWNLOAD_URL }
        val downloadSection = if (activeUrl.isNotBlank()) {
            "👉 অ্যাপ ডাউনলোড ও আপডেট লিংক:\n$activeUrl\n"
        } else {
            "📲 সরাসরি APK ফাইল শেয়ার পেতে দোকানে যোগাযোগ করুন অথবা WhatsApp-এ মেসেজ দিন।\n"
        }

        return """
📱 JISAN MOBILE ("মোবাইল মানেই জিসান মোবাইল")
📍 বারোমাসিয়া হাজির হাট, সাগরপাড়া, জলঙ্গী, মুর্শিদাবাদ।

নতুন ও সেকেন্ড-হ্যান্ড স্মার্টফোন এবং সমস্ত মোবাইল অ্যাক্সেসরিজের সেরা দোকান! ঘরে বসেই ক্যাশ অন ডেলিভারি ও প্রিপেইড ইউপিআই-তে অর্ডার করুন।

$downloadSection
🎁 রেফার কোড: $code
(অর্ডারে সাথে সাথে ₹১০০ ছাড় পাবেন!)
📞 কল বা WhatsApp: +91 9382302242 / 8918933017
        """.trimIndent()
    }

    /**
     * Directly shares the installed APK file via Android FileProvider (WhatsApp, Bluetooth, ShareMe, etc.)
     * If direct file share fails for any reason, gracefully falls back to link sharing.
     */
    fun shareApkFile(context: Context, referralCode: String = "", downloadUrl: String = "") {
        val shareText = getShareMessage(referralCode, downloadUrl)
        try {
            val appInfo = context.applicationInfo
            val sourceApk = File(appInfo.sourceDir)
            if (sourceApk.exists()) {
                val cacheApkDir = File(context.cacheDir, "apk")
                if (!cacheApkDir.exists()) {
                    cacheApkDir.mkdirs()
                }
                val destApk = File(cacheApkDir, "JISAN_MOBILE.apk")
                if (!destApk.exists() || destApk.length() != sourceApk.length()) {
                    sourceApk.copyTo(destApk, overwrite = true)
                }

                val apkUri: Uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    destApk
                )

                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "application/vnd.android.package-archive"
                    putExtra(Intent.EXTRA_STREAM, apkUri)
                    putExtra(Intent.EXTRA_SUBJECT, "JISAN MOBILE App APK")
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                val chooser = Intent.createChooser(sendIntent, "JISAN MOBILE অ্যাপ (APK) শেয়ার করুন")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
                return
            }
        } catch (_: Exception) {
            // Fallback to text link sharing below
        }

        // If file sharing is not supported or failed, use system link share
        shareApp(context, referralCode, downloadUrl)
    }

    /**
     * Opens Android System Share Sheet (WhatsApp, Facebook, Messenger, Telegram, SMS, etc.)
     */
    fun shareApp(context: Context, referralCode: String = "", downloadUrl: String = "") {
        val shareText = getShareMessage(referralCode, downloadUrl)
        try {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_SUBJECT, "JISAN MOBILE App")
                putExtra(Intent.EXTRA_TEXT, shareText)
                type = "text/plain"
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(sendIntent, "JISAN MOBILE অ্যাপ শেয়ার করুন")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (_: Exception) {
            // Fallback to clipboard
            copyToClipboard(context, shareText, "শেয়ার লিংক কপি করা হয়েছে!")
        }
    }

    /**
     * Directly share to WhatsApp, fallback to system share sheet if WhatsApp not found.
     */
    fun shareViaWhatsApp(context: Context, referralCode: String = "", downloadUrl: String = "") {
        val shareText = getShareMessage(referralCode, downloadUrl)
        val encodedText = try {
            URLEncoder.encode(shareText, StandardCharsets.UTF_8.toString())
        } catch (_: Exception) {
            shareText
        }
        try {
            val waIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://api.whatsapp.com/send?text=$encodedText")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(waIntent)
        } catch (_: Exception) {
            // If direct WhatsApp launch fails, open system share chooser
            shareApp(context, referralCode, downloadUrl)
        }
    }

    fun copyToClipboard(context: Context, text: String, toastMessage: String = "কপি করা হয়েছে!") {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clip = ClipData.newPlainText("JISAN_MOBILE", text)
            clipboard?.setPrimaryClip(clip)
            Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {}
    }

    /**
     * Send Admin OTP directly to Admin WhatsApp (+91 9382302242 / +91 8918933017)
     */
    fun sendAdminOtpToWhatsApp(context: Context, adminMobile: String, otp: String) {
        val cleanMobile = adminMobile.replace("[^0-9]".toRegex(), "")
        val targetNumber = if (cleanMobile.length == 10) "91$cleanMobile" else cleanMobile
        val message = "🔐 *JISAN MOBILE অ্যাডমিন পাসওয়ার্ড রিসেট OTP*\n\nআপনার ৬ ডিজিটের ওটিপি ভেরিফিকেশন কোড: *$otp*\n\nদয়া করে কাউকে এই কোড শেয়ার করবেন না। এটি কেবল দোকান মালিকের জন্য।"
        val encoded = try {
            URLEncoder.encode(message, StandardCharsets.UTF_8.toString())
        } catch (_: Exception) {
            message
        }
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://api.whatsapp.com/send?phone=$targetNumber&text=$encoded")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "WhatsApp খোলা সম্ভব হয়নি। সিকিউরিটি পিন ব্যবহার করুন।", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Open phone SMS app with pre-filled OTP to Admin mobile number
     */
    fun sendAdminOtpViaSms(context: Context, adminMobile: String, otp: String) {
        val cleanMobile = adminMobile.replace("[^0-9]".toRegex(), "")
        val targetNumber = if (cleanMobile.length == 10) "+91$cleanMobile" else adminMobile
        val message = "JISAN MOBILE Admin Reset OTP: $otp (Confidential - Owner Only)"
        try {
            val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:$targetNumber")
                putExtra("sms_body", message)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(smsIntent)
        } catch (_: Exception) {
            Toast.makeText(context, "এসএমএস অ্যাপ খোলা যায়নি। সিকিউরিটি পিন ব্যবহার করুন।", Toast.LENGTH_SHORT).show()
        }
    }
}
