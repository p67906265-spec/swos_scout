package it.paolo.swosscout

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SwosTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onBackground
                ) {
                    AppRoot()
                }
            }
        }
    }
}

@Composable
fun AppRoot(vm: MainViewModel = viewModel()) {
    var showFilters by rememberSaveable { mutableStateOf(false) }
    var splashDone by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(2300)
        splashDone = true
    }
    BackHandler(enabled = showFilters) { showFilters = false }

    val showSplash = !splashDone || vm.loading

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgTop, BgBottom)))
    ) {
        Crossfade(targetState = showSplash, animationSpec = tween(600), label = "root") { splash ->
            if (splash) {
                SplashScreen()
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
    }
}

// ---------------------------------------------------------------- Splash

@Composable
fun SplashScreen() {
    val inf = rememberInfiniteTransition(label = "splash")
    val angle by inf.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(2600, easing = LinearEasing)),
        label = "angle"
    )
    val pulse by inf.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "pulse"
    )
    val logoScale = remember { Animatable(0.4f) }
    val logoAlpha = remember { Animatable(0f) }
    val titleAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) { logoAlpha.animateTo(1f, tween(600)) }
    LaunchedEffect(Unit) { logoScale.animateTo(1f, tween(900, easing = FastOutSlowInEasing)) }
    LaunchedEffect(Unit) {
        delay(500)
        titleAlpha.animateTo(1f, tween(800))
    }

    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier
                .size(240.dp)
                .graphicsLayer {
                    scaleX = logoScale.value
                    scaleY = logoScale.value
                    this.alpha = logoAlpha.value
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(Modifier.fillMaxSize().rotate(angle)) {
                val w = 5.dp.toPx()
                val arcSize = Size(size.width - 2 * w, size.height - 2 * w)
                drawCircle(
                    color = NeonCyan.copy(alpha = 0.12f),
                    style = Stroke(width = 2.dp.toPx())
                )
                drawArc(
                    color = NeonCyan,
                    startAngle = 0f,
                    sweepAngle = 110f,
                    useCenter = false,
                    topLeft = Offset(w, w),
                    size = arcSize,
                    style = Stroke(width = w, cap = StrokeCap.Round)
                )
                drawArc(
                    color = NeonPink,
                    startAngle = 180f,
                    sweepAngle = 60f,
                    useCenter = false,
                    topLeft = Offset(w, w),
                    size = arcSize,
                    style = Stroke(width = w, cap = StrokeCap.Round)
                )
            }
            Image(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier.size(170.dp)
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            "SWOS//SCOUT",
            color = NeonCyan,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            letterSpacing = 4.sp,
            modifier = Modifier.graphicsLayer { this.alpha = titleAlpha.value }
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "SCANNING PLAYER DATABASE",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            letterSpacing = 2.sp,
            modifier = Modifier.alpha(pulse)
        )
        Spacer(Modifier.height(28.dp))
        LinearProgressIndicator(
            modifier = Modifier.width(180.dp).clip(RoundedCornerShape(2.dp)),
            color = NeonCyan,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

// ---------------------------------------------------------------- Lista

@Composable
fun ListScreen(vm: MainViewModel, onOpenFilters: () -> Unit) {
    val f = vm.filters
    var sortDialog by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "SWOS//SCOUT",
                color = NeonCyan,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                letterSpacing = 3.sp
            )
        }
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = f.query,
                onValueChange = { vm.updateFilters(vm.filters.copy(query = it)) },
                label = { Text("Cerca giocatore") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            val n = f.activeCount()
            Button(
                onClick = onOpenFilters,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.height(56.dp)
            ) { Text(if (n > 0) "FILTRI ($n)" else "FILTRI", fontWeight = FontWeight.Bold) }
        }
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "${vm.results.size} / ${vm.totalPlayers}",
                color = NeonGreen,
                fontFamily = FontFamily.Monospace,
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
            Row(Modifier.fillMaxWidth()) {
                Text(
                    "TOT ${p.total}",
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    if (p.keySkills.isEmpty()) "" else "KEY ${p.keySkills}",
                    color = NeonPink,
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    formatValue(p.valueK),
                    color = NeonGreen,
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun SkillBar(code: Char, value: Int, modifier: Modifier) {
    val color = when {
        value >= 6 -> NeonPink
        value >= 4 -> NeonGreen
        value >= 1 -> NeonCyan
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

// ---------------------------------------------------------------- Filtri

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FilterScreen(vm: MainViewModel, onClose: () -> Unit) {
    val f = vm.filters

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "FILTRI",
                color = NeonCyan,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                letterSpacing = 3.sp,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = {
                val cur = vm.filters
                vm.updateFilters(Filters(query = cur.query, sortBy = cur.sortBy, sortDesc = cur.sortDesc))
            }) { Text("Azzera") }
        }

        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)
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
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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

            SectionTitle("Livello abilità (tocca Min e Max)")
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
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().padding(12.dp).height(52.dp)
        ) { Text("MOSTRA ${vm.results.size} GIOCATORI", fontWeight = FontWeight.Bold) }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text.uppercase(),
        color = NeonCyan,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(top = 20.dp, bottom = 6.dp)
    )
}

@Composable
fun SkillRange(label: String, range: IntRange, onChange: (IntRange) -> Unit) {
    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(Modifier.fillMaxWidth()) {
            Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            Text(
                if (range == 0..7) "tutti" else "${range.first}–${range.last}",
                fontWeight = FontWeight.Bold,
                color = NeonGreen,
                fontFamily = FontFamily.Monospace
            )
        }
        Spacer(Modifier.height(4.dp))
        LevelRow("Min", range.first) { v -> onChange(v..maxOf(v, range.last)) }
        Spacer(Modifier.height(4.dp))
        LevelRow("Max", range.last) { v -> onChange(minOf(v, range.first)..v) }
    }
}

@Composable
fun LevelRow(title: String, selected: Int, onPick: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, Modifier.width(36.dp), style = MaterialTheme.typography.bodySmall)
        for (v in 0..7) {
            val isSel = v == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 2.dp)
                    .height(38.dp)
                    .background(
                        if (isSel) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { onPick(v) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    v.toString(),
                    fontWeight = FontWeight.Bold,
                    color = if (isSel) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
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
    OutlinedButton(
        onClick = { open = true },
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
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
