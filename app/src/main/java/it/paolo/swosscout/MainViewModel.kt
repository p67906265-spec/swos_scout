package it.paolo.swosscout

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import java.io.ByteArrayInputStream
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val appContext = app.applicationContext
    private val userData = UserData(appContext)

    // --- dati
    var loading by mutableStateOf(true)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var totalPlayers by mutableStateOf(0)
        private set
    var countries by mutableStateOf<List<String>>(emptyList())
        private set
    var nationalities by mutableStateOf<List<String>>(emptyList())
        private set
    var clubs by mutableStateOf<List<ClubEntry>>(emptyList())
        private set
    var usingCustomCsv by mutableStateOf(false)
        private set
    var importStatus by mutableStateOf<ImportStatus?>(null)
        private set

    // --- lista e filtri
    var filters by mutableStateOf(Filters())
        private set
    var results by mutableStateOf<List<Player>>(emptyList())
        private set
    var favorites by mutableStateOf<Set<String>>(emptySet())
        private set
    var overrides by mutableStateOf<Map<String, Int>>(emptyMap())
        private set

    // --- impostazioni
    var lang by mutableStateOf(Lang.fromCode(userData.loadLang()))
        private set
    var dark by mutableStateOf(userData.loadDark())
        private set

    // --- la mia squadra e confronto
    var myTeamKey by mutableStateOf<String?>(userData.loadTeam())
        private set
    var myPlayers by mutableStateOf<List<Player>>(emptyList())
        private set
    var compareMine by mutableStateOf<Player?>(null)
        private set
    var compareScout by mutableStateOf<Player?>(null)
        private set
    var otherTeamKey by mutableStateOf<String?>(null)
        private set
    var otherPlayers by mutableStateOf<List<Player>>(emptyList())
        private set

    private var all: List<Player> = emptyList()
    private var job: Job? = null

    val allPlayers: List<Player>
        get() = all

    init {
        favorites = userData.loadFavorites()
        overrides = userData.loadOverrides()
        loadData()
    }

    // ------------------------------------------------------------ caricamento dati

    private fun customFile() = File(appContext.filesDir, CUSTOM_FILE)

    private fun loadData() {
        viewModelScope.launch {
            try {
                val custom = customFile()
                var list: List<Player> = emptyList()
                var useCustom = false
                if (custom.exists()) {
                    list = withContext(Dispatchers.IO) {
                        try {
                            custom.inputStream().use { parseCsv(it) }
                        } catch (e: Exception) {
                            emptyList()
                        }
                    }
                    useCustom = list.isNotEmpty()
                    if (!useCustom) custom.delete()
                }
                if (!useCustom) {
                    list = withContext(Dispatchers.IO) {
                        appContext.assets.open("players.csv").use { parseCsv(it) }
                    }
                }
                applyData(list, useCustom)
            } catch (e: Exception) {
                error = e.message ?: "CSV error"
                loading = false
            }
        }
    }

    private suspend fun applyData(list: List<Player>, custom: Boolean) {
        val prepared = withContext(Dispatchers.Default) {
            Triple(
                list.map { it.country }.distinct().sorted(),
                list.map { it.nationality }.distinct().sorted(),
                list.map { ClubEntry("${it.country}|${it.club}", "${it.club} (${it.country})") }
                    .distinct()
                    .sortedBy { it.label }
            )
        }
        all = list
        totalPlayers = list.size
        countries = prepared.first
        nationalities = prepared.second
        clubs = prepared.third
        usingCustomCsv = custom
        loading = false
        recomputeTeam()
        compareMine = null
        compareScout = null
        refilter(0)
    }

    /** Carica un CSV scelto dall'utente; se valido lo salva e sostituisce il database. */
    fun importCsv(uri: Uri) {
        viewModelScope.launch {
            val list: List<Player>? = withContext(Dispatchers.IO) {
                try {
                    val bytes = appContext.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    if (bytes == null) {
                        null
                    } else {
                        val parsed = parseCsv(ByteArrayInputStream(bytes))
                        if (parsed.isEmpty()) {
                            null
                        } else {
                            customFile().writeBytes(bytes)
                            parsed
                        }
                    }
                } catch (e: Exception) {
                    null
                }
            }
            if (list == null) {
                importStatus = ImportStatus(false, 0)
            } else {
                applyData(list, true)
                importStatus = ImportStatus(true, list.size)
            }
        }
    }

    /** Torna al database integrato nell'app. */
    fun resetCsv() {
        customFile().delete()
        loadData()
    }

    fun clearImportStatus() {
        importStatus = null
    }

    // ------------------------------------------------------------ filtri

    fun updateFilters(f: Filters) {
        filters = f
        refilter(120)
    }

    fun toggleFavorite(p: Player) {
        favorites = if (p.id in favorites) favorites - p.id else favorites + p.id
        userData.saveFavorites(favorites)
        if (filters.onlyFavorites) refilter(0)
    }

    /** Imposta un valore personalizzato (in migliaia); null o uguale all'originale = ripristina. */
    fun setCustomValue(p: Player, k: Int?) {
        overrides = if (k == null || k == p.valueK) overrides - p.id else overrides + (p.id to k)
        userData.saveOverrides(overrides)
        refilter(0)
    }

    fun valueOf(p: Player): Int = overrides[p.id] ?: p.valueK

    private fun refilter(delayMs: Long) {
        job?.cancel()
        job = viewModelScope.launch {
            if (delayMs > 0) delay(delayMs)
            val f = filters
            val fav = favorites
            val ov = overrides
            results = withContext(Dispatchers.Default) { applyFilters(all, f, fav, ov) }
        }
    }

    // ------------------------------------------------------------ impostazioni

    fun chooseLang(l: Lang) {
        lang = l
        userData.saveLang(l.code)
    }

    fun chooseDark(value: Boolean) {
        dark = value
        userData.saveDark(value)
    }

    // ------------------------------------------------------------ squadra e confronto

    fun chooseTeam(key: String?) {
        val changed = key != myTeamKey
        myTeamKey = key
        userData.saveTeam(key)
        recomputeTeam()
        if (changed) compareMine = null
    }

    fun teamLabel(): String = clubs.firstOrNull { it.key == myTeamKey }?.label ?: ""

    private fun recomputeTeam() {
        myPlayers = playersOf(myTeamKey)
        otherPlayers = playersOf(otherTeamKey)
    }

    private fun playersOf(key: String?): List<Player> {
        if (key == null) return emptyList()
        val country = key.substringBefore('|')
        val club = key.substringAfter('|')
        return all
            .filter { it.country == country && it.club == club }
            .sortedWith(
                compareBy<Player> { POSITIONS.indexOf(it.position) }.thenByDescending { it.total }
            )
    }

    /** Seconda squadra per il confronto tra squadre. */
    fun chooseOtherTeam(key: String?) {
        otherTeamKey = key
        otherPlayers = playersOf(key)
    }

    fun otherLabel(): String = clubs.firstOrNull { it.key == otherTeamKey }?.label ?: ""

    fun pickMine(p: Player) {
        compareMine = p
    }

    fun pickScout(p: Player) {
        compareScout = p
    }

    companion object {
        private const val CUSTOM_FILE = "players_custom.csv"
    }
}
