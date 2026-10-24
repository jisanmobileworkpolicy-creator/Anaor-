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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleanHands
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Product
import com.example.ui.theme.JisanAccent
import com.example.ui.theme.JisanGreen
import com.example.ui.theme.JisanGreenLight
import com.example.ui.theme.JisanPrimary
import com.example.ui.theme.JisanRed
import com.example.ui.theme.JisanRedLight
import com.example.ui.theme.SecondHandBadgeBg
import com.example.ui.theme.SecondHandBadgeText
import com.example.util.AppLanguage
import com.example.util.Formatters
import com.example.util.Strings
import com.example.viewmodel.ShopViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    product: Product,
    viewModel: ShopViewModel,
    onBack: () -> Unit,
    onNavigateToCheckout: () -> Unit
) {
    val language by viewModel.currentLanguage.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val wishlistIds by viewModel.wishlistIds.collectAsState()
    val isWishlisted = wishlistIds.contains(product.id)

    var selectedColor by remember { mutableStateOf(product.parsedColors.firstOrNull() ?: "") }
    var selectedStorage by remember { mutableStateOf(product.parsedStorages.firstOrNull() ?: "") }

    val displayName = if (language == AppLanguage.BENGALI) product.nameBn else product.nameEn
    val displayDesc = if (language == AppLanguage.BENGALI) product.descriptionBn else product.descriptionEn

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = product.brand,
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
                actions = {
                    IconButton(onClick = { viewModel.toggleWishlist(product.id) }) {
                        Icon(
                            imageVector = if (isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Wishlist",
                            tint = if (isWishlisted) JisanRed else Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = JisanPrimary)
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.addToCart(
                                product = product,
                                color = selectedColor,
                                storage = selectedStorage
                            )
                        },
                        enabled = product.isInStock,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("detail_add_to_cart_btn"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = JisanPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = Strings.addToCart(language), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            viewModel.addToCart(
                                product = product,
                                color = selectedColor,
                                storage = selectedStorage
                            )
                            onNavigateToCheckout()
                        },
                        enabled = product.isInStock,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("detail_buy_now_btn"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = JisanPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = Strings.buyNow(language), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surface)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (product.displayImageUrl.isNotBlank()) {
                    AsyncImage(
                        model = product.displayImageUrl,
                        contentDescription = displayName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (product.isSecondHand) Icons.Default.Recycling else Icons.Default.PhoneAndroid,
                            contentDescription = null,
                            tint = if (product.isSecondHand) JisanAccent else JisanPrimary,
                            modifier = Modifier.size(60.dp)
                        )
                    }
                }

                Surface(
                    modifier = Modifier.align(Alignment.TopStart).padding(14.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = if (product.isSecondHand) SecondHandBadgeBg else Color(0xFFE3F2FD),
                    tonalElevation = 2.dp
                ) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (product.isSecondHand) Icons.Default.Recycling else Icons.Default.Verified,
                            contentDescription = null,
                            tint = if (product.isSecondHand) SecondHandBadgeText else JisanPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (product.isSecondHand) Strings.secondHand(language) else Strings.brandNew(language),
                            color = if (product.isSecondHand) SecondHandBadgeText else JisanPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).padding(14.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = if (product.isInStock) JisanGreenLight else JisanRedLight
                ) {
                    Text(
                        text = if (product.isInStock) "${Strings.inStock(language)} (${product.stock})" else Strings.outOfStock(language),
                        color = if (product.isInStock) JisanGreen else JisanRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "${product.brand} • ${product.model}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = JisanPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = displayName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, lineHeight = 24.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.Start) {
                        Text(text = Formatters.formatPrice(product.price), fontSize = 24.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                        if (product.mrp > product.price) {
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = "MRP: ${Formatters.formatPrice(product.mrp)}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textDecoration = TextDecoration.LineThrough)
                            Spacer(modifier = Modifier.width(10.dp))
                            Surface(shape = RoundedCornerShape(4.dp), color = JisanAccent) {
                                Text(text = "${product.discountPercentage}% OFF", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }
                }
            }

            if (product.isSecondHand) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFE082))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = JisanAccent, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = if (language == AppLanguage.BENGALI) "জিসান মোবাইল টেকনিক্যাল ইন্সপেকশন" else "JISAN MOBILE Certified Inspection", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        if (product.batteryHealth != null) {
                            InspectionItem(icon = Icons.Default.BatteryChargingFull, label = Strings.batteryHealth(language), value = "${product.batteryHealth}% আসল ব্যাকআপ")
                        }
                        if (!product.cosmeticCondition.isNullOrEmpty()) {
                            InspectionItem(icon = Icons.Default.CleanHands, label = Strings.cosmeticCondition(language), value = product.cosmeticCondition)
                        }
                        if (!product.includedAccessories.isNullOrEmpty()) {
                            InspectionItem(icon = Icons.Default.CardGiftcard, label = Strings.includedAccessories(language), value = product.includedAccessories)
                        }
                        if (product.warrantyMonths != null && product.warrantyMonths > 0) {
                            InspectionItem(icon = Icons.Default.Security, label = Strings.shopWarranty(language), value = "${product.warrantyMonths} Months Shop Warranty by JISAN MOBILE")
                        }
                    }
                }
            }

            if (product.parsedColors.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = if (language == AppLanguage.BENGALI) "রং নির্বাচন করুন:" else "Select Color:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            product.parsedColors.forEach { color ->
                                val isSelected = selectedColor == color
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) JisanPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface)
                                        .border(if (isSelected) 2.dp else 1.dp, if (isSelected) JisanPrimary else MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                        .clickable { selectedColor = color }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(text = color, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) JisanPrimary else MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                }
            }

            if (product.parsedStorages.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = if (language == AppLanguage.BENGALI) "স্টোরেজ ও ভ্যারিয়েন্ট:" else "Select Storage:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            product.parsedStorages.forEach { storage ->
                                val isSelected = selectedStorage == storage
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) JisanPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface)
                                        .border(if (isSelected) 2.dp else 1.dp, if (isSelected) JisanPrimary else MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                        .clickable { selectedStorage = storage }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(text = storage, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) JisanPrimary else MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = if (language == AppLanguage.BENGALI) "পণ্যের বিবরণ ও স্পেসিফিকেশন" else "Product Details & Specifications", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = displayDesc.ifEmpty { displayName }, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 20.sp)
                    if (product.warrantyInfo.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = JisanGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "ওয়ারেন্টি: ${product.warrantyInfo}", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                    if (product.returnPolicy.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = JisanPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "রিটার্ন নীতি: ${product.returnPolicy}", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth().padding(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Store, contentDescription = null, tint = JisanPrimary, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "JISAN MOBILE শপ গ্যারান্টি", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = JisanPrimary)
                        Text(text = settings.shopAddress, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "কল/হোয়াটসঅ্যাপ: ${settings.primaryPhone}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun InspectionItem(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFFE65100),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$label: ",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
