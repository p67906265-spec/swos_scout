package it.paolo.swosscout

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val appContext = app.applicationContext

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
    var filters by mutableStateOf(Filters())
        private set
    var results by mutableStateOf<List<Player>>(emptyList())
        private set

    private var all: List<Player> = emptyList()
    private var job: Job? = null

    init {
        viewModelScope.launch {
            try {
                val list = withContext(Dispatchers.IO) {
                    appContext.assets.open("players.csv").use { parseCsv(it) }
                }
                val c = withContext(Dispatchers.Default) { list.map { it.country }.distinct().sorted() }
                val n = withContext(Dispatchers.Default) { list.map { it.nationality }.distinct().sorted() }
                all = list
                totalPlayers = list.size
                countries = c
                nationalities = n
                loading = false
                refilter(0)
            } catch (e: Exception) {
                error = e.message ?: "Errore nella lettura del CSV"
                loading = false
            }
        }
    }

    fun updateFilters(f: Filters) {
        filters = f
        refilter(120)
    }

    private fun refilter(delayMs: Long) {
        job?.cancel()
        job = viewModelScope.launch {
            if (delayMs > 0) delay(delayMs)
            val f = filters
            results = withContext(Dispatchers.Default) { applyFilters(all, f) }
        }
    }
}
