package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.CircularProgressIndicator
import com.example.ui.theme.JisanGreen
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.platform.LocalContext
import com.example.util.ShareHelper
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Product
import com.example.ui.components.BannerCarousel
import com.example.ui.components.CategoryChipRow
import com.example.ui.components.LanguageToggle
import com.example.ui.components.ProductCard
import com.example.ui.components.ReferralBanner
import com.example.ui.theme.JisanAccent
import com.example.ui.theme.JisanPrimary
import com.example.ui.theme.JisanPrimaryVariant
import com.example.util.AppLanguage
import com.example.util.Strings
import com.example.viewmodel.ShopViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ShopViewModel,
    onNavigateToProduct: (Product) -> Unit,
    onNavigateToCategory: (String?) -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToWishlist: () -> Unit,
    onOpenSearch: () -> Unit
) {
    val language by viewModel.currentLanguage.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val banners by viewModel.activeBanners.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val cartCount by viewModel.cartItemCount.collectAsState()
    val wishlistCount by viewModel.wishlistCount.collectAsState()
    val wishlistIds by viewModel.wishlistIds.collectAsState()
    val customerProfile by viewModel.customerProfile.collectAsState()
    val isUpdateAvailable by viewModel.isUpdateAvailable.collectAsState()
    val appUpdateInfo by viewModel.appUpdateInfo.collectAsState()
    val isSyncingProducts by viewModel.isSyncingProducts.collectAsState()

    val newMobiles by viewModel.newMobiles.collectAsState()
    val secondHandMobiles by viewModel.secondHandMobiles.collectAsState()
    val bestSellers by viewModel.bestSellers.collectAsState()
    val accessories by viewModel.accessories.collectAsState()

    val context = LocalContext.current
    val referralCode = customerProfile?.referralCode ?: "JMREF-786"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = Strings.appName(),
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = Strings.shopLocation(language),
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                        }
                    }
                },
                actions = {
                    // Live Sync Products Button
                    IconButton(
                        onClick = { viewModel.syncProducts(showToast = true) },
                        modifier = Modifier.testTag("home_sync_products_btn")
                    ) {
                        if (isSyncingProducts) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Sync Products",
                                tint = Color.White
                            )
                        }
                    }

                    // Share App Button
                    IconButton(
                        onClick = { ShareHelper.shareApp(context, referralCode) },
                        modifier = Modifier.testTag("home_share_app_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share App",
                            tint = Color.White
                        )
                    }

                    // Dark Mode / White Mode Toggle
                    IconButton(
                        onClick = { viewModel.toggleDarkMode() },
                        modifier = Modifier.testTag("theme_mode_toggle_btn")
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Dark Mode",
                            tint = Color.White
                        )
                    }

                    // Wishlist Icon
                    IconButton(
                        onClick = onNavigateToWishlist,
                        modifier = Modifier.testTag("home_wishlist_btn")
                    ) {
                        BadgedBox(
                            badge = {
                                if (wishlistCount > 0) {
                                    Badge(containerColor = Color(0xFFC62828), contentColor = Color.White) {
                                        Text("$wishlistCount", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Wishlist",
                                tint = Color.White
                            )
                        }
                    }

                    // Language Switch
                    LanguageToggle(
                        currentLanguage = language,
                        onToggle = { viewModel.toggleLanguage() }
                    )

                    Spacer(modifier = Modifier.width(2.dp))

                    // Cart Icon with Badge
                    IconButton(
                        onClick = onNavigateToCart,
                        modifier = Modifier.testTag("home_cart_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (cartCount > 0) {
                                    Badge(
                                        containerColor = JisanAccent,
                                        contentColor = Color.White
                                    ) {
                                        Text("$cartCount", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Cart",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = JisanPrimary
                )
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isSyncingProducts,
            onRefresh = {
                viewModel.syncProducts(showToast = true)
                viewModel.checkForAppUpdate(manual = false)
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
            // Slogan Strip
            item {
                Surface(
                    color = JisanPrimaryVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "✨ ${Strings.slogan(language)} ✨",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 5.dp, horizontal = 16.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            // Live App Update Available Alert Banner
            if (isUpdateAvailable && appUpdateInfo != null) {
                val rawUrl = appUpdateInfo?.downloadUrl?.trim() ?: ""
                val apkDownloadUrl = when {
                    rawUrl.startsWith("http://") || rawUrl.startsWith("https://") -> rawUrl
                    rawUrl.isNotBlank() -> "${com.example.data.remote.JisanApiService.getActiveBackendUrl()}/$rawUrl".replace("//", "/").replace(":/", "://")
                    else -> "${com.example.data.remote.JisanApiService.getActiveBackendUrl()}/JISAN_MOBILE.apk"
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .clickable {
                                try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(apkDownloadUrl))) } catch (_: Exception) {}
                            },
                        colors = CardDefaults.cardColors(containerColor = JisanGreen.copy(alpha = 0.14f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, JisanGreen)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CloudDownload,
                                contentDescription = null,
                                tint = JisanGreen,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "🎉 নতুন অ্যাপ আপডেট উপলভ্য (v${appUpdateInfo?.versionName})",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = JisanGreen
                                )
                                Text(
                                    text = appUpdateInfo?.releaseNotes?.ifBlank { "নতুন ফিচার ও আপডেট পেতে এখনই ইনস্টল করুন।" } ?: "",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 2
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            androidx.compose.material3.Button(
                                onClick = {
                                    try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(apkDownloadUrl))) } catch (_: Exception) {}
                                },
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = JisanGreen),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("আপডেট", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // REFER & EARN BANNER - Placed at the very top as requested
            item {
                Box(modifier = Modifier.padding(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 4.dp)) {
                    ReferralBanner(
                        referralCode = referralCode,
                        language = language,
                        onCopied = {}
                    )
                }
            }

            // Search Bar
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .clickable { onOpenSearch() }
                ) {
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        enabled = false,
                        placeholder = {
                            Text(
                                text = Strings.searchPlaceholder(language),
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = JisanPrimary
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_search_box"),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledContainerColor = MaterialTheme.colorScheme.surface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            // Promotional Banners Carousel
            if (banners.isNotEmpty()) {
                item {
                    Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) {
                        BannerCarousel(
                            banners = banners,
                            language = language,
                            onBannerClick = { banner ->
                                if (banner.targetCategoryId != null) {
                                    onNavigateToCategory(banner.targetCategoryId)
                                }
                            }
                        )
                    }
                }
            }

            // Category Chips Row
            item {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Text(
                        text = Strings.navCategories(language),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                    )
                    CategoryChipRow(
                        categories = categories,
                        selectedCategoryId = null,
                        language = language,
                        onCategorySelected = { catId -> onNavigateToCategory(catId) }
                    )
                }
            }

            // Section: New Mobiles
            if (newMobiles.isNotEmpty()) {
                item {
                    ProductSectionRow(
                        title = Strings.secNewMobiles(language),
                        products = newMobiles,
                        wishlistIds = wishlistIds,
                        language = language,
                        onProductClick = onNavigateToProduct,
                        onAddToCart = { viewModel.addToCart(it) },
                        onToggleWishlist = { viewModel.toggleWishlist(it.id) },
                        onViewAllClick = { onNavigateToCategory("cat_new_smartphones") }
                    )
                }
            }

            // Section: Second-Hand Mobiles
            if (secondHandMobiles.isNotEmpty()) {
                item {
                    ProductSectionRow(
                        title = Strings.secSecondHand(language),
                        products = secondHandMobiles,
                        wishlistIds = wishlistIds,
                        language = language,
                        onProductClick = onNavigateToProduct,
                        onAddToCart = { viewModel.addToCart(it) },
                        onToggleWishlist = { viewModel.toggleWishlist(it.id) },
                        onViewAllClick = { onNavigateToCategory("cat_second_hand") }
                    )
                }
            }

            // Section: Best Sellers
            if (bestSellers.isNotEmpty()) {
                item {
                    ProductSectionRow(
                        title = Strings.secBestSellers(language),
                        products = bestSellers,
                        wishlistIds = wishlistIds,
                        language = language,
                        onProductClick = onNavigateToProduct,
                        onAddToCart = { viewModel.addToCart(it) },
                        onToggleWishlist = { viewModel.toggleWishlist(it.id) },
                        onViewAllClick = { onNavigateToCategory(null) }
                    )
                }
            }

            // Section: Mobile Accessories
            if (accessories.isNotEmpty()) {
                item {
                    ProductSectionRow(
                        title = Strings.secAccessories(language),
                        products = accessories,
                        wishlistIds = wishlistIds,
                        language = language,
                        onProductClick = onNavigateToProduct,
                        onAddToCart = { viewModel.addToCart(it) },
                        onToggleWishlist = { viewModel.toggleWishlist(it.id) },
                        onViewAllClick = { onNavigateToCategory("cat_chargers") }
                    )
                }
            }

            // Empty catalog placeholder
            val hasNoProducts = newMobiles.isEmpty() && secondHandMobiles.isEmpty() && bestSellers.isEmpty() && accessories.isEmpty()
            if (hasNoProducts && !isSyncingProducts) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = null,
                                modifier = Modifier.size(44.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "বর্তমানে দোকানে কোনো পণ্য যোগ করা নেই",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "অ্যাডমিন নতুন প্রোডাক্ট আপলোড করলে তা এখানে দেখতে পাবেন। লাইভ রিফ্রেশ করতে স্ক্রিনটি নিচে টেনে ছেড়ে দিন অথবা নিচের বাটনে চাপুন।",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { viewModel.syncProducts(showToast = true) },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("পণ্য তালিকা রিফ্রেশ করুন", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
fun ProductSectionRow(
    title: String,
    products: List<Product>,
    wishlistIds: Set<String>,
    language: AppLanguage,
    onProductClick: (Product) -> Unit,
    onAddToCart: (Product) -> Unit,
    onToggleWishlist: (Product) -> Unit,
    onViewAllClick: () -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onViewAllClick() }
            ) {
                Text(
                    text = Strings.secViewAll(language),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = JisanPrimary
                )
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = JisanPrimary,
                    modifier = Modifier.size(13.dp)
                )
            }
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(products, key = { it.id }) { product ->
                ProductCard(
                    product = product,
                    language = language,
                    isWishlisted = wishlistIds.contains(product.id),
                    onProductClick = onProductClick,
                    onAddToCart = onAddToCart,
                    onToggleWishlist = onToggleWishlist,
                    modifier = Modifier.width(170.dp)
                )
            }
        }
    }
}
