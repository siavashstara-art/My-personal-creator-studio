package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TakeEntity
import com.example.ui.AppScreen
import com.example.ui.SiavashStudioViewModel
import com.example.ui.components.AudioLevelMeter
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun StudioScreen(
    viewModel: SiavashStudioViewModel,
    modifier: Modifier = Modifier
) {
    val activeProject by viewModel.activeProject.collectAsStateWithLifecycle()
    val activeTracks by viewModel.activeTracks.collectAsStateWithLifecycle()
    val selectedTrackId by viewModel.selectedTrackId.collectAsStateWithLifecycle()
    val currentTakes by viewModel.currentTakes.collectAsStateWithLifecycle()
    val isRecording by viewModel.recorder.isRecording.collectAsStateWithLifecycle()
    val rmsLevel by viewModel.recorder.currentRmsLevel.collectAsStateWithLifecycle()
    val recDurationMs by viewModel.recorder.recordedDurationMs.collectAsStateWithLifecycle()
    val isPlaying by viewModel.player.isPlaying.collectAsStateWithLifecycle()
    val playPositionMs by viewModel.player.currentPositionMs.collectAsStateWithLifecycle()
    val activeTakeAB by viewModel.activeTakeAB.collectAsStateWithLifecycle()
    val takeA by viewModel.takeA.collectAsStateWithLifecycle()
    val takeB by viewModel.takeB.collectAsStateWithLifecycle()
    val activeLyrics by viewModel.activeLyrics.collectAsStateWithLifecycle()

    var showLyricsDialog by remember { mutableStateOf(false) }
    var isTeleprompterAutoScroll by remember { mutableStateOf(false) }
    val lyricsScrollState = rememberScrollState()

    // Teleprompter auto scroll effect
    LaunchedEffect(isTeleprompterAutoScroll, isPlaying) {
        if (isTeleprompterAutoScroll) {
            while (isTeleprompterAutoScroll) {
                delay(120)
                if (lyricsScrollState.value < lyricsScrollState.maxValue) {
                    lyricsScrollState.scrollTo(lyricsScrollState.value + 4)
                }
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
    ) {
        // Project Overview Card
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
                        Column {
                            Text(
                                text = activeProject?.name ?: "پروژه فعال انتخاب نشده",
                                color = GoldLight,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "گام و کوک: ${activeProject?.referenceTuning ?: "میکروتونال ایرانی"}",
                                color = StudioMuted,
                                fontSize = 11.sp
                            )
                        }
                        AssistChip(
                            onClick = { viewModel.navigateTo(AppScreen.PROJECTS) },
                            label = { Text("تغییر پروژه", fontSize = 11.sp, color = StudioCream) },
                            leadingIcon = {
                                Icon(Icons.Default.Folder, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                            },
                            colors = AssistChipDefaults.assistChipColors(containerColor = StudioCardElevated)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Track selector chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        activeTracks.forEach { track ->
                            FilterChip(
                                selected = track.id == selectedTrackId,
                                onClick = { viewModel.selectTrack(track.id) },
                                label = { Text(track.title, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GoldContainer,
                                    selectedLabelColor = GoldLight,
                                    containerColor = StudioCardElevated,
                                    labelColor = StudioCream
                                )
                            )
                        }
                    }
                }
            }
        }

        // Live Mic VU Meter & Record Control
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = StudioCardDark),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isRecording) RecordActiveRed else StudioBorder
                )
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AudioLevelMeter(level = rmsLevel, isRecording = isRecording)

                    Spacer(modifier = Modifier.height(12.dp))

                    val recSeconds = (recDurationMs / 1000L) % 60
                    val recMinutes = (recDurationMs / 1000L) / 60
                    Text(
                        text = if (isRecording)
                            "درحال ضبط زنده کانونیکال: %02d:%02d".format(recMinutes, recSeconds)
                        else
                            "آماده برای ضبط برداشت جدید (Direct Canonical Mode)",
                        color = if (isRecording) RecordActiveRed else StudioCream,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Primary Record Button
                    Button(
                        onClick = {
                            if (isRecording) {
                                viewModel.stopRecordingAndSave()
                            } else {
                                viewModel.startRecording()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRecording) RecordActiveRed else GoldPrimary,
                            contentColor = if (isRecording) StudioCream else StudioObsidian
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = if (isRecording) "Stop" else "Record",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isRecording) "توقف و محاسبه امضای SHA-256" else "آغاز ضبط آواز (Record Take)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // A/B Take Comparison Strip
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = StudioCardElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldDark)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "مقایسه بلادرنگ برداشت‌ها (A/B Instant Compare)",
                            color = GoldLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "موقعیت: ${(playPositionMs / 1000f)}s",
                            color = StudioMuted,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.switchAB("A") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (activeTakeAB == "A") GoldContainer else Color.Transparent,
                                contentColor = if (activeTakeAB == "A") GoldLight else StudioCream
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (activeTakeAB == "A") GoldPrimary else StudioBorder
                            )
                        ) {
                            Text(text = "برداشت A: ${takeA?.title ?: "تعیین نشده"}", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.switchAB("B") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (activeTakeAB == "B") GoldContainer else Color.Transparent,
                                contentColor = if (activeTakeAB == "B") GoldLight else StudioCream
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (activeTakeAB == "B") GoldPrimary else StudioBorder
                            )
                        ) {
                            Text(text = "برداشت B: ${takeB?.title ?: "تعیین نشده"}", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Persian Lyrics & Teleprompter Card
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "تله‌پرومپتر اشعار (Persian Teleprompter)",
                                color = StudioCream,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row {
                            IconButton(onClick = { isTeleprompterAutoScroll = !isTeleprompterAutoScroll }) {
                                Icon(
                                    imageVector = if (isTeleprompterAutoScroll) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Auto Scroll",
                                    tint = if (isTeleprompterAutoScroll) RecordActiveRed else GoldLight
                                )
                            }
                            IconButton(onClick = { showLyricsDialog = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Lyrics", tint = StudioMuted)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(StudioObsidian)
                            .padding(10.dp)
                            .verticalScroll(lyricsScrollState)
                    ) {
                        Text(
                            text = activeLyrics?.content ?: "شعری ثبت نشده است. برای ویرایش، روی آیکون مداد ضربه بزنید.",
                            color = StudioCream,
                            fontSize = 15.sp,
                            lineHeight = 24.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Multi-Take Management List Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "برداشت‌های ضبط‌شده ترک جاری (${currentTakes.size} Take)",
                    color = GoldLight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "محافظت کانونیکال فعال 🛡️",
                    color = StudioEmeraldGreen,
                    fontSize = 11.sp
                )
            }
        }

        // Takes List
        if (currentTakes.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = StudioCardDark)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "هنوز برداشتی برای این ترک ضبط نشده است. دکمه ضبط را فشار دهید.",
                            color = StudioMuted,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(currentTakes) { take ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.playTake(take) },
                    colors = CardDefaults.cardColors(containerColor = StudioCardDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(GoldContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Audiotrack, contentDescription = null, tint = GoldLight, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = take.title,
                                    color = StudioCream,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "نوع: ${take.sourceType}",
                                        color = GoldLight,
                                        fontSize = 10.sp
                                    )
                                    Text(
                                        text = "• SHA-256 تأیید شده",
                                        color = StudioEmeraldGreen,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(onClick = { viewModel.setTakeA(take) }) {
                                Text("A", color = GoldLight, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            IconButton(onClick = { viewModel.setTakeB(take) }) {
                                Text("B", color = GoldLight, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            IconButton(onClick = {
                                viewModel.playTake(take)
                                viewModel.navigateTo(AppScreen.ANALYSIS)
                            }) {
                                Icon(Icons.Default.Analytics, contentDescription = "Analyze", tint = StudioAmber)
                            }
                        }
                    }
                }
            }
        }
    }

    // Lyrics Edit Dialog
    if (showLyricsDialog) {
        var textContent by remember { mutableStateOf(activeLyrics?.content ?: "") }
        AlertDialog(
            onDismissRequest = { showLyricsDialog = false },
            title = { Text("ویرایش متن ترانه و تله‌پرومپتر", color = GoldLight, fontSize = 14.sp) },
            text = {
                OutlinedTextField(
                    value = textContent,
                    onValueChange = { textContent = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = StudioCream,
                        unfocusedTextColor = StudioCream,
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = StudioBorder
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateLyrics(textContent)
                    showLyricsDialog = false
                }) {
                    Text("ذخیره شعر", color = GoldPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLyricsDialog = false }) {
                    Text("انصراف", color = StudioMuted)
                }
            },
            containerColor = StudioCardDark
        )
    }
}
