package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SaharaGold,
    secondary = PharaohAmber,
    tertiary = TurquoiseNile,
    background = CairoMidnight,
    surface = NileDeepBlue,
    onPrimary = CairoMidnight,
    onSecondary = IvoryWhite,
    onTertiary = CairoMidnight,
    onBackground = IvoryWhite,
    onSurface = SandBeige
)

private val LightColorScheme = lightColorScheme(
    primary = PharaohAmber,
    secondary = ClaySienna,
    tertiary = TurquoiseNile,
    background = IvoryWhite,
    surface = SandBeige,
    onPrimary = IvoryWhite,
    onSecondary = IvoryWhite,
    onTertiary = IvoryWhite,
    onBackground = CairoMidnight,
    onSurface = NileDeepBlue
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Keep dynamic color standard
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
