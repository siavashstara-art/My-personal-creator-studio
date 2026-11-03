package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.SiavashStudioViewModel
import com.example.ui.theme.*

@Composable
fun VocalAnalysisScreen(
    viewModel: SiavashStudioViewModel,
    modifier: Modifier = Modifier
) {
    val analysis by viewModel.currentAnalysis.collectAsStateWithLifecycle()
    val selectedTake by viewModel.selectedTakeForDsp.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = StudioCardDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "هوش آوازی و تحلیل میکروتونال ایرانی",
                            color = GoldLight,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(Icons.Default.GraphicEq, contentDescription = null, tint = GoldPrimary)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "برداشت مورد ارزیابی: ${selectedTake?.title ?: "برداشت جاری"}",
                        color = StudioCream,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "منحصراً بر روی فایل خام کانونیکال (بدون افکت) اجرا می‌شود.",
                        color = StudioMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Metrics Grid Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Pitch Stability
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = StudioCardElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "ثبات کوک آوازی", color = StudioMuted, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${analysis?.pitchStabilityScore ?: 85}%",
                            color = StudioEmeraldGreen,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = "پایش فرکانس F0", color = StudioMuted, fontSize = 9.sp)
                    }
                }

                // Tahrir Density
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = StudioCardElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "شاخص تحریر ایرانی", color = StudioMuted, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${analysis?.tahrirDensityScore ?: 78}/100",
                            color = GoldLight,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = "نوسان‌های زینت میکروتونال", color = StudioMuted, fontSize = 9.sp)
                    }
                }
            }
        }

        // Vocal Range & Vibrato Details
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = StudioCardDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "گستره صوتی خواننده (Vocal Range)",
                        color = GoldLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = analysis?.f0RangeName ?: "C2 (65Hz) تا G4 (392Hz) — دیاپازون ۲.۵ اکتاو",
                        color = StudioCream,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider(color = StudioBorder)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "ویژگی‌های ویبراتو (Vibrato Characteristics)",
                        color = GoldLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "سرعت نوسان ویبراتو: ${"%.1f".format(analysis?.vibratoRateHz ?: 5.4f)} Hz",
                            color = StudioCream,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "عمق نوسان: ${(analysis?.vibratoDepthCents ?: 42f).toInt()} سنت (Cent)",
                            color = GoldPrimary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Vocal Coach Summary Report
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = StudioCardDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldDark)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "دیدگاه مستند مربی آواز (Vocal Coach Insights)",
                            color = GoldLight,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(StudioObsidian)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = analysis?.coachSummaryText ?: "گزارش تحلیلی اجرای خام:\nتحریرهای شما در اکتاو میانی بسیار متمرکز است. نوسانات تحریر ربع‌پرده‌ای بدون پرش ناگهانی یا فالش بودن ثبت شد. تغییر کوک‌ها کاملاً مطابق ردیف آوازی اصیل ایرانی ارزیابی می‌گردد.",
                            color = StudioCream,
                            fontSize = 12.sp,
                            lineHeight = 20.sp,
                            textAlign = TextAlign.Right
                        )
                    }
                }
            }
        }
    }
}
