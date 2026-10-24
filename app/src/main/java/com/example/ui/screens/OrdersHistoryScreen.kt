package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderEntity
import com.example.data.model.OrderStatus
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.JisanAccent
import com.example.ui.theme.JisanBorder
import com.example.ui.theme.JisanGreen
import com.example.ui.theme.JisanGreenLight
import com.example.ui.theme.JisanPrimary
import com.example.ui.theme.JisanRed
import com.example.ui.theme.JisanRedLight
import com.example.util.AppLanguage
import com.example.util.Formatters
import com.example.util.Strings
import com.example.viewmodel.ShopViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersHistoryScreen(
    viewModel: ShopViewModel,
    onContinueShopping: () -> Unit
) {
    val language by viewModel.currentLanguage.collectAsState()
    val orders by viewModel.allOrders.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "${Strings.navOrders(language)} (${orders.size})",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = JisanPrimary)
            )
        }
    ) { innerPadding ->
        if (orders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateView(
                    icon = Icons.Default.ReceiptLong,
                    title = Strings.noOrders(language),
                    description = if (language == AppLanguage.BENGALI)
                        "আপনি এখনও কোনো অর্ডার করেননি। আমাদের নতুন মোবাইল ও অ্যাক্সেসরিজ কালেকশন দেখুন!"
                    else
                        "You haven't placed any orders yet. Discover our latest mobiles and accessories!",
                    actionLabel = Strings.startShopping(language),
                    onActionClick = onContinueShopping
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(orders, key = { it.orderNumber }) { order ->
                    OrderHistoryCard(
                        order = order,
                        language = language,
                        whatsappNumber = settings.whatsappNumber,
                        onCancel = {
                            viewModel.cancelOrder(order.orderNumber) { _, _ -> }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun OrderHistoryCard(
    order: OrderEntity,
    language: AppLanguage,
    whatsappNumber: String,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(false) }
    val orderStatus = order.orderStatus

    val statusColor = when (orderStatus) {
        OrderStatus.DELIVERED -> JisanGreen
        OrderStatus.CANCELLED -> JisanRed
        OrderStatus.SHIPPED, OrderStatus.OUT_FOR_DELIVERY -> Color(0xFF0288D1)
        else -> JisanAccent
    }

    val statusBg = when (orderStatus) {
        OrderStatus.DELIVERED -> JisanGreenLight
        OrderStatus.CANCELLED -> JisanRedLight
        OrderStatus.SHIPPED, OrderStatus.OUT_FOR_DELIVERY -> Color(0xFFE1F5FE)
        else -> Color(0xFFFFF8E1)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("order_card_${order.orderNumber}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Order ID & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = order.orderNumber,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = JisanPrimary
                    )
                    Text(
                        text = Formatters.formatDate(order.createdAt),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusBg
                ) {
                    Text(
                        text = if (language == AppLanguage.BENGALI) orderStatus.bnLabel else orderStatus.enLabel,
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = JisanBorder)
            Spacer(modifier = Modifier.height(10.dp))

            // Items Summary
            Text(
                text = order.itemsJson.replace(" | ", "\n• ").replace(";", "\n• "),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 17.sp,
                maxLines = if (isExpanded) 10 else 2
            )

            if (order.trackingNotes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "📢 আপডেট: ${order.trackingNotes}",
                    fontSize = 11.sp,
                    color = JisanPrimary,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Price & Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = Strings.grandTotal(language),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = Formatters.formatPrice(order.totalAmount),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // WhatsApp Support for this order
                    OutlinedButton(
                        onClick = {
                            val msg = "নমস্কার জিসান মোবাইল, আমার অর্ডার (${order.orderNumber}) সম্পর্কে জানতে চাই।"
                            val url = Formatters.buildWhatsAppUrl(whatsappNumber, msg)
                            try {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            } catch (_: Exception) {}
                        },
                        modifier = Modifier.height(34.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF25D366))
                    ) {
                        Text("WhatsApp", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Cancel Order Button (if status is PENDING or CONFIRMED)
                    if (order.canBeCancelled) {
                        OutlinedButton(
                            onClick = onCancel,
                            modifier = Modifier.height(34.dp),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = JisanRed)
                        ) {
                            Text(Strings.cancelOrder(language), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
