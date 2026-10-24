package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderEntity
import com.example.ui.theme.JisanAccent
import com.example.ui.theme.JisanGreen
import com.example.ui.theme.JisanPrimary
import com.example.ui.theme.JisanRed
import com.example.util.AppLanguage
import com.example.util.Formatters
import com.example.util.Strings
import com.example.viewmodel.ShopViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    viewModel: ShopViewModel,
    onBack: () -> Unit,
    onOrderPlaced: (OrderEntity) -> Unit
) {
    val language by viewModel.currentLanguage.collectAsState()
    val cartCalc by viewModel.cartState.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val customerProfile by viewModel.customerProfile.collectAsState()

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var name by remember { mutableStateOf(customerProfile?.name ?: "") }
    var phone by remember { mutableStateOf(customerProfile?.phone ?: "") }
    var address by remember { mutableStateOf(customerProfile?.address ?: "") }
    var locality by remember { mutableStateOf(customerProfile?.locality ?: "") }
    var district by remember { mutableStateOf(customerProfile?.district ?: "Murshidabad") }
    var pinCode by remember { mutableStateOf(customerProfile?.pinCode ?: "742149") }

    // Payment Selection: "COD" vs "PREPAID_UPI"
    var selectedPaymentMethod by remember { mutableStateOf("COD") }
    var upiTransactionRef by remember { mutableStateOf("") }

    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (language == AppLanguage.BENGALI) "অর্ডার চেকআউট" else "Checkout & Shipping",
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
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = JisanRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Button(
                        onClick = {
                            errorMessage = null
                            if (name.isBlank()) {
                                errorMessage = if (language == AppLanguage.BENGALI) "দয়া করে আপনার নাম লিখুন" else "Please enter your name"
                                return@Button
                            }
                            val cleanPhone = phone.replace("[^0-9]".toRegex(), "")
                            if (cleanPhone.length < 10) {
                                errorMessage = if (language == AppLanguage.BENGALI) "দয়া করে সঠিক ১০ ডিজিটের ফোন নম্বর দিন" else "Please enter a valid 10-digit phone number"
                                return@Button
                            }
                            if (address.isBlank()) {
                                errorMessage = if (language == AppLanguage.BENGALI) "দয়া করে আপনার ঠিকানা লিখুন" else "Please enter your delivery address"
                                return@Button
                            }
                            if (pinCode.length < 6) {
                                errorMessage = if (language == AppLanguage.BENGALI) "দয়া করে ৬ ডিজিটের পিন কোড দিন" else "Please enter a 6-digit PIN code"
                                return@Button
                            }
                            val effectiveTransactionRef = if (selectedPaymentMethod == "PREPAID_UPI") {
                                if (upiTransactionRef.isNotBlank()) upiTransactionRef.trim()
                                else "UPI-SCAN-${System.currentTimeMillis().toString().takeLast(6)}"
                            } else ""

                            isSubmitting = true
                            val finalMethod = if (selectedPaymentMethod == "PREPAID_UPI") "Prepaid UPI (${settings.upiId})" else "Cash on Delivery (COD)"
                            viewModel.placeOrder(
                                customerName = name,
                                customerPhone = cleanPhone,
                                addressLine = address,
                                locality = locality.ifBlank { "Sagar Para" },
                                district = district,
                                pinCode = pinCode,
                                paymentMethod = finalMethod,
                                transactionRef = effectiveTransactionRef,
                                onSuccess = { placedOrder ->
                                    isSubmitting = false
                                    onOrderPlaced(placedOrder)
                                },
                                onError = { err ->
                                    isSubmitting = false
                                    errorMessage = err
                                }
                            )
                        },
                        enabled = !isSubmitting && cartCalc.items.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("submit_order_btn"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = JisanPrimary)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "${Strings.confirmOrder(language)} • ${Formatters.formatPrice(cartCalc.total)}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
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
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Address Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = JisanPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (language == AppLanguage.BENGALI) "ডেলিভারি ঠিকানা ও তথ্য" else "Delivery Address & Details",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(Strings.customerName(language)) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = JisanPrimary) },
                        modifier = Modifier.fillMaxWidth().testTag("checkout_name_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { if (it.length <= 10) phone = it.filter { ch -> ch.isDigit() } },
                        label = { Text(Strings.mobileNumber(language)) },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = JisanPrimary) },
                        prefix = { Text("+91 ", fontWeight = FontWeight.Bold) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth().testTag("checkout_phone_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text(Strings.address(language)) },
                        leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = JisanPrimary) },
                        modifier = Modifier.fillMaxWidth().testTag("checkout_address_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = locality,
                        onValueChange = { locality = it },
                        label = { Text(Strings.locality(language)) },
                        placeholder = { Text("e.g. Near Hajir Hut / High School") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = district,
                            onValueChange = { district = it },
                            label = { Text(Strings.district(language)) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = pinCode,
                            onValueChange = { if (it.length <= 6) pinCode = it.filter { ch -> ch.isDigit() } },
                            label = { Text(Strings.pinCode(language)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            }

            // Payment Method Selection: COD & Prepaid UPI
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (language == AppLanguage.BENGALI) "পেমেন্ট পদ্ধতি নির্বাচন করুন" else "Select Payment Method",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // COD Radio Option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedPaymentMethod == "COD") Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .clickable { selectedPaymentMethod = "COD" }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedPaymentMethod == "COD",
                            onClick = { selectedPaymentMethod = "COD" },
                            colors = RadioButtonDefaults.colors(selectedColor = JisanGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = Strings.codPayment(language),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (language == AppLanguage.BENGALI)
                                    "পণ্য হাতে পেয়ে টাকা দিন (Cash on Delivery)"
                                else
                                    "Pay cash when order is delivered to your door.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Prepaid UPI Radio Option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedPaymentMethod == "PREPAID_UPI") Color(0xFFE3F2FD) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .clickable { selectedPaymentMethod = "PREPAID_UPI" }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedPaymentMethod == "PREPAID_UPI",
                            onClick = { selectedPaymentMethod = "PREPAID_UPI" },
                            colors = RadioButtonDefaults.colors(selectedColor = JisanPrimary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = if (language == AppLanguage.BENGALI) "প্রিপেড অনলাইন (UPI QR Code / GPay / PhonePe)" else "Prepaid UPI (Scan QR / Pay Online)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (language == AppLanguage.BENGALI)
                                    "দোকানের অফিসিয়াল কিউআর স্ক্যান করে দ্রুত পেমেন্ট করুন।"
                                else
                                    "Pay directly via official shop UPI QR Code.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // UPI QR & Details container if PREPAID_UPI is selected
                    if (selectedPaymentMethod == "PREPAID_UPI") {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, JisanPrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.QrCode2, contentDescription = null, tint = JisanPrimary, modifier = Modifier.size(36.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "JISAN MOBILE অফিসিয়াল UPI QR",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = JisanPrimary
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // UPI ID Box
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFF1F5F9),
                                    modifier = Modifier.clickable {
                                        clipboardManager.setText(AnnotatedString(settings.upiId))
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "UPI ID: ", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                        Text(text = settings.upiId, fontSize = 13.sp, fontWeight = FontWeight.Black, color = JisanPrimary)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = JisanPrimary, modifier = Modifier.size(16.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // One-Tap UPI Pay App Launcher + Copy Action
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            val upiUri = Uri.parse(
                                                "upi://pay?pa=${settings.upiId}&pn=${Uri.encode(settings.upiName)}&am=${cartCalc.total}&cu=INR&tn=JISAN_MOBILE_ORDER"
                                            )
                                            try {
                                                context.startActivity(Intent(Intent.ACTION_VIEW, upiUri))
                                            } catch (_: Exception) {
                                                clipboardManager.setText(AnnotatedString(settings.upiId))
                                                android.widget.Toast.makeText(
                                                    context,
                                                    "UPI আইডি কপি হয়েছে: ${settings.upiId}\nPhonePe বা GPay খুলে পে করুন।",
                                                    android.widget.Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = JisanPrimary),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).height(40.dp)
                                    ) {
                                        Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("UPI অ্যাপে পে করুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(settings.upiId))
                                            android.widget.Toast.makeText(context, "UPI ID কপি হয়েছে: ${settings.upiId}", android.widget.Toast.LENGTH_SHORT).show()
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(40.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("আইডি কপি", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFF1F8E9),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "💡 টিপস: UPI অ্যাপ বা কিউআর কোডে পে করার পর ১২ সংখ্যার UTR নম্বর নিচে দিন অথবা খালি রেখেই কনফার্ম করুন। দোকান থেকে দ্রুত যাচাই করে ডেলিভারি শুরু হবে।",
                                        fontSize = 11.sp,
                                        color = Color(0xFF33691E),
                                        lineHeight = 14.sp,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Transaction Reference Input Field
                                OutlinedTextField(
                                    value = upiTransactionRef,
                                    onValueChange = { upiTransactionRef = it },
                                    label = { Text("UPI UTR / রেফারেন্স নম্বর (ঐচ্ছিক)") },
                                    placeholder = { Text("12-digit UTR (থাকলে দিন, না থাকলে ফাঁকা রাখুন)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                        }
                    }
                }
            }

            // Order Review Snapshot
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = Strings.orderSummary(language), fontSize = 15.sp, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(8.dp))

                    cartCalc.items.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "${item.product.nameEn} x ${item.cartItem.quantity}", fontSize = 12.sp, modifier = Modifier.weight(1f))
                            Text(text = Formatters.formatPrice(item.itemTotal), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (cartCalc.discount > 0.0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "রেফার কোড ডিসকাউন্ট", fontSize = 12.sp, color = JisanGreen, fontWeight = FontWeight.Bold)
                            Text(text = "-${Formatters.formatPrice(cartCalc.discount)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = JisanGreen)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = Strings.deliveryFee(language), fontSize = 12.sp)
                        Text(
                            text = if (cartCalc.deliveryFee == 0.0) Strings.freeDelivery(language) else Formatters.formatPrice(cartCalc.deliveryFee),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (cartCalc.deliveryFee == 0.0) JisanGreen else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = Strings.grandTotal(language), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = Formatters.formatPrice(cartCalc.total),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = JisanPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
