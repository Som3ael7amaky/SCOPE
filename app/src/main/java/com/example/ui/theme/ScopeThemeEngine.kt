package com.example.ui.theme

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.data.model.ThemePack

object ScopeThemeEngine {

    val DEFAULT_THEME_PACK = ThemePack(
        id = "cyber_academia",
        name = "Cyber Academia (الافتراضي)",
        description = "الخلفيات الرسمية ثلاثية الأبعاد لمحطات SCOPE مع تدرجات نيون فسيحة",
        previewColorHex = "#3B82F6",
        homeBgRes = R.drawable.home_bg_default,
        studyBgRes = R.drawable.study_bg_focus,
        scheduleBgRes = R.drawable.schedule_bg_intel,
        projectsBgRes = R.drawable.projects_bg_kanban,
        campusBgRes = R.drawable.campus_bg_social,
        isUnlocked = true,
        tokenPrice = 0
    )

    val SOLAR_NEBULA_PACK = ThemePack(
        id = "solar_nebula",
        name = "Solar Nebula (السديم الشمسي)",
        description = "أطياف كوزمية متوهجة مستوحاة من مدارات كوكب الزهرة وطاقة صولا الذهبية",
        previewColorHex = "#F59E0B",
        homeBgRes = R.drawable.home_bg_default,
        studyBgRes = R.drawable.study_bg_focus,
        scheduleBgRes = R.drawable.schedule_bg_intel,
        projectsBgRes = R.drawable.projects_bg_kanban,
        campusBgRes = R.drawable.campus_bg_social,
        isUnlocked = false,
        tokenPrice = 35
    )

    val AVAILABLE_PACKS = listOf(
        DEFAULT_THEME_PACK,
        SOLAR_NEBULA_PACK
    )

    fun getThemePack(packId: String): ThemePack {
        return AVAILABLE_PACKS.firstOrNull { it.id == packId } ?: DEFAULT_THEME_PACK
    }
}

/**
 * Adaptive Frosted Glass Depth Container:
 * When [enable3dBackgrounds] is ON: Renders screen-specific 3D background with adaptive
 * frosted glass overlay (multi-layer gradient depth blur scrim) ensuring 100% text/UI legibility.
 * When [enable3dBackgrounds] is OFF: Minimalist AMOLED dark canvas.
 */
@Composable
fun AdaptiveBackgroundContainer(
    bgResId: Int,
    enable3dBackgrounds: Boolean,
    modifier: Modifier = Modifier,
    frostedAlpha: Float = 0.86f,
    content: @Composable () -> Unit
) {
    val bgColor = MaterialTheme.colorScheme.background
    val scrimBrush = androidx.compose.runtime.remember(bgColor, frostedAlpha) {
        Brush.verticalGradient(
            colors = listOf(
                bgColor.copy(alpha = (frostedAlpha - 0.08f).coerceAtLeast(0.5f)),
                bgColor.copy(alpha = frostedAlpha),
                bgColor.copy(alpha = (frostedAlpha + 0.08f).coerceAtMost(0.97f))
            )
        )
    }
    val sheenBrush = androidx.compose.runtime.remember {
        Brush.radialGradient(
            colors = listOf(
                Color(0x1238BDF8),
                Color.Transparent
            ),
            radius = 1200f
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (enable3dBackgrounds) {
            Box(modifier = Modifier.fillMaxSize()) {
                // 3D Background Drawable
                Image(
                    painter = painterResource(id = bgResId),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Adaptive Frosted Glass Depth Blur Overlay for 100% legibility
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(scrimBrush)
                )

                // Subtle cyan/purple holographic sheen highlight on top
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(sheenBrush)
                )
            }
        } else {
            // Minimalist AMOLED dark slate background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bgColor)
            )
        }

        // Child composables render clearly on top of the frosted scrim
        content()
    }
}
