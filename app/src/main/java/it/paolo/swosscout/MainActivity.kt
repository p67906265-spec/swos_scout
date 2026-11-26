package it.paolo.swosscout

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SwosTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppRoot()
                }
            }
        }
    }
}

@Composable
fun SwosTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) {
        darkColorScheme(primary = Color(0xFF81C784), onPrimary = Color(0xFF003910))
    } else {
        lightColorScheme(primary = Color(0xFF2E7D32))
    }
    MaterialTheme(colorScheme = colors, content = content)
}

@Composable
fun AppRoot(vm: MainViewModel = viewModel()) {
    var showFilters by rememberSaveable { mutableStateOf(false) }
    BackHandler(enabled = showFilters) { showFilters = false }

    if (vm.loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (vm.error != null) {
        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Text("Errore: ${vm.error}")
        }
    } else if (showFilters) {
        FilterScreen(vm) { showFilters = false }
    } else {
        ListScreen(vm) { showFilters = true }
    }
}

// ---------------------------------------------------------------- Lista

@Composable
fun ListScreen(vm: MainViewModel, onOpenFilters: () -> Unit) {
    val f = vm.filters
    var sortDialog by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = f.query,
                onValueChange = { vm.updateFilters(vm.filters.copy(query = it)) },
                label = { Text("Cerca giocatore") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            val n = f.activeCount()
            Button(onClick = onOpenFilters) { Text(if (n > 0) "Filtri ($n)" else "Filtri") }
        }
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "${vm.results.size} di ${vm.totalPlayers}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = { sortDialog = true }) {
                Text("Ordina: ${f.sortBy.label} ${if (f.sortDesc) "↓" else "↑"}")
            }
        }
        if (vm.results.isEmpty()) {
            Text(
                "Nessun giocatore trovato.",
                modifier = Modifier.padding(24.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(vm.results) { p -> PlayerCard(p) }
        }
    }

    if (sortDialog) {
        val fields = SortField.values()
        OptionDialog(
            title = "Ordina per",
            options = fields.map { it.label },
            onPick = { i ->
                val cur = vm.filters
                val field = fields[i]
                vm.updateFilters(
                    if (field == cur.sortBy) cur.copy(sortDesc = !cur.sortDesc)
                    else cur.copy(sortBy = field, sortDesc = field != SortField.NAME)
                )
            },
            onDismiss = { sortDialog = false }
        )
    }
}

@Composable
fun PlayerCard(p: Player) {
    Card(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    p.name,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    p.position,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                "${p.club} (${p.country}) · #${p.number} · ${p.nationality}",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth()) {
                SKILL_CODES.forEachIndexed { i, c ->
                    SkillCell(c, p.skills[i], Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth()) {
                Text(
                    "Tot ${p.total}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    if (p.keySkills.isEmpty()) "" else "Chiave: ${p.keySkills}",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "Valore ${formatValue(p.valueK)}",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun SkillCell(code: Char, value: Int, modifier: Modifier) {
    val color = when {
        value >= 6 -> MaterialTheme.colorScheme.primary
        value == 0 -> MaterialTheme.colorScheme.outline
        else -> MaterialTheme.colorScheme.onSurface
    }
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            code.toString(),
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(value.toString(), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = color)
    }
}

// ---------------------------------------------------------------- Filtri

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FilterScreen(vm: MainViewModel, onClose: () -> Unit) {
    val f = vm.filters

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Filtri",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = {
                val cur = vm.filters
                vm.updateFilters(Filters(query = cur.query, sortBy = cur.sortBy, sortDesc = cur.sortDesc))
            }) { Text("Azzera") }
        }

        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 12.dp)
        ) {
            SectionTitle("Ruolo")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                POSITIONS.forEach { pos ->
                    FilterChip(
                        selected = pos in f.positions,
                        onClick = {
                            val cur = vm.filters
                            vm.updateFilters(
                                cur.copy(
                                    positions = if (pos in cur.positions) cur.positions - pos
                                    else cur.positions + pos
                                )
                            )
                        },
                        label = { Text(pos) }
                    )
                }
            }
            Text(
                "G portiere · D difensore · RB/LB terzini · M centrocampista · RW/LW ali · A attaccante",
                style = MaterialTheme.typography.bodySmall
            )

            SectionTitle("Abilità chiave (le deve avere tutte)")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SKILL_CODES.forEachIndexed { i, c ->
                    FilterChip(
                        selected = c in f.keySkills,
                        onClick = {
                            val cur = vm.filters
                            vm.updateFilters(
                                cur.copy(
                                    keySkills = if (c in cur.keySkills) cur.keySkills - c
                                    else cur.keySkills + c
                                )
                            )
                        },
                        label = { Text("$c ${SKILL_NAMES[i]}") }
                    )
                }
            }

            SectionTitle("Livello abilità (0–7)")
            SKILL_CODES.forEachIndexed { i, c ->
                SkillRange("$c · ${SKILL_NAMES[i]}", f.skillRanges[i]) { r ->
                    val cur = vm.filters
                    vm.updateFilters(
                        cur.copy(skillRanges = cur.skillRanges.toMutableList().also { it[i] = r })
                    )
                }
            }

            SectionTitle("Valore (in migliaia)")
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = f.valueMin,
                    onValueChange = { s ->
                        vm.updateFilters(vm.filters.copy(valueMin = s.filter { it.isDigit() }))
                    },
                    label = { Text("Min") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = f.valueMax,
                    onValueChange = { s ->
                        vm.updateFilters(vm.filters.copy(valueMax = s.filter { it.isDigit() }))
                    },
                    label = { Text("Max") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            SectionTitle("Squadra e provenienza")
            OutlinedTextField(
                value = f.club,
                onValueChange = { vm.updateFilters(vm.filters.copy(club = it)) },
                label = { Text("Nome squadra") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            PickerField("Paese del club", f.country, vm.countries, "Tutti") {
                vm.updateFilters(vm.filters.copy(country = it))
            }
            Spacer(Modifier.height(8.dp))
            PickerField("Nazionalità", f.nationality, vm.nationalities, "Tutte") {
                vm.updateFilters(vm.filters.copy(nationality = it))
            }
            Spacer(Modifier.height(16.dp))
        }

        Button(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) { Text("Mostra ${vm.results.size} giocatori") }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillRange(label: String, range: IntRange, onChange: (IntRange) -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth()) {
            Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            Text("${range.first}–${range.last}", fontWeight = FontWeight.Bold)
        }
        RangeSlider(
            value = range.first.toFloat()..range.last.toFloat(),
            onValueChange = { r -> onChange(r.start.roundToInt()..r.endInclusive.roundToInt()) },
            valueRange = 0f..7f,
            steps = 6
        )
    }
}

@Composable
fun PickerField(
    label: String,
    value: String?,
    options: List<String>,
    allLabel: String,
    onSelect: (String?) -> Unit
) {
    var open by remember { mutableStateOf(false) }
    OutlinedButton(onClick = { open = true }, modifier = Modifier.fillMaxWidth()) {
        Text("$label: ${value ?: allLabel}")
    }
    if (open) {
        OptionDialog(
            title = label,
            options = listOf(allLabel) + options,
            onPick = { i -> onSelect(if (i == 0) null else options[i - 1]) },
            onDismiss = { open = false }
        )
    }
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
        confirmButton = { TextButton(onClick = onDismiss) { Text("Chiudi") } },
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
