package it.paolo.swosscout

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ScreenHeader(title: String, onBack: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "←",
            fontSize = 26.sp,
            color = Neon.cyan,
            modifier = Modifier.clickable { onBack() }.padding(horizontal = 12.dp, vertical = 4.dp)
        )
        Text(
            title,
            color = Neon.cyan,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            letterSpacing = 3.sp
        )
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text.uppercase(),
        color = Neon.cyan,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(top = 20.dp, bottom = 6.dp)
    )
}

@Composable
fun PlayerCard(
    p: Player,
    isFav: Boolean,
    custom: Int?,
    onToggleFav: () -> Unit,
    onEditValue: () -> Unit,
    onCompare: () -> Unit
) {
    val accent = posColor(p.position)
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 5.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            1.dp,
            Brush.horizontalGradient(listOf(accent.copy(alpha = 0.8f), accent.copy(alpha = 0.08f)))
        )
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        p.name,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "${p.club} · ${p.country}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "#${p.number} · ${p.nationality}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    "⇄",
                    fontSize = 24.sp,
                    color = Neon.cyan,
                    modifier = Modifier
                        .clickable { onCompare() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
                Text(
                    if (isFav) "★" else "☆",
                    fontSize = 28.sp,
                    color = if (isFav) Neon.gold else MaterialTheme.colorScheme.outline,
                    modifier = Modifier
                        .clickable { onToggleFav() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
                Box(
                    Modifier
                        .background(accent.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .border(1.dp, accent, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        p.position,
                        color = accent,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth()) {
                SKILL_CODES.forEachIndexed { i, c ->
                    SkillBar(c, p.skills[i], Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.tot_label, p.total),
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    if (p.keySkills.isEmpty()) "" else stringResource(R.string.key_label, p.keySkills),
                    color = Neon.pink,
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                Column(
                    Modifier
                        .weight(1f)
                        .clickable { onEditValue() }
                        .padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        formatValue(custom ?: p.valueK) + " ✎",
                        color = if (custom != null) Neon.pink else Neon.green,
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodySmall
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
            }
        }
    }
}

@Composable
fun SkillBar(code: Char, value: Int, modifier: Modifier) {
    val color = when {
        value >= 6 -> Neon.pink
        value >= 4 -> Neon.green
        value >= 1 -> Neon.cyan
        else -> MaterialTheme.colorScheme.outline
    }
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            code.toString(),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Box(
            Modifier
                .width(12.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (value > 0) {
                Box(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .fillMaxHeight(value / 7f)
                        .background(color)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            value.toString(),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = color
        )
    }
}

@Composable
fun ValueEditor(vm: MainViewModel, p: Player?, onDismiss: () -> Unit) {
    if (p != null) {
        ValueDialog(
            p = p,
            custom = vm.overrides[p.id],
            onSave = { v -> vm.setCustomValue(p, v) },
            onReset = { vm.setCustomValue(p, null) },
            onDismiss = onDismiss
        )
    }
}

@Composable
fun ValueDialog(
    p: Player,
    custom: Int?,
    onSave: (Int) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf((custom ?: p.valueK).toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(p.name) },
        text = {
            Column {
                Text(
                    stringResource(R.string.value_original, formatValue(p.valueK)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { s -> text = s.filter { it.isDigit() }.take(6) },
                    label = { Text(stringResource(R.string.value_in_k)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val v = text.toIntOrNull()
                if (v != null) {
                    onSave(v)
                    onDismiss()
                }
            }) { Text(stringResource(R.string.save)) }
        },
        dismissButton = {
            Row {
                if (custom != null) {
                    TextButton(onClick = {
                        onReset()
                        onDismiss()
                    }) { Text(stringResource(R.string.restore)) }
                }
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
            }
        }
    )
}

@Composable
fun OptionDialog(
    title: String,
    options: List<String>,
    onPick: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) } },
        title = { Text(title) },
        text = {
            LazyColumn(Modifier.heightIn(max = 420.dp)) {
                itemsIndexed(options) { i, o ->
                    TextButton(
                        onClick = {
                            onPick(i)
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(o, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
                    }
                }
            }
        }
    )
}

@Composable
fun PlayerPickerDialog(
    title: String,
    players: List<Player>,
    valueOf: (Player) -> Int,
    onPick: (Player) -> Unit,
    onDismiss: () -> Unit
) {
    var q by remember { mutableStateOf("") }
    val filtered = remember(q, players) {
        val s = q.trim().lowercase()
        if (s.isEmpty()) players.take(100) else players.filter { it.nameLower.contains(s) }.take(100)
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) } },
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = q,
                    onValueChange = { q = it },
                    label = { Text(stringResource(R.string.picker_search)) },
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                LazyColumn(Modifier.heightIn(max = 360.dp)) {
                    items(filtered) { p ->
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onPick(p)
                                    onDismiss()
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            Text(p.name, fontWeight = FontWeight.Bold)
                            Text(
                                "${p.club} · ${p.position} · ${formatValue(valueOf(p))}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun ClubPickerDialog(
    clubs: List<ClubEntry>,
    showClear: Boolean,
    onPick: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    var q by remember { mutableStateOf("") }
    val filtered = remember(q, clubs) {
        val s = q.trim().lowercase()
        if (s.isEmpty()) clubs else clubs.filter { it.label.lowercase().contains(s) }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) } },
        title = { Text(stringResource(R.string.team_choose)) },
        text = {
            Column {
                OutlinedTextField(
                    value = q,
                    onValueChange = { q = it },
                    label = { Text(stringResource(R.string.team_search)) },
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                LazyColumn(Modifier.heightIn(max = 360.dp)) {
                    if (showClear) {
                        item {
                            Text(
                                stringResource(R.string.team_clear),
                                color = Neon.pink,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onPick(null)
                                        onDismiss()
                                    }
                                    .padding(vertical = 10.dp)
                            )
                        }
                    }
                    items(filtered) { c ->
                        Text(
                            c.label,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onPick(c.key)
                                    onDismiss()
                                }
                                .padding(vertical = 10.dp)
                        )
                    }
                }
            }
        }
    )
}
