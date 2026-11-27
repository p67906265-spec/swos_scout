package it.paolo.swosscout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FilterScreen(vm: MainViewModel, onClose: () -> Unit) {
    val f = vm.filters
    val skillNames = SKILL_NAME_RES.map { stringResource(it) }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.filters),
                color = Neon.cyan,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                letterSpacing = 3.sp,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = {
                val cur = vm.filters
                vm.updateFilters(
                    Filters(
                        query = cur.query,
                        sortBy = cur.sortBy,
                        sortDesc = cur.sortDesc,
                        onlyFavorites = cur.onlyFavorites
                    )
                )
            }) { Text(stringResource(R.string.reset)) }
        }

        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)
        ) {
            SectionTitle(stringResource(R.string.sec_role))
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
                stringResource(R.string.pos_legend),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            SectionTitle(stringResource(R.string.sec_key))
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
                        label = { Text("$c ${skillNames[i]}") }
                    )
                }
            }

            SectionTitle(stringResource(R.string.sec_levels))
            SKILL_CODES.forEachIndexed { i, c ->
                SkillRange("$c · ${skillNames[i]}", f.skillRanges[i]) { r ->
                    val cur = vm.filters
                    vm.updateFilters(
                        cur.copy(skillRanges = cur.skillRanges.toMutableList().also { it[i] = r })
                    )
                }
            }

            SectionTitle(stringResource(R.string.sec_value))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = f.valueMin,
                    onValueChange = { s ->
                        vm.updateFilters(vm.filters.copy(valueMin = s.filter { it.isDigit() }))
                    },
                    label = { Text(stringResource(R.string.level_min)) },
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
                    label = { Text(stringResource(R.string.level_max)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            SectionTitle(stringResource(R.string.sec_team))
            OutlinedTextField(
                value = f.club,
                onValueChange = { vm.updateFilters(vm.filters.copy(club = it)) },
                label = { Text(stringResource(R.string.team_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            PickerField(
                label = stringResource(R.string.club_country),
                value = f.country,
                options = vm.countries,
                allLabel = stringResource(R.string.all_m)
            ) { vm.updateFilters(vm.filters.copy(country = it)) }
            Spacer(Modifier.height(8.dp))
            PickerField(
                label = stringResource(R.string.nationality),
                value = f.nationality,
                options = vm.nationalities,
                allLabel = stringResource(R.string.all_f)
            ) { vm.updateFilters(vm.filters.copy(nationality = it)) }
            Spacer(Modifier.height(16.dp))
        }

        Button(
            onClick = onClose,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().padding(12.dp).height(52.dp)
        ) { Text(stringResource(R.string.show_n, vm.results.size), fontWeight = FontWeight.Bold) }
    }
}

@Composable
fun SkillRange(label: String, range: IntRange, onChange: (IntRange) -> Unit) {
    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(Modifier.fillMaxWidth()) {
            Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            Text(
                if (range == 0..7) stringResource(R.string.all_levels) else "${range.first}–${range.last}",
                fontWeight = FontWeight.Bold,
                color = Neon.green,
                fontFamily = FontFamily.Monospace
            )
        }
        Spacer(Modifier.height(4.dp))
        LevelRow(stringResource(R.string.level_min), range.first) { v -> onChange(v..maxOf(v, range.last)) }
        Spacer(Modifier.height(4.dp))
        LevelRow(stringResource(R.string.level_max), range.last) { v -> onChange(minOf(v, range.first)..v) }
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
