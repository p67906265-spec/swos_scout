package it.paolo.swosscout

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    vm: MainViewModel,
    onBack: () -> Unit,
    onOpenTeam: () -> Unit,
    onOpenCompare: () -> Unit
) {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) vm.importCsv(uri)
    }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(stringResource(R.string.menu_title), onBack)

        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)
        ) {
            MenuItem(
                title = stringResource(R.string.menu_team),
                subtitle = if (vm.myTeamKey == null) stringResource(R.string.menu_team_none) else vm.teamLabel(),
                onClick = onOpenTeam
            )
            MenuItem(
                title = stringResource(R.string.menu_compare),
                subtitle = stringResource(R.string.menu_compare_sub),
                onClick = onOpenCompare
            )
            MenuItem(
                title = stringResource(R.string.menu_csv),
                subtitle = stringResource(
                    R.string.csv_source,
                    stringResource(if (vm.usingCustomCsv) R.string.csv_custom else R.string.csv_builtin),
                    vm.totalPlayers
                ),
                onClick = { launcher.launch(arrayOf("*/*")) }
            )
            Text(
                stringResource(R.string.csv_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            if (vm.usingCustomCsv) {
                TextButton(onClick = { vm.resetCsv() }) {
                    Text(stringResource(R.string.csv_reset))
                }
            }

            SectionTitle(stringResource(R.string.menu_language))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Lang.values().forEach { l ->
                    FilterChip(
                        selected = vm.lang == l,
                        onClick = { vm.chooseLang(l) },
                        label = { Text(l.label) }
                    )
                }
            }

            SectionTitle(stringResource(R.string.menu_theme))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = vm.dark,
                    onClick = { vm.chooseDark(true) },
                    label = { Text(stringResource(R.string.theme_dark)) }
                )
                FilterChip(
                    selected = !vm.dark,
                    onClick = { vm.chooseDark(false) },
                    label = { Text(stringResource(R.string.theme_light)) }
                )
            }
            Spacer(Modifier.height(24.dp))
        }

        Text(
            SIGNATURE,
            color = Neon.cyan,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            letterSpacing = 2.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        )
    }

    val st = vm.importStatus
    if (st != null) {
        AlertDialog(
            onDismissRequest = { vm.clearImportStatus() },
            confirmButton = {
                TextButton(onClick = { vm.clearImportStatus() }) { Text(stringResource(R.string.close)) }
            },
            title = { Text(stringResource(R.string.menu_csv)) },
            text = {
                Text(
                    if (st.ok) stringResource(R.string.csv_ok, st.count)
                    else stringResource(R.string.csv_bad)
                )
            }
        )
    }
}

@Composable
fun MenuItem(title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp).clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Neon.cyan.copy(alpha = 0.35f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
