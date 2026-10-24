package com.example.data.repository

import com.example.data.local.BannerDao
import com.example.data.local.CartDao
import com.example.data.local.CategoryDao
import com.example.data.local.CustomerDao
import com.example.data.local.OrderDao
import com.example.data.local.ProductDao
import com.example.data.local.SettingsDao
import com.example.data.local.WishlistDao
import com.example.data.model.CartItem
import com.example.data.model.CartItemDetail
import com.example.data.model.Category
import com.example.data.model.CustomerProfile
import com.example.data.model.OrderEntity
import com.example.data.model.OrderStatus
import com.example.data.model.Product
import com.example.data.model.PromotionalBanner
import com.example.data.model.ShopSettings
import com.example.data.model.WishlistItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

data class CartCalculation(
    val items: List<CartItemDetail>,
    val subtotal: Double,
    val deliveryFee: Double,
    val discount: Double,
    val total: Double,
    val hasUnavailableItems: Boolean,
    val appliedReferralCode: String = ""
)

data class AdminDashboardStats(
    val totalProducts: Int,
    val totalOrders: Int,
    val pendingOrders: Int,
    val totalSales: Double,
    val lowStockCount: Int
)

class ShopRepository(
    private val productDao: ProductDao,
    private val categoryDao: CategoryDao,
    private val cartDao: CartDao,
    private val orderDao: OrderDao,
    private val bannerDao: BannerDao,
    private val settingsDao: SettingsDao,
    private val wishlistDao: WishlistDao,
    private val customerDao: CustomerDao
) {
    // Products
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val availableProducts: Flow<List<Product>> = productDao.getAvailableProducts()
    val bestSellers: Flow<List<Product>> = productDao.getBestSellers()
    val newArrivals: Flow<List<Product>> = productDao.getNewArrivals()
    val secondHandProducts: Flow<List<Product>> = productDao.getSecondHandProducts()

    fun getProduct(id: String): Flow<Product?> = productDao.getProductById(id)

    // Categories & Banners
    val visibleCategories: Flow<List<Category>> = categoryDao.getVisibleCategories()
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()
    val activeBanners: Flow<List<PromotionalBanner>> = bannerDao.getActiveBanners()
    val settings: Flow<ShopSettings?> = settingsDao.getSettings()

    // Wishlist
    val wishlistIds: Flow<Set<String>> = wishlistDao.getAllWishlistItems()
        .map { list -> list.map { it.productId }.toSet() }

    val wishlistCount: Flow<Int> = wishlistDao.getWishlistCount()

    suspend fun toggleWishlist(productId: String) {
        if (wishlistDao.isWishlistedSync(productId)) {
            wishlistDao.removeFromWishlist(productId)
        } else {
            wishlistDao.addToWishlist(WishlistItem(productId))
        }
    }

    // Customer Profile
    val customerProfile: Flow<CustomerProfile?> = customerDao.getProfile()

    suspend fun saveCustomerProfile(name: String, phone: String, address: String, locality: String, pinCode: String) {
        val existing = customerDao.getProfileSync() ?: CustomerProfile()
        customerDao.saveProfile(
            existing.copy(
                name = name,
                phone = phone,
                address = address,
                locality = locality,
                pinCode = pinCode,
                isLoggedIn = true
            )
        )
    }

    suspend fun logoutCustomer() {
        customerDao.logout()
    }

    // Dynamic Cart with order-based delivery & referral discount
    fun observeCart(appliedReferralCode: String = ""): Flow<CartCalculation> = combine(
        cartDao.getAllCartItems(),
        productDao.getAllProducts(),
        settingsDao.getSettings()
    ) { cartItems, products, currentSettings ->
        val productMap = products.associateBy { it.id }
        val itemDetails = mutableListOf<CartItemDetail>()
        var subtotal = 0.0
        var hasUnavailable = false

        for (item in cartItems) {
            val product = productMap[item.productId]
            if (product != null) {
                itemDetails.add(CartItemDetail(item, product))
                if (product.isInStock) {
                    subtotal += product.price * item.quantity
                } else {
                    hasUnavailable = true
                }
            }
        }

        val settingsObj = currentSettings ?: ShopSettings()
        // Delivery fee based on order total
        val delivery = if (subtotal == 0.0 || subtotal >= settingsObj.freeDeliveryAbove) 0.0 else settingsObj.deliveryFee

        // Referral discount (e.g. ₹100 if referral code is applied and subtotal > 0)
        val discount = if (appliedReferralCode.isNotBlank() && subtotal > 0) {
            settingsObj.referralDiscountAmount.coerceAtMost(subtotal)
        } else 0.0

        val total = if (subtotal > 0.0) (subtotal - discount + delivery).coerceAtLeast(0.0) else 0.0

        CartCalculation(
            items = itemDetails,
            subtotal = subtotal,
            deliveryFee = delivery,
            discount = discount,
            total = total,
            hasUnavailableItems = hasUnavailable,
            appliedReferralCode = appliedReferralCode
        )
    }

    val cartItemCount: Flow<Int> = cartDao.getCartCount()

    suspend fun addToCart(productId: String, quantity: Int = 1, color: String = "", storage: String = "") {
        val existing = cartDao.getCartItem(productId)
        val product = productDao.getProductByIdSync(productId) ?: return
        val maxAvailable = product.stock

        val newQuantity = if (existing != null) {
            (existing.quantity + quantity).coerceAtMost(maxAvailable)
        } else {
            quantity.coerceAtMost(maxAvailable)
        }

        if (newQuantity > 0) {
            cartDao.insertOrUpdateCartItem(
                CartItem(
                    productId = productId,
                    quantity = newQuantity,
                    selectedColor = color.ifEmpty { existing?.selectedColor ?: "" },
                    selectedStorage = storage.ifEmpty { existing?.selectedStorage ?: "" },
                    addedAt = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun updateCartQuantity(productId: String, quantity: Int) {
        if (quantity <= 0) {
            cartDao.deleteCartItem(productId)
        } else {
            val product = productDao.getProductByIdSync(productId)
            val maxAvailable = product?.stock ?: 1
            cartDao.updateQuantity(productId, quantity.coerceAtMost(maxAvailable))
        }
    }

    suspend fun removeFromCart(productId: String) {
        cartDao.deleteCartItem(productId)
    }

    suspend fun clearCart() {
        cartDao.clearCart()
    }

    // Orders
    val allOrders: Flow<List<OrderEntity>> = orderDao.getAllOrders()

    fun getOrder(orderNumber: String): Flow<OrderEntity?> = orderDao.getOrderByNumber(orderNumber)

    suspend fun placeOrder(
        customerName: String,
        customerPhone: String,
        addressLine: String,
        locality: String,
        district: String,
        pinCode: String,
        cartItems: List<CartItemDetail>,
        deliveryFee: Double,
        subtotal: Double,
        discountAmount: Double,
        totalAmount: Double,
        paymentMethod: String,
        transactionRef: String = "",
        appliedReferralCode: String = ""
    ): Result<OrderEntity> {
        if (customerName.isBlank()) return Result.failure(IllegalArgumentException("Name cannot be empty"))
        val cleanPhone = customerPhone.replace("[^0-9]".toRegex(), "")
        if (cleanPhone.length < 10) return Result.failure(IllegalArgumentException("Please enter a valid 10-digit mobile number"))
        if (addressLine.isBlank()) return Result.failure(IllegalArgumentException("Address line cannot be empty"))
        if (pinCode.isBlank() || pinCode.length < 6) return Result.failure(IllegalArgumentException("Please enter a valid 6-digit PIN code"))
        if (cartItems.isEmpty()) return Result.failure(IllegalStateException("Cart is empty"))

        // Re-validate stock and prices
        val itemsSummaryList = mutableListOf<String>()
        for (item in cartItems) {
            val currentProduct = productDao.getProductByIdSync(item.product.id)
                ?: return Result.failure(IllegalStateException("Product ${item.product.nameEn} is no longer available."))

            if (!currentProduct.isInStock || currentProduct.stock < item.cartItem.quantity) {
                return Result.failure(IllegalStateException("Product '${currentProduct.nameEn}' has insufficient stock (Available: ${currentProduct.stock})."))
            }

            val variantInfo = listOfNotNull(
                item.cartItem.selectedColor.takeIf { it.isNotEmpty() },
                item.cartItem.selectedStorage.takeIf { it.isNotEmpty() }
            ).joinToString("/")

            val variantLabel = if (variantInfo.isNotEmpty()) " ($variantInfo)" else ""
            itemsSummaryList.add("${item.product.nameEn}$variantLabel x ${item.cartItem.quantity} @ ₹${item.product.price.toInt()}")
        }

        // Deduct inventory
        for (item in cartItems) {
            val currentProduct = productDao.getProductByIdSync(item.product.id)!!
            val remainingStock = (currentProduct.stock - item.cartItem.quantity).coerceAtLeast(0)
            productDao.updateStock(currentProduct.id, remainingStock)
        }

        val dateStamp = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
        val randomSuffix = Random.nextInt(1000, 9999)
        val orderNumber = "JM-$dateStamp-$randomSuffix"

        val order = OrderEntity(
            orderNumber = orderNumber,
            customerName = customerName.trim(),
            customerPhone = cleanPhone,
            addressLine = addressLine.trim(),
            locality = locality.trim(),
            district = district.trim().ifEmpty { "Murshidabad" },
            state = "West Bengal",
            pinCode = pinCode.trim(),
            itemsJson = itemsSummaryList.joinToString(" | "),
            subtotalAmount = subtotal,
            deliveryFee = deliveryFee,
            discountAmount = discountAmount,
            totalAmount = totalAmount,
            paymentMethod = paymentMethod,
            transactionRef = transactionRef.trim(),
            appliedReferralCode = appliedReferralCode.trim(),
            status = OrderStatus.PENDING.code,
            trackingNotes = if (paymentMethod.contains("Prepaid", ignoreCase = true))
                "Prepaid UPI payment received (Ref: ${transactionRef.ifEmpty { "Pending verification" }}). Awaiting shop confirmation."
            else
                "Cash on Delivery order received. Awaiting shop verification.",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        orderDao.insertOrder(order)
        cartDao.clearCart()
        // Asynchronously sync order to backend server
        com.example.data.remote.JisanApiService.syncOrderToServer(order)

        return Result.success(order)
    }

    suspend fun cancelOrder(orderNumber: String, reason: String = "Cancelled by customer"): Result<Boolean> {
        val existing = orderDao.getOrderByNumberSync(orderNumber)
            ?: return Result.failure(IllegalArgumentException("Order not found"))

        if (!existing.canBeCancelled) {
            return Result.failure(IllegalStateException("Order cannot be cancelled as it is already being processed or dispatched."))
        }

        orderDao.updateOrderStatus(
            orderNumber = orderNumber,
            status = OrderStatus.CANCELLED.code,
            notes = reason,
            updatedAt = System.currentTimeMillis()
        )
        return Result.success(true)
    }

    // Admin Operations & Live Sync
    suspend fun syncProductsFromServer(): Result<Int> {
        val remoteResult = com.example.data.remote.JisanApiService.fetchProducts()
        return remoteResult.fold(
            onSuccess = { remoteProducts ->
                if (remoteProducts.isNotEmpty()) {
                    val remoteIds = remoteProducts.map { it.id }
                    productDao.deleteProductsNotIn(remoteIds)
                    productDao.insertProducts(remoteProducts)
                } else {
                    productDao.deleteAllProducts()
                }
                Result.success(remoteProducts.size)
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }

    suspend fun addProduct(product: Product): Result<Boolean> {
        productDao.insertProduct(product)
        // Immediately sync live to server so all customers receive it
        return com.example.data.remote.JisanApiService.syncProductToServer(product)
    }

    suspend fun updateProduct(product: Product): Result<Boolean> {
        productDao.updateProduct(product)
        return com.example.data.remote.JisanApiService.syncProductToServer(product)
    }

    suspend fun pushAllProductsToServer(): Result<Int> {
        val allLocal = productDao.getAllProductsList()
        return com.example.data.remote.JisanApiService.bulkSyncProducts(allLocal)
    }

    suspend fun deleteProduct(product: Product) {
        productDao.deleteProduct(product)
        com.example.data.remote.JisanApiService.deleteProductFromServer(product.id)
    }

    suspend fun deleteDemoProducts(): Int = productDao.deleteDemoProducts()
    suspend fun updateProductStock(productId: String, newStock: Int) {
        productDao.updateStock(productId, newStock)
        val updated = productDao.getProductByIdSync(productId)
        if (updated != null) {
            com.example.data.remote.JisanApiService.syncProductToServer(updated)
        }
    }
    suspend fun updateOrderStatus(orderNumber: String, newStatus: String, notes: String) {
        orderDao.updateOrderStatus(orderNumber, newStatus, notes, System.currentTimeMillis())
    }
    suspend fun updateSettings(settings: ShopSettings) = settingsDao.update(settings)

    suspend fun getAdminDashboardStats(): AdminDashboardStats {
        val totalProducts = productDao.getProductCount()
        val totalOrders = orderDao.getOrderCount()
        val pendingOrders = orderDao.getPendingOrderCount()
        val totalSales = orderDao.getTotalSales() ?: 0.0
        return AdminDashboardStats(
            totalProducts = totalProducts,
            totalOrders = totalOrders,
            pendingOrders = pendingOrders,
            totalSales = totalSales,
            lowStockCount = 0
        )
    }
}
