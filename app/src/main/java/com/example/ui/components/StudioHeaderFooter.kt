package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun StudioHeader(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            .border(
                1.dp,
                Brush.horizontalGradient(listOf(GoldDark, GoldLight, GoldDark)),
                RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = StudioCardDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "«اکوسیستم آفرینش آفرینا شهر جدید نیو متاورسی جهان ایبل سیتی (توانا سیتی) - FB NewMeta SIAVASH MUSIC CREATOR»",
                color = GoldLight,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "استودیو شخصی و موتور آفرینش موسیقی و آواز سیاوش (متدولوژی خدمات‌جو)",
                color = StudioCream,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun StudioFooter(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .border(
                1.dp,
                Brush.horizontalGradient(listOf(GoldDark, GoldLight, GoldDark)),
                RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = StudioCardDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "«تمام حقوق مادی و معنوی این اثر متعلق است به آقای اکبر علی‌میری (معروف به سیاوش / توسعه‌دهنده).»",
                color = GoldPrimary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "پورتفوی جامع: فرش‌باز • لینگوی • تی‌دیپلوی • توانا پلتفرم • دکورمیت • سالونیار • کلینیکیار • خدمات‌یار • شطرنج هم‌جهان و ابزارهای توسعه‌دهندگان",
                color = StudioMuted,
                fontSize = 9.sp,
                textAlign = TextAlign.Center,
                lineHeight = 13.sp
            )
        }
    }
}
