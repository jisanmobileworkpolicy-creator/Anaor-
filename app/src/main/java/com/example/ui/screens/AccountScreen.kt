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
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.BuildConfig
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import com.example.util.ShareHelper
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JisanGreen
import com.example.ui.theme.JisanPrimary
import com.example.ui.theme.JisanRed
import com.example.util.AppLanguage
import com.example.util.Formatters
import com.example.util.Strings
import com.example.viewmodel.ShopViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    viewModel: ShopViewModel,
    onNavigateToAdmin: () -> Unit
) {
    val language by viewModel.currentLanguage.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val customerProfile by viewModel.customerProfile.collectAsState()
    val appUpdateInfo by viewModel.appUpdateInfo.collectAsState()
    val isCheckingUpdate by viewModel.isCheckingUpdate.collectAsState()
    val isUpdateAvailable by viewModel.isUpdateAvailable.collectAsState()
    val updateCheckStatusMessage by viewModel.updateCheckStatusMessage.collectAsState()
    val isSyncingProducts by viewModel.isSyncingProducts.collectAsState()
    val context = LocalContext.current

    // Customer Login Dialog
    var showCustomerLoginDialog by remember { mutableStateOf(false) }
    var custName by remember { mutableStateOf(customerProfile?.name ?: "") }
    var custPhone by remember { mutableStateOf(customerProfile?.phone ?: "") }
    var custAddress by remember { mutableStateOf(customerProfile?.address ?: "") }
    var custPin by remember { mutableStateOf(customerProfile?.pinCode ?: "742149") }

    // Admin Login with ID & Password
    var showAdminLoginDialog by remember { mutableStateOf(false) }
    var adminIdInput by remember { mutableStateOf("") }
    var adminPasswordInput by remember { mutableStateOf("") }
    var adminLoginError by remember { mutableStateOf<String?>(null) }

    // Admin Password Reset with Multi-step Verification (Strict Security)
    var showAdminForgotPasswordDialog by remember { mutableStateOf(false) }
    var resetStep by remember { mutableStateOf(1) } // 1 = Admin Mobile Gate, 2 = Secret OTP/PIN Verification
    var adminMobileInput by remember { mutableStateOf("") }
    var secretOtp by remember { mutableStateOf("") }
    var otpInput by remember { mutableStateOf("") }
    var newPasswordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var resetError by remember { mutableStateOf<String?>(null) }
    var resetSuccessMessage by remember { mutableStateOf<String?>(null) }

    var showPolicyDialog by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = Strings.navAccount(language),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = JisanPrimary)
            )
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
            // Customer Profile / Login Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(JisanPrimary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = JisanPrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                if (customerProfile?.isLoggedIn == true && customerProfile?.name?.isNotBlank() == true) {
                                    Text(
                                        text = customerProfile!!.name,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "+91 ${customerProfile!!.phone}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "রেফার কোড: ${customerProfile!!.referralCode}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFE65100)
                                    )
                                } else {
                                    Text(
                                        text = if (language == AppLanguage.BENGALI) "অতিথি ক্রেতা (Guest Customer)" else "Guest Customer",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (language == AppLanguage.BENGALI) "লগইন করে ঠিকানা ও রেফারেল সুবিধা পান" else "Login to save address & get referral rewards",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        if (customerProfile?.isLoggedIn == true) {
                            IconButton(onClick = { viewModel.logoutCustomer() }) {
                                Icon(Icons.Default.Logout, contentDescription = "Logout", tint = JisanRed)
                            }
                        } else {
                            Button(
                                onClick = { showCustomerLoginDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = JisanPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text(if (language == AppLanguage.BENGALI) "লগইন" else "Login", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Dark Mode & Light Mode Switch Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                            contentDescription = null,
                            tint = JisanPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (language == AppLanguage.BENGALI) "ডার্ক মোড ও লাইট মোড (Theme)" else "Dark & White Mode",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isDarkMode) "ডার্ক মোড সক্রিয় (Dark Mode)" else "হোয়াইট মোড সক্রিয় (White Mode)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { viewModel.toggleDarkMode() },
                        colors = SwitchDefaults.colors(checkedThumbColor = JisanPrimary)
                    )
                }
            }

            // Language Switch
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.toggleLanguage() },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Translate, contentDescription = null, tint = JisanPrimary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (language == AppLanguage.BENGALI) "ভাষা পরিবর্তন (Language)" else "App Language (ভাষা)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (language == AppLanguage.BENGALI) "বর্তমান: বাংলা" else "Current: English",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = if (language == AppLanguage.BENGALI) "English" else "বাংলা",
                        color = JisanPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Shop Details Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = settings.shopName, fontSize = 16.sp, fontWeight = FontWeight.Black, color = JisanPrimary)
                    Text(text = "“${if (language == AppLanguage.BENGALI) settings.brandSloganBn else settings.brandSloganEn}”", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "📍 ${settings.shopAddress}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val url = Formatters.buildWhatsAppUrl(settings.whatsappNumber, "নমস্কার জিসান মোবাইল, আমি আপনার শপ সম্পর্কে জানতে চাই।")
                                try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) } catch (_: Exception) {}
                            },
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${settings.primaryPhone}"))
                                try { context.startActivity(intent) } catch (_: Exception) {}
                            },
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = JisanPrimary)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Call Shop", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Share JISAN MOBILE App Card (Direct APK & Link Share)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("account_share_app_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(JisanPrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = JisanPrimary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "JISAN MOBILE অ্যাপ শেয়ার করুন",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "বন্ধুদের সরাসরি APK ফাইল অথবা ডাউনলোড লিংক পাঠান",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Direct APK Share Button
                        Button(
                            onClick = {
                                ShareHelper.shareApkFile(context, customerProfile?.referralCode ?: "JMREF-786")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = JisanPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(38.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("📲 APK ফাইল পাঠান", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        // WhatsApp Direct Share
                        Button(
                            onClick = {
                                ShareHelper.shareViaWhatsApp(context, customerProfile?.referralCode ?: "JMREF-786")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(38.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp)
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("WhatsApp শেয়ার", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        // Link Share
                        OutlinedButton(
                            onClick = {
                                ShareHelper.shareApp(context, customerProfile?.referralCode ?: "JMREF-786")
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(0.9f).height(38.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp)
                        ) {
                            Text("🔗 লিংক", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // App Update & Live Product Sync Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("account_app_update_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = if (isUpdateAvailable) androidx.compose.foundation.BorderStroke(1.5.dp, JisanGreen) else null
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (isUpdateAvailable) JisanGreen.copy(alpha = 0.15f) else JisanPrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isUpdateAvailable) Icons.Default.CloudDownload else Icons.Default.SystemUpdate,
                                contentDescription = "App Update",
                                tint = if (isUpdateAvailable) JisanGreen else JisanPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "অ্যাপ আপডেট ও সংস্করণ",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (isUpdateAvailable) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = JisanGreen,
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = "NEW UPDATE",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "বর্তমান ভার্সন: v${BuildConfig.VERSION_NAME} (বিল্ড: ${BuildConfig.VERSION_CODE})",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Update Status Banner
                    if (isCheckingUpdate) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = JisanPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "সার্ভারে নতুন আপডেট চেক করা হচ্ছে...",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    } else if (isUpdateAvailable && appUpdateInfo != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(JisanGreen.copy(alpha = 0.10f), RoundedCornerShape(10.dp))
                                .border(1.dp, JisanGreen.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "🎉 নতুন সংস্করণ পাওয়া গেছে: v${appUpdateInfo?.versionName}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = JisanGreen
                            )
                            if (!appUpdateInfo?.releaseNotes.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "💡 নতুন কি আছে: ${appUpdateInfo?.releaseNotes}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            if (!appUpdateInfo?.releaseDate.isNullOrBlank()) {
                                Text(
                                    text = "📅 তারিখ: ${appUpdateInfo?.releaseDate}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val rawUrl = appUpdateInfo?.downloadUrl?.trim() ?: ""
                                        val apkDownloadUrl = when {
                                            rawUrl.startsWith("http://") || rawUrl.startsWith("https://") -> rawUrl
                                            rawUrl.isNotBlank() -> "${com.example.data.remote.JisanApiService.getActiveBackendUrl()}/$rawUrl".replace("//", "/").replace(":/", "://")
                                            else -> "${com.example.data.remote.JisanApiService.getActiveBackendUrl()}/JISAN_MOBILE.apk"
                                        }
                                        try {
                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(apkDownloadUrl)))
                                        } catch (_: Exception) {}
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = JisanGreen),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).height(38.dp)
                                ) {
                                    Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("📥 আপডেট ডাউনলোড", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = {
                                        val waUrl = Formatters.buildWhatsAppUrl(
                                            settings.whatsappNumber,
                                            "নমস্কার জিসান মোবাইল, আমার JISAN MOBILE অ্যাপটি v${appUpdateInfo?.versionName} তে আপডেট করতে চাই। দয়া করে নতুন APK দিন।"
                                        )
                                        try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(waUrl))) } catch (_: Exception) {}
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(0.9f).height(38.dp)
                                ) {
                                    Icon(Icons.Default.Chat, contentDescription = null, tint = Color(0xFF25D366), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("হোয়াটসঅ্যাপ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    } else if (updateCheckStatusMessage != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = updateCheckStatusMessage ?: "অ্যাপটি আপ-টু-ডেট আছে",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Action buttons: Check Update & Live Sync
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.checkForAppUpdate(manual = true) },
                            enabled = !isCheckingUpdate,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Icon(Icons.Default.SystemUpdate, contentDescription = null, modifier = Modifier.size(14.dp), tint = JisanPrimary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("আপডেট চেক করুন", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.syncProducts(showToast = true) },
                            enabled = !isSyncingProducts,
                            colors = ButtonDefaults.buttonColors(containerColor = JisanPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            if (isSyncingProducts) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = Color.White)
                            } else {
                                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("লাইভ প্রোডাক্ট সিঙ্ক", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Store Policies
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    PolicyMenuItem(icon = Icons.Default.Policy, title = "রিটার্ন ও রিপ্লেসমেন্ট পলিসি", onClick = { showPolicyDialog = "RETURN" })
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    PolicyMenuItem(icon = Icons.Default.Info, title = "ডেলিভারি চার্জ ও নিয়মাবলী", onClick = { showPolicyDialog = "DELIVERY" })
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    PolicyMenuItem(icon = Icons.Default.Shield, title = "গোপনীয়তা ও নিরাপত্তা নীতি", onClick = { showPolicyDialog = "PRIVACY" })
                }
            }

            // Admin Portal Access Card (with ID & Password)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAdminLoginDialog = true }
                    .testTag("admin_portal_entry_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, JisanPrimary)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(JisanPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = JisanPrimary, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "দোকানদার / অ্যাডমিন লগইন (Admin Login)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = JisanPrimary
                            )
                            Text(
                                text = "আইডি ও পাসওয়ার্ড দিয়ে প্রবেশ করুন",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = JisanPrimary)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Customer Login Dialog
    if (showCustomerLoginDialog) {
        AlertDialog(
            onDismissRequest = { showCustomerLoginDialog = false },
            title = { Text("কাস্টমার লগইন / রেজিস্ট্রেশন", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("আপনার নাম ও ফোন নম্বর দিয়ে লগইন করুন:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(value = custName, onValueChange = { custName = it }, label = { Text("আপনার পুরো নাম") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(
                        value = custPhone,
                        onValueChange = { if (it.length <= 10) custPhone = it.filter { ch -> ch.isDigit() } },
                        label = { Text("১০ ডিজিটের ফোন নম্বর") },
                        prefix = { Text("+91 ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(value = custAddress, onValueChange = { custAddress = it }, label = { Text("ডেলিভারি ঠিকানা") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(
                        value = custPin,
                        onValueChange = { if (it.length <= 6) custPin = it.filter { ch -> ch.isDigit() } },
                        label = { Text("পিন কোড") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (custName.isNotBlank() && custPhone.length >= 10) {
                            viewModel.loginCustomer(custName, custPhone, custAddress, "Sagar Para", custPin)
                            showCustomerLoginDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JisanPrimary)
                ) {
                    Text("লগইন সম্পন্ন করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomerLoginDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Admin Login Dialog (ID + Password) - STRICT ADMIN ONLY
    if (showAdminLoginDialog) {
        AlertDialog(
            onDismissRequest = {
                showAdminLoginDialog = false
                adminLoginError = null
            },
            icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = JisanPrimary, modifier = Modifier.size(36.dp)) },
            title = { Text("অ্যাডমিন লগইন", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = JisanPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "দোকান মালিকের ব্যক্তিগত অ্যাকাউন্ট। অননুমোদিত কাস্টমারদের প্রবেশ সম্পূর্ণ নিষিদ্ধ।",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    OutlinedTextField(
                        value = adminIdInput,
                        onValueChange = { adminIdInput = it; adminLoginError = null },
                        label = { Text("অ্যাডমিন আইডি / ইউজারনেম") },
                        placeholder = { Text("আইডি লিখুন") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("admin_id_input")
                    )

                    OutlinedTextField(
                        value = adminPasswordInput,
                        onValueChange = { adminPasswordInput = it; adminLoginError = null },
                        label = { Text("পাসওয়ার্ড (Password)") },
                        placeholder = { Text("পাসওয়ার্ড লিখুন") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("admin_password_input")
                    )

                    if (adminLoginError != null) {
                        Text(text = adminLoginError!!, color = JisanRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Forgot Password link with Mobile OTP verification
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "পাসওয়ার্ড ভুলে গেছেন? (অ্যাডমিন ভেরিফিকেশন)",
                            color = JisanPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                showAdminLoginDialog = false
                                resetStep = 1
                                adminMobileInput = ""
                                secretOtp = ""
                                otpInput = ""
                                newPasswordInput = ""
                                confirmPasswordInput = ""
                                resetError = null
                                resetSuccessMessage = null
                                showAdminForgotPasswordDialog = true
                            }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val verified = viewModel.verifyAdminCredentials(adminIdInput, adminPasswordInput)
                        if (verified) {
                            showAdminLoginDialog = false
                            adminIdInput = ""
                            adminPasswordInput = ""
                            adminLoginError = null
                            onNavigateToAdmin()
                        } else {
                            adminLoginError = "ভুল আইডি বা পাসওয়ার্ড! (Invalid ID or Password)"
                        }
                    },
                    modifier = Modifier.testTag("admin_login_submit_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = JisanPrimary)
                ) {
                    Text("লগইন করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdminLoginDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Admin Password Reset Dialog - 2-STEP STRICT SECURITY
    if (showAdminForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                showAdminForgotPasswordDialog = false
                resetError = null
                resetSuccessMessage = null
            },
            icon = { Icon(Icons.Default.LockReset, contentDescription = null, tint = JisanPrimary, modifier = Modifier.size(36.dp)) },
            title = {
                Text(
                    text = if (resetStep == 1) "🔐 ধাপ ১: অ্যাডমিন মোবাইল ভেরিফিকেশন" else "🔑 ধাপ ২: সিকিউরিটি ওটিপি ও পাসওয়ার্ড",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (resetStep == 1) {
                        // STEP 1: MOBILE VERIFICATION GATE
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFF3E0),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB300))
                        ) {
                            Row(modifier = Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "অননুমোদিত প্রবেশ নিষিদ্ধ। পাসওয়ার্ড রিসেট করতে দোকান মালিকের নিবন্ধিত মোবাইল নাম্বার যাচাই বাধ্যতামূলক।",
                                    fontSize = 11.sp,
                                    color = Color(0xFFE65100),
                                    lineHeight = 15.sp
                                )
                            }
                        }

                        Text("নিবন্ধিত অ্যাডমিন মোবাইল নাম্বার দিন:", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                        OutlinedTextField(
                            value = adminMobileInput,
                            onValueChange = { adminMobileInput = it; resetError = null },
                            label = { Text("১০ ডিজিটের মোবাইল নাম্বার") },
                            placeholder = { Text("e.g. 9382302242") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("admin_reset_mobile_input")
                        )

                        if (resetError != null) {
                            Text(text = resetError!!, color = JisanRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // STEP 2: SECURE OTP DISPATCH & NEW PASSWORD
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFE8F5E9),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF81C784))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "✅ নিবন্ধিত নম্বর যাচাইকৃত: +91 ******" + adminMobileInput.takeLast(4),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "নিরাপত্তার স্বার্থে ওটিপি স্ক্রিনে দেখানো হয় না। নিচের বাটনে ট্যাপ করে আপনার WhatsApp বা SMS থেকে কোডটি সংগ্রহ করুন:",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 15.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    val targetPhone = adminMobileInput.trim().ifBlank { settings.adminMobile }
                                    Button(
                                        onClick = {
                                            android.widget.Toast.makeText(context, "অ্যাডমিন WhatsApp এ ওটিপি পাঠানো হচ্ছে...", android.widget.Toast.LENGTH_SHORT).show()
                                            ShareHelper.sendAdminOtpToWhatsApp(context, targetPhone, secretOtp)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.weight(1f).height(34.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp)
                                    ) {
                                        Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("WhatsApp OTP", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = {
                                            android.widget.Toast.makeText(context, "SMS অ্যাপে ওটিপি পাঠানো হচ্ছে...", android.widget.Toast.LENGTH_SHORT).show()
                                            ShareHelper.sendAdminOtpViaSms(context, targetPhone, secretOtp)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = JisanPrimary),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.weight(1f).height(34.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp)
                                    ) {
                                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("SMS OTP", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = otpInput,
                            onValueChange = { otpInput = it; resetError = null },
                            label = { Text("৬ ডিজিটের গোপন OTP বা Master PIN") },
                            placeholder = { Text("কোডটি এখানে লিখুন") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("admin_otp_input")
                        )

                        OutlinedTextField(
                            value = newPasswordInput,
                            onValueChange = { newPasswordInput = it; resetError = null },
                            label = { Text("নতুন পাসওয়ার্ড (কমপক্ষে ৪ অক্ষর)") },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("admin_new_password_input")
                        )

                        OutlinedTextField(
                            value = confirmPasswordInput,
                            onValueChange = { confirmPasswordInput = it; resetError = null },
                            label = { Text("নতুন পাসওয়ার্ড নিশ্চিত করুন") },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("admin_confirm_password_input")
                        )

                        if (resetError != null) {
                            Text(text = resetError!!, color = JisanRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (resetSuccessMessage != null) {
                        Text(text = resetSuccessMessage!!, color = JisanGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                if (resetStep == 1) {
                    Button(
                        onClick = {
                            val isAuthorized = viewModel.isAuthorizedAdminMobile(adminMobileInput)
                            if (isAuthorized) {
                                secretOtp = viewModel.requestAdminPasswordResetOtp()
                                otpInput = "" // STRICTLY BLANK - NEVER AUTOFILL!
                                resetError = null
                                resetStep = 2
                            } else {
                                resetError = "⛔ ভুল মোবাইল নম্বর! এই নম্বরটি অনুমোদিত অ্যাডমিনের নয়। কাস্টমারদের জন্য এই অপশন নয়।"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = JisanPrimary)
                    ) {
                        Text("নম্বর যাচাই ও পরবর্তী ধাপ")
                    }
                } else {
                    Button(
                        onClick = {
                            if (otpInput.isBlank()) {
                                resetError = "অনুগ্রহ করে ওটিপি বা মাস্টার পিন লিখুন"
                                return@Button
                            }
                            if (newPasswordInput.trim().length < 4) {
                                resetError = "পাসওয়ার্ড কমপক্ষে ৪ অক্ষরের হতে হবে"
                                return@Button
                            }
                            if (newPasswordInput != confirmPasswordInput) {
                                resetError = "দুই ঘরের নতুন পাসওয়ার্ড মেলেনি!"
                                return@Button
                            }
                            val result = viewModel.verifyOtpOrPinAndResetPassword(otpInput, newPasswordInput)
                            result.fold(
                                onSuccess = {
                                    resetSuccessMessage = "✅ পাসওয়ার্ড সফলভাবে পরিবর্তন হয়েছে!"
                                    ShareHelper.copyToClipboard(context, "", "পাসওয়ার্ড পরিবর্তন সম্পন্ন!")
                                    showAdminForgotPasswordDialog = false
                                    adminPasswordInput = ""
                                    adminLoginError = null
                                    showAdminLoginDialog = true
                                },
                                onFailure = {
                                    resetError = it.message ?: "রিসেট ব্যর্থ হয়েছে! সঠিক ওটিপি বা পিন দিন।"
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = JisanPrimary)
                    ) {
                        Text("পাসওয়ার্ড আপডেট করুন")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAdminForgotPasswordDialog = false
                    resetStep = 1
                }) {
                    Text(if (resetStep == 1) "বাতিল" else "পিছনে")
                }
            }
        )
    }
    // Policy Detail Dialogs
    if (showPolicyDialog != null) {
        AlertDialog(
            onDismissRequest = { showPolicyDialog = null },
            title = { Text(text = "শপ নীতিমালা ও শর্তাবলী", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = when (showPolicyDialog) {
                        "RETURN" -> settings.returnPolicyBn
                        "DELIVERY" -> "• ₹${settings.freeDeliveryAbove.toInt()} বা তার বেশি মূল্যের অর্ডারে ফ্রি হোম ডেলিভারি!\n• ছোট অর্ডারে ডেলিভারি চার্জ মাত্র ₹${settings.deliveryFee.toInt()}।"
                        else -> "JISAN MOBILE গ্রাহকদের ডেটা সুরক্ষায় প্রতিশ্রুতিবদ্ধ। গ্রাহকের ফোন ও ঠিকানা শুধুমাত্র ডেলিভারির জন্য সংরক্ষিত থাকে।"
                    },
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            },
            confirmButton = {
                Button(onClick = { showPolicyDialog = null }) { Text("বুঝেছি (OK)") }
            }
        )
    }
}

@Composable
fun PolicyMenuItem(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = JisanPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
