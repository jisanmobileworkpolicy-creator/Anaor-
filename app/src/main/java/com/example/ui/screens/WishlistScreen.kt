package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Product
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ProductCard
import com.example.ui.theme.JisanPrimary
import com.example.util.AppLanguage
import com.example.viewmodel.ShopViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistScreen(
    viewModel: ShopViewModel,
    onNavigateToProduct: (Product) -> Unit,
    onBack: () -> Unit,
    onContinueShopping: () -> Unit
) {
    val language by viewModel.currentLanguage.collectAsState()
    val wishlistProducts by viewModel.wishlistProducts.collectAsState()
    val wishlistIds by viewModel.wishlistIds.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (language == AppLanguage.BENGALI) "উইশ লিস্ট (${wishlistProducts.size})" else "My Wishlist (${wishlistProducts.size})",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = JisanPrimary)
            )
        }
    ) { innerPadding ->
        if (wishlistProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateView(
                    icon = Icons.Default.FavoriteBorder,
                    title = if (language == AppLanguage.BENGALI) "উইশ লিস্ট খালি" else "Your Wishlist is Empty",
                    description = if (language == AppLanguage.BENGALI)
                        "পছন্দের মোবাইল বা গ্যাজেট সেভ করে রাখতে হার্ট আইকনে চাপ দিন।"
                    else
                        "Tap the heart icon on any product to save it here for later.",
                    actionLabel = if (language == AppLanguage.BENGALI) "কেনাকাটা করুন" else "Explore Products",
                    onActionClick = onContinueShopping
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                items(wishlistProducts, key = { it.id }) { product ->
                    ProductCard(
                        product = product,
                        language = language,
                        isWishlisted = true,
                        onProductClick = onNavigateToProduct,
                        onAddToCart = { viewModel.addToCart(it) },
                        onToggleWishlist = { viewModel.toggleWishlist(it.id) }
                    )
                }
            }
        }
    }
}
