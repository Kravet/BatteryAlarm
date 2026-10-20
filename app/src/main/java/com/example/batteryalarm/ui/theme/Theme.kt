package com.example.batteryalarm.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Immutable
data class BatteryAlarmColors(
    /** Text links and light accents on the current surface. */
    val brandGreen: Color,
    /** Solid fills (buttons, shield, switch track) — same in both themes for contrast. */
    val brandGreenFill: Color,
    val brandGreenContainer: Color,
    val onBrandGreenFill: Color,
    /** Glyph/check drawn on solid brand or accent fills. */
    val onFilledAccent: Color,
    val protectionOffBackground: Color,
    val protectionOffBorder: Color,
    val protectionOffIconBackground: Color,
    val protectionOffAccent: Color,
    val protectionOffDropShadow: Color,
    val statusCardBackground: Color,
    val statusCardBorder: Color,
    val statusCardDropShadow: Color,
)

private val LightBatteryAlarmColors = BatteryAlarmColors(
    brandGreen = BrandGreen,
    brandGreenFill = BrandGreen,
    brandGreenContainer = BrandGreenContainerLight,
    onBrandGreenFill = Color.White,
    onFilledAccent = Color.White,
    protectionOffBackground = ProtectionOffBackgroundLight,
    protectionOffBorder = ProtectionOffBorderLight,
    protectionOffIconBackground = ProtectionOffIconBackgroundLight,
    protectionOffAccent = ProtectionOffAccentLight,
    protectionOffDropShadow = ProtectionOffDropShadowLight,
    statusCardBackground = LightSurface,
    statusCardBorder = StatusCardBorderLight,
    statusCardDropShadow = StatusCardDropShadowLight,
)

private val DarkBatteryAlarmColors = BatteryAlarmColors(
    brandGreen = BrandGreenOnDark,
    brandGreenFill = BrandGreen,
    brandGreenContainer = BrandGreenContainerDark,
    onBrandGreenFill = Color.White,
    onFilledAccent = Color.White,
    protectionOffBackground = ProtectionOffBackgroundDark,
    protectionOffBorder = ProtectionOffBorderDark,
    protectionOffIconBackground = ProtectionOffIconBackgroundDark,
    protectionOffAccent = ProtectionOffAccentDark,
    protectionOffDropShadow = ProtectionOffDropShadowDark,
    statusCardBackground = DarkSurface,
    statusCardBorder = StatusCardBorderDark,
    statusCardDropShadow = StatusCardDropShadowDark,
)

private val LocalBatteryAlarmColors = staticCompositionLocalOf<BatteryAlarmColors> {
    error("BatteryAlarmThemeTokens.colors used outside BatteryAlarmTheme")
}

object BatteryAlarmThemeTokens {
    val colors: BatteryAlarmColors
        @Composable
        @ReadOnlyComposable
        get() = LocalBatteryAlarmColors.current
}

private val DarkColorScheme = darkColorScheme(
    primary = BrandGreenOnDark,
    onPrimary = Color(0xFF0D1F0E),
    secondary = DarkSecondary,
    onSecondary = DarkBackground,
    tertiary = ProtectionOffAccentDark,
    onTertiary = Color.White,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = StatusCardBorderDark,
)

private val LightColorScheme = lightColorScheme(
    primary = BrandGreen,
    onPrimary = Color.White,
    secondary = LightSecondary,
    onSecondary = Color.White,
    tertiary = ProtectionOffAccentLight,
    onTertiary = Color.White,
    background = LightBackground,
    surface = LightSurface,
    onBackground = LightOnBackground,
    onSurface = LightOnSurface,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = StatusCardBorderLight,
)

@Composable
fun BatteryAlarmTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Brand surfaces and Material roles must stay aligned; dynamic wallpaper
    // schemes would diverge from BatteryAlarmThemeTokens.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val batteryAlarmColors = if (darkTheme) DarkBatteryAlarmColors else LightBatteryAlarmColors

    CompositionLocalProvider(LocalBatteryAlarmColors provides batteryAlarmColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
