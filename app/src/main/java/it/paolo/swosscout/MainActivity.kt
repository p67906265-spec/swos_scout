package it.paolo.swosscout

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.Locale
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AppEntry() }
    }
}

/**
 * Contesto con le risorse nella lingua scelta. Deve restare un ContextWrapper dell'activity,
 * altrimenti Compose non ritrova l'activity (selettore file, ecc.) e l'app va in crash.
 */
private class LocalizedContext(base: Context, private val res: Resources) : ContextWrapper(base) {
    override fun getResources(): Resources = res
}

private fun localizedContext(base: Context, lang: Lang): Context {
    val cfg = Configuration(base.resources.configuration)
    cfg.setLocale(Locale.forLanguageTag(lang.code))
    return LocalizedContext(base, base.createConfigurationContext(cfg).resources)
}

@Composable
fun AppEntry(vm: MainViewModel = viewModel()) {
    val base = LocalContext.current
    val localized = remember(vm.lang, base) { localizedContext(base, vm.lang) }
    CompositionLocalProvider(
        LocalContext provides localized,
        LocalConfiguration provides localized.resources.configuration
    ) {
        SwosTheme(dark = vm.dark) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onBackground
            ) {
                AppRoot(vm)
            }
        }
    }
}

enum class Screen { LIST, FILTERS, MENU, TEAM, COMPARE }

@Composable
fun AppRoot(vm: MainViewModel) {
    var screen by rememberSaveable { mutableStateOf(Screen.LIST) }
    var compareBack by rememberSaveable { mutableStateOf(Screen.MENU) }
    var splashDone by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(2500)
        splashDone = true
    }
    BackHandler(enabled = screen != Screen.LIST) {
        screen = when (screen) {
            Screen.TEAM -> Screen.MENU
            Screen.COMPARE -> compareBack
            else -> Screen.LIST
        }
    }

    val showSplash = !splashDone || vm.loading

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Neon.bgTop, Neon.bgBottom)))
    ) {
        Crossfade(targetState = showSplash, animationSpec = tween(600), label = "root") { splash ->
            if (splash) {
                SplashScreen()
            } else if (vm.error != null) {
                Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.error_prefix, vm.error ?: ""))
                }
            } else {
                when (screen) {
                    Screen.LIST -> ListScreen(
                        vm = vm,
                        onOpenFilters = { screen = Screen.FILTERS },
                        onOpenMenu = { screen = Screen.MENU },
                        onCompare = {
                            compareBack = Screen.LIST
                            screen = Screen.COMPARE
                        }
                    )
                    Screen.FILTERS -> FilterScreen(vm) { screen = Screen.LIST }
                    Screen.MENU -> MenuScreen(
                        vm = vm,
                        onBack = { screen = Screen.LIST },
                        onOpenTeam = { screen = Screen.TEAM },
                        onOpenCompare = {
                            compareBack = Screen.MENU
                            screen = Screen.COMPARE
                        }
                    )
                    Screen.TEAM -> TeamScreen(
                        vm = vm,
                        onBack = { screen = Screen.MENU },
                        onCompare = {
                            compareBack = Screen.TEAM
                            screen = Screen.COMPARE
                        }
                    )
                    Screen.COMPARE -> CompareScreen(
                        vm = vm,
                        onBack = { screen = compareBack },
                        onOpenTeam = { screen = Screen.TEAM }
                    )
                }
            }
        }
    }
}
