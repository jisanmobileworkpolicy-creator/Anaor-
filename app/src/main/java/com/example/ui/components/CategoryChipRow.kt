package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.DevicesOther
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Category
import com.example.ui.theme.JisanBorder
import com.example.ui.theme.JisanPrimary
import com.example.util.AppLanguage

@Composable
fun CategoryChipRow(
    categories: List<Category>,
    selectedCategoryId: String?,
    language: AppLanguage,
    onCategorySelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // "All" item
        CategoryItem(
            name = if (language == AppLanguage.BENGALI) "সব" else "All",
            icon = Icons.Default.DevicesOther,
            isSelected = selectedCategoryId == null,
            onClick = { onCategorySelected(null) },
            testTag = "category_all"
        )

        categories.forEach { cat ->
            val icon = resolveCategoryIcon(cat.iconKey)
            val name = if (language == AppLanguage.BENGALI) cat.nameBn else cat.nameEn

            CategoryItem(
                name = name,
                icon = icon,
                isSelected = selectedCategoryId == cat.id,
                onClick = { onCategorySelected(cat.id) },
                testTag = "category_${cat.id}"
            )
        }
    }
}

@Composable
private fun CategoryItem(
    name: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        modifier = Modifier
            .width(68.dp)
            .clickable { onClick() }
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) JisanPrimary else MaterialTheme.colorScheme.surface
                )
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) JisanPrimary else JisanBorder,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = name,
                tint = if (isSelected) Color.White else JisanPrimary,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = name,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) JisanPrimary else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2,
            lineHeight = 13.sp
        )
    }
}

fun resolveCategoryIcon(key: String): ImageVector {
    return when (key) {
        "phone_android" -> Icons.Default.PhoneAndroid
        "recycling" -> Icons.Default.Recycling
        "dialpad" -> Icons.Default.Dialpad
        "power" -> Icons.Default.Power
        "cable" -> Icons.Default.Cable
        "battery_charging_full" -> Icons.Default.BatteryChargingFull
        "headphones" -> Icons.Default.Headphones
        "speaker" -> Icons.Default.Speaker
        "shield" -> Icons.Default.Shield
        "wallpaper" -> Icons.Default.Wallpaper
        else -> Icons.Default.DevicesOther
    }
}
