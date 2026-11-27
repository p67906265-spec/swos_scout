package it.paolo.swosscout

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Sprite del giocatore (9 colonne) - disegno originale
private val PLAYER_TOP = listOf(
    "...KKK...",
    "..KKKKK..",
    "..KSSSK..",
    "..KSSSK..",
    "...SSS...",
    ".RRRRRRR.",
    "RRRRRRRRR",
    "SRRRRRRRS",
    "S.RRRRR.S",
    "..WWWWW.."
)
private val LEGS_A = listOf("..WW.WW..", "..SS.SS..", "..KK.KK..")
private val LEGS_B = listOf(".WW...WW.", ".SS...SS.", ".KK...KK.")
private val BALL = listOf(
    "..KKK..",
    ".KWWWK.",
    "KWWKWWK",
    "KWKKKWK",
    "KWWKWWK",
    ".KWWWK.",
    "..KKK.."
)
private val SPRITE_COLORS = mapOf(
    'K' to Color(0xFF1A1A1A),
    'S' to Color(0xFFF2A65A),
    'R' to Color(0xFFC62828),
    'W' to Color.White
)

// Font a blocchi 5x7
private val FONT = mapOf(
    'S' to listOf(".####", "#....", "#....", ".###.", "....#", "....#", "####."),
    'W' to listOf("#...#", "#...#", "#...#", "#.#.#", "#.#.#", "##.##", "#...#"),
    'O' to listOf(".###.", "#...#", "#...#", "#...#", "#...#", "#...#", ".###."),
    'C' to listOf(".###.", "#...#", "#....", "#....", "#....", "#...#", ".###."),
    'U' to listOf("#...#", "#...#", "#...#", "#...#", "#...#", "#...#", ".###."),
    'T' to listOf("#####", "..#..", "..#..", "..#..", "..#..", "..#..", "..#..")
)
private val BAND = listOf(
    Color(0xFFFFD54F), Color(0xFFFFD54F), Color(0xFFFFD54F),
    Color.White, Color.White,
    Color(0xFF4FC3F7), Color(0xFF4FC3F7)
)

private fun bounce(x: Float): Float {
    val n1 = 7.5625f
    val d1 = 2.75f
    return when {
        x < 1f / d1 -> n1 * x * x
        x < 2f / d1 -> {
            val y = x - 1.5f / d1
            n1 * y * y + 0.75f
        }
        x < 2.5f / d1 -> {
            val y = x - 2.25f / d1
            n1 * y * y + 0.9375f
        }
        else -> {
            val y = x - 2.625f / d1
            n1 * y * y + 0.984375f
        }
    }
}

private fun DrawScope.drawSprite(
    rows: List<String>,
    ox: Float,
    oy: Float,
    px: Float,
    colors: Map<Char, Color>
) {
    rows.forEachIndexed { j, row ->
        row.forEachIndexed { i, ch ->
            val c = colors[ch]
            if (c != null) {
                drawRect(c, Offset(ox + i * px, oy + j * px), Size(px + 0.6f, px + 0.6f))
            }
        }
    }
}

private fun DrawScope.drawPitch() {
    val stripes = 12
    val sw = size.width / stripes
    for (i in 0 until stripes) {
        drawRect(
            if (i % 2 == 0) Color(0xFF4C9A2A) else Color(0xFF43882A),
            Offset(i * sw, 0f),
            Size(sw + 1f, size.height)
        )
    }
    val line = Color.White.copy(alpha = 0.85f)
    val lw = size.width * 0.012f
    val m = size.width * 0.05f
    drawRect(line, Offset(m, m), Size(size.width - 2 * m, size.height - 2 * m), style = Stroke(lw))
    drawLine(line, Offset(m, size.height / 2f), Offset(size.width - m, size.height / 2f), strokeWidth = lw)
    drawCircle(line, radius = size.width * 0.2f, center = center, style = Stroke(lw))
}

private fun DrawScope.drawGlyph(ch: Char, x: Float, y: Float, u: Float) {
    val rows = FONT[ch] ?: return
    val off = u * 0.45f
    val shadow = Color(0xFF1A1A1A)
    rows.forEachIndexed { j, row ->
        row.forEachIndexed { i, c ->
            if (c == '#') {
                drawRect(shadow, Offset(x + i * u + off, y + j * u + off), Size(u + 0.6f, u + 0.6f))
            }
        }
    }
    rows.forEachIndexed { j, row ->
        row.forEachIndexed { i, c ->
            if (c == '#') {
                drawRect(BAND[j], Offset(x + i * u, y + j * u), Size(u + 0.6f, u + 0.6f))
            }
        }
    }
}

private fun DrawScope.drawTitle(p: Float) {
    val unit = size.width * 0.8f / 29f
    val lines = listOf("SWOS", "SCOUT")
    val lineH = 7 * unit
    val gap = 2.5f * unit
    val blockH = lines.size * lineH + (lines.size - 1) * gap
    val top = size.height * 0.30f - blockH / 2f
    val pad = unit * 2f
    drawRoundRect(
        Color(0x66000000),
        Offset(size.width * 0.1f - pad, top - pad),
        Size(size.width * 0.8f + 2 * pad, blockH + 2 * pad),
        CornerRadius(unit)
    )
    var idx = 0
    lines.forEachIndexed { li, text ->
        val textW = text.length * 6 * unit - unit
        var x = (size.width - textW) / 2f
        val y = top + li * (lineH + gap)
        text.forEach { ch ->
            val q = ((p - (0.04f + idx * 0.05f)) / 0.22f).coerceIn(0f, 1f)
            if (q > 0f) {
                val dy = -(1f - bounce(q)) * (size.height * 0.4f)
                drawGlyph(ch, x, y + dy, unit)
            }
            x += 6 * unit
            idx++
        }
    }
}

private fun DrawScope.drawRunner(p: Float, legsA: Boolean) {
    val px = size.width / 44f
    val sprite = PLAYER_TOP + (if (legsA) LEGS_A else LEGS_B)
    val spriteW = 9 * px
    val runP = (p / 0.95f).coerceIn(0f, 1f)
    val x = -spriteW + runP * (size.width * 0.5f + spriteW)
    val y = size.height * 0.70f
    val shadow = Color(0x55000000)

    drawRect(shadow, Offset(x + px, y + 13 * px), Size(spriteW, px))
    drawSprite(sprite, x, y, px, SPRITE_COLORS)

    val bx = x + spriteW + px * 1.5f
    val by = y + 6 * px - (if (legsA) 0f else px)
    drawRect(shadow, Offset(bx + px, y + 13 * px), Size(5 * px, px))
    drawSprite(BALL, bx, by, px, SPRITE_COLORS)
}

@Composable
fun SplashScreen() {
    val t = remember { Animatable(0f) }
    LaunchedEffect(Unit) { t.animateTo(1f, tween(2300, easing = LinearEasing)) }

    val inf = rememberInfiniteTransition(label = "retro")
    val step by inf.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(260, easing = LinearEasing)),
        label = "step"
    )
    val blink by inf.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(700, easing = LinearEasing)),
        label = "blink"
    )

    Box(Modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            val p = t.value
            drawPitch()
            drawTitle(p)
            drawRunner(p, step < 0.5f)
        }
        Text(
            stringResource(R.string.loading),
            color = Color.White,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 56.dp)
                .alpha(if (blink < 0.5f) 1f else 0.25f)
        )
    }
}
