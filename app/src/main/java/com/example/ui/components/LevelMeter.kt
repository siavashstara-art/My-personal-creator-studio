package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun AudioLevelMeter(
    level: Float,
    isRecording: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedLevel by animateFloatAsState(
        targetValue = level.coerceIn(0f, 1f),
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "levelAnimation"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(StudioCardDark)
            .border(1.dp, StudioBorder, RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(if (isRecording) RecordActiveRed else GoldDark)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isRecording) "سطح ورودی زنده میکروفون (Real-Time Mic VU)" else "سطح پایش سیگنال (VU Meter Standby)",
                    color = if (isRecording) RecordActiveRed else StudioMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "${(animatedLevel * 100).toInt()}%",
                color = GoldLight,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Multi-segment LED style VU meter
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(StudioObsidian)
        ) {
            val segmentCount = 32
            val segmentWidth = (size.width - (segmentCount - 1) * 2f) / segmentCount
            val activeSegments = (animatedLevel * segmentCount).toInt()

            for (i in 0 until segmentCount) {
                val left = i * (segmentWidth + 2f)
                val color = when {
                    i >= 28 -> RecordActiveRed
                    i >= 22 -> StudioAmber
                    else -> StudioEmeraldGreen
                }
                val alpha = if (i < activeSegments) 1.0f else 0.15f

                drawRect(
                    color = color.copy(alpha = alpha),
                    topLeft = Offset(left, 0f),
                    size = Size(segmentWidth, size.height)
                )
            }
        }
    }
}
