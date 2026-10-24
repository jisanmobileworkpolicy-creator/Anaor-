package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PromotionalBanner
import com.example.ui.theme.JisanAccent
import com.example.ui.theme.JisanPrimary
import com.example.ui.theme.JisanPrimaryVariant
import com.example.util.AppLanguage
import kotlinx.coroutines.delay

@Composable
fun BannerCarousel(
    banners: List<PromotionalBanner>,
    language: AppLanguage,
    onBannerClick: (PromotionalBanner) -> Unit,
    modifier: Modifier = Modifier
) {
    if (banners.isEmpty()) return

    var currentIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(banners.size) {
        if (banners.size > 1) {
            while (true) {
                delay(4000)
                currentIndex = (currentIndex + 1) % banners.size
            }
        }
    }

    val banner = banners[currentIndex % banners.size]
    val title = if (language == AppLanguage.BENGALI) banner.titleBn else banner.titleEn
    val subtitle = if (language == AppLanguage.BENGALI) banner.subtitleBn else banner.subtitleEn

    Column(modifier = modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(14.dp))
                .clickable { onBannerClick(banner) },
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(JisanPrimary, JisanPrimaryVariant, Color(0xFF0288D1))
                        )
                    )
                    .padding(14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .align(Alignment.CenterStart)
                ) {
                    if (banner.discountTag.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = JisanAccent
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalOffer,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = banner.discountTag,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 21.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = subtitle,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 2,
                        lineHeight = 16.sp
                    )
                }

                // Decorative Icon
                Icon(
                    imageVector = Icons.Default.Store,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.18f),
                    modifier = Modifier
                        .size(80.dp)
                        .align(Alignment.BottomEnd)
                )
            }
        }

        // Indicator dots
        if (banners.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                banners.indices.forEach { index ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (index == currentIndex) 16.dp else 6.dp, 6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                if (index == currentIndex) JisanPrimary else Color(0xFFCBD5E1)
                            )
                    )
                }
            }
        }
    }
}
