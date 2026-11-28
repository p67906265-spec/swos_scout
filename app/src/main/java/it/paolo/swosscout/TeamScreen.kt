package it.paolo.swosscout

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun TeamScreen(
    vm: MainViewModel,
    onBack: () -> Unit,
    onCompare: () -> Unit,
    onCompareTeams: () -> Unit
) {
    var pickClub by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Player?>(null) }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(stringResource(R.string.team_title), onBack)

        if (vm.myTeamKey == null) {
            Column(
                Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(R.string.team_none_msg),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(16.dp))
                Button(onClick = { pickClub = true }, shape = RoundedCornerShape(14.dp)) {
                    Text(stringResource(R.string.team_choose), fontWeight = FontWeight.Bold)
                }
            }
        } else {
            val total = vm.myPlayers.sumOf { vm.valueOf(it) }
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 5.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Neon.cyan.copy(alpha = 0.5f))
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text(
                        vm.teamLabel(),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        stringResource(R.string.team_summary, vm.myPlayers.size, formatValue(total)),
                        color = Neon.green,
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = { pickClub = true }, shape = RoundedCornerShape(12.dp)) {
                        Text(stringResource(R.string.team_change))
                    }
                    Spacer(Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = onCompareTeams,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.tcmp_button))
                    }
                }
            }
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(vm.myPlayers) { p ->
                    PlayerCard(
                        p = p,
                        isFav = p.id in vm.favorites,
                        custom = vm.overrides[p.id],
                        onToggleFav = { vm.toggleFavorite(p) },
                        onEditValue = { editing = p },
                        onCompare = {
                            vm.pickMine(p)
                            onCompare()
                        }
                    )
                }
            }
        }
    }

    if (pickClub) {
        ClubPickerDialog(
            clubs = vm.clubs,
            showClear = vm.myTeamKey != null,
            onPick = { key -> vm.chooseTeam(key) },
            onDismiss = { pickClub = false }
        )
    }
    ValueEditor(vm, editing) { editing = null }
}
