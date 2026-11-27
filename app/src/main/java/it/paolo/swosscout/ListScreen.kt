package it.paolo.swosscout

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    vm: MainViewModel,
    onOpenFilters: () -> Unit,
    onOpenMenu: () -> Unit,
    onCompare: () -> Unit
) {
    val f = vm.filters
    var sortDialog by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Player?>(null) }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 12.dp),
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
                color = Neon.cyan,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                letterSpacing = 3.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                "☰",
                fontSize = 28.sp,
                color = Neon.cyan,
                modifier = Modifier
                    .clickable { onOpenMenu() }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            )
        }
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = f.query,
                onValueChange = { vm.updateFilters(vm.filters.copy(query = it)) },
                label = { Text(stringResource(R.string.search_player)) },
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
            ) {
                Text(
                    if (n > 0) stringResource(R.string.filters_n, n) else stringResource(R.string.filters),
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = f.onlyFavorites,
                onClick = {
                    val cur = vm.filters
                    vm.updateFilters(cur.copy(onlyFavorites = !cur.onlyFavorites))
                },
                label = { Text(stringResource(R.string.only_fav, vm.favorites.size)) }
            )
        }
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "${vm.results.size} / ${vm.totalPlayers}",
                color = Neon.green,
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = { sortDialog = true }) {
                Text(stringResource(R.string.sort_label, stringResource(f.sortBy.labelRes)))
            }
            TextButton(onClick = {
                val cur = vm.filters
                vm.updateFilters(cur.copy(sortDesc = !cur.sortDesc))
            }) {
                Text(
                    if (f.sortDesc) stringResource(R.string.sort_desc) else stringResource(R.string.sort_asc),
                    fontWeight = FontWeight.Bold
                )
            }
        }
        if (vm.results.isEmpty()) {
            Text(
                stringResource(R.string.no_players),
                modifier = Modifier.padding(24.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(vm.results) { p ->
                PlayerCard(
                    p = p,
                    isFav = p.id in vm.favorites,
                    custom = vm.overrides[p.id],
                    onToggleFav = { vm.toggleFavorite(p) },
                    onEditValue = { editing = p },
                    onCompare = {
                        vm.pickScout(p)
                        onCompare()
                    }
                )
            }
        }
    }

    ValueEditor(vm, editing) { editing = null }

    if (sortDialog) {
        val fields = SortField.values()
        OptionDialog(
            title = stringResource(R.string.sort_title),
            options = fields.map { stringResource(it.labelRes) },
            onPick = { i ->
                val cur = vm.filters
                val field = fields[i]
                if (field != cur.sortBy) {
                    vm.updateFilters(cur.copy(sortBy = field, sortDesc = field != SortField.NAME))
                }
            },
            onDismiss = { sortDialog = false }
        )
    }
}
