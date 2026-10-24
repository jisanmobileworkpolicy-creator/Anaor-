package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Product
import com.example.data.model.ProductCondition
import com.example.ui.components.CategoryChipRow
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ProductCard
import com.example.ui.theme.JisanPrimary
import com.example.util.AppLanguage
import com.example.util.Strings
import com.example.viewmodel.ShopViewModel
import com.example.viewmodel.SortOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryProductsScreen(
    viewModel: ShopViewModel,
    onNavigateToProduct: (Product) -> Unit,
    onBack: (() -> Unit)? = null
) {
    val language by viewModel.currentLanguage.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val filteredProducts by viewModel.filteredProducts.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategoryFilter.collectAsState()
    val selectedCondition by viewModel.selectedConditionFilter.collectAsState()
    val sortOption by viewModel.selectedSortOption.collectAsState()
    val wishlistIds by viewModel.wishlistIds.collectAsState()
    val isSyncingProducts by viewModel.isSyncingProducts.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (language == AppLanguage.BENGALI) "পণ্য তালিকা ও সন্ধান" else "Products & Search",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = JisanPrimary)
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isSyncingProducts,
            onRefresh = { viewModel.syncProducts(showToast = true) },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
            Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchQuery.value = it },
                    placeholder = {
                        Text(
                            text = Strings.searchPlaceholder(language),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = JisanPrimary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("catalog_search_input"),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = JisanPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    singleLine = true
                )
            }

            CategoryChipRow(
                categories = categories,
                selectedCategoryId = selectedCategory,
                language = language,
                onCategorySelected = { viewModel.selectedCategoryFilter.value = it }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = selectedCondition == null,
                    onClick = { viewModel.selectedConditionFilter.value = null },
                    label = { Text(if (language == AppLanguage.BENGALI) "সকল অবস্থা" else "All Conditions", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = JisanPrimary.copy(alpha = 0.15f))
                )

                FilterChip(
                    selected = selectedCondition == ProductCondition.NEW.name,
                    onClick = {
                        viewModel.selectedConditionFilter.value =
                            if (selectedCondition == ProductCondition.NEW.name) null else ProductCondition.NEW.name
                    },
                    label = { Text(Strings.brandNew(language), fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = JisanPrimary.copy(alpha = 0.15f))
                )

                FilterChip(
                    selected = selectedCondition == ProductCondition.SECOND_HAND.name,
                    onClick = {
                        viewModel.selectedConditionFilter.value =
                            if (selectedCondition == ProductCondition.SECOND_HAND.name) null else ProductCondition.SECOND_HAND.name
                    },
                    label = { Text(Strings.secondHand(language), fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = JisanPrimary.copy(alpha = 0.15f))
                )

                FilterChip(
                    selected = sortOption == SortOption.PRICE_LOW_TO_HIGH,
                    onClick = {
                        viewModel.selectedSortOption.value =
                            if (sortOption == SortOption.PRICE_LOW_TO_HIGH) SortOption.POPULARITY else SortOption.PRICE_LOW_TO_HIGH
                    },
                    label = { Text(if (language == AppLanguage.BENGALI) "কম দাম আগে" else "Price: Low to High", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = JisanPrimary.copy(alpha = 0.15f))
                )

                FilterChip(
                    selected = sortOption == SortOption.PRICE_HIGH_TO_LOW,
                    onClick = {
                        viewModel.selectedSortOption.value =
                            if (sortOption == SortOption.PRICE_HIGH_TO_LOW) SortOption.POPULARITY else SortOption.PRICE_HIGH_TO_LOW
                    },
                    label = { Text(if (language == AppLanguage.BENGALI) "বেশি দাম আগে" else "Price: High to Low", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = JisanPrimary.copy(alpha = 0.15f))
                )
            }

            if (filteredProducts.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.SearchOff,
                    title = if (language == AppLanguage.BENGALI) "কোনো পণ্য খুঁজে পাওয়া যায়নি" else "No Products Found",
                    description = if (language == AppLanguage.BENGALI)
                        "অনুগ্রহ করে ফিল্টার পরিবর্তন করুন অথবা অন্য কোনো কিওয়ার্ড দিয়ে সন্ধান করুন।"
                    else
                        "Try adjusting your filters or search keywords.",
                    actionLabel = if (language == AppLanguage.BENGALI) "ফিল্টার রিসেট করুন" else "Reset Filters",
                    onActionClick = { viewModel.clearFilters() }
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 80.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        ProductCard(
                            product = product,
                            language = language,
                            isWishlisted = wishlistIds.contains(product.id),
                            onProductClick = onNavigateToProduct,
                            onAddToCart = { viewModel.addToCart(it) },
                            onToggleWishlist = { viewModel.toggleWishlist(it.id) }
                        )
                    }
                }
            }
        }
    }
}
}
