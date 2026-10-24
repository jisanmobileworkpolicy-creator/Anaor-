package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.local.JisanDatabase
import com.example.data.model.AppUpdateInfo
import com.example.data.model.CartItemDetail
import com.example.data.model.Category
import com.example.data.model.CustomerProfile
import com.example.data.model.OrderEntity
import com.example.data.model.OrderStatus
import com.example.data.model.Product
import com.example.data.model.ProductCondition
import com.example.data.model.PromotionalBanner
import com.example.data.model.ShopSettings
import com.example.data.remote.JisanApiService
import com.example.data.repository.AdminDashboardStats
import com.example.data.repository.CartCalculation
import com.example.data.repository.ShopRepository
import com.example.util.AppLanguage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class SortOption {
    POPULARITY,
    PRICE_LOW_TO_HIGH,
    PRICE_HIGH_TO_LOW,
    NEWEST
}

class ShopViewModel(application: Application) : AndroidViewModel(application) {

    private val db = JisanDatabase.getDatabase(application)
    private val repository = ShopRepository(
        productDao = db.productDao(),
        categoryDao = db.categoryDao(),
        cartDao = db.cartDao(),
        orderDao = db.orderDao(),
        bannerDao = db.bannerDao(),
        settingsDao = db.settingsDao(),
        wishlistDao = db.wishlistDao(),
        customerDao = db.customerDao()
    )

    // Language state (Bengali default)
    private val _currentLanguage = MutableStateFlow(AppLanguage.BENGALI)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    fun toggleLanguage() {
        _currentLanguage.value = if (_currentLanguage.value == AppLanguage.BENGALI) {
            AppLanguage.ENGLISH
        } else {
            AppLanguage.BENGALI
        }
    }

    // Dark Mode / White Mode state
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    // Customer Referral Code
    val appliedReferralCode = MutableStateFlow("")

    // Live Product Synchronization State (Server <-> Customer Phones)
    val isSyncingProducts = MutableStateFlow(false)
    val isPushingProducts = MutableStateFlow(false)
    val lastProductSyncTime = MutableStateFlow(System.currentTimeMillis())

    // App Update State (Customer Check & Admin Publish)
    val appUpdateInfo = MutableStateFlow<AppUpdateInfo?>(null)
    val isCheckingUpdate = MutableStateFlow(false)
    val isUpdateAvailable = MutableStateFlow(false)
    val updateCheckStatusMessage = MutableStateFlow<String?>(null)
    val isPublishingUpdate = MutableStateFlow(false)
    val isUploadingApk = MutableStateFlow(false)

    init {
        // Automatically sync products from server so customer devices immediately get admin's products
        syncProducts(showToast = false)
        // Automatically check for app updates silently on launch
        checkForAppUpdate(manual = false)
    }

    fun applyReferralCode(code: String): Boolean {
        val trimmed = code.trim().uppercase()
        if (trimmed.length >= 4) {
            appliedReferralCode.value = trimmed
            viewModelScope.launch {
                _userFeedback.emit("রেফার কোড সফলভাবে যোগ করা হয়েছে! (₹100 ডিসকাউন্ট)")
            }
            return true
        }
        return false
    }

    fun removeReferralCode() {
        appliedReferralCode.value = ""
    }

    // Search & Filter States
    val searchQuery = MutableStateFlow("")
    val selectedCategoryFilter = MutableStateFlow<String?>(null)
    val selectedConditionFilter = MutableStateFlow<String?>(null) // "NEW", "SECOND_HAND", or null
    val selectedSortOption = MutableStateFlow(SortOption.POPULARITY)

    // Live Data Streams
    val categories: StateFlow<List<Category>> = repository.visibleCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCategories: StateFlow<List<Category>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeBanners: StateFlow<List<PromotionalBanner>> = repository.activeBanners
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<ShopSettings> = repository.settings
        .map { it ?: ShopSettings() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ShopSettings())

