package it.paolo.swosscout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Riga compatta di un giocatore: ruolo, nome, totale abilità, valore, confronto e preferito. */
@Composable
fun PlayerRow(
    p: Player,
    isFav: Boolean,
    custom: Int?,
    onToggleFav: () -> Unit,
    onEditValue: () -> Unit,
    onCompare: () -> Unit
) {
    val accent = posColor(p.position)
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .width(44.dp)
                    .background(accent.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    .border(1.dp, accent, RoundedCornerShape(8.dp))
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    p.position,
                    color = accent,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    p.name,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    stringResource(R.string.tot_label, p.total) +
                        (if (p.keySkills.isEmpty()) "" else " · " + stringResource(R.string.key_label, p.keySkills)),
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(
                Modifier
                    .clickable { onEditValue() }
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    formatValue(custom ?: p.valueK) + " ✎",
                    color = if (custom != null) Neon.pink else Neon.green,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (custom != null) {
                    Text(
                        stringResource(R.string.orig_value, formatValue(p.valueK)),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                "⇄",
                fontSize = 22.sp,
                color = Neon.cyan,
                modifier = Modifier
                    .clickable { onCompare() }
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            )
            Text(
                if (isFav) "★" else "☆",
                fontSize = 26.sp,
                color = if (isFav) Neon.gold else MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .clickable { onToggleFav() }
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            )
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
        )
    }
}
