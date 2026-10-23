package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.economy.ScopeEconomy
import com.example.data.model.ShopCategory
import com.example.data.model.ShopItem
import com.example.data.model.UserProfile
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.ScopeFocusGreen
import com.example.ui.theme.ScopePrimary
import com.example.ui.theme.ScopeWarningAmber

/**
 * In-App Student Inventory ("الحقيبة والمقتنيات 🎒")
 * Displays owned items with real-time Equip / Unequip capabilities.
 */
@Composable
fun StudentInventoryTab(
    userProfile: UserProfile?,
    onEquipItem: (ShopCategory, ShopItem) -> Unit,
    onUnequipItem: (ShopCategory) -> Unit,
    onOpenStore: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategoryFilter by remember { mutableStateOf<ShopCategory?>(null) }

    val ownedItemIds = userProfile?.unlockedItemIds ?: emptyList()

    // Determine owned items from catalog
    val ownedItems = remember(ownedItemIds, userProfile?.isDeveloper) {
        val list = ScopeEconomy.storeCatalog.filter { item ->
            ownedItemIds.contains(item.id) ||
                (userProfile?.isDeveloper == true && item.id.contains("dev")) ||
                (item.sparksPrice == 0 && item.tokensPrice == 0) // Free starter items
        }
        // If empty, supply default free items so the student can enjoy the inventory immediately
        if (list.isEmpty()) {
            ScopeEconomy.storeCatalog.take(3)
        } else {
            list
        }
    }

    val filteredItems = remember(ownedItems, selectedCategoryFilter) {
        if (selectedCategoryFilter == null) ownedItems
        else ownedItems.filter { it.category == selectedCategoryFilter }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("student_inventory_tab")
    ) {
        // Inventory Header & Store Shortcut
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "الحقيبة والمقتنيات 🎒",
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${ownedItems.size} عنصر متاح للتخصيص الفوري",
                    fontFamily = CairoFontFamily,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            Button(
                onClick = onOpenStore,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ScopeWarningAmber),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("inventory_open_store_btn")
            ) {
                Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
                Spacer(modifier = Modifier.width(4.dp))
                Text("المتجر 🛒", fontFamily = CairoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Category Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedCategoryFilter == null,
                onClick = { selectedCategoryFilter = null },
                label = { Text("الكل", fontFamily = CairoFontFamily, fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ScopePrimary,
                    selectedLabelColor = Color.White
                )
            )

            FilterChip(
                selected = selectedCategoryFilter == ShopCategory.FRAME,
                onClick = { selectedCategoryFilter = ShopCategory.FRAME },
                label = { Text("الإطارات 🖼️", fontFamily = CairoFontFamily, fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ScopePrimary,
                    selectedLabelColor = Color.White
                )
            )

            FilterChip(
                selected = selectedCategoryFilter == ShopCategory.AURA,
                onClick = { selectedCategoryFilter = ShopCategory.AURA },
                label = { Text("الهالات 🔥", fontFamily = CairoFontFamily, fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ScopePrimary,
                    selectedLabelColor = Color.White
                )
            )

            FilterChip(
                selected = selectedCategoryFilter == ShopCategory.TITLE,
                onClick = { selectedCategoryFilter = ShopCategory.TITLE },
                label = { Text("الألقاب 👑", fontFamily = CairoFontFamily, fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ScopePrimary,
                    selectedLabelColor = Color.White
                )
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Items Grid
        if (filteredItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🎒", fontSize = 42.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "لا توجد عناصر في هذا التصنيف حالياً",
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "تصفح المتجر لاقتناء إطارات وهالات مميزة بشرارات التركيز!",
                        fontFamily = CairoFontFamily,
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onOpenStore,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ScopePrimary)
                    ) {
                        Text("زيارة المتجر 🎨", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(480.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(filteredItems, key = { it.id }) { item ->
                    val isEquipped = when (item.category) {
                        ShopCategory.FRAME -> userProfile?.equippedFrame == item.id
                        ShopCategory.AURA -> userProfile?.equippedAura == item.id
                        ShopCategory.TITLE -> userProfile?.equippedTitle == item.name
                        ShopCategory.GLASS_FX -> userProfile?.equippedGlassFx == item.id
                    }

                    InventoryItemCard(
                        item = item,
                        isEquipped = isEquipped,
                        onEquip = { onEquipItem(item.category, item) },
                        onUnequip = { onUnequipItem(item.category) }
                    )
                }
            }
        }
    }
}

@Composable
fun InventoryItemCard(
    item: ShopItem,
    isEquipped: Boolean,
    onEquip: () -> Unit,
    onUnequip: () -> Unit
) {
    val previewColor = remember(item.previewColorHex) {
        try {
            Color(android.graphics.Color.parseColor(item.previewColorHex))
        } catch (e: Exception) {
            ScopePrimary
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulsePreview")
    val previewGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("inventory_item_${item.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F172A)
        ),
        border = if (isEquipped) {
            androidx.compose.foundation.BorderStroke(
                width = 2.dp,
                brush = Brush.sweepGradient(
                    colors = listOf(
                        ScopeFocusGreen,
                        previewColor,
                        ScopeFocusGreen
                    )
                )
            )
        } else {
            androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Rarity and Equipped indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when (item.rarity) {
                                "أسطوري" -> Color(0xFFFFD700).copy(alpha = 0.15f)
                                "نادر" -> Color(0xFF8B5CF6).copy(alpha = 0.15f)
                                else -> Color(0xFF06B6D4).copy(alpha = 0.15f)
                            }
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = item.rarity,
                        fontFamily = CairoFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (item.rarity) {
                            "أسطوري" -> Color(0xFFFFD700)
                            "نادر" -> Color(0xFFC084FC)
                            else -> Color(0xFF38BDF8)
                        }
                    )
                }

                if (isEquipped) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(ScopeFocusGreen)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "مُرتدى",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = ScopeFocusGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Animated Visual Preview Box
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E293B))
                    .border(
                        width = if (isEquipped) 2.5.dp else 1.5.dp,
                        color = previewColor.copy(alpha = if (isEquipped) previewGlow else 0.7f),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (item.category) {
                        ShopCategory.FRAME -> "🖼️"
                        ShopCategory.AURA -> "⚡"
                        ShopCategory.TITLE -> "👑"
                        ShopCategory.GLASS_FX -> "🪟"
                    },
                    fontSize = 28.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Item Name
            Text(
                text = item.name,
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Short Description
            Text(
                text = item.description,
                fontFamily = CairoFontFamily,
                fontSize = 10.sp,
                color = Color.LightGray,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action Button: Equip / Unequip
            if (isEquipped) {
                OutlinedButton(
                    onClick = onUnequip,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                        .testTag("unequip_btn_${item.id}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFEF4444)
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFEF4444))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إلغاء التفعيل", fontFamily = CairoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onEquip,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                        .testTag("equip_btn_${item.id}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ScopePrimary),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تفعيل / ارتداء", fontFamily = CairoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