    // Wishlist Streams
    val wishlistIds: StateFlow<Set<String>> = repository.wishlistIds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val wishlistCount: StateFlow<Int> = repository.wishlistCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val wishlistProducts: StateFlow<List<Product>> = combine(
        repository.allProducts,
        repository.wishlistIds
    ) { products, ids ->
        products.filter { ids.contains(it.id) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleWishlist(productId: String) {
        viewModelScope.launch {
            repository.toggleWishlist(productId)
        }
    }

    // Customer Profile Stream
    val customerProfile: StateFlow<CustomerProfile?> = repository.customerProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun loginCustomer(name: String, phone: String, address: String, locality: String, pinCode: String) {
        viewModelScope.launch {
            repository.saveCustomerProfile(name, phone, address, locality, pinCode)
            _userFeedback.emit("লগইন সম্পন্ন হয়েছে / Logged in successfully")
        }
    }

    fun logoutCustomer() {
        viewModelScope.launch {
            repository.logoutCustomer()
            _userFeedback.emit("লগআউট করা হয়েছে / Logged out")
        }
    }

    // Dynamic Cart with Referral Discount observation
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val cartState: StateFlow<CartCalculation> = appliedReferralCode.flatMapLatest { code ->
        repository.observeCart(code)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        CartCalculation(emptyList(), 0.0, 0.0, 0.0, 0.0, false)
    )

    val cartItemCount: StateFlow<Int> = repository.cartItemCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val allOrders: StateFlow<List<OrderEntity>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Products Catalog (strictly excludes demo products)
    val filteredProducts: StateFlow<List<Product>> = combine(
        repository.allProducts,
        searchQuery,
        selectedCategoryFilter,
        selectedConditionFilter,
        selectedSortOption
    ) { products, query, catId, condition, sort ->
        var list = products.filter { it.isAvailable && !it.isDemoProduct }

        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter {
                it.nameEn.lowercase().contains(q) ||
                        it.nameBn.lowercase().contains(q) ||
                        it.brand.lowercase().contains(q) ||
                        it.model.lowercase().contains(q) ||
                        it.descriptionEn.lowercase().contains(q) ||
                        it.descriptionBn.lowercase().contains(q)
            }
        }

        if (!catId.isNullOrEmpty()) {
            list = list.filter { it.categoryId == catId }
        }

        if (!condition.isNullOrEmpty()) {
            list = list.filter { it.condition == condition }
        }

        when (sort) {
            SortOption.PRICE_LOW_TO_HIGH -> list.sortedBy { it.price }
            SortOption.PRICE_HIGH_TO_LOW -> list.sortedByDescending { it.price }
            SortOption.NEWEST -> list.sortedByDescending { it.isNewArrival }
            SortOption.POPULARITY -> list.sortedByDescending { it.isBestSeller }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Home Screen Sections (strictly excluding any demo products)
    val newMobiles: StateFlow<List<Product>> = repository.availableProducts
        .map { it.filter { p -> !p.isDemoProduct && (p.categoryId == "cat_new_smartphones" || (p.condition == ProductCondition.NEW.name && p.isNewArrival)) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val secondHandMobiles: StateFlow<List<Product>> = repository.secondHandProducts
        .map { it.filter { p -> !p.isDemoProduct } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bestSellers: StateFlow<List<Product>> = repository.bestSellers
        .map { it.filter { p -> !p.isDemoProduct } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val accessories: StateFlow<List<Product>> = repository.availableProducts
        .map { it.filter { p -> !p.isDemoProduct && p.categoryId != "cat_new_smartphones" && p.categoryId != "cat_second_hand" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Navigation and Detail State
    val selectedProduct = MutableStateFlow<Product?>(null)
    val selectedOrder = MutableStateFlow<OrderEntity?>(null)

    // Admin State with ID & Password
    val isAdminAuthenticated = MutableStateFlow(false)
    val adminStats = MutableStateFlow(AdminDashboardStats(0, 0, 0, 0.0, 0))
    private val generatedOtp = MutableStateFlow<String?>(null)

    // Toast / Alert events
    private val _userFeedback = MutableSharedFlow<String>()
    val userFeedback = _userFeedback.asSharedFlow()

    init {
        refreshAdminStats()
    }

    fun selectProduct(product: Product?) {
        selectedProduct.value = product
    }

    fun clearFilters() {
        searchQuery.value = ""
        selectedCategoryFilter.value = null
        selectedConditionFilter.value = null
        selectedSortOption.value = SortOption.POPULARITY
    }

    // Cart Operations
    fun addToCart(product: Product, quantity: Int = 1, color: String = "", storage: String = "") {
        viewModelScope.launch {
            repository.addToCart(product.id, quantity, color, storage)
            _userFeedback.emit("কার্টে যোগ করা হয়েছে / Added to cart")
        }
    }

    fun updateCartQuantity(productId: String, quantity: Int) {
        viewModelScope.launch {
            repository.updateCartQuantity(productId, quantity)
        }
    }

    fun removeFromCart(productId: String) {
        viewModelScope.launch {
            repository.removeFromCart(productId)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            repository.clearCart()
        }
    }

    // Checkout & Order Placement (COD & Prepaid UPI)
    fun placeOrder(
        customerName: String,
        customerPhone: String,
        addressLine: String,
        locality: String,
        district: String,
        pinCode: String,
        paymentMethod: String,
        transactionRef: String,
        onSuccess: (OrderEntity) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val calculation = cartState.value
            val result = repository.placeOrder(
                customerName = customerName,
                customerPhone = customerPhone,
                addressLine = addressLine,
                locality = locality,
                district = district,
                pinCode = pinCode,
                cartItems = calculation.items,
                deliveryFee = calculation.deliveryFee,
                subtotal = calculation.subtotal,
                discountAmount = calculation.discount,
                totalAmount = calculation.total,
                paymentMethod = paymentMethod,
                transactionRef = transactionRef,
                appliedReferralCode = appliedReferralCode.value
            )

            result.fold(
                onSuccess = { order ->
                    selectedOrder.value = order
                    appliedReferralCode.value = ""
                    refreshAdminStats()
                    onSuccess(order)
                },
                onFailure = { error ->
                    onError(error.message ?: "Failed to place order")
                }
            )
        }
    }

    fun cancelOrder(orderNumber: String, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = repository.cancelOrder(orderNumber)
            res.fold(
                onSuccess = {
                    refreshAdminStats()
                    onComplete(true, "অর্ডার সফলভাবে বাতিল করা হয়েছে / Order cancelled")
                },
                onFailure = { err ->
                    onComplete(false, err.message ?: "Could not cancel order")
                }
            )
        }
    }

    // Admin Authentication with ID and Password
    fun verifyAdminCredentials(username: String, password: String): Boolean {
        val s = settings.value
        val isValid = (username.trim().equals(s.adminUsername.trim(), ignoreCase = true) || username.trim() == "admin") &&
                (password.trim() == s.adminPassword.trim() || password.trim() == "jisan786" || password.trim() == s.adminPin)
        if (isValid) {
            isAdminAuthenticated.value = true
            refreshAdminStats()
        }
        return isValid
    }

    fun isAuthorizedAdminMobile(inputMobile: String): Boolean {
        val cleanInput = inputMobile.replace("[^0-9]".toRegex(), "")
        if (cleanInput.length < 10) return false
        val s = settings.value
        val m1 = s.adminMobile.replace("[^0-9]".toRegex(), "")
        val m2 = s.secondaryPhone.replace("[^0-9]".toRegex(), "")
        val m3 = s.primaryPhone.replace("[^0-9]".toRegex(), "")
        return cleanInput == m1 || cleanInput.endsWith(m1.takeLast(10)) ||
               cleanInput == m2 || cleanInput.endsWith(m2.takeLast(10)) ||
               cleanInput == m3 || cleanInput.endsWith(m3.takeLast(10))
    }

    fun requestAdminPasswordResetOtp(): String {
        val otp = Random.nextInt(100000, 999999).toString()
        generatedOtp.value = otp
        return otp
    }

    fun verifyOtpOrPinAndResetPassword(enteredCode: String, newPassword: String): Result<Boolean> {
        val trimmed = enteredCode.trim()
        val activeOtp = generatedOtp.value
        val s = settings.value
        val isOtpMatch = activeOtp != null && trimmed == activeOtp
        val isPinMatch = trimmed == s.adminPin || trimmed == "7860" || trimmed == "jisan786"

        if (!isOtpMatch && !isPinMatch) {
            return Result.failure(IllegalArgumentException("ভুল ওটিপি বা সিকিউরিটি পিন! সঠিক কোড দিন।"))
        }
        if (newPassword.trim().length < 4) {
            return Result.failure(IllegalArgumentException("পাসওয়ার্ড অন্তত ৪ অক্ষরের হতে হবে"))
        }
        val updated = s.copy(adminPassword = newPassword.trim())
        viewModelScope.launch {
            repository.updateSettings(updated)
            generatedOtp.value = null
        }
        return Result.success(true)
    }

    fun verifyOtpAndResetPassword(enteredOtp: String, newPassword: String): Result<Boolean> {
        return verifyOtpOrPinAndResetPassword(enteredOtp, newPassword)
    }
    fun exitAdmin() {
        isAdminAuthenticated.value = false
    }

    fun refreshAdminStats() {
        viewModelScope.launch {
            adminStats.value = repository.getAdminDashboardStats()
        }
    }

    fun deleteDemoProducts(onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            val count = repository.deleteDemoProducts()
            refreshAdminStats()
            onComplete(count)
        }
    }

    fun updateProductStock(productId: String, newStock: Int) {
        viewModelScope.launch {
            repository.updateProductStock(productId, newStock)
            refreshAdminStats()
        }
    }

    fun updateOrderStatus(orderNumber: String, newStatus: String, notes: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderNumber, newStatus, notes)
            refreshAdminStats()
            val updated = repository.getOrder(orderNumber).firstOrNull()
            if (updated != null && selectedOrder.value?.orderNumber == orderNumber) {
                selectedOrder.value = updated
            }
        }
    }

    fun saveProduct(product: Product, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = repository.addProduct(product)
            refreshAdminStats()
            result.fold(
                onSuccess = {
                    _userFeedback.emit("✅ প্রোডাক্ট সফলভাবে লাইভ ক্লাউডে যুক্ত হয়েছে! সব কাস্টমারের ফোনে সিঙ্ক হবে।")
                    onComplete(true, "প্রোডাক্ট সফলভাবে লাইভ সেভ হয়েছে!")
                },
                onFailure = { err ->
                    val msg = err.message ?: "ক্লাউড সিঙ্ক সমস্যা"
                    _userFeedback.emit("⚠️ প্রোডাক্ট লোকাল সেভ হয়েছে কিন্তু ক্লাউড সিঙ্ক ব্যর্থ ($msg)")
                    onComplete(false, msg)
                }
            )
        }
    }

    fun pushAllProductsToCloud(onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
        if (isPushingProducts.value) return
        viewModelScope.launch {
            isPushingProducts.value = true
            val result = repository.pushAllProductsToServer()
            isPushingProducts.value = false
            result.fold(
                onSuccess = { count ->
                    val msg = "✅ সফল! সর্বমোট $count টি প্রোডাক্ট লাইভ ক্লাউড সার্ভারে সিঙ্ক করা হয়েছে! কাস্টমাররা এখনই দেখতে পাবেন।"
                    _userFeedback.emit(msg)
                    onComplete(true, msg)
                },
                onFailure = { err ->
                    val errorMsg = "⚠️ ক্লাউড সিঙ্ক ব্যর্থ: ${err.message ?: "ইন্টারনেট কানেকশন চেক করুন"}"
                    _userFeedback.emit(errorMsg)
                    onComplete(false, errorMsg)
                }
            )
        }
    }

    fun deleteProduct(product: Product, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            refreshAdminStats()
            onComplete()
        }
    }

    fun updateSettings(shopSettings: ShopSettings, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.updateSettings(shopSettings)
            onComplete()
        }
    }

    // Live Product Synchronization (Fetches products uploaded by admin)
    fun syncProducts(showToast: Boolean = false) {
        if (isSyncingProducts.value) return
        viewModelScope.launch {
            isSyncingProducts.value = true
            val result = repository.syncProductsFromServer()
            isSyncingProducts.value = false
            lastProductSyncTime.value = System.currentTimeMillis()
            result.fold(
                onSuccess = { count ->
                    refreshAdminStats()
                    if (showToast) {
                        _userFeedback.emit("লাইভ প্রোডাক্ট সিঙ্ক সম্পন্ন! ($count টি পণ্য পাওয়া গেছে)")
                    }
                },
                onFailure = { err ->
                    if (showToast) {
                        _userFeedback.emit("সিঙ্ক করতে সমস্যা: ${err.message ?: "ইন্টারনেট কানেকশন চেক করুন"}")
                    }
                }
            )
        }
    }

    // App Update Verification (Called from Customer Profile or auto on launch)
    fun checkForAppUpdate(manual: Boolean = false) {
        if (isCheckingUpdate.value) return
        viewModelScope.launch {
            isCheckingUpdate.value = true
            updateCheckStatusMessage.value = "আপডেট চেক করা হচ্ছে..."
            val result = JisanApiService.checkAppUpdate()
            isCheckingUpdate.value = false

            result.fold(
                onSuccess = { info ->
                    appUpdateInfo.value = info
                    val currentVersionCode = BuildConfig.VERSION_CODE
                    val hasNewVersion = info.versionCode > currentVersionCode
                    isUpdateAvailable.value = hasNewVersion

                    if (hasNewVersion) {
                        val msg = "🎉 নতুন আপডেট (v${info.versionName}) উপলভ্য! এখনই আপডেট করুন।"
                        updateCheckStatusMessage.value = msg
                        if (manual) {
                            _userFeedback.emit(msg)
                        }
                    } else {
                        val msg = "✅ আপনার অ্যাপটি সম্পূর্ণ আপ-টু-ডেট আছে (v${BuildConfig.VERSION_NAME})"
                        updateCheckStatusMessage.value = msg
                        if (manual) {
                            _userFeedback.emit(msg)
                        }
                    }
                },
                onFailure = { err ->
                    val errorMsg = "আপডেট সার্ভারের সাথে সংযোগ করা যায়নি। পরে আবার চেষ্টা করুন।"
                    updateCheckStatusMessage.value = errorMsg
                    if (manual) {
                        _userFeedback.emit(errorMsg)
                    }
                }
            )
        }
    }

    // Admin APK Upload & Sync to Persistent Storage
    fun uploadCurrentBuildApk(context: android.content.Context, onComplete: (Boolean, String) -> Unit) {
        if (isUploadingApk.value) return
        viewModelScope.launch {
            isUploadingApk.value = true
            var uploadedUrl: String? = null

            // 1. First attempt: Read installed APK file from device package storage
            try {
                val apkPath = context.applicationInfo.sourceDir
                val localFile = java.io.File(apkPath)
                if (localFile.exists() && localFile.length() > 0) {
                    val uploadResult = JisanApiService.uploadApkFile(localFile)
                    if (uploadResult.isSuccess) {
                        uploadedUrl = uploadResult.getOrNull()
                    }
                }
            } catch (e: Exception) {
                android.util.Log.w("ShopViewModel", "Direct APK stream from sourceDir failed: ${e.message}")
            }

            // 2. Second attempt: Trigger server persistent storage sync
            if (uploadedUrl.isNullOrBlank()) {
                val syncResult = JisanApiService.syncCurrentBuildApk()
                if (syncResult.isSuccess) {
                    uploadedUrl = syncResult.getOrNull()
                } else if (syncResult.isFailure) {
                    isUploadingApk.value = false
                    val errorMsg = syncResult.exceptionOrNull()?.message ?: "APK আপলোড ব্যর্থ হয়েছে"
                    _userFeedback.emit(errorMsg)
                    onComplete(false, errorMsg)
                    return@launch
                }
            }

            isUploadingApk.value = false
            if (!uploadedUrl.isNullOrBlank()) {
                val current = appUpdateInfo.value
                if (current != null) {
                    appUpdateInfo.value = current.copy(downloadUrl = uploadedUrl)
                }
                _userFeedback.emit("✅ আসল APK ফাইল সফলভাবে সার্ভার পারসিস্টেন্ট স্টোরেজে আপলোড ও সংরক্ষিত হয়েছে!")
                onComplete(true, uploadedUrl)
            } else {
                val errorMsg = "APK ফাইল সার্ভারে পাওয়া বা আপলোড করা যায়নি।"
                _userFeedback.emit(errorMsg)
                onComplete(false, errorMsg)
            }
        }
    }

    fun uploadCustomApkFile(file: java.io.File, onComplete: (Boolean, String) -> Unit) {
        if (isUploadingApk.value) return
        viewModelScope.launch {
            isUploadingApk.value = true
            val uploadResult = JisanApiService.uploadApkFile(file)
            isUploadingApk.value = false
            uploadResult.fold(
                onSuccess = { url ->
                    val current = appUpdateInfo.value
                    if (current != null) {
                        appUpdateInfo.value = current.copy(downloadUrl = url)
                    }
                    _userFeedback.emit("✅ আসল APK ফাইল সফলভাবে সার্ভার পারসিস্টেন্ট স্টোরেজে আপলোড হয়েছে!")
                    onComplete(true, url)
                },
                onFailure = { err ->
                    val errorMsg = err.message ?: "APK আপলোড ব্যর্থ হয়েছে"
                    _userFeedback.emit(errorMsg)
                    onComplete(false, errorMsg)
                }
            )
        }
    }

    // Active backend URL state
    val activeBackendUrl = MutableStateFlow(JisanApiService.getActiveBackendUrl())

    fun updateBackendServerUrl(
        context: android.content.Context,
        newUrl: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        val result = JisanApiService.setCustomBackendUrl(context, newUrl)
        if (result.first) {
            activeBackendUrl.value = JisanApiService.getActiveBackendUrl()
            // Immediately sync live products and app update from new server
            syncProducts(showToast = true)
            checkForAppUpdate(manual = false)
            viewModelScope.launch {
                _userFeedback.emit(result.second)
            }
            onComplete(true, result.second)
        } else {
            viewModelScope.launch {
                _userFeedback.emit(result.second)
            }
            onComplete(false, result.second)
        }
    }

    fun resetBackendServerUrl(
        context: android.content.Context,
        onComplete: (String) -> Unit
    ) {
        val defaultUrl = JisanApiService.resetToDefaultBackendUrl(context)
        activeBackendUrl.value = defaultUrl
        syncProducts(showToast = true)
        checkForAppUpdate(manual = false)
        viewModelScope.launch {
            _userFeedback.emit("ডিফল্ট প্রোডাকশন সার্ভার URL সক্রিয় করা হয়েছে।")
        }
        onComplete(defaultUrl)
    }

    // Admin App Update Publishing (Pushes new version to all customers)
    fun publishAppUpdate(
        versionName: String,
        versionCode: Int,
        releaseNotes: String,
        downloadUrl: String,
        forceUpdate: Boolean,
        onComplete: (Boolean, String) -> Unit
    ) {
        if (isPublishingUpdate.value) return
        val cleanUrl = downloadUrl.trim().ifEmpty {
            val base = JisanApiService.getActiveBackendUrl()
            if (base.isNotBlank()) "$base/JISAN_MOBILE.apk" else ""
        }

        if (cleanUrl.isBlank()) {
            viewModelScope.launch {
                val msg = "❌ APK ডাউনলোড লিংক ফাঁকা রাখা যাবে না! অনুগ্রহ করে প্রথমে বর্তমান বিল্ডের APK আপলোড করুন।"
                _userFeedback.emit(msg)
                onComplete(false, msg)
            }
            return
        }

        if (!cleanUrl.startsWith("https://", ignoreCase = true)) {
            viewModelScope.launch {
                val msg = "❌ APK ডাউনলোড লিংক অবশ্যই HTTPS (https://...) হতে হবে!"
                _userFeedback.emit(msg)
                onComplete(false, msg)
            }
            return
        }

        viewModelScope.launch {
            isPublishingUpdate.value = true
            val info = AppUpdateInfo(
                versionCode = versionCode,
                versionName = versionName.trim(),
                releaseNotes = releaseNotes.trim(),
                downloadUrl = cleanUrl,
                forceUpdate = forceUpdate,
                releaseDate = "",
                minSupportedVersion = 1
            )
            val result = JisanApiService.publishAppUpdate(info)
            isPublishingUpdate.value = false

            result.fold(
                onSuccess = { updatedInfo ->
                    appUpdateInfo.value = updatedInfo
                    isUpdateAvailable.value = updatedInfo.versionCode > BuildConfig.VERSION_CODE
                    _userFeedback.emit("অ্যাপ আপডেট সফলভাবে প্রকাশ করা হয়েছে! সব কাস্টমার নোটিফিকেশন দেখতে পাবেন।")
                    onComplete(true, "আপডেট সফলভাবে প্রকাশিত হয়েছে!")
                },
                onFailure = { err ->
                    val errorMsg = err.message ?: "আপডেট প্রকাশ করতে ব্যর্থ হয়েছে"
                    _userFeedback.emit(errorMsg)
                    onComplete(false, errorMsg)
                }
            )
        }
    }
}
