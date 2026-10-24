package com.example.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
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

@Composable
fun ProductCard(
    product: Product,
    language: AppLanguage,
    isWishlisted: Boolean = false,
    onProductClick: (Product) -> Unit,
    onAddToCart: (Product) -> Unit,
    onToggleWishlist: ((Product) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val displayName = if (language == AppLanguage.BENGALI) product.nameBn else product.nameEn

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .clickable { onProductClick(product) }
            .testTag("product_card_${product.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Image Area with badges & wishlist toggle
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
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
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (product.isSecondHand) Icons.Default.Recycling else Icons.Default.PhoneAndroid,
                            contentDescription = null,
                            tint = if (product.isSecondHand) JisanAccent else JisanPrimary,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                // Condition Badge (Top Start)
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = if (product.isSecondHand) SecondHandBadgeBg else Color(0xFFE3F2FD),
                    tonalElevation = 2.dp
                ) {
                    Text(
                        text = if (product.isSecondHand) Strings.secondHand(language) else Strings.brandNew(language),
                        color = if (product.isSecondHand) SecondHandBadgeText else JisanPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Wishlist Heart Button (Top End)
                if (onToggleWishlist != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.35f))
                            .clickable { onToggleWishlist(product) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Wishlist",
                            tint = if (isWishlisted) JisanRed else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Stock Badge (Bottom End)
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = if (product.isInStock) JisanGreenLight else JisanRedLight
                ) {
                    Text(
                        text = if (product.isInStock) Strings.inStock(language) else Strings.outOfStock(language),
                        color = if (product.isInStock) JisanGreen else JisanRed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Discount percentage pill (Bottom Start)
                if (product.discountPercentage > 0) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp),
                        shape = RoundedCornerShape(4.dp),
                        color = JisanAccent
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${product.discountPercentage}% OFF",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Info Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                // Brand
                Text(
                    text = product.brand,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (MaterialTheme.colorScheme.background == Color(0xFF0B132B)) Color(0xFF90CAF9) else JisanPrimary,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Product Name - High Contrast
                Text(
                    text = displayName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 17.sp,
                    modifier = Modifier.height(34.dp)
                )

                // Second-Hand Highlights (battery health etc.)
                if (product.isSecondHand && (product.batteryHealth != null || product.warrantyMonths != null)) {
                    Row(
                        modifier = Modifier.padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (product.batteryHealth != null) {
                            Text(
                                text = "⚡${product.batteryHealth}%",
                                fontSize = 11.sp,
                                color = JisanGreen,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        if (product.warrantyMonths != null) {
                            Text(
                                text = "🛡️${product.warrantyMonths}m Warranty",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Price Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        text = Formatters.formatPrice(product.price),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (product.mrp > product.price) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = Formatters.formatPrice(product.mrp),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Add to Cart Button
                Button(
                    onClick = { onAddToCart(product) },
                    enabled = product.isInStock,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .testTag("add_to_cart_${product.id}"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JisanPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.outline
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = Strings.addToCart(language),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
