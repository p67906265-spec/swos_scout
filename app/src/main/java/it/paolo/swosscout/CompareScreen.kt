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

@Composable
fun CompareScreen(vm: MainViewModel, onBack: () -> Unit, onOpenTeam: () -> Unit) {
    var pickMine by remember { mutableStateOf(false) }
    var pickScout by remember { mutableStateOf(false) }
    val mine = vm.compareMine
    val scout = vm.compareScout
    val noTeam = vm.myTeamKey == null

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(stringResource(R.string.compare_title), onBack)

        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SlotCard(
                    title = stringResource(R.string.compare_mine),
                    color = Neon.cyan,
                    p = mine,
                    vm = vm,
                    enabled = !noTeam,
                    modifier = Modifier.weight(1f),
                    onPick = { pickMine = true }
                )
                SlotCard(
                    title = stringResource(R.string.compare_scout),
                    color = Neon.pink,
                    p = scout,
                    vm = vm,
                    enabled = true,
                    modifier = Modifier.weight(1f),
                    onPick = { pickScout = true }
                )
            }
            if (noTeam) {
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.compare_need_team),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = onOpenTeam) { Text(stringResource(R.string.compare_go_team)) }
            }
            if (mine == null || scout == null) {
                Spacer(Modifier.height(24.dp))
                Text(
                    stringResource(R.string.compare_empty),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                CompareBody(mine, scout, vm)
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (pickMine) {
        PlayerPickerDialog(
            title = stringResource(R.string.picker_mine_title),
            players = vm.myPlayers,
            valueOf = { vm.valueOf(it) },
            onPick = { vm.pickMine(it) },
            onDismiss = { pickMine = false }
        )
    }
    if (pickScout) {
        PlayerPickerDialog(
            title = stringResource(R.string.picker_scout_title),
            players = vm.allPlayers,
            valueOf = { vm.valueOf(it) },
            onPick = { vm.pickScout(it) },
            onDismiss = { pickScout = false }
        )
    }
}

@Composable
fun SlotCard(
    title: String,
    color: Color,
    p: Player?,
    vm: MainViewModel,
    enabled: Boolean,
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
            if (p == null) {
                Text("—", fontSize = 20.sp)
            } else {
                Text(p.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Text(
                    "${p.club} · ${p.position}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    formatValue(vm.valueOf(p)),
                    fontFamily = FontFamily.Monospace,
                    color = Neon.green
                )
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = onPick,
                enabled = enabled,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(if (p == null) R.string.compare_pick else R.string.compare_change))
            }
        }
    }
}

@Composable
fun CompareBody(a: Player, b: Player, vm: MainViewModel) {
    val names = SKILL_NAME_RES.map { stringResource(it) }
    val va = vm.valueOf(a)
    val vb = vm.valueOf(b)
    val dTotal = b.total - a.total
    val dValue = vb - va

    Column(Modifier.fillMaxWidth()) {
        SectionTitle(stringResource(R.string.compare_skills))
        SKILL_CODES.forEachIndexed { i, c ->
            SkillCompareRow(c, names[i], a.skills[i], b.skills[i])
        }

        SectionTitle(stringResource(R.string.compare_summary))
        SummaryRow(
            label = stringResource(R.string.sort_total),
            left = "${a.total}",
            right = "${b.total}",
            delta = if (dTotal > 0) "+$dTotal" else if (dTotal < 0) "$dTotal" else "=",
            deltaColor = deltaColor(dTotal, true)
        )
        SummaryRow(
            label = stringResource(R.string.sort_value),
            left = formatValue(va),
            right = formatValue(vb),
            delta = if (dValue > 0) "+${formatValue(dValue)}"
            else if (dValue < 0) "−${formatValue(-dValue)}"
            else "=",
            deltaColor = deltaColor(dValue, false)
        )
        SummaryRow(
            label = stringResource(R.string.compare_key),
            left = a.keySkills.ifEmpty { "—" },
            right = b.keySkills.ifEmpty { "—" },
            delta = "",
            deltaColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun deltaColor(d: Int, higherIsBetter: Boolean): Color = when {
    d == 0 -> MaterialTheme.colorScheme.onSurfaceVariant
    (d > 0) == higherIsBetter -> Neon.green
    else -> Neon.pink
}

@Composable
fun SkillCompareRow(code: Char, name: String, a: Int, b: Int) {
    val d = b - a
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(Modifier.fillMaxWidth()) {
            Text("$code · $name", Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            Text(
                if (d > 0) "+$d" else if (d < 0) "$d" else "=",
                color = deltaColor(d, true),
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "$a",
                Modifier.width(22.dp),
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
                    if (a > 0) {
                        Box(
                            Modifier
                                .fillMaxWidth(a / 7f)
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
                    if (b > 0) {
                        Box(
                            Modifier
                                .fillMaxWidth(b / 7f)
                                .fillMaxHeight()
                                .background(Neon.pink, RoundedCornerShape(3.dp))
                        )
                    }
                }
            }
            Spacer(Modifier.width(6.dp))
            Text(
                "$b",
                Modifier.width(22.dp),
                fontFamily = FontFamily.Monospace,
                color = Neon.pink
            )
        }
    }
}

@Composable
fun SummaryRow(label: String, left: String, right: String, delta: String, deltaColor: Color) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            left,
            Modifier.weight(1f),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = Neon.cyan
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (delta.isNotEmpty()) {
                Text(
                    delta,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = deltaColor
                )
            }
        }
        Text(
            right,
            Modifier.weight(1f),
            textAlign = TextAlign.End,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = Neon.pink
        )
    }
}
