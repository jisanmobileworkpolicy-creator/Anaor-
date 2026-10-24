package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Checkbox
import com.example.data.model.Category
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import coil.compose.AsyncImage
import java.io.File
import java.io.FileOutputStream
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import com.example.BuildConfig
import com.example.util.ShareHelper
import android.content.Intent
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.example.data.remote.JisanApiService
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderEntity
import com.example.data.model.OrderStatus
import com.example.data.model.Product
import com.example.data.model.ProductCondition
import com.example.data.model.ShopSettings
import com.example.ui.theme.JisanAccent
import com.example.ui.theme.JisanBorder
import com.example.ui.theme.JisanGreen
import com.example.ui.theme.JisanGreenLight
import com.example.ui.theme.JisanPrimary
import com.example.ui.theme.JisanRed
import com.example.util.AppLanguage
import com.example.util.Formatters
import com.example.viewmodel.ShopViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: ShopViewModel,
    onBack: () -> Unit
) {
    val language by viewModel.currentLanguage.collectAsState()
    val stats by viewModel.adminStats.collectAsState()
    val allOrders by viewModel.allOrders.collectAsState()
    val allProducts by viewModel.filteredProducts.collectAsState()
    val categories by viewModel.allCategories.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val isPushingProducts by viewModel.isPushingProducts.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showProductDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var showClearDemoDialog by remember { mutableStateOf(false) }
    var statusUpdateOrder by remember { mutableStateOf<OrderEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "JISAN MOBILE অ্যাডমিন কনসোল",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Shop Management Console",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
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
                actions = {
                    IconButton(onClick = { viewModel.refreshAdminStats() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
                    }
                    IconButton(onClick = {
                        viewModel.exitAdmin()
                        onBack()
                    }) {
                        Icon(Icons.Default.Logout, contentDescription = "Exit Admin", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = JisanPrimary)
            )
        },
        floatingActionButton = {
            if (selectedTab == 2) {
                FloatingActionButton(
                    onClick = {
                        editingProduct = null
                        showProductDialog = true
                    },
                    containerColor = JisanPrimary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("admin_add_product_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Product")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = JisanPrimary,
                edgePadding = 12.dp
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("ওভারভিউ", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("অর্ডারসমূহ (${allOrders.size})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("পণ্য তালিকা", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) })
                Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("UPI ও সেটিংস", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) })
                Tab(selected = selectedTab == 4, onClick = { selectedTab = 4 }, text = { Text("অ্যাপ আপডেট", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) })
            }

            when (selectedTab) {
                0 -> AdminOverviewTab(stats = stats, onClearDemoClicked = { showClearDemoDialog = true })
                1 -> AdminOrdersTab(orders = allOrders, language = language, onUpdateStatusClicked = { statusUpdateOrder = it })
                2 -> AdminProductsTab(
                    products = allProducts,
                    language = language,
                    isPushingProducts = isPushingProducts,
                    onPushAllToCloud = { viewModel.pushAllProductsToCloud() },
                    onEditProduct = {
                        editingProduct = it
                        showProductDialog = true
                    },
                    onDeleteProduct = { product -> viewModel.deleteProduct(product) {} },
                    onUpdateStock = { id, stock -> viewModel.updateProductStock(id, stock) }
                )
                3 -> AdminSettingsTab(
                    settings = settings,
                    onSaveSettings = { updated -> viewModel.updateSettings(updated) {} }
                )
                4 -> AdminUpdateManagementTab(
                    viewModel = viewModel,
                    language = language
                )
            }
        }
    }

    if (showProductDialog) {
        ProductEditorDialog(
            initialProduct = editingProduct,
            categories = categories,
            onDismiss = { showProductDialog = false },
            onSave = { product: Product, onFinished ->
                viewModel.saveProduct(product) { success, msg ->
                    onFinished(success, msg)
                }
            }
        )
    }

    if (showClearDemoDialog) {
        AlertDialog(
            onDismissRequest = { showClearDemoDialog = false },
            icon = { Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = JisanRed) },
            title = { Text("সব ডেমো পণ্য মুছে ফেলতে চান?") },
            text = { Text("এটি প্রিভিউয়ের জন্য তৈরি করা সমস্ত [DEMO] পণ্য স্থায়ীভাবে মুছে ফেলবে। আপনার নিজস্ব যুক্ত পণ্যগুলো অক্ষত থাকবে।") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteDemoProducts { showClearDemoDialog = false }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JisanRed)
                ) { Text("মুছে ফেলুন") }
            },
            dismissButton = {
                TextButton(onClick = { showClearDemoDialog = false }) { Text("বাতিল") }
            }
        )
    }

    if (statusUpdateOrder != null) {
        val currentOrder = statusUpdateOrder!!
        var newStatus by remember { mutableStateOf(currentOrder.status) }
        var notes by remember { mutableStateOf(currentOrder.trackingNotes) }

        AlertDialog(
            onDismissRequest = { statusUpdateOrder = null },
            title = { Text("অর্ডার স্ট্যাটাস আপডেট: ${currentOrder.orderNumber}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OrderStatus.entries.forEach { st ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable { newStatus = st.code }.padding(vertical = 4.dp)
                        ) {
                            androidx.compose.material3.RadioButton(
                                selected = newStatus == st.code,
                                onClick = { newStatus = st.code }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "${st.bnLabel} (${st.enLabel})", fontSize = 12.sp)
                        }
                    }

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("ট্র্যাকিং নোট / মেসেজ") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateOrderStatus(currentOrder.orderNumber, newStatus, notes)
                        statusUpdateOrder = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JisanPrimary)
                ) { Text("সংরক্ষণ করুন") }
            },
            dismissButton = {
                TextButton(onClick = { statusUpdateOrder = null }) { Text("বাতিল") }
            }
        )
    }
}

