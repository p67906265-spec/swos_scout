package it.paolo.swosscout

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private fun signedInt(d: Int): String = if (d > 0) "+$d" else if (d < 0) "$d" else "="

private fun signedValue(d: Int): String =
    if (d > 0) "+${formatValue(d)}" else if (d < 0) "−${formatValue(-d)}" else "="

private fun signed1(d: Double): String =
    if (d > 0.049) "+" + fmt1(d) else if (d < -0.049) "−" + fmt1(-d) else "="

@Composable
fun TeamCompareScreen(vm: MainViewModel, onBack: () -> Unit, onOpenTeam: () -> Unit) {
    var pickClub by remember { mutableStateOf(false) }
    val noTeam = vm.myTeamKey == null
    val hasOther = vm.otherTeamKey != null
    val a = remember(vm.myPlayers, vm.overrides) { teamStats(vm.myPlayers) { vm.valueOf(it) } }
    val b = remember(vm.otherPlayers, vm.overrides) { teamStats(vm.otherPlayers) { vm.valueOf(it) } }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(stringResource(R.string.tcmp_title), onBack)

        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)
        ) {
            if (noTeam) {
                Text(
                    stringResource(R.string.tcmp_need_team),
                    style = MaterialTheme.typography.bodyLarge
                )
                TextButton(onClick = onOpenTeam) { Text(stringResource(R.string.compare_go_team)) }
            } else {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TeamSlot(
                        title = stringResource(R.string.menu_team),
                        color = Neon.cyan,
                        label = vm.teamLabel(),
                        stats = a,
                        buttonText = null,
                        modifier = Modifier.weight(1f),
                        onPick = {}
                    )
                    TeamSlot(
                        title = stringResource(R.string.tcmp_other),
                        color = Neon.pink,
                        label = if (hasOther) vm.otherLabel() else "—",
                        stats = if (hasOther) b else null,
                        buttonText = stringResource(
                            if (hasOther) R.string.compare_change else R.string.compare_pick
                        ),
                        modifier = Modifier.weight(1f),
                        onPick = { pickClub = true }
                    )
                }
                if (!hasOther) {
                    Spacer(Modifier.height(24.dp))
                    Text(
                        stringResource(R.string.tcmp_empty),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    TeamCompareBody(a, b)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (pickClub) {
        ClubPickerDialog(
            clubs = vm.clubs,
            showClear = hasOther,
            onPick = { key -> vm.chooseOtherTeam(key) },
            onDismiss = { pickClub = false }
        )
    }
}

@Composable
fun TeamSlot(
    title: String,
    color: Color,
    label: String,
    stats: TeamStats?,
    buttonText: String?,
    modifier: Modifier,
    onPick: () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, color.copy(alpha = 0.7f))
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                title.uppercase(),
                color = color,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(label, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            if (stats != null) {
                Text(
                    "${stats.count} · ${formatValue(stats.totalValue)}",
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodySmall,
                    color = Neon.green
                )
            }
            if (buttonText != null) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onPick,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) { Text(buttonText) }
            }
        }
    }
}

@Composable
fun TeamCompareBody(a: TeamStats, b: TeamStats) {
    val names = SKILL_NAME_RES.map { stringResource(it) }
    val neutral = MaterialTheme.colorScheme.onSurfaceVariant
    val dAvg = b.totalAvg - a.totalAvg

    Column(Modifier.fillMaxWidth()) {
        SectionTitle(stringResource(R.string.compare_summary))
        SummaryRow(
            label = stringResource(R.string.tcmp_players),
            left = "${a.count}",
            right = "${b.count}",
            delta = signedInt(b.count - a.count),
            deltaColor = neutral
        )
        SummaryRow(
            label = stringResource(R.string.tcmp_total_value),
            left = formatValue(a.totalValue),
            right = formatValue(b.totalValue),
            delta = signedValue(b.totalValue - a.totalValue),
            deltaColor = neutral
        )
        SummaryRow(
            label = stringResource(R.string.tcmp_avg_total),
            left = fmt1(a.totalAvg),
            right = fmt1(b.totalAvg),
            delta = signed1(dAvg),
            deltaColor = deltaColor(Math.round(dAvg * 10).toInt(), true)
        )

        SectionTitle(stringResource(R.string.tcmp_skill_avg))
        SKILL_CODES.forEachIndexed { i, c ->
            AvgCompareRow(c, names[i], a.skillAvg[i], b.skillAvg[i])
        }

        SectionTitle(stringResource(R.string.tcmp_roles))
        POSITIONS.forEach { pos ->
            val na = a.roleCount[pos] ?: 0
            val nb = b.roleCount[pos] ?: 0
            if (na > 0 || nb > 0) {
                val avA = a.roleAvg[pos] ?: 0.0
                val avB = b.roleAvg[pos] ?: 0.0
                val isG = pos == "G"
                val comparable = !isG && na > 0 && nb > 0
                val d = avB - avA
                SummaryRow(
                    label = pos,
                    left = if (isG || na == 0) "$na" else "$na · ${fmt1(avA)}",
                    right = if (isG || nb == 0) "$nb" else "$nb · ${fmt1(avB)}",
                    delta = if (comparable) signed1(d) else "",
                    deltaColor = if (comparable) deltaColor(Math.round(d * 10).toInt(), true) else neutral
                )
            }
        }
    }
}

@Composable
fun AvgCompareRow(code: Char, name: String, a: Double, b: Double) {
    val d = b - a
    val fa = (a / 7.0).toFloat().coerceIn(0f, 1f)
    val fb = (b / 7.0).toFloat().coerceIn(0f, 1f)
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(Modifier.fillMaxWidth()) {
            Text("$code · $name", Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            Text(
                signed1(d),
                color = deltaColor(Math.round(d * 10).toInt(), true),
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                fmt1(a),
                Modifier.width(34.dp),
                textAlign = TextAlign.End,
                fontFamily = FontFamily.Monospace,
                color = Neon.cyan
            )
            Spacer(Modifier.width(6.dp))
            Row(Modifier.weight(1f).height(12.dp)) {
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(3.dp)),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    if (fa > 0f) {
                        Box(
                            Modifier
                                .fillMaxWidth(fa)
                                .fillMaxHeight()
                                .background(Neon.cyan, RoundedCornerShape(3.dp))
                        )
                    }
                }
                Spacer(Modifier.width(3.dp))
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(3.dp)),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (fb > 0f) {
                        Box(
                            Modifier
                                .fillMaxWidth(fb)
                                .fillMaxHeight()
                                .background(Neon.pink, RoundedCornerShape(3.dp))
                        )
                    }
                }
            }
            Spacer(Modifier.width(6.dp))
            Text(
                fmt1(b),
                Modifier.width(34.dp),
                fontFamily = FontFamily.Monospace,
                color = Neon.pink
            )
        }
    }
}
