package it.paolo.swosscout

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val NeonCyan = Color(0xFF00E5FF)
val NeonGreen = Color(0xFF7CFFB2)
val NeonPink = Color(0xFFFF2BD6)
val BgTop = Color(0xFF0B1230)
val BgBottom = Color(0xFF050814)

@Composable
fun SwosTheme(content: @Composable () -> Unit) {
    val colors = darkColorScheme(
        primary = NeonCyan,
        onPrimary = Color(0xFF00232B),
        primaryContainer = Color(0xFF0B3A4A),
        onPrimaryContainer = NeonCyan,
        secondary = NeonGreen,
        onSecondary = Color(0xFF003919),
        secondaryContainer = Color(0xFF0B3A4A),
        onSecondaryContainer = NeonCyan,
        tertiary = NeonPink,
        background = Color(0xFF070B1A),
        onBackground = Color(0xFFE6F1FF),
        surface = Color(0xFF0E1530),
        onSurface = Color(0xFFE6F1FF),
        surfaceVariant = Color(0xFF18224A),
        onSurfaceVariant = Color(0xFF9FB3D9),
        outline = Color(0xFF4A5B8C)
    )
    MaterialTheme(colorScheme = colors, content = content)
}

/** Colore del ruolo in campo. */
fun posColor(pos: String): Color = when (pos) {
    "G" -> Color(0xFFFFD54F)
    "D", "RB", "LB" -> Color(0xFF4FC3F7)
    "M" -> NeonGreen
    "RW", "LW" -> Color(0xFFB388FF)
    else -> Color(0xFFFF4D8D)
}
