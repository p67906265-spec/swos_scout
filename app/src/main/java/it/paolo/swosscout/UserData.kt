package it.paolo.swosscout

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Preferiti, valori modificati e impostazioni dell'utente, salvati sul telefono. */
class UserData(context: Context) {
    private val prefs = context.getSharedPreferences("swos_user", Context.MODE_PRIVATE)

    fun loadFavorites(): Set<String> {
        val raw = prefs.getString("fav", null) ?: return emptySet()
        return try {
            val a = JSONArray(raw)
            (0 until a.length()).map { a.getString(it) }.toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    fun saveFavorites(set: Set<String>) {
        prefs.edit().putString("fav", JSONArray(set.toList()).toString()).apply()
    }

    fun loadOverrides(): Map<String, Int> {
        val raw = prefs.getString("val", null) ?: return emptyMap()
        return try {
            val o = JSONObject(raw)
            val out = HashMap<String, Int>()
            val keys = o.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                out[k] = o.getInt(k)
            }
            out
        } catch (e: Exception) {
            emptyMap()
        }
    }

    fun saveOverrides(map: Map<String, Int>) {
        val o = JSONObject()
        for ((k, v) in map) o.put(k, v)
        prefs.edit().putString("val", o.toString()).apply()
    }

    fun loadLang(): String = prefs.getString("lang", "it") ?: "it"

    fun saveLang(code: String) {
        prefs.edit().putString("lang", code).apply()
    }

    fun loadDark(): Boolean = prefs.getBoolean("dark", true)

    fun saveDark(dark: Boolean) {
        prefs.edit().putBoolean("dark", dark).apply()
    }

    fun loadTeam(): String? = prefs.getString("team", null)

    fun saveTeam(key: String?) {
        if (key == null) prefs.edit().remove("team").apply()
        else prefs.edit().putString("team", key).apply()
    }
}
