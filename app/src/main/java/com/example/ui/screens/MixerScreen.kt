package com.example.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.SiavashStudioViewModel
import com.example.ui.theme.*

@Composable
fun MixerScreen(
    viewModel: SiavashStudioViewModel,
    modifier: Modifier = Modifier
) {
    val selectedTake by viewModel.selectedTakeForDsp.collectAsStateWithLifecycle()
    val chain by viewModel.currentProcessingChain.collectAsStateWithLifecycle()

    var eqLow by remember(chain) { mutableStateOf(chain?.eqLowGain ?: 0f) }
    var eqMid by remember(chain) { mutableStateOf(chain?.eqMidGain ?: 0f) }
    var eqHigh by remember(chain) { mutableStateOf(chain?.eqHighGain ?: 0f) }
    var compThresh by remember(chain) { mutableStateOf(chain?.compressorThreshold ?: -18f) }
    var compRatio by remember(chain) { mutableStateOf(chain?.compressorRatio ?: 3.0f) }
    var reverbRoom by remember(chain) { mutableStateOf(chain?.reverbRoomSize ?: 0.25f) }
    var reverbWet by remember(chain) { mutableStateOf(chain?.reverbWetDry ?: 0.15f) }
    var volGain by remember(chain) { mutableStateOf(chain?.volumeGain ?: 1.0f) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
    ) {
        // DSP Target Info
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
                        Text(
                            text = "میکسر پردازش غیرمخرب (Non-Destructive DSP)",
                            color = GoldLight,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(Icons.Default.Equalizer, contentDescription = null, tint = GoldPrimary)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "برداشت تحت پردازش: ${selectedTake?.title ?: "برداشت اول ترک جاری"}",
                        color = StudioCream,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "اصل فایل خام (Canonical Raw) دست‌نخورده باقی می‌ماند.",
                        color = StudioEmeraldGreen,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // 3-Band Parametric EQ Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = StudioCardDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "اکولایزر ۳ بانده پارامتریک (3-Band Studio EQ)",
                        color = GoldLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(text = "فرکانس‌های بم (Low Gain): ${"%.1f".format(eqLow)} dB", color = StudioCream, fontSize = 11.sp)
                    Slider(
                        value = eqLow,
                        onValueChange = {
                            eqLow = it
                            viewModel.updateProcessingChain(eqLow = it)
                        },
                        valueRange = -12f..12f,
                        colors = SliderDefaults.colors(thumbColor = GoldPrimary, activeTrackColor = GoldLight)
                    )

                    Text(text = "فرکانس‌های میانی (Mid Gain): ${"%.1f".format(eqMid)} dB", color = StudioCream, fontSize = 11.sp)
                    Slider(
                        value = eqMid,
                        onValueChange = {
                            eqMid = it
                            viewModel.updateProcessingChain(eqMid = it)
                        },
                        valueRange = -12f..12f,
                        colors = SliderDefaults.colors(thumbColor = GoldPrimary, activeTrackColor = GoldLight)
                    )

                    Text(text = "فرکانس‌های زیر و شفافیت (High Gain): ${"%.1f".format(eqHigh)} dB", color = StudioCream, fontSize = 11.sp)
                    Slider(
                        value = eqHigh,
                        onValueChange = {
                            eqHigh = it
                            viewModel.updateProcessingChain(eqHigh = it)
                        },
                        valueRange = -12f..12f,
                        colors = SliderDefaults.colors(thumbColor = GoldPrimary, activeTrackColor = GoldLight)
                    )
                }
            }
        }

        // Compressor & Dynamics
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = StudioCardDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "کمپرسور داینامیک صدا (Vocal Compressor)",
                        color = GoldLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(text = "آستانه تحریک (Threshold): ${compThresh.toInt()} dB", color = StudioCream, fontSize = 11.sp)
                    Slider(
                        value = compThresh,
                        onValueChange = {
                            compThresh = it
                            viewModel.updateProcessingChain(compThresh = it)
                        },
                        valueRange = -40f..0f,
                        colors = SliderDefaults.colors(thumbColor = GoldPrimary, activeTrackColor = GoldLight)
                    )

                    Text(text = "نسبت فشردگی (Ratio): ${"%.1f".format(compRatio)}:1", color = StudioCream, fontSize = 11.sp)
                    Slider(
                        value = compRatio,
                        onValueChange = {
                            compRatio = it
                            viewModel.updateProcessingChain(compRatio = it)
                        },
                        valueRange = 1f..10f,
                        colors = SliderDefaults.colors(thumbColor = GoldPrimary, activeTrackColor = GoldLight)
                    )
                }
            }
        }

        // Reverb & Ambience
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = StudioCardDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "ریورب استودیویی (Studio Reverb & Ambience)",
                        color = GoldLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(text = "ابعاد اتاق و پژواک (Room Size): ${(reverbRoom * 100).toInt()}%", color = StudioCream, fontSize = 11.sp)
                    Slider(
                        value = reverbRoom,
                        onValueChange = {
                            reverbRoom = it
                            viewModel.updateProcessingChain(reverbRoom = it)
                        },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(thumbColor = GoldPrimary, activeTrackColor = GoldLight)
                    )

                    Text(text = "میزان ترکیب افکت (Wet/Dry Mix): ${(reverbWet * 100).toInt()}%", color = StudioCream, fontSize = 11.sp)
                    Slider(
                        value = reverbWet,
                        onValueChange = {
                            reverbWet = it
                            viewModel.updateProcessingChain(reverbWet = it)
                        },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(thumbColor = GoldPrimary, activeTrackColor = GoldLight)
                    )
                }
            }
        }

        // Master Output & Export Button
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = StudioCardDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "خروجی مستر و لیمیتر نهایی",
                        color = GoldLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(text = "ولوم کلی فیدر (Master Gain): ${(volGain * 100).toInt()}%", color = StudioCream, fontSize = 11.sp)
                    Slider(
                        value = volGain,
                        onValueChange = {
                            volGain = it
                            viewModel.updateProcessingChain(volGain = it)
                        },
                        valueRange = 0f..2f,
                        colors = SliderDefaults.colors(thumbColor = GoldPrimary, activeTrackColor = GoldLight)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { viewModel.exportMasterMix() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = StudioObsidian),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "رندر و استخراج مستر نهایی (Export Master WAV)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
