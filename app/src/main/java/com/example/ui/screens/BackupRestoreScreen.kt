package com.example.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.SiavashStudioViewModel
import com.example.ui.theme.*

@Composable
fun BackupRestoreScreen(
    viewModel: SiavashStudioViewModel,
    modifier: Modifier = Modifier
) {
    val activeProject by viewModel.activeProject.collectAsStateWithLifecycle()

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
                            text = "موتور پشتیبان‌گیری و بازیابی امن (Backup & Restore Engine)",
                            color = GoldLight,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(Icons.Default.Security, contentDescription = null, tint = GoldPrimary)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "پروژه فعال برای پشتیبان: ${activeProject?.name ?: "پروژه انتخاب نشده"}",
                        color = StudioCream,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "تمامی داده‌های کانونیکال با امضای SHA-256 و فایل مانیفست استاندارد پکیج می‌شوند.",
                        color = StudioMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Backup Action
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = StudioCardElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "ایجاد بسته پشتیبان کامل (Full Project Backup)",
                        color = StudioCream,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "یک فایل آرشیو جامع شامل مانیفست، تنظیمات ترک‌ها، اشعار و تمام برداشت‌های صوتی کانونیکال ایجاد می‌کند.",
                        color = StudioMuted,
                        fontSize = 11.sp,
                        lineHeight = 17.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { viewModel.backupCurrentProject() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = StudioObsidian),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ایجاد نسخه پشتیبان اتمیک (.siavash-bak)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Storage Access Framework & USB OTG Info
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = StudioCardDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "پشتیبانی از حافظه خارجی و USB OTG",
                        color = GoldLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• سازگار با استاندارد Storage Access Framework (SAF) اندروید ۱۱ به بالا\n• امکان انتقال بسته‌های پشتیبان به فلش مموری، حافظه SD یا ذخیره‌ساز ابری اختصاصی کاربر\n• بدون دستکاری و بدون حذف فایلهای اصلی پس از پشتیبان‌گیری\n• اعتبارسنجی خودکار هش SHA-256 قبل از بازگردانی برای اطمینان از سلامت صفر تا صد داده‌ها",
                        color = StudioCream,
                        fontSize = 11.sp,
                        lineHeight = 19.sp
                    )
                }
            }
        }
    }
}
