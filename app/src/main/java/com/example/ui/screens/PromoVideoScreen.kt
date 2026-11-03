package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SiavashStudioViewModel
import com.example.ui.theme.*

@Composable
fun PromoVideoScreen(
    viewModel: SiavashStudioViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val promoScript = """
🎬 [سناریو و متن تیزر تبلیغاتی ۳۰ ثانیه‌ای برای آپارات]
عنوان: استودیو شخصی سیاوش | قدرت ضبط و آفرینش آوازی در جیب شما
طراح: اکبر علی‌میری (سیاوش) - اکوسیستم توانا سیتی

[ثانیه ۰ تا ۵ | قلاب بصری (Hook)]:
• تصویر: نمای کلوزآپ از تلفن همراه با امواج طلایی و وی یومتر درخشان در محیط تاریک استودیویی.
• نریشن: «حنجره تو، ساز توست... اما استودیوی حرفه‌ای کجاست؟»

[ثانیه ۶ تا ۱۵ | نمایش امکانات کلیدی]:
• تصویر: خواننده در حال ضبط آواز؛ نرم‌افزار به صورت زنده فرکانس F0، تحریرهای ایرانی و نوسان‌های ربع‌پرده‌ای را بدون خطای فالش آنالیز می‌کند.
• نریشن: «استودیو شخصی سیاوش؛ اولین اپلیکیشن هوشمند بومی با درک عمیق دستگاه‌های ایرانی، تحریرهای اصیل و کوک میکروتونال!»

[ثانیه ۱۶ تا ۲۳ | امنیت و پردازش غیرمخرب]:
• تصویر: مقایسه آنی برداشت‌های A/B، مسترینگ با اکولایزر ۳ بانده و کمپرسور غیرمخرب، و امضای یکپارچگی SHA-256.
• نریشن: «برداشت‌های نامحدود، پردازش غیرمخرب بدون افت کیفیت، و مربی صوتی اختصاصی برای پرورش صدایتان.»

[ثانیه ۲۴ تا ۳۰ | فراخوان به اقدام (Call to Action)]:
• تصویر: نمایش لوگوی طلایی استودیو، شناسه اکوسیستم توانا سیتی و نام سازنده: اکبر علی‌میری (سیاوش).
• نریشن: «صدای تو. موسیقی تو. استودیوی تو. همین حالا در مایکت و کافه بازار دانلود کنید!»
• تگ‌های آپارات: #استودیو_موسیقی #سیاوش_موزیک #آواز_ایرانی #خوانندگی #مربی_آواز #اکبر_علی_میری #توانا_سیتی
    """.trimIndent()

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
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "سناریو و متن تیزر تبلیغاتی ۳۰ ثانیه‌ای آپارات",
                            color = GoldLight,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(Icons.Default.Videocam, contentDescription = null, tint = GoldPrimary)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "طراحی شده مطابق متدولوژی خدمات‌جو و استانداردهای جذابیت رسانه‌ای",
                        color = StudioMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = StudioCardDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "متن و پلان‌های فیلم‌برداری:", color = StudioCream, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Promo Video Script", promoScript)
                                clipboard.setPrimaryClip(clip)
                                viewModel.showToast("متن سناریو کپی شد!")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = StudioObsidian),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("کپی سناریو", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
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
                            text = promoScript,
                            color = StudioCream,
                            fontSize = 12.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}
