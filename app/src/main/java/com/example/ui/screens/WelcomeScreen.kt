package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.ScopePrimary

data class OnboardingStep(
    val title: String,
    val subtitle: String,
    val mascotRes: Int
)

@Composable
fun WelcomeScreen(
    onNavigateToSignUp: () -> Unit,
    onNavigateToSignIn: () -> Unit,
    onContinueAsGuest: () -> Unit,
    onSkip: () -> Unit
) {
    var pagerStep by remember { mutableIntStateOf(0) }

    val steps = listOf(
        OnboardingStep(
            title = "Your academic journey, smarter\n(رحلتك الأكاديمية بشكل أذكى)",
            subtitle = "نظام تشغيل الطلاب الذكي SCOPE يجمع جدولك، مهامك، وذكاء صولا الاصطناعي في مكان واحد.",
            mascotRes = R.drawable.sola_happy
        ),
        OnboardingStep(
            title = "Manage projects & courses\n(إدارة المشاريع والمقررات)",
            subtitle = "نظم مشاريعك الجامعية، تتبع درجاتك، وتواصل مع زملائك في فرق العمل بكفاءة عالية.",
            mascotRes = R.drawable.sola_thinking
        ),
        OnboardingStep(
            title = "Smart Assistant & Memos\n(مساعد ذكي وملاحظات متقدمة)",
            subtitle = "استفد من خرائط المفاهيم، البطاقات التفاعلية، ونظام الملاحظات المتقدم لتوثيق أفكارك الدراسية.",
            mascotRes = R.drawable.sola_happy
        )
    )

    val currentStep = steps[pagerStep]

    val infiniteTransition = rememberInfiniteTransition(label = "WelcomeFloat")
    val floatOffsetY by infiniteTransition.animateFloat(
        initialValue = -7f,
        targetValue = 7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF070A12),
                        Color(0xFF030712)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("welcome_screen")
    ) {
        // Skip Button top-end
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            TextButton(
                onClick = onSkip,
                modifier = Modifier.testTag("welcome_skip_btn")
            ) {
                Text(
                    text = "تخطي",
                    fontFamily = CairoFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF94A3B8),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Hero Sola Mascot
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .offset(y = floatOffsetY.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(ScopePrimary.copy(alpha = 0.5f), Color.Transparent)
                        )
                    )
                    .border(2.dp, Color(0xFF38BDF8).copy(alpha = 0.7f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = currentStep.mascotRes),
                    contentDescription = "Sola Mascot",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(105.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Step Indicator Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                steps.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .size(if (index == pagerStep) 24.dp else 8.dp, 8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (index == pagerStep) Color(0xFF38BDF8) else Color(0xFF334155))
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = currentStep.title,
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Color(0xFFF8FAFC),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = currentStep.subtitle,
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(36.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF121826).copy(alpha = 0.78f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155).copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (pagerStep < steps.size - 1) {
                        Button(
                            onClick = { pagerStep += 1 },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("welcome_next_btn"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8))
                        ) {
                            Text(
                                text = "التالي ➡️",
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    } else {
                        Button(
                            onClick = onNavigateToSignUp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("welcome_signup_btn"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8))
                        ) {
                            Text(
                                text = "ابدأ الآن (إنشاء حساب)",
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onNavigateToSignIn,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("welcome_signin_btn"),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text(
                            text = "لدي حساب بالفعل",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    TextButton(
                        onClick = onContinueAsGuest,
                        modifier = Modifier.testTag("welcome_guest_btn")
                    ) {
                        Text(
                            text = "التجربة كزائر 🚀",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = Color(0xFF38BDF8),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
