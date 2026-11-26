package it.paolo.swosscout

import java.io.InputStream
import java.util.Locale

/** Abilità SWOS nell'ordine delle colonne del CSV: P V H T C S F. */
val SKILL_CODES = listOf('P', 'V', 'H', 'T', 'C', 'S', 'F')
val SKILL_NAMES = listOf(
    "Passaggio", "Potenza", "Testa", "Contrasto", "Controllo", "Velocità", "Finalizzazione"
)

/** Ruoli nell'ordine in cui compaiono tra i filtri. */
val POSITIONS = listOf("G", "RB", "D", "LB", "RW", "M", "LW", "A")

class Player(
    val country: String,
    val club: String,
    val number: Int,
    val name: String,
    val position: String,
    val skills: IntArray,
    val keySkills: String,
    val price: Int,
    val valueK: Int,
    val nationality: String
) {
    val total: Int = skills.sum()
    val nameLower: String = name.lowercase()
    val clubLower: String = club.lowercase()

    /** Chiave stabile del giocatore, usata per preferiti e valori modificati. */
    val id: String = "$country|$club|$number|$name"
}

enum class SortField(val label: String) {
    NAME("Nome"),
    VALUE("Valore"),
    TOTAL("Totale abilità"),
    P("Passaggio"),
    V("Potenza"),
    H("Testa"),
    T("Contrasto"),
    C("Controllo"),
    S("Velocità"),
    F("Finalizzazione")
}

data class Filters(
    val query: String = "",
    val club: String = "",
    val country: String? = null,
    val nationality: String? = null,
    val positions: Set<String> = emptySet(),
    val keySkills: Set<Char> = emptySet(),
    val skillRanges: List<IntRange> = List(7) { 0..7 },
    val valueMin: String = "",
    val valueMax: String = "",
    val sortBy: SortField = SortField.VALUE,
    val sortDesc: Boolean = true,
    val onlyFavorites: Boolean = false
) {
    /** Numero di filtri attivi (la ricerca per nome e l'ordinamento non contano). */
    fun activeCount(): Int = listOf(
        club.isNotBlank(),
        country != null,
        nationality != null,
        positions.isNotEmpty(),
        keySkills.isNotEmpty(),
        skillRanges.any { it != 0..7 },
        valueMin.isNotBlank() || valueMax.isNotBlank()
    ).count { it }
}

private fun parseLine(line: String): List<String> {
    if (!line.contains('"')) return line.split(',')
    val res = ArrayList<String>()
    val sb = StringBuilder()
    var inQuotes = false
    var i = 0
    while (i < line.length) {
        val ch = line[i]
        if (ch == '"') {
            if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                sb.append('"')
                i++
            } else {
                inQuotes = !inQuotes
            }
        } else if (ch == ',' && !inQuotes) {
            res.add(sb.toString())
            sb.setLength(0)
        } else {
            sb.append(ch)
        }
        i++
    }
    res.add(sb.toString())
    return res
}

/** Legge il CSV (con intestazione) cercando le colonne per nome. */
fun parseCsv(input: InputStream): List<Player> {
    val out = ArrayList<Player>(60000)
    input.bufferedReader(Charsets.UTF_8).useLines { lines ->
        var idx: Map<String, Int>? = null
        for (raw in lines) {
            if (raw.isBlank()) continue
            val cols = parseLine(raw)
            val map = idx
            if (map == null) {
                idx = cols.mapIndexed { i, h -> h.trim().removePrefix("\uFEFF").lowercase() to i }.toMap()
                continue
            }
            fun col(name: String): String {
                val i = map[name] ?: return ""
                return if (i < cols.size) cols[i].trim() else ""
            }
            fun num(name: String): Int = col(name).toIntOrNull() ?: 0
            val name = col("player")
            if (name.isEmpty()) continue
            out.add(
                Player(
                    country = col("country"),
                    club = col("club"),
                    number = num("number"),
                    name = name,
                    position = col("position"),
                    skills = intArrayOf(num("p"), num("v"), num("h"), num("t"), num("c"), num("s"), num("f")),
                    keySkills = col("key_skills"),
                    price = num("price"),
                    valueK = num("value_k"),
                    nationality = col("nationality")
                )
            )
        }
    }
    return out
}

fun applyFilters(
    all: List<Player>,
    f: Filters,
    favorites: Set<String> = emptySet(),
    overrides: Map<String, Int> = emptyMap()
): List<Player> {
    val q = f.query.trim().lowercase()
    val club = f.club.trim().lowercase()
    val vMin = f.valueMin.trim().toIntOrNull()
    val vMax = f.valueMax.trim().toIntOrNull()

    val filtered = all.filter { pl ->
        val value = overrides[pl.id] ?: pl.valueK
        (!f.onlyFavorites || pl.id in favorites) &&
            (q.isEmpty() || pl.nameLower.contains(q)) &&
            (club.isEmpty() || pl.clubLower.contains(club)) &&
            (f.country == null || pl.country == f.country) &&
            (f.nationality == null || pl.nationality == f.nationality) &&
            (f.positions.isEmpty() || pl.position in f.positions) &&
            f.keySkills.all { it in pl.keySkills } &&
            (0 until 7).all { i -> pl.skills[i] in f.skillRanges[i] } &&
            (vMin == null || value >= vMin) &&
            (vMax == null || value <= vMax)
    }

    val cmp: Comparator<Player> = when (f.sortBy) {
        SortField.NAME -> compareBy<Player> { it.nameLower }
        SortField.VALUE -> compareBy<Player> { overrides[it.id] ?: it.valueK }.thenBy { it.nameLower }
        SortField.TOTAL -> compareBy<Player> { it.total }.thenBy { it.nameLower }
        SortField.P -> compareBy<Player> { it.skills[0] }.thenBy { it.nameLower }
        SortField.V -> compareBy<Player> { it.skills[1] }.thenBy { it.nameLower }
        SortField.H -> compareBy<Player> { it.skills[2] }.thenBy { it.nameLower }
        SortField.T -> compareBy<Player> { it.skills[3] }.thenBy { it.nameLower }
        SortField.C -> compareBy<Player> { it.skills[4] }.thenBy { it.nameLower }
        SortField.S -> compareBy<Player> { it.skills[5] }.thenBy { it.nameLower }
        SortField.F -> compareBy<Player> { it.skills[6] }.thenBy { it.nameLower }
    }
    val sorted = filtered.sortedWith(cmp)
    return if (f.sortDesc) sorted.asReversed() else sorted
}

fun formatValue(k: Int): String =
    if (k >= 1000) {
        val txt = if (k % 1000 == 0) "${k / 1000}" else String.format(Locale.ITALY, "%.1f", k / 1000.0)
        "$txt M"
    } else {
        "$k K"
    }
