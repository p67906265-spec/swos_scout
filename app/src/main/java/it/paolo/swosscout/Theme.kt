package it.paolo.swosscout

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/** Colori "neon" del tema, nelle due varianti scura e chiara. */
class NeonColors(
    val cyan: Color,
    val green: Color,
    val pink: Color,
    val gold: Color,
    val bgTop: Color,
    val bgBottom: Color
)

private val DarkNeon = NeonColors(
    cyan = Color(0xFF00E5FF),
    green = Color(0xFF7CFFB2),
    pink = Color(0xFFFF2BD6),
    gold = Color(0xFFFFD54F),
    bgTop = Color(0xFF0B1230),
    bgBottom = Color(0xFF050814)
)

private val LightNeon = NeonColors(
    cyan = Color(0xFF00838F),
    green = Color(0xFF2E7D32),
    pink = Color(0xFFC2185B),
    gold = Color(0xFFF9A825),
    bgTop = Color(0xFFEAF2FF),
    bgBottom = Color(0xFFD5E3F8)
)

val LocalNeon = staticCompositionLocalOf { DarkNeon }

object Neon {
    val cyan: Color
        @Composable get() = LocalNeon.current.cyan
    val green: Color
        @Composable get() = LocalNeon.current.green
    val pink: Color
        @Composable get() = LocalNeon.current.pink
    val gold: Color
        @Composable get() = LocalNeon.current.gold
    val bgTop: Color
        @Composable get() = LocalNeon.current.bgTop
    val bgBottom: Color
        @Composable get() = LocalNeon.current.bgBottom
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun SwosTheme(dark: Boolean, content: @Composable () -> Unit) {
    val neon = if (dark) DarkNeon else LightNeon
    val colors = if (dark) {
        darkColorScheme(
            primary = Color(0xFF00E5FF),
            onPrimary = Color(0xFF00232B),
            primaryContainer = Color(0xFF0B3A4A),
            onPrimaryContainer = Color(0xFF00E5FF),
            secondary = Color(0xFF7CFFB2),
            onSecondary = Color(0xFF003919),
            secondaryContainer = Color(0xFF0B3A4A),
            onSecondaryContainer = Color(0xFF00E5FF),
            tertiary = Color(0xFFFF2BD6),
            background = Color(0xFF070B1A),
            onBackground = Color(0xFFE6F1FF),
            surface = Color(0xFF0E1530),
            onSurface = Color(0xFFE6F1FF),
            surfaceVariant = Color(0xFF18224A),
            onSurfaceVariant = Color(0xFF9FB3D9),
            outline = Color(0xFF4A5B8C)
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF00838F),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFCDEFF3),
            onPrimaryContainer = Color(0xFF00363D),
            secondary = Color(0xFF2E7D32),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFCDEFF3),
            onSecondaryContainer = Color(0xFF00363D),
            tertiary = Color(0xFFC2185B),
            background = Color(0xFFF2F6FF),
            onBackground = Color(0xFF0B1230),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF0B1230),
            surfaceVariant = Color(0xFFDCE6F7),
            onSurfaceVariant = Color(0xFF45577F),
            outline = Color(0xFF8FA0C8)
        )
    }

    val view = LocalView.current
    SideEffect {
        val window = view.context.findActivity()?.window
        if (window != null) {
            window.statusBarColor = neon.bgTop.toArgb()
            window.navigationBarColor = neon.bgBottom.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !dark
            controller.isAppearanceLightNavigationBars = !dark
        }
    }

    CompositionLocalProvider(LocalNeon provides neon) {
        MaterialTheme(colorScheme = colors, content = content)
    }
}

/** Colore del ruolo in campo. */
fun posColor(pos: String): Color = when (pos) {
    "G" -> Color(0xFFFFD54F)
    "D", "RB", "LB" -> Color(0xFF4FC3F7)
    "M" -> Color(0xFF2ECC71)
    "RW", "LW" -> Color(0xFFB388FF)
    else -> Color(0xFFFF4D8D)
}
