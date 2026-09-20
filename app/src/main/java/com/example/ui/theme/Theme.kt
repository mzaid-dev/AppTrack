package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SoftLightColorScheme = lightColorScheme(
    primary = SmartNestBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEBF2FE),
    onPrimaryContainer = SmartNestBlueHover,
    secondary = SoftClientEmerald,
    onSecondary = Color.White,
    secondaryContainer = SoftClientEmeraldLight,
    onSecondaryContainer = SoftClientEmeraldHover,
    tertiary = SoftPurple,
    onTertiary = Color.White,
    background = SmartNestBg,
    onBackground = SmartNestTextHeader,
    surface = SmartNestCard,
    onSurface = SmartNestTextHeader,
    surfaceVariant = SmartNestInputBg,
    onSurfaceVariant = SmartNestPlaceholder,
    outline = SmartNestInputBorder,
    outlineVariant = Color(0xFFF1F5F9)
)

private val AppTrackColorScheme = lightColorScheme(
    primary = Color(0xFF2563EB),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEFF6FF),
    onPrimaryContainer = Color(0xFF1D4ED8),
    secondary = Color(0xFF16A34A),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCFCE7),
    onSecondaryContainer = Color(0xFF15803D),
    tertiary = Color(0xFF6366F1),
    onTertiary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF64748B),
    outline = Color(0xFFE2E8F0),
    outlineVariant = Color(0xFFF8FAFC)
)

@Composable
fun AppTrackTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AppTrackColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun SmartNestTheme(
    content: @Composable () -> Unit
) = AppTrackTheme(content)

@Composable
fun LaunchPulseTheme(
    content: @Composable () -> Unit
) = AppTrackTheme(content)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) = AppTrackTheme(content)
