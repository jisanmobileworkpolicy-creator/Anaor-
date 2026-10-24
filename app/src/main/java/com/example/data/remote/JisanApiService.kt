package com.example.data.remote

import android.content.Context
import android.os.Build
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.data.model.AppUpdateInfo
import com.example.data.model.OrderEntity
import com.example.data.model.Product
import com.example.data.model.ProductCondition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object JisanApiService {
    private const val TAG = "JisanApiService"
    private const val PREFS_NAME = "jisan_api_preferences"
    private const val KEY_CUSTOM_BACKEND_URL = "custom_backend_url"

    // Default production backend URL for JISAN MOBILE live catalog, updates, and APK distribution.
    const val DEFAULT_PRODUCTION_URL = "https://ais-dev-wrt3x77rkmwpod2cweoth6-79122091620.asia-east1.run.app"

    @Volatile
    private var customBackendUrl: String? = null

    fun init(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val saved = prefs.getString(KEY_CUSTOM_BACKEND_URL, null)
            if (!saved.isNullOrBlank()) {
                customBackendUrl = saved.trim().removeSuffix("/")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error initializing JisanApiService preferences: ${e.message}")
        }
    }

    /**
     * Validates that the provided backend URL is a valid public HTTPS URL.
     * Blocks empty, malformed, cleartext HTTP, and localhost/loopback addresses.
     */
    fun validateBackendUrl(url: String): Pair<Boolean, String> {
        val clean = url.trim().removeSuffix("/")
        if (clean.isBlank()) {
            return Pair(false, "❌ সার্ভার URL ফাঁকা রাখা যাবে না!")
        }
        if (clean.startsWith("http://", ignoreCase = true)) {
            return Pair(false, "❌ অনিরাপদ HTTP গ্রহণযোগ্য নয়। কাস্টমার ডিভাইসের নিরাপত্তার জন্য অবশ্যই HTTPS (https://...) ব্যবহার করতে হবে।")
        }
        if (!clean.startsWith("https://", ignoreCase = true)) {
            return Pair(false, "❌ URL অবশ্যই https:// দিয়ে শুরু হতে হবে (যেমন: https://your-server.onrender.com)।")
        }
        val lower = clean.lowercase()
        if (lower.contains("localhost") || lower.contains("127.0.0.1") || lower.contains("10.0.2.2") || lower.contains("0.0.0.0")) {
            return Pair(false, "❌ কাস্টমারদের মোবাইল ডিভাইসের জন্য লোকালহোস্ট বা 127.0.0.1 গ্রহণযোগ্য নয়। আপনার পাবলিক ক্লাউড সার্ভার URL দিন।")
        }
        return try {
            val parsed = URL(clean)
            if (parsed.host.isNullOrBlank() || !parsed.host.contains(".")) {
                Pair(false, "❌ অবৈধ ডোমেইন ফরম্যাট। সঠিক হোস্ট ডোমেইন প্রদান করুন।")
            } else {
                Pair(true, clean)
            }
        } catch (e: Exception) {
            Pair(false, "❌ অবৈধ URL ফরম্যাট: ${e.message}")
        }
    }

    fun setCustomBackendUrl(context: Context, url: String): Pair<Boolean, String> {
        val validation = validateBackendUrl(url)
        if (!validation.first) {
            return validation
        }
        val clean = validation.second
        customBackendUrl = clean
        try {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_CUSTOM_BACKEND_URL, customBackendUrl)
                .apply()
        } catch (e: Exception) {
            Log.w(TAG, "Error saving custom backend URL: ${e.message}")
            return Pair(false, "❌ সেভ করতে ত্রুটি: ${e.message}")
        }
        return Pair(true, "✅ সার্ভার URL সফলভাবে সেভ ও সক্রিয় করা হয়েছে!")
    }

    fun resetToDefaultBackendUrl(context: Context): String {
        customBackendUrl = null
        try {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .remove(KEY_CUSTOM_BACKEND_URL)
                .apply()
        } catch (e: Exception) {
            Log.w(TAG, "Error resetting backend URL: ${e.message}")
        }
        return DEFAULT_PRODUCTION_URL
    }

    fun getActiveBackendUrl(): String {
        return customBackendUrl ?: DEFAULT_PRODUCTION_URL
    }

    /**
     * Resolves the production backend URL for both Admin and Customer apps.
     * Strictly avoids localhost, 127.0.0.1, or emulator-only addresses.
     */
    fun getBaseUrls(): List<String> {
        val active = getActiveBackendUrl().trim().removeSuffix("/")
        return if (active.isNotBlank()) listOf(active) else listOf(DEFAULT_PRODUCTION_URL)
    }

    fun formatNetworkException(e: Exception, baseUrl: String): Exception {
        val rawMsg = e.message ?: ""
        val friendlyMessage = when {
            rawMsg.contains("HTTP 302") || rawMsg.contains("__cookie_check") ->
                "সার্ভারটি ব্যক্তিগত AI Studio গেটওয়ে দ্বারা সুরক্ষিত (HTTP 302)। কাস্টমারদের ফোনের জন্য অ্যাডমিন কনসোলে আপনার পাবলিক ব্যাকএন্ড সার্ভার URL কনফিগার করুন।"
            rawMsg.contains("HTTP 404") ->
                "সার্ভার এন্ডপয়েন্ট পাওয়া যায়নি (HTTP 404)। URL যাচাই করুন।"
            rawMsg.contains("HTTP 401") || rawMsg.contains("HTTP 403") ->
                "সার্ভার অ্যাক্সেস অনুমোদিত নয় (HTTP 401/403)। অ্যাডমিন কী চেক করুন।"
            e is java.net.UnknownHostException ->
                "সার্ভার ডোমেইন পাওয়া যায়নি (${e.message})। ইন্টারনেট কানেকশন বা সার্ভার URL চেক করুন।"
            e is java.net.SocketTimeoutException ->
                "সার্ভার থেকে উত্তর আসতে সময় অতিবাহিত হয়েছে (টাইমআউট)। পরে আবার চেষ্টা করুন।"
            e is java.net.ConnectException ->
                "সার্ভারের সাথে সংযোগ করা সম্ভব হয়নি ($baseUrl বন্ধ বা অ্যাক্সেসযোগ্য নয়)।"
            rawMsg.isNotBlank() -> rawMsg
            else -> "ইন্টারনেট বা সার্ভার সংযোগ ব্যর্থ হয়েছে।"
        }
        return Exception(friendlyMessage, e)
    }

    /**
     * Diagnostic tool: tests /api/health against a target URL
     */
    suspend fun testBackendConnection(targetUrl: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val validation = validateBackendUrl(targetUrl)
        if (!validation.first) {
            return@withContext validation
        }
        val clean = validation.second

        val start = System.currentTimeMillis()
        var conn: HttpURLConnection? = null
        try {
            val url = URL("$clean/api/health")
            conn = url.openConnection() as HttpURLConnection
            conn.instanceFollowRedirects = false
            conn.connectTimeout = 6000
            conn.readTimeout = 6000
            conn.requestMethod = "GET"
            conn.setRequestProperty("Accept", "application/json")
            conn.setRequestProperty("User-Agent", "JISAN-MOBILE-Android-App")

            val code = conn.responseCode
            val duration = System.currentTimeMillis() - start

            if (code in 200..299) {
                var statsInfo = ""
                try {
                    val body = conn.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(body)
                    val prodCount = json.optInt("productsCount", -1)
                    val orderCount = json.optInt("ordersCount", -1)
                    if (prodCount >= 0) {
                        statsInfo = " | পণ্য: $prodCount টি, অর্ডার: $orderCount টি"
                    }
                } catch (_: Exception) {}
                return@withContext Pair(true, "✅ সার্ভার সফলভাবে সংযুক্ত হয়েছে! ($duration ms$statsInfo)")
            } else if (code in 300..399) {
                val loc = conn.getHeaderField("Location") ?: ""
                return@withContext Pair(false, "⚠️ সার্ভার রিডাইরেক্ট করছে (HTTP $code -> $loc)। এটি AI Studio প্রিভিউ সেশন। কাস্টমারদের ফোনের জন্য Render/Railway/Cloud Run-এ ডিপ্লয় করা পাবলিক HTTPS লিঙ্ক ব্যবহার করুন।")
            } else {
                return@withContext Pair(false, "❌ সার্ভার রেসপন্স ত্রুটি (HTTP $code)")
            }
        } catch (e: Exception) {
            val formatted = formatNetworkException(e, clean)
            return@withContext Pair(false, "❌ কানেকশন ব্যর্থ: ${formatted.message}")
        } finally {
            conn?.disconnect()
        }
    }

    suspend fun fetchProducts(): Result<List<Product>> = withContext(Dispatchers.IO) {
        val urls = getBaseUrls()
        if (urls.isEmpty()) {
            return@withContext Result.failure(Exception("পাবলিক ক্লাউড ব্যাকএন্ড সার্ভার কনফিগার করা বাকি রয়েছে। অ্যাডমিন স্ক্রিনে আপনার সার্ভার URL দিন।"))
        }

        var lastException: Exception? = null
        for (base in urls) {
            var conn: HttpURLConnection? = null
            try {
                val url = URL("$base/api/products")
                conn = url.openConnection() as HttpURLConnection
                conn.instanceFollowRedirects = false
                conn.connectTimeout = 7000
                conn.readTimeout = 7000
                conn.requestMethod = "GET"
                conn.setRequestProperty("Accept", "application/json")
                conn.setRequestProperty("User-Agent", "JISAN-MOBILE-Android-App")

                val code = conn.responseCode
                if (code in 200..299) {
                    val reader = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8"))
                    val sb = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        sb.append(line)
                    }
                    reader.close()

                    val root = JSONObject(sb.toString())
                    val arr = root.optJSONArray("products") ?: JSONArray()
                    val list = mutableListOf<Product>()
                    for (i in 0 until arr.length()) {
                        val item = arr.getJSONObject(i)
                        list.add(parseProductJson(item))
                    }
                    Log.d(TAG, "Successfully fetched ${list.size} products from $base")
                    return@withContext Result.success(list)
                } else {
                    lastException = formatNetworkException(Exception("HTTP $code from $base"), base)
                }
            } catch (e: Exception) {
                lastException = formatNetworkException(e, base)
                Log.w(TAG, "Failed to fetch products from $base: ${e.message}")
            } finally {
                conn?.disconnect()
            }
        }
        Result.failure(lastException ?: Exception("সার্ভার থেকে প্রোডাক্ট লোড করা সম্ভব হয়নি।"))
    }

    suspend fun syncProductToServer(product: Product): Result<Boolean> = withContext(Dispatchers.IO) {
        val urls = getBaseUrls()
        if (urls.isEmpty()) {
            return@withContext Result.failure(Exception("পাবলিক ক্লাউড ব্যাকএন্ড সার্ভার কনফিগার করা বাকি রয়েছে। অ্যাডমিন স্ক্রিনে আপনার সার্ভার URL দিন।"))
        }

        val payload = productToJson(product).toString()
        var lastException: Exception? = null

        for (base in urls) {
            var conn: HttpURLConnection? = null
            try {
                val url = URL("$base/api/products")
                conn = url.openConnection() as HttpURLConnection
                conn.instanceFollowRedirects = false
                conn.connectTimeout = 7000
                conn.readTimeout = 7000
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                conn.setRequestProperty("Accept", "application/json")
                conn.setRequestProperty("Authorization", "Bearer jisan786")
                conn.setRequestProperty("X-Admin-Key", "jisan786")
                conn.setRequestProperty("User-Agent", "JISAN-MOBILE-Android-App")
                conn.doOutput = true

                OutputStreamWriter(conn.outputStream, "UTF-8").use { it.write(payload); it.flush() }

                val code = conn.responseCode
                if (code in 200..299) {
                    val resp = conn.inputStream.bufferedReader().use { it.readText() }
                    Log.d(TAG, "Product ${product.id} successfully synced to $base: $resp")
                    return@withContext Result.success(true)
                } else {
                    val err = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                    lastException = formatNetworkException(Exception("HTTP $code on $base: $err"), base)
                }
            } catch (e: Exception) {
                lastException = formatNetworkException(e, base)
                Log.w(TAG, "Failed syncing product to $base: ${e.message}")
            } finally {
                conn?.disconnect()
            }
        }
        Result.failure(lastException ?: Exception("প্রোডাক্ট ক্লাউডে সিঙ্ক করা সম্ভব হয়নি।"))
    }

    suspend fun bulkSyncProducts(products: List<Product>): Result<Int> = withContext(Dispatchers.IO) {
        val urls = getBaseUrls()
        if (urls.isEmpty()) {
            return@withContext Result.failure(Exception("পাবলিক ক্লাউড ব্যাকএন্ড সার্ভার কনফিগার করা বাকি রয়েছে। অ্যাডমিন স্ক্রিনে আপনার সার্ভার URL দিন।"))
        }

        val jsonArray = JSONArray()
        for (p in products) {
            jsonArray.put(productToJson(p))
        }
        val payload = jsonArray.toString()
        var lastException: Exception? = null

        for (base in urls) {
            var conn: HttpURLConnection? = null
            try {
                val url = URL("$base/api/products")
                conn = url.openConnection() as HttpURLConnection
                conn.instanceFollowRedirects = false
                conn.connectTimeout = 10000
                conn.readTimeout = 10000
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                conn.setRequestProperty("Accept", "application/json")
                conn.setRequestProperty("Authorization", "Bearer jisan786")
                conn.setRequestProperty("X-Admin-Key", "jisan786")
                conn.setRequestProperty("User-Agent", "JISAN-MOBILE-Android-App")
                conn.doOutput = true

                OutputStreamWriter(conn.outputStream, "UTF-8").use { it.write(payload); it.flush() }

                val code = conn.responseCode
                if (code in 200..299) {
                    conn.inputStream.bufferedReader().use { it.readText() }
                    Log.d(TAG, "Bulk synced ${products.size} products to $base")
                    return@withContext Result.success(products.size)
                } else {
                    val err = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                    lastException = formatNetworkException(Exception("HTTP $code on $base: $err"), base)
                }
            } catch (e: Exception) {
                lastException = formatNetworkException(e, base)
                Log.w(TAG, "Failed bulk syncing products to $base: ${e.message}")
            } finally {
                conn?.disconnect()
            }
        }
        Result.failure(lastException ?: Exception("বাল্ক প্রোডাক্ট সিঙ্ক করা সম্ভব হয়নি।"))
    }

    suspend fun deleteProductFromServer(productId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val urls = getBaseUrls()
        if (urls.isEmpty()) {
            return@withContext Result.failure(Exception("পাবলিক ক্লাউড ব্যাকএন্ড সার্ভার কনফিগার করা বাকি রয়েছে। অ্যাডমিন স্ক্রিনে আপনার সার্ভার URL দিন।"))
        }

        var lastException: Exception? = null
        for (base in urls) {
            var conn: HttpURLConnection? = null
            try {
                val url = URL("$base/api/products/$productId")
                conn = url.openConnection() as HttpURLConnection
                conn.instanceFollowRedirects = false
                conn.connectTimeout = 6000
                conn.readTimeout = 6000
                conn.requestMethod = "DELETE"
                conn.setRequestProperty("Accept", "application/json")
                conn.setRequestProperty("Authorization", "Bearer jisan786")
                conn.setRequestProperty("X-Admin-Key", "jisan786")
                conn.setRequestProperty("User-Agent", "JISAN-MOBILE-Android-App")

                val code = conn.responseCode
                if (code in 200..299) {
                    Log.d(TAG, "Product $productId deleted on $base")
                    return@withContext Result.success(true)
                } else {
                    lastException = formatNetworkException(Exception("HTTP $code deleting from $base"), base)
                }
            } catch (e: Exception) {
                lastException = formatNetworkException(e, base)
            } finally {
                conn?.disconnect()
            }
        }
        Result.failure(lastException ?: Exception("সার্ভার থেকে প্রোডাক্ট ডিলিট করা সম্ভব হয়নি।"))
    }

    suspend fun checkAppUpdate(): Result<AppUpdateInfo> = withContext(Dispatchers.IO) {
        val urls = getBaseUrls()
        if (urls.isEmpty()) {
            return@withContext Result.failure(Exception("পাবলিক ক্লাউড ব্যাকএন্ড সার্ভার কনফিগার করা বাকি রয়েছে।"))
        }

        var lastException: Exception? = null
        for (base in urls) {
            var conn: HttpURLConnection? = null
            try {
                val url = URL("$base/api/app/check-update")
                conn = url.openConnection() as HttpURLConnection
                conn.instanceFollowRedirects = false
                conn.connectTimeout = 6000
                conn.readTimeout = 6000
                conn.requestMethod = "GET"
                conn.setRequestProperty("Accept", "application/json")
                conn.setRequestProperty("User-Agent", "JISAN-MOBILE-Android-App")

                val code = conn.responseCode
                if (code in 200..299) {
                    val reader = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8"))
                    val sb = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        sb.append(line)
                    }
                    reader.close()

                    val json = JSONObject(sb.toString())
                    val info = AppUpdateInfo(
                        versionCode = json.optInt("versionCode", 1),
                        versionName = json.optString("versionName", "1.0"),
                        releaseNotes = json.optString("releaseNotes", ""),
                        downloadUrl = json.optString("downloadUrl", ""),
                        forceUpdate = json.optBoolean("forceUpdate", false),
                        releaseDate = json.optString("releaseDate", ""),
                        minSupportedVersion = json.optInt("minSupportedVersion", 1)
                    )
                    Log.d(TAG, "Update check successful from $base: v${info.versionName}")
                    return@withContext Result.success(info)
                } else {
                    lastException = formatNetworkException(Exception("HTTP $code checking update from $base"), base)
                }
            } catch (e: Exception) {
                lastException = formatNetworkException(e, base)
                Log.w(TAG, "Update check failed on $base: ${e.message}")
            } finally {
                conn?.disconnect()
            }
        }
        Result.failure(lastException ?: Exception("আপডেট সার্ভারের সাথে সংযোগ করা সম্ভব হয়নি।"))
    }

    suspend fun syncCurrentBuildApk(adminSecret: String = "jisan786"): Result<String> = withContext(Dispatchers.IO) {
        val urls = getBaseUrls()
        if (urls.isEmpty()) {
            return@withContext Result.failure(Exception("পাবলিক ব্যাকএন্ড সার্ভার কনফিগার করা বাকি রয়েছে।"))
        }

        var lastException: Exception? = null
        for (base in urls) {
            var conn: HttpURLConnection? = null
            try {
                val url = URL("$base/api/admin/app/sync-current-apk")
                conn = url.openConnection() as HttpURLConnection
                conn.instanceFollowRedirects = false
                conn.connectTimeout = 10000
                conn.readTimeout = 10000
                conn.requestMethod = "POST"
                conn.setRequestProperty("Accept", "application/json")
                conn.setRequestProperty("Authorization", "Bearer $adminSecret")
                conn.setRequestProperty("X-Admin-Key", adminSecret)
                conn.setRequestProperty("User-Agent", "JISAN-MOBILE-Android-App")

                val code = conn.responseCode
                if (code in 200..299) {
                    val resp = conn.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(resp)
                    val downloadUrl = json.optString("downloadUrl", "")
                    if (downloadUrl.isNotBlank()) {
                        Log.d(TAG, "Current build APK synced successfully to $base: $downloadUrl")
                        return@withContext Result.success(downloadUrl)
                    }
                } else {
                    val err = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                    lastException = formatNetworkException(Exception("HTTP $code syncing APK on $base: $err"), base)
                }
            } catch (e: Exception) {
                lastException = formatNetworkException(e, base)
            } finally {
                conn?.disconnect()
            }
        }
        Result.failure(lastException ?: Exception("সার্ভারে বর্তমান বিল্ডের APK সিঙ্ক করা সম্ভব হয়নি।"))
    }

    suspend fun uploadApkFile(file: File, adminSecret: String = "jisan786"): Result<String> = withContext(Dispatchers.IO) {
        val urls = getBaseUrls()
        if (urls.isEmpty()) {
            return@withContext Result.failure(Exception("পাবলিক ব্যাকএন্ড সার্ভার কনফিগার করা বাকি রয়েছে।"))
        }
        if (!file.exists() || file.length() == 0L) {
            return@withContext Result.failure(IllegalArgumentException("APK ফাইল পাওয়া যায়নি বা ফাইলের আকার ০ বাইট।"))
        }

        var lastException: Exception? = null
        for (base in urls) {
            var conn: HttpURLConnection? = null
            try {
                val url = URL("$base/api/admin/app/upload-apk")
                conn = url.openConnection() as HttpURLConnection
                conn.instanceFollowRedirects = false
                conn.connectTimeout = 30000
                conn.readTimeout = 60000
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/octet-stream")
                conn.setRequestProperty("Accept", "application/json")
                conn.setRequestProperty("Authorization", "Bearer $adminSecret")
                conn.setRequestProperty("X-Admin-Key", adminSecret)
                conn.setRequestProperty("User-Agent", "JISAN-MOBILE-Android-App")
                conn.setFixedLengthStreamingMode(file.length())
                conn.doOutput = true

                file.inputStream().use { input ->
                    conn.outputStream.use { output ->
                        input.copyTo(output, bufferSize = 64 * 1024)
                        output.flush()
                    }
                }

                val code = conn.responseCode
                if (code in 200..299) {
                    val resp = conn.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(resp)
                    val downloadUrl = json.optString("downloadUrl", "")
                    if (downloadUrl.isNotBlank()) {
                        Log.d(TAG, "Uploaded APK to $base: $downloadUrl")
                        return@withContext Result.success(downloadUrl)
                    }
                } else {
                    val err = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                    lastException = formatNetworkException(Exception("HTTP $code uploading APK to $base: $err"), base)
                }
            } catch (e: Exception) {
                lastException = formatNetworkException(e, base)
            } finally {
                conn?.disconnect()
            }
        }
        Result.failure(lastException ?: Exception("APK ফাইল সার্ভারে আপলোড করা সম্ভব হয়নি।"))
    }

    suspend fun publishAppUpdate(info: AppUpdateInfo, adminSecret: String = "jisan786"): Result<AppUpdateInfo> = withContext(Dispatchers.IO) {
        val urls = getBaseUrls()
        if (urls.isEmpty()) {
            return@withContext Result.failure(Exception("পাবলিক ক্লাউড ব্যাকএন্ড সার্ভার কনফিগার করা বাকি রয়েছে।"))
        }

        val effectiveDownloadUrl = info.downloadUrl.trim().ifEmpty {
            val base = urls.firstOrNull() ?: ""
            if (base.isNotBlank()) "$base/JISAN_MOBILE.apk" else ""
        }

        if (effectiveDownloadUrl.isBlank() || (!effectiveDownloadUrl.startsWith("http://") && !effectiveDownloadUrl.startsWith("https://"))) {
            return@withContext Result.failure(IllegalArgumentException("APK ডাউনলোড লিংক ফাঁকা রাখা যাবে না! অনুগ্রহ করে প্রথমে আসল APK ফাইলটি আপলোড করুন।"))
        }

        val payload = JSONObject().apply {
            put("versionCode", info.versionCode)
            put("versionName", info.versionName)
            put("releaseNotes", info.releaseNotes)
            put("downloadUrl", effectiveDownloadUrl)
            put("forceUpdate", info.forceUpdate)
            put("releaseDate", info.releaseDate)
            put("minSupportedVersion", info.minSupportedVersion)
        }.toString()

        var lastException: Exception? = null
        for (base in urls) {
            var conn: HttpURLConnection? = null
            try {
                val url = URL("$base/api/admin/app/publish-update")
                conn = url.openConnection() as HttpURLConnection
                conn.instanceFollowRedirects = false
                conn.connectTimeout = 7000
                conn.readTimeout = 7000
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                conn.setRequestProperty("Accept", "application/json")
                conn.setRequestProperty("Authorization", "Bearer $adminSecret")
                conn.setRequestProperty("X-Admin-Key", adminSecret)
                conn.setRequestProperty("User-Agent", "JISAN-MOBILE-Android-App")
                conn.doOutput = true

                OutputStreamWriter(conn.outputStream, "UTF-8").use { it.write(payload); it.flush() }

                val code = conn.responseCode
                if (code in 200..299) {
                    val reader = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8"))
                    val sb = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        sb.append(line)
                    }
                    reader.close()

                    val root = JSONObject(sb.toString())
                    val saved = root.optJSONObject("updateInfo") ?: JSONObject()
                    val resultInfo = AppUpdateInfo(
                        versionCode = saved.optInt("versionCode", info.versionCode),
                        versionName = saved.optString("versionName", info.versionName),
                        releaseNotes = saved.optString("releaseNotes", info.releaseNotes),
                        downloadUrl = saved.optString("downloadUrl", info.downloadUrl),
                        forceUpdate = saved.optBoolean("forceUpdate", info.forceUpdate),
                        releaseDate = saved.optString("releaseDate", info.releaseDate),
                        minSupportedVersion = saved.optInt("minSupportedVersion", info.minSupportedVersion)
                    )
                    Log.d(TAG, "Update successfully published to $base")
                    return@withContext Result.success(resultInfo)
                } else {
                    lastException = formatNetworkException(Exception("HTTP $code publishing update on $base"), base)
                }
            } catch (e: Exception) {
                lastException = formatNetworkException(e, base)
                Log.w(TAG, "Publish update failed on $base: ${e.message}")
            } finally {
                conn?.disconnect()
            }
        }
        Result.failure(lastException ?: Exception("সার্ভারে আপডেট প্রকাশ করা সম্ভব হয়নি।"))
    }

    suspend fun uploadImage(file: File): Result<String> = withContext(Dispatchers.IO) {
        val urls = getBaseUrls()
        if (urls.isEmpty()) {
            return@withContext Result.failure(Exception("পাবলিক ক্লাউড ব্যাকএন্ড সার্ভার কনফিগার করা বাকি রয়েছে।"))
        }

        try {
            val bytes = file.readBytes()
            val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
            val json = JSONObject().apply {
                put("data", base64)
                put("fileName", file.name)
            }.toString()

            var lastEx: Exception? = null
            for (base in urls) {
                var conn: HttpURLConnection? = null
                try {
                    val url = URL("$base/api/upload")
                    conn = url.openConnection() as HttpURLConnection
                    conn.instanceFollowRedirects = false
                    conn.connectTimeout = 12000
                    conn.readTimeout = 12000
                    conn.requestMethod = "POST"
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                    conn.setRequestProperty("Accept", "application/json")
                    conn.setRequestProperty("Authorization", "Bearer jisan786")
                    conn.setRequestProperty("X-Admin-Key", "jisan786")
                    conn.setRequestProperty("User-Agent", "JISAN-MOBILE-Android-App")
                    conn.doOutput = true

                    OutputStreamWriter(conn.outputStream, "UTF-8").use { it.write(json); it.flush() }

                    val code = conn.responseCode
                    if (code in 200..299) {
                        val reader = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8"))
                        val sb = StringBuilder()
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            sb.append(line)
                        }
                        reader.close()
                        val res = JSONObject(sb.toString())
                        val uploadedUrl = res.optString("url", "")
                        if (uploadedUrl.isNotBlank()) {
                            Log.d(TAG, "Uploaded image to $base: $uploadedUrl")
                            return@withContext Result.success(uploadedUrl)
                        }
                    } else {
                        lastEx = formatNetworkException(Exception("HTTP $code uploading image"), base)
                    }
                } catch (e: Exception) {
                    lastEx = formatNetworkException(e, base)
                    Log.w(TAG, "Upload failed on $base: ${e.message}")
                } finally {
                    conn?.disconnect()
                }
            }
            Result.failure(lastEx ?: Exception("সার্ভারে ছবি আপলোড করা সম্ভব হয়নি।"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncOrderToServer(order: OrderEntity): Result<Boolean> = withContext(Dispatchers.IO) {
        val urls = getBaseUrls()
        if (urls.isEmpty()) {
            return@withContext Result.failure(Exception("পাবলিক ক্লাউড ব্যাকএন্ড সার্ভার কনফিগার করা বাকি রয়েছে।"))
        }

        val payload = JSONObject().apply {
            put("orderNumber", order.orderNumber)
            put("customerName", order.customerName)
            put("customerPhone", order.customerPhone)
            put("addressLine", order.addressLine)
            put("locality", order.locality)
            put("district", order.district)
            put("state", order.state)
            put("pinCode", order.pinCode)
            put("itemsJson", order.itemsJson)
            put("subtotalAmount", order.subtotalAmount)
            put("deliveryFee", order.deliveryFee)
            put("discountAmount", order.discountAmount)
            put("totalAmount", order.totalAmount)
            put("paymentMethod", order.paymentMethod)
            put("transactionRef", order.transactionRef)
            put("appliedReferralCode", order.appliedReferralCode)
            put("status", order.status)
            put("trackingNotes", order.trackingNotes)
            put("createdAt", order.createdAt)
            put("updatedAt", order.updatedAt)
        }.toString()

        var lastException: Exception? = null
        for (base in urls) {
            var conn: HttpURLConnection? = null
            try {
                val url = URL("$base/api/orders")
                conn = url.openConnection() as HttpURLConnection
                conn.instanceFollowRedirects = false
                conn.connectTimeout = 7000
                conn.readTimeout = 7000
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                conn.setRequestProperty("Accept", "application/json")
                conn.setRequestProperty("User-Agent", "JISAN-MOBILE-Android-App")
                conn.doOutput = true

                OutputStreamWriter(conn.outputStream, "UTF-8").use { it.write(payload); it.flush() }

                val code = conn.responseCode
                if (code in 200..299) {
                    return@withContext Result.success(true)
                } else {
                    lastException = formatNetworkException(Exception("HTTP $code syncing order to $base"), base)
                }
            } catch (e: Exception) {
                lastException = formatNetworkException(e, base)
            } finally {
                conn?.disconnect()
            }
        }
        Result.failure(lastException ?: Exception("অর্ডার সার্ভারে সিঙ্ক করা যায়নি।"))
    }

    private fun parseProductJson(item: JSONObject): Product {
        return Product(
            id = item.optString("id", ""),
            nameEn = item.optString("nameEn", ""),
            nameBn = item.optString("nameBn", item.optString("nameEn", "")),
            categoryId = item.optString("categoryId", "cat_new_smartphones"),
            brand = item.optString("brand", ""),
            model = item.optString("model", ""),
            condition = item.optString("condition", ProductCondition.NEW.name),
            price = item.optDouble("price", 0.0),
            mrp = item.optDouble("mrp", item.optDouble("price", 0.0)),
            stock = item.optInt("stock", 1),
            imageUrl = item.optString("imageUrl", ""),
            isAvailable = item.optBoolean("isAvailable", true),
            isBestSeller = item.optBoolean("isBestSeller", false),
            isNewArrival = item.optBoolean("isNewArrival", false),
            descriptionEn = item.optString("descriptionEn", ""),
            descriptionBn = item.optString("descriptionBn", ""),
            batteryHealth = if (item.has("batteryHealth") && !item.isNull("batteryHealth")) item.getInt("batteryHealth") else null,
            cosmeticCondition = if (item.has("cosmeticCondition") && !item.isNull("cosmeticCondition")) item.getString("cosmeticCondition") else null,
            includedAccessories = if (item.has("includedAccessories") && !item.isNull("includedAccessories")) item.getString("includedAccessories") else null,
            warrantyMonths = if (item.has("warrantyMonths") && !item.isNull("warrantyMonths")) item.getInt("warrantyMonths") else null,
            colorVariants = item.optString("colorVariants", ""),
            storageVariants = item.optString("storageVariants", ""),
            warrantyInfo = item.optString("warrantyInfo", ""),
            returnPolicy = item.optString("returnPolicy", ""),
            isDemoProduct = item.optBoolean("isDemoProduct", false)
        )
    }

    private fun productToJson(p: Product): JSONObject {
        return JSONObject().apply {
            put("id", p.id)
            put("nameEn", p.nameEn)
            put("nameBn", p.nameBn)
            put("categoryId", p.categoryId)
            put("brand", p.brand)
            put("model", p.model)
            put("condition", p.condition)
            put("price", p.price)
            put("mrp", p.mrp)
            put("stock", p.stock)
            put("imageUrl", p.imageUrl)
            put("isAvailable", p.isAvailable)
            put("isBestSeller", p.isBestSeller)
            put("isNewArrival", p.isNewArrival)
            put("descriptionEn", p.descriptionEn)
            put("descriptionBn", p.descriptionBn)
            if (p.batteryHealth != null) put("batteryHealth", p.batteryHealth)
            if (p.cosmeticCondition != null) put("cosmeticCondition", p.cosmeticCondition)
            if (p.includedAccessories != null) put("includedAccessories", p.includedAccessories)
            if (p.warrantyMonths != null) put("warrantyMonths", p.warrantyMonths)
            put("colorVariants", p.colorVariants)
            put("storageVariants", p.storageVariants)
            put("warrantyInfo", p.warrantyInfo)
            put("returnPolicy", p.returnPolicy)
            put("isDemoProduct", p.isDemoProduct)
        }
    }
}