@Composable
fun AdminOverviewTab(
    stats: com.example.data.repository.AdminDashboardStats?,
    onClearDemoClicked: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricCard(title = "মোট বিক্রি (Sales)", value = Formatters.formatPrice(stats?.totalSales ?: 0.0), icon = Icons.Default.Money, color = JisanGreen, modifier = Modifier.weight(1f))
            MetricCard(title = "মোট অর্ডার (Orders)", value = "${stats?.totalOrders ?: 0}", icon = Icons.Default.ShoppingCart, color = JisanPrimary, modifier = Modifier.weight(1f))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricCard(title = "অপেক্ষারত (Pending)", value = "${stats?.pendingOrders ?: 0}", icon = Icons.Default.PendingActions, color = JisanAccent, modifier = Modifier.weight(1f))
            MetricCard(title = "মোট পণ্য (Products)", value = "${stats?.totalProducts ?: 0}", icon = Icons.Default.Inventory, color = Color(0xFF0288D1), modifier = Modifier.weight(1f))
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFCC80))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = JisanAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ডেমো প্রিভিউ পণ্য পরিষ্কার", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text("১ ক্লিকেই সমস্ত প্রাথমিক [DEMO] পণ্য মুছে ফেলতে পারবেন।", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(onClick = onClearDemoClicked, colors = ButtonDefaults.outlinedButtonColors(contentColor = JisanRed)) {
                    Text("ডেমো পণ্যগুলো মুছুন (Clear Demo Items)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AdminOrdersTab(
    orders: List<OrderEntity>,
    language: AppLanguage,
    onUpdateStatusClicked: (OrderEntity) -> Unit
) {
    if (orders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("এখনও কোনো গ্রাহক অর্ডার আসেনি।", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(orders, key = { it.orderNumber }) { order ->
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(text = order.orderNumber, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = JisanPrimary)
                            Surface(shape = RoundedCornerShape(4.dp), color = if (order.status == "DELIVERED") JisanGreenLight else Color(0xFFFFF8E1)) {
                                Text(text = order.orderStatus.bnLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (order.status == "DELIVERED") JisanGreen else JisanAccent, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "গ্রাহক: ${order.customerName} (${order.customerPhone})", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "ঠিকানা: ${order.addressLine}, ${order.locality}, ${order.district} - ${order.pinCode}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "পণ্য: ${order.itemsJson}", fontSize = 12.sp)

                        // Payment Details & UPI UTR
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (order.paymentMethod.contains("Prepaid", ignoreCase = true)) Color(0xFFE3F2FD) else Color(0xFFE8F5E9)
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                                Text(
                                    text = "পেমেন্ট পদ্ধতি: ${order.paymentMethod}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (order.paymentMethod.contains("Prepaid", ignoreCase = true)) JisanPrimary else JisanGreen
                                )
                                if (order.transactionRef.isNotEmpty()) {
                                    Text(
                                        text = "🔍 ইউপিআই UTR / Ref: ${order.transactionRef}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = JisanPrimary
                                    )
                                }
                            }
                        }

                        if (order.appliedReferralCode.isNotEmpty()) {
                            Text(text = "🏷️ রেফার কোড ব্যবহার: ${order.appliedReferralCode} (-₹${order.discountAmount.toInt()})", fontSize = 11.sp, color = JisanGreen, fontWeight = FontWeight.Bold)
                        }

                        Text(text = "সর্বমোট প্রদেয়: ${Formatters.formatPrice(order.totalAmount)}", fontSize = 13.sp, fontWeight = FontWeight.Black, color = JisanPrimary)

                        if (order.trackingNotes.isNotEmpty()) {
                            Text(text = "নোট: ${order.trackingNotes}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { onUpdateStatusClicked(order) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(6.dp), colors = ButtonDefaults.buttonColors(containerColor = JisanPrimary)) {
                            Text("স্ট্যাটাস আপডেট করুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminProductsTab(
    products: List<Product>,
    language: AppLanguage,
    isPushingProducts: Boolean = false,
    onPushAllToCloud: () -> Unit = {},
    onEditProduct: (Product) -> Unit,
    onDeleteProduct: (Product) -> Unit,
    onUpdateStock: (String, Int) -> Unit
) {
    LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 80.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = JisanPrimary.copy(alpha = 0.08f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, JisanPrimary.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "☁️ কাস্টমারদের ফোনে লাইভ সিঙ্ক",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = JisanPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "আপনার ফোনের সবকটি প্রোডাক্ট (${products.size} টি) লাইভ সার্ভারে পাঠান যাতে কাস্টমারদের ফোনে তৎক্ষণাৎ যুক্ত হয়ে যায়।",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onPushAllToCloud,
                            enabled = !isPushingProducts,
                            colors = ButtonDefaults.buttonColors(containerColor = JisanPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            if (isPushingProducts) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("সিঙ্ক হচ্ছে...", fontSize = 11.sp)
                            } else {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("লাইভ পাঠান", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
        items(products, key = { it.id }) { product ->
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    // Product Photo Thumbnail
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (product.imageUrl.isNotBlank()) {
                            AsyncImage(
                                model = product.imageUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = if (product.isSecondHand) Icons.Default.Inventory else Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = if (product.isSecondHand) JisanAccent else JisanPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = product.nameBn.ifEmpty { product.nameEn }, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        val catLabel = com.example.data.seed.InitialData.categories.firstOrNull { it.id == product.categoryId }?.nameBn ?: ""
                        Text(text = "${product.brand} • ${if (product.isSecondHand) "সেকেন্ড-হ্যান্ড" else "নতুন"}${if (catLabel.isNotEmpty()) " • $catLabel" else ""}", fontSize = 11.sp, color = JisanPrimary)
                        Text(text = "${Formatters.formatPrice(product.price)} (MRP: ${Formatters.formatPrice(product.mrp)})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "স্টক: ${product.stock} টি", fontSize = 11.sp, color = if (product.isInStock) JisanGreen else JisanRed, fontWeight = FontWeight.Bold)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        IconButton(onClick = { onEditProduct(product) }) { Icon(Icons.Default.Edit, contentDescription = "Edit", tint = JisanPrimary) }
                        IconButton(onClick = { onDeleteProduct(product) }) { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = JisanRed) }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminSettingsTab(
    settings: ShopSettings,
    onSaveSettings: (ShopSettings) -> Unit
) {
    var upiId by remember { mutableStateOf(settings.upiId) }
    var upiName by remember { mutableStateOf(settings.upiName) }
    var adminUsername by remember { mutableStateOf(settings.adminUsername) }
    var adminPassword by remember { mutableStateOf(settings.adminPassword) }
    var adminMobile by remember { mutableStateOf(settings.adminMobile) }
    var deliveryFee by remember { mutableStateOf(settings.deliveryFee.toInt().toString()) }
    var freeThreshold by remember { mutableStateOf(settings.freeDeliveryAbove.toInt().toString()) }
    var referralDiscount by remember { mutableStateOf(settings.referralDiscountAmount.toInt().toString()) }
    var primaryPhone by remember { mutableStateOf(settings.primaryPhone) }
    var savedMessage by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("অফিসিয়াল UPI QR ও পেমেন্ট কনফিগারেশন:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = JisanPrimary)

        OutlinedTextField(
            value = upiId,
            onValueChange = { upiId = it },
            label = { Text("দোকানের UPI ID (যেখানে টাকা আসবে)") },
            placeholder = { Text("e.g. 9382302242@upi") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = upiName,
            onValueChange = { upiName = it },
            label = { Text("UPI Account Name") },
            modifier = Modifier.fillMaxWidth()
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        Text("অর্ডার নির্ভর ডেলিভারি চার্জ ও রেফারেল ছাড়:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = JisanPrimary)

        OutlinedTextField(
            value = deliveryFee,
            onValueChange = { deliveryFee = it },
            label = { Text("স্ট্যান্ডার্ড ডেলিভারি চার্জ (₹)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = freeThreshold,
            onValueChange = { freeThreshold = it },
            label = { Text("কত টাকার অর্ডারে ফ্রি ডেলিভারি (₹)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = referralDiscount,
            onValueChange = { referralDiscount = it },
            label = { Text("রেফার করলে কাস্টমার ডিসকাউন্ট (₹)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        Text("অ্যাডমিন আইডি, পাসওয়ার্ড ও সিকিউরিটি মোবাইল:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = JisanPrimary)

        OutlinedTextField(
            value = adminUsername,
            onValueChange = { adminUsername = it },
            label = { Text("অ্যাডমিন লগইন আইডি") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = adminPassword,
            onValueChange = { adminPassword = it },
            label = { Text("অ্যাডমিন লগইন পাসওয়ার্ড") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = adminMobile,
            onValueChange = { adminMobile = it },
            label = { Text("পাসওয়ার্ড রিসেটের জন্য রেজিস্টার্ড মোবাইল (OTP)") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                val updated = settings.copy(
                    upiId = upiId.trim(),
                    upiName = upiName.trim(),
                    deliveryFee = deliveryFee.toDoubleOrNull() ?: 40.0,
                    freeDeliveryAbove = freeThreshold.toDoubleOrNull() ?: 499.0,
                    referralDiscountAmount = referralDiscount.toDoubleOrNull() ?: 100.0,
                    adminUsername = adminUsername.trim(),
                    adminPassword = adminPassword.trim(),
                    adminMobile = adminMobile.trim()
                )
                onSaveSettings(updated)
                savedMessage = true
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = JisanPrimary),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("সমস্ত সেটিংস সংরক্ষণ করুন", fontWeight = FontWeight.Bold)
        }

        if (savedMessage) {
            Text("✅ সেটিংস সফলভাবে আপডেট করা হয়েছে!", color = JisanGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ProductEditorDialog(
    initialProduct: Product?,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onSave: (Product, onFinished: (Boolean, String) -> Unit) -> Unit
) {
    val isEdit = initialProduct != null
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isUploadingImage by remember { mutableStateOf(false) }
    var isSavingProduct by remember { mutableStateOf(false) }
    var uploadStatusMessage by remember { mutableStateOf<String?>(null) }
    var nameEn by remember { mutableStateOf(initialProduct?.nameEn ?: "") }
    var nameBn by remember { mutableStateOf(initialProduct?.nameBn ?: "") }
    var brand by remember { mutableStateOf(initialProduct?.brand ?: "") }
    var model by remember { mutableStateOf(initialProduct?.model ?: "") }
    val availableCategories = if (categories.isNotEmpty()) categories else com.example.data.seed.InitialData.categories
    var selectedCategoryId by remember {
        mutableStateOf(
            initialProduct?.categoryId ?: (availableCategories.firstOrNull()?.id ?: "cat_new_smartphones")
        )
    }
    var condition by remember { mutableStateOf(initialProduct?.condition ?: ProductCondition.NEW.name) }
    var priceText by remember { mutableStateOf(initialProduct?.price?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "") }
    var mrpText by remember { mutableStateOf(initialProduct?.mrp?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "") }
    var stockText by remember { mutableStateOf(initialProduct?.stock?.toString() ?: "1") }
    var imageUrl by remember { mutableStateOf(initialProduct?.imageUrl ?: "") }
    var showUrlField by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            uploadStatusMessage = "ছবি প্রস্তুত হচ্ছে ও আপলোড হচ্ছে..."
            isUploadingImage = true
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val rawInputStream = context.contentResolver.openInputStream(uri)
                    val originalBitmap = android.graphics.BitmapFactory.decodeStream(rawInputStream)
                    rawInputStream?.close()

                    val destFile = File(context.filesDir, "prod_${System.currentTimeMillis()}.jpg")
                    if (originalBitmap != null) {
                        val maxDimension = 1080
                        val width = originalBitmap.width
                        val height = originalBitmap.height
                        val ratio = if (width > height) {
                            if (width > maxDimension) maxDimension.toFloat() / width else 1f
                        } else {
                            if (height > maxDimension) maxDimension.toFloat() / height else 1f
                        }
                        val scaledBitmap = if (ratio < 1f) {
                            android.graphics.Bitmap.createScaledBitmap(
                                originalBitmap,
                                (width * ratio).toInt(),
                                (height * ratio).toInt(),
                                true
                            )
                        } else {
                            originalBitmap
                        }
                        val fos = FileOutputStream(destFile)
                        scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 82, fos)
                        fos.flush()
                        fos.close()
                    } else {
                        val inputStream = context.contentResolver.openInputStream(uri)
                        val fos = FileOutputStream(destFile)
                        inputStream?.copyTo(fos)
                        inputStream?.close()
                        fos.close()
                    }

                    withContext(Dispatchers.Main) {
                        imageUrl = destFile.absolutePath
                        uploadStatusMessage = "ক্লাউড সার্ভারে ফটো আপলোড হচ্ছে..."
                    }

                    val result = JisanApiService.uploadImage(destFile)
                    withContext(Dispatchers.Main) {
                        result.onSuccess { publicUrl ->
                            imageUrl = publicUrl
                            uploadStatusMessage = "✅ ফটো সফলভাবে ক্লাউডে আপলোড হয়েছে!"
                            isUploadingImage = false
                        }.onFailure { ex ->
                            uploadStatusMessage = "⚠️ ফটো লোকাল সেভ হয়েছে (${ex.message ?: "আপলোড সমস্যা"})"
                            isUploadingImage = false
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        imageUrl = uri.toString()
                        uploadStatusMessage = "ত্রুটি: ${e.message}"
                        isUploadingImage = false
                    }
                }
            }
        }
    }
    var descriptionBn by remember { mutableStateOf(initialProduct?.descriptionBn ?: "") }
    var colorVariants by remember { mutableStateOf(initialProduct?.colorVariants ?: "") }
    var storageVariants by remember { mutableStateOf(initialProduct?.storageVariants ?: "") }

    // Second-hand inspection specifics
    var batteryHealthText by remember { mutableStateOf(initialProduct?.batteryHealth?.toString() ?: "") }
    var cosmeticCondition by remember { mutableStateOf(initialProduct?.cosmeticCondition ?: "") }
    var warrantyMonthsText by remember { mutableStateOf(initialProduct?.warrantyMonths?.toString() ?: "") }
    var includedAccessories by remember { mutableStateOf(initialProduct?.includedAccessories ?: "") }

    var isBestSeller by remember { mutableStateOf(initialProduct?.isBestSeller ?: false) }
    var isNewArrival by remember { mutableStateOf(initialProduct?.isNewArrival ?: false) }
    var isAvailable by remember { mutableStateOf(initialProduct?.isAvailable ?: true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEdit) "পণ্য এডিট করুন" else "নতুন পণ্য যোগ করুন",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (errorMessage != null) {
                    Text(text = errorMessage!!, color = JisanRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedTextField(
                    value = nameBn,
                    onValueChange = { nameBn = it },
                    label = { Text("পণ্যের নাম (বাংলা)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = nameEn,
                    onValueChange = { nameEn = it },
                    label = { Text("Product Name (English)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = brand,
                        onValueChange = { brand = it },
                        label = { Text("ব্র্যান্ড (Brand)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = model,
                        onValueChange = { model = it },
                        label = { Text("মডেল (Model)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Category selection UI
                val selectedCatObj = availableCategories.firstOrNull { it.id == selectedCategoryId } ?: availableCategories.firstOrNull()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "পণ্যের ক্যাটাগরি নির্বাচন করুন (Category)*:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = JisanPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(availableCategories) { cat ->
                            val isSelected = cat.id == selectedCategoryId
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) JisanPrimary else MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) JisanPrimary else MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier.clickable {
                                    selectedCategoryId = cat.id
                                    if (cat.id == "cat_second_hand") {
                                        condition = ProductCondition.SECOND_HAND.name
                                    }
                                }
                            ) {
                                Text(
                                    text = "${cat.nameBn} (${cat.nameEn})",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                    if (selectedCatObj != null) {
                        Text(
                            text = "বর্তমান ক্যাটাগরি: ${selectedCatObj.nameBn} (${selectedCatObj.nameEn})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = JisanPrimary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                // Product Photo / Image Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF8FAFC))
                        .border(1.dp, JisanBorder, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = JisanPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "পণ্যের ছবি / ফটো (Product Photo):",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = JisanPrimary
                            )
                        }
                        if (imageUrl.isNotBlank()) {
                            TextButton(
                                onClick = { imageUrl = "" },
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Delete", tint = JisanRed, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("ছবি মুছুন", color = JisanRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (imageUrl.isNotBlank()) {
                        // Live image preview
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "Selected product photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Button(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = JisanPrimary),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth().height(36.dp)
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("গ্যালারি থেকে অন্য ছবি দিন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // Empty state: prompt to pick photo from phone gallery
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "কাস্টমার যাতে পণ্যটি দেখতে পারে তার জন্য আসল ফটো যুক্ত করুন",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Button(
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = JisanPrimary),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.fillMaxWidth().height(38.dp)
                                ) {
                                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("📁 ফোন গ্যালারি থেকে ফটো বাছুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    if (uploadStatusMessage != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (isUploadingImage) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = JisanPrimary
                                )
                            }
                            Text(
                                text = uploadStatusMessage!!,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isUploadingImage) JisanPrimary else if (uploadStatusMessage!!.startsWith("✅")) JisanGreen else JisanRed
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Or paste web image URL
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (showUrlField) "ওয়েব ছবির লিঙ্ক বন্ধ করুন" else "🔗 অথবা ছবির ওয়েব লিঙ্ক দিন (URL)",
                            color = JisanPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { showUrlField = !showUrlField }
                        )
                    }

                    if (showUrlField) {
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = imageUrl,
                            onValueChange = { imageUrl = it },
                            label = { Text("ছবির লিঙ্ক (Image URL)") },
                            placeholder = { Text("https://example.com/phone.jpg") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Condition selection (New vs Second-Hand)
                Text("পণ্যের অবস্থা (Condition):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.RadioButton(
                        selected = condition == ProductCondition.NEW.name,
                        onClick = { condition = ProductCondition.NEW.name }
                    )
                    Text("ব্র্যান্ড নিউ (Brand New)", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    androidx.compose.material3.RadioButton(
                        selected = condition == ProductCondition.SECOND_HAND.name,
                        onClick = { condition = ProductCondition.SECOND_HAND.name }
                    )
                    Text("সেকেন্ড-হ্যান্ড (Used)", fontSize = 12.sp)
                }

                // Price, MRP and Stock
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("বিক্রয় মূল্য (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = mrpText,
                        onValueChange = { mrpText = it },
                        label = { Text("MRP (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = stockText,
                    onValueChange = { stockText = it },
                    label = { Text("স্টক সংখ্যা (Stock)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Second hand details
                if (condition == ProductCondition.SECOND_HAND.name) {
                    Text("সেকেন্ড-হ্যান্ড ইন্সপেকশন বিবরণ:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = JisanPrimary)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = batteryHealthText,
                            onValueChange = { batteryHealthText = it },
                            label = { Text("ব্যাটারি হেলথ %") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = warrantyMonthsText,
                            onValueChange = { warrantyMonthsText = it },
                            label = { Text("শপ ওয়ারেন্টি (মাস)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    OutlinedTextField(
                        value = cosmeticCondition,
                        onValueChange = { cosmeticCondition = it },
                        label = { Text("বডি কন্ডিশন (e.g. 9.5/10)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = includedAccessories,
                        onValueChange = { includedAccessories = it },
                        label = { Text("বক্স ও আনুষঙ্গিক (Accessories)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = colorVariants,
                    onValueChange = { colorVariants = it },
                    label = { Text("রঙসমূহ (কমা দিয়ে লিখুন: Black, Blue)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = storageVariants,
                    onValueChange = { storageVariants = it },
                    label = { Text("স্টোরেজ ভ্যারিয়েন্ট (e.g. 64GB, 128GB)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = descriptionBn,
                    onValueChange = { descriptionBn = it },
                    label = { Text("সংক্ষিপ্ত বিবরণ (Description)") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Flags
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isBestSeller, onCheckedChange = { isBestSeller = it })
                    Text("বেস্ট সেলার (Best Seller)", fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isNewArrival, onCheckedChange = { isNewArrival = it })
                    Text("নতুন আগমন (New Arrival)", fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isAvailable, onCheckedChange = { isAvailable = it })
                    Text("অর্ডারের জন্য উপলব্ধ (Available in shop)", fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val priceVal = priceText.toDoubleOrNull()
                    val mrpVal = mrpText.toDoubleOrNull() ?: (priceVal ?: 0.0)
                    val stockVal = stockText.toIntOrNull() ?: 1

                    if (nameBn.isBlank() && nameEn.isBlank()) {
                        errorMessage = "পণ্যের নাম লিখুন"
                        return@Button
                    }
                    if (priceVal == null || priceVal <= 0.0) {
                        errorMessage = "সঠিক মূল্য লিখুন"
                        return@Button
                    }

                    val finalNameBn = nameBn.ifBlank { nameEn }
                    val finalNameEn = nameEn.ifBlank { nameBn }

                    val productToSave = Product(
                        id = initialProduct?.id ?: UUID.randomUUID().toString(),
                        nameEn = finalNameEn,
                        nameBn = finalNameBn,
                        categoryId = selectedCategoryId,
                        brand = brand.ifBlank { "Generic" },
                        model = model.ifBlank { finalNameEn },
                        condition = condition,
                        price = priceVal,
                        mrp = mrpVal,
                        stock = stockVal,
                        imageUrl = imageUrl,
                        isAvailable = isAvailable,
                        isBestSeller = isBestSeller,
                        isNewArrival = isNewArrival,
                        descriptionEn = descriptionBn,
                        descriptionBn = descriptionBn,
                        batteryHealth = batteryHealthText.toIntOrNull(),
                        cosmeticCondition = cosmeticCondition.ifBlank { null },
                        includedAccessories = includedAccessories.ifBlank { null },
                        warrantyMonths = warrantyMonthsText.toIntOrNull(),
                        colorVariants = colorVariants,
                        storageVariants = storageVariants,
                        warrantyInfo = if (condition == ProductCondition.SECOND_HAND.name) "৭ দিনের টেস্টিং ওয়ারেন্টি" else "১ বছরের ব্র্যান্ড ওয়ারেন্টি",
                        returnPolicy = "৭ দিনের সহজ রিটার্ন বা রিপ্লেসমেন্ট",
                        isDemoProduct = false
                    )
                    isSavingProduct = true
                    errorMessage = null
                    onSave(productToSave) { success, msg ->
                        isSavingProduct = false
                        if (success) {
                            onDismiss()
                        } else {
                            errorMessage = "ক্লাউড সিঙ্ক সমস্যা: $msg"
                        }
                    }
                },
                enabled = !isUploadingImage && !isSavingProduct,
                colors = ButtonDefaults.buttonColors(containerColor = JisanPrimary)
            ) {
                if (isUploadingImage || isSavingProduct) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isUploadingImage) "ছবি আপলোড হচ্ছে..." else "ক্লাউডে সেভ হচ্ছে...", fontSize = 12.sp)
                } else {
                    Text(if (isEdit) "আপডেট করুন" else "যোগ করুন")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("বাতিল") }
        }
    )
}

@Composable
fun AdminUpdateManagementTab(
    viewModel: ShopViewModel,
    language: AppLanguage
) {
    val context = LocalContext.current
    val appUpdateInfo by viewModel.appUpdateInfo.collectAsState()
    val isCheckingUpdate by viewModel.isCheckingUpdate.collectAsState()
    val isPublishingUpdate by viewModel.isPublishingUpdate.collectAsState()
    val isUploadingApk by viewModel.isUploadingApk.collectAsState()
    val isSyncingProducts by viewModel.isSyncingProducts.collectAsState()

    var newVersionName by remember(appUpdateInfo) { mutableStateOf(appUpdateInfo?.versionName ?: "1.1") }
    var newVersionCode by remember(appUpdateInfo) { mutableStateOf((appUpdateInfo?.versionCode ?: 2).toString()) }
    var releaseNotes by remember(appUpdateInfo) { mutableStateOf(appUpdateInfo?.releaseNotes ?: "নতুন প্রোডাক্ট ক্যাটাগরি, প্রোডাক্ট ফটো যুক্ত করার সুবিধা এবং দ্রুত অর্ডার ট্র্যাকিং।") }
    var downloadUrl by remember(appUpdateInfo) {
        val initial = appUpdateInfo?.downloadUrl?.trim() ?: ""
        val fallback = "${com.example.data.remote.JisanApiService.getActiveBackendUrl()}/JISAN_MOBILE.apk"
        mutableStateOf(if (initial.isNotBlank()) initial else fallback)
    }
    var forceUpdate by remember(appUpdateInfo) { mutableStateOf(appUpdateInfo?.forceUpdate ?: false) }

    val activeBackendUrl by viewModel.activeBackendUrl.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var backendUrlInput by remember(activeBackendUrl) { mutableStateOf(activeBackendUrl) }
    var isTestingConnection by remember { mutableStateOf(false) }
    var testConnectionResult by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Current App Version Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(JisanPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.SystemUpdate, contentDescription = null, tint = JisanPrimary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "বর্তমান অ্যাপ সংস্করণ",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "ইনস্টল করা: v${BuildConfig.VERSION_NAME} (কোড: ${BuildConfig.VERSION_CODE})",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { viewModel.checkForAppUpdate(manual = true) },
                        enabled = !isCheckingUpdate
                    ) {
                        if (isCheckingUpdate) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = JisanPrimary)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = JisanPrimary)
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "সার্ভারে লাইভ ভার্সন:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "v${appUpdateInfo?.versionName ?: "1.1"} (কোড: ${appUpdateInfo?.versionCode ?: 2})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = JisanGreen
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "জরুরি আপডেট:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = if (appUpdateInfo?.forceUpdate == true) "সক্রিয় (হ্যাঁ)" else "সাধারণ (না)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (appUpdateInfo?.forceUpdate == true) JisanRed else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                if (!appUpdateInfo?.releaseNotes.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "বিবরণ: ${appUpdateInfo?.releaseNotes}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Publish New Update Form
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(JisanGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, tint = JisanGreen, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "কাস্টমারদের জন্য আপডেট প্রকাশ করুন",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "এখানে তথ্য দিয়ে প্রকাশ করলে সব কাস্টমারের অ্যাপে আপডেট নোটিফিকেশন যাবে",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newVersionName,
                        onValueChange = { newVersionName = it },
                        label = { Text("ভার্সন নাম (e.g. 1.1)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newVersionCode,
                        onValueChange = { if (it.all { ch -> ch.isDigit() }) newVersionCode = it },
                        label = { Text("ভার্সন কোড (e.g. 2)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = releaseNotes,
                    onValueChange = { releaseNotes = it },
                    label = { Text("রিলিজ বিবরণী (কাস্টমাররা কি কি নতুন পাবে)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                // APK Upload & Download URL Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "আসল APK ফাইল পারসিস্টেন্ট আপলোড",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "বর্তমান অ্যাপ বিল্ডের আসল ১৯.৮ MB APK ফাইলটি ক্লাউড পারসিস্টেন্ট স্টোরেজে সেভ করুন",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Button(
                                onClick = {
                                    viewModel.uploadCurrentBuildApk(context) { success, url ->
                                        if (success && url.isNotBlank()) {
                                            downloadUrl = url
                                        }
                                    }
                                },
                                enabled = !isUploadingApk,
                                colors = ButtonDefaults.buttonColors(containerColor = JisanPrimary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                if (isUploadingApk) {
                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = Color.White)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("আপলোড হচ্ছে...", fontSize = 10.sp)
                                } else {
                                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("APK আপলোড", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        OutlinedTextField(
                            value = downloadUrl,
                            onValueChange = { downloadUrl = it },
                            label = { Text("APK ডাউনলোড লিংক (URL)") },
                            placeholder = { Text("https://.../JISAN_MOBILE.apk") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = downloadUrl.isBlank() || (!downloadUrl.startsWith("http://") && !downloadUrl.startsWith("https://")),
                            supportingText = {
                                if (downloadUrl.isBlank()) {
                                    Text("⚠️ ডাউনলোড URL ফাঁকা রাখা যাবে না। 'APK আপলোড' বাটনে চাপুন।", color = JisanRed, fontSize = 10.sp)
                                } else if (!downloadUrl.startsWith("http://") && !downloadUrl.startsWith("https://")) {
                                    Text("⚠️ URL টি অবশ্যই https:// দিয়ে শুরু হতে হবে।", color = JisanRed, fontSize = 10.sp)
                                } else {
                                    Text("✅ স্থায়ী ক্লাউড APK ডাউনলোড লিংক তৈরি আছে।", color = JisanGreen, fontSize = 10.sp)
                                }
                            }
                        )
                    }
                }

                // Force update toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "জরুরি আপডেট (Force Update)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "চালু করলে কাস্টমারদের অ্যাপ চালাতে হলে আগে আপডেট করতে বলবে",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = forceUpdate,
                        onCheckedChange = { forceUpdate = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = JisanPrimary)
                    )
                }

                // Publish Button
                Button(
                    onClick = {
                        val trimmedUrl = downloadUrl.trim()
                        if (trimmedUrl.isBlank()) {
                            android.widget.Toast.makeText(context, "⚠️ APK ডাউনলোড লিংক ফাঁকা রাখা যাবে না! অনুগ্রহ করে 'বর্তমান রিলিজ বিল্ড APK সিঙ্ক ও আপলোড করুন' বাটনে চাপুন।", android.widget.Toast.LENGTH_LONG).show()
                            return@Button
                        }
                        if (!trimmedUrl.startsWith("https://", ignoreCase = true)) {
                            android.widget.Toast.makeText(context, "⚠️ নিরাপত্তার স্বার্থে APK ডাউনলোড লিংক অবশ্যই HTTPS (https://...) হতে হবে!", android.widget.Toast.LENGTH_LONG).show()
                            return@Button
                        }
                        val vCode = newVersionCode.toIntOrNull() ?: 2
                        viewModel.publishAppUpdate(
                            versionName = newVersionName,
                            versionCode = vCode,
                            releaseNotes = releaseNotes,
                            downloadUrl = trimmedUrl,
                            forceUpdate = forceUpdate,
                            onComplete = { success, msg -> }
                        )
                    },
                    enabled = !isPublishingUpdate && !isUploadingApk && newVersionName.isNotBlank() && downloadUrl.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = JisanGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isPublishingUpdate) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("আপডেট প্রকাশ করা হচ্ছে...")
                    } else {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("🚀 কাস্টমারদের জন্য আপডেট প্রকাশ করুন", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // Direct APK Sharing to Customers Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "📲 সরাসরি কাস্টমারদের আপডেট APK পাঠান",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "আপনি হোয়াটসঅ্যাপ বা শেয়ারমির মাধ্যমে সরাসরি ফোনের APK ফাইলটি কাস্টমারকে পাঠিয়ে দিতে পারেন।",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { ShareHelper.shareApkFile(context, "JM-ADMIN", downloadUrl) },
                        colors = ButtonDefaults.buttonColors(containerColor = JisanPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(40.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("APK ফাইল পাঠান", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { ShareHelper.shareViaWhatsApp(context, "JM-ADMIN", downloadUrl) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(40.dp)
                    ) {
                        Text("WhatsApp শেয়ার", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedButton(
                    onClick = {
                        val activeUrl = downloadUrl.trim()
                        if (activeUrl.isNotBlank() && (activeUrl.startsWith("http://") || activeUrl.startsWith("https://"))) {
                            try {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(activeUrl)))
                            } catch (_: Exception) {
                                android.widget.Toast.makeText(context, "ব্রাউজার খোলা সম্ভব হয়নি", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            android.widget.Toast.makeText(context, "দয়া করে উপরের ঘরে আপনার পাবলিক ডাউনলোড লিংক (URL) দিন।", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("🌐 অনলাইন ডাউনলোড লিংক টেস্ট করুন", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Live Product Sync Control Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "⚡ লাইভ প্রোডাক্ট ডেটাবেজ সিঙ্ক",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "অ্যাডমিন যে প্রোডাক্ট যোগ বা ডিলিট করবেন তা সার্ভারে সেভ হবে এবং সব কাস্টমারের ফোনে লাইভ আপডেট হবে।",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = { viewModel.syncProducts(showToast = true) },
                    enabled = !isSyncingProducts,
                    colors = ButtonDefaults.buttonColors(containerColor = JisanPrimary),
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isSyncingProducts) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("সার্ভার থেকে সিঙ্ক করা হচ্ছে...")
                    } else {
                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("সার্ভার থেকে প্রোডাক্ট রিফ্রেশ ও সিঙ্ক করুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Persistent Backend Server URL Configuration Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "🌐 ক্লাউড ব্যাকঅ্যান্ড সার্ভার URL",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (activeBackendUrl.startsWith("https://")) JisanGreen.copy(alpha = 0.12f) else JisanRed.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = if (activeBackendUrl.startsWith("https://")) "● সুরক্ষিত HTTPS" else "● অনিরাপদ / লোকাল",
                            color = if (activeBackendUrl.startsWith("https://")) JisanGreen else JisanRed,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "বর্তমানে সক্রিয় সার্ভার:",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = activeBackendUrl,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Text(
                    text = "কাস্টমারদের শারীরিক ফোনে লাইভ প্রোডাক্ট ও অর্ডার সিঙ্কের জন্য আপনার নিজস্ব স্থায়ী পাবলিক HTTPS সার্ভার URL দিন (যেমন Render/Railway/Cloud Run)। লোকালহোস্ট বা 127.0.0.1 কাস্টমারদের ফোনে কাজ করে না।",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val hasUrlError = backendUrlInput.isNotBlank() && (
                    !backendUrlInput.startsWith("https://", ignoreCase = true) ||
                    backendUrlInput.contains("localhost") ||
                    backendUrlInput.contains("127.0.0.1") ||
                    backendUrlInput.contains("10.0.2.2")
                )

                OutlinedTextField(
                    value = backendUrlInput,
                    onValueChange = {
                        backendUrlInput = it
                        testConnectionResult = null
                    },
                    label = { Text("সার্ভার API URL (অবশ্যই HTTPS)") },
                    placeholder = { Text("https://your-server.onrender.com") },
                    singleLine = true,
                    isError = hasUrlError,
                    supportingText = {
                        when {
                            backendUrlInput.startsWith("http://", ignoreCase = true) ->
                                Text("❌ অনিরাপদ HTTP গ্রহণযোগ্য নয়! অবশ্যই https:// ব্যবহার করুন।", color = JisanRed, fontSize = 10.sp)
                            backendUrlInput.contains("localhost") || backendUrlInput.contains("127.0.0.1") ->
                                Text("❌ লোকালহোস্ট কাস্টমারদের ফোনে কাজ করে না! পাবলিক HTTPS ডোমেইন দিন।", color = JisanRed, fontSize = 10.sp)
                            hasUrlError ->
                                Text("❌ URL অবশ্যই https:// দিয়ে শুরু হতে হবে।", color = JisanRed, fontSize = 10.sp)
                            else ->
                                Text("উদাহরণ: https://your-server.onrender.com", fontSize = 10.sp)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isTestingConnection = true
                                testConnectionResult = "সার্ভারের সাথে সংযোগ যাচাই করা হচ্ছে..."
                                val res = com.example.data.remote.JisanApiService.testBackendConnection(backendUrlInput)
                                isTestingConnection = false
                                testConnectionResult = res.second
                            }
                        },
                        enabled = !isTestingConnection && backendUrlInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = JisanPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(40.dp)
                    ) {
                        if (isTestingConnection) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("টেস্ট...", fontSize = 11.sp)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("টেস্ট করুন", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.updateBackendServerUrl(context, backendUrlInput) { success, msg ->
                                testConnectionResult = msg
                            }
                        },
                        enabled = backendUrlInput.isNotBlank() && !hasUrlError,
                        colors = ButtonDefaults.buttonColors(containerColor = JisanGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1.2f).height(40.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("সেভ ও সিঙ্ক", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.resetBackendServerUrl(context) { defaultUrl ->
                                backendUrlInput = defaultUrl
                                testConnectionResult = "✅ ডিফল্ট প্রোডাকশন সার্ভার সফলভাবে পুনরুদ্ধার ও সক্রিয় করা হয়েছে!"
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(40.dp)
                    ) {
                        Text("রিসেট", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                if (testConnectionResult != null) {
                    val isSuccess = testConnectionResult!!.startsWith("✅")
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSuccess) JisanGreen.copy(alpha = 0.08f) else JisanRed.copy(alpha = 0.08f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSuccess) JisanGreen.copy(alpha = 0.4f) else JisanRed.copy(alpha = 0.4f)
                        )
                    ) {
                        Text(
                            text = testConnectionResult!!,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isSuccess) JisanGreen else JisanRed,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }
    }
}
