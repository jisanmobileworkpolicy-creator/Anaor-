package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderEntity
import com.example.data.model.Product
import com.example.ui.screens.AccountScreen
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.CartScreen
import com.example.ui.screens.CategoryProductsScreen
import com.example.ui.screens.CheckoutScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OrderConfirmationScreen
import com.example.ui.screens.OrdersHistoryScreen
import com.example.ui.screens.ProductDetailScreen
import com.example.ui.screens.WishlistScreen
import com.example.ui.theme.JisanAccent
import com.example.ui.theme.JisanPrimary
import com.example.util.AppLanguage
import com.example.util.Strings
import com.example.viewmodel.ShopViewModel
import kotlinx.coroutines.flow.collectLatest

enum class MainTab {
    HOME,
    CATEGORIES,
    CART,
    ORDERS,
    ACCOUNT
}

enum class SubScreen {
    NONE,
    PRODUCT_DETAIL,
    CHECKOUT,
    ORDER_CONFIRMATION,
    ADMIN,
    WISHLIST
}

@Composable
fun AppNavigation(viewModel: ShopViewModel) {
    val language by viewModel.currentLanguage.collectAsState()
    val cartCount by viewModel.cartItemCount.collectAsState()
    val selectedProduct by viewModel.selectedProduct.collectAsState()

    var currentTab by remember { mutableStateOf(MainTab.HOME) }
    var currentSubScreen by remember { mutableStateOf(SubScreen.NONE) }
    var confirmedOrder by remember { mutableStateOf<OrderEntity?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.userFeedback.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    // Sub-Screen Back Navigation with Android BackHandler
    when (currentSubScreen) {
        SubScreen.PRODUCT_DETAIL -> {
            BackHandler {
                currentSubScreen = SubScreen.NONE
                viewModel.selectProduct(null)
            }
            if (selectedProduct != null) {
                ProductDetailScreen(
                    product = selectedProduct!!,
                    viewModel = viewModel,
                    onBack = {
                        currentSubScreen = SubScreen.NONE
                        viewModel.selectProduct(null)
                    },
                    onNavigateToCheckout = {
                        currentSubScreen = SubScreen.CHECKOUT
                    }
                )
                return
            } else {
                currentSubScreen = SubScreen.NONE
            }
        }

        SubScreen.WISHLIST -> {
            BackHandler {
                currentSubScreen = SubScreen.NONE
            }
            WishlistScreen(
                viewModel = viewModel,
                onNavigateToProduct = { product ->
                    viewModel.selectProduct(product)
                    currentSubScreen = SubScreen.PRODUCT_DETAIL
                },
                onBack = { currentSubScreen = SubScreen.NONE },
                onContinueShopping = { currentSubScreen = SubScreen.NONE; currentTab = MainTab.HOME }
            )
            return
        }

        SubScreen.CHECKOUT -> {
            BackHandler {
                currentSubScreen = SubScreen.NONE
            }
            CheckoutScreen(
                viewModel = viewModel,
                onBack = { currentSubScreen = SubScreen.NONE },
                onOrderPlaced = { order ->
                    confirmedOrder = order
                    currentSubScreen = SubScreen.ORDER_CONFIRMATION
                }
            )
            return
        }

        SubScreen.ORDER_CONFIRMATION -> {
            BackHandler {
                currentSubScreen = SubScreen.NONE
                currentTab = MainTab.ORDERS
            }
            if (confirmedOrder != null) {
                OrderConfirmationScreen(
                    order = confirmedOrder!!,
                    viewModel = viewModel,
                    onViewOrders = {
                        currentSubScreen = SubScreen.NONE
                        currentTab = MainTab.ORDERS
                    },
                    onContinueShopping = {
                        currentSubScreen = SubScreen.NONE
                        currentTab = MainTab.HOME
                    }
                )
                return
            } else {
                currentSubScreen = SubScreen.NONE
            }
        }

        SubScreen.ADMIN -> {
            BackHandler {
                currentSubScreen = SubScreen.NONE
            }
            AdminScreen(
                viewModel = viewModel,
                onBack = {
                    currentSubScreen = SubScreen.NONE
                }
            )
            return
        }

        SubScreen.NONE -> {
            // Render primary bottom navigation
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                // Home
                NavigationBarItem(
                    selected = currentTab == MainTab.HOME,
                    onClick = { currentTab = MainTab.HOME },
                    icon = { Icon(Icons.Default.Home, contentDescription = Strings.navHome(language)) },
                    label = { Text(Strings.navHome(language), fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = JisanPrimary,
                        selectedTextColor = JisanPrimary,
                        indicatorColor = JisanPrimary.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.testTag("nav_tab_home")
                )

                // Categories
                NavigationBarItem(
                    selected = currentTab == MainTab.CATEGORIES,
                    onClick = { currentTab = MainTab.CATEGORIES },
                    icon = { Icon(Icons.Default.Category, contentDescription = Strings.navCategories(language)) },
                    label = { Text(Strings.navCategories(language), fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = JisanPrimary,
                        selectedTextColor = JisanPrimary,
                        indicatorColor = JisanPrimary.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.testTag("nav_tab_categories")
                )

                // Cart
                NavigationBarItem(
                    selected = currentTab == MainTab.CART,
                    onClick = { currentTab = MainTab.CART },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (cartCount > 0) {
                                    Badge(containerColor = JisanAccent, contentColor = androidx.compose.ui.graphics.Color.White) {
                                        Text("$cartCount", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = Strings.navCart(language))
                        }
                    },
                    label = { Text(Strings.navCart(language), fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = JisanPrimary,
                        selectedTextColor = JisanPrimary,
                        indicatorColor = JisanPrimary.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.testTag("nav_tab_cart")
                )

                // Orders
                NavigationBarItem(
                    selected = currentTab == MainTab.ORDERS,
                    onClick = { currentTab = MainTab.ORDERS },
                    icon = { Icon(Icons.Default.ReceiptLong, contentDescription = Strings.navOrders(language)) },
                    label = { Text(Strings.navOrders(language), fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = JisanPrimary,
                        selectedTextColor = JisanPrimary,
                        indicatorColor = JisanPrimary.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.testTag("nav_tab_orders")
                )

                // Account
                NavigationBarItem(
                    selected = currentTab == MainTab.ACCOUNT,
                    onClick = { currentTab = MainTab.ACCOUNT },
                    icon = { Icon(Icons.Default.Person, contentDescription = Strings.navAccount(language)) },
                    label = { Text(Strings.navAccount(language), fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = JisanPrimary,
                        selectedTextColor = JisanPrimary,
                        indicatorColor = JisanPrimary.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.testTag("nav_tab_account")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                MainTab.HOME -> HomeScreen(
                    viewModel = viewModel,
                    onNavigateToProduct = { product ->
                        viewModel.selectProduct(product)
                        currentSubScreen = SubScreen.PRODUCT_DETAIL
                    },
                    onNavigateToCategory = { catId ->
                        viewModel.selectedCategoryFilter.value = catId
                        currentTab = MainTab.CATEGORIES
                    },
                    onNavigateToCart = { currentTab = MainTab.CART },
                    onNavigateToWishlist = { currentSubScreen = SubScreen.WISHLIST },
                    onOpenSearch = { currentTab = MainTab.CATEGORIES }
                )

                MainTab.CATEGORIES -> CategoryProductsScreen(
                    viewModel = viewModel,
                    onNavigateToProduct = { product ->
                        viewModel.selectProduct(product)
                        currentSubScreen = SubScreen.PRODUCT_DETAIL
                    }
                )

                MainTab.CART -> CartScreen(
                    viewModel = viewModel,
                    onNavigateToCheckout = { currentSubScreen = SubScreen.CHECKOUT },
                    onContinueShopping = { currentTab = MainTab.HOME }
                )

                MainTab.ORDERS -> OrdersHistoryScreen(
                    viewModel = viewModel,
                    onContinueShopping = { currentTab = MainTab.HOME }
                )

                MainTab.ACCOUNT -> AccountScreen(
                    viewModel = viewModel,
                    onNavigateToAdmin = { currentSubScreen = SubScreen.ADMIN }
                )
            }
        }
    }
}
