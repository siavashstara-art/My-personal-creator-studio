package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SiavashStudioColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = StudioObsidian,
    primaryContainer = GoldContainer,
    onPrimaryContainer = OnGoldContainer,
    secondary = GoldLight,
    onSecondary = StudioObsidian,
    secondaryContainer = StudioCardElevated,
    onSecondaryContainer = StudioCream,
    tertiary = StudioAmber,
    onTertiary = StudioObsidian,
    background = StudioObsidian,
    onBackground = StudioCream,
    surface = StudioSurfaceDark,
    onSurface = StudioCream,
    surfaceVariant = StudioCardDark,
    onSurfaceVariant = StudioMuted,
    outline = StudioBorder,
    error = RecordActiveRed,
    onError = StudioCream
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = SiavashStudioColorScheme,
        typography = Typography,
        content = content
    )
}
