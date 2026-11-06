package com.pspshell.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns

data class Game(val uri: String, val title: String)

/** Stores the user's game list (SAF document URIs) in SharedPreferences. */
object GameStore {
    private const val PREFS = "games"
    private const val KEY = "list"
    private const val SEP = "\u0001"

    fun load(ctx: Context): List<Game> =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getStringSet(KEY, emptySet()).orEmpty()
            .mapNotNull { s ->
                val p = s.split(SEP)
                if (p.size == 2) Game(p[0], p[1]) else null
            }
            .sortedBy { it.title.lowercase() }

    fun add(ctx: Context, uri: Uri): List<Game> {
        runCatching {
            ctx.contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
        val title = displayName(ctx, uri).substringBeforeLast('.')
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val set = prefs.getStringSet(KEY, emptySet()).orEmpty().toMutableSet()
        set.removeAll { it.startsWith(uri.toString() + SEP) }
        set.add(uri.toString() + SEP + title)
        prefs.edit().putStringSet(KEY, set).apply()
        return load(ctx)
    }

    fun remove(ctx: Context, game: Game): List<Game> {
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val set = prefs.getStringSet(KEY, emptySet()).orEmpty().toMutableSet()
        set.removeAll { it.startsWith(game.uri + SEP) }
        prefs.edit().putStringSet(KEY, set).apply()
        return load(ctx)
    }

    private fun displayName(ctx: Context, uri: Uri): String {
        ctx.contentResolver.query(uri, null, null, null, null)?.use { c ->
            val i = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (i >= 0 && c.moveToFirst()) return c.getString(i)
        }
        return uri.lastPathSegment ?: "Game"
    }
}

private val PPSSPP_PACKAGES = listOf("org.ppsspp.ppsspp", "org.ppsspp.ppssppgold")

/** Starts the game in PPSSPP. Returns false if PPSSPP is not installed. */
fun launchInPpsspp(ctx: Context, game: Game): Boolean {
    for (pkg in PPSSPP_PACKAGES) {
        val intent = Intent(Intent.ACTION_VIEW)
            .setClassName(pkg, "org.ppsspp.ppsspp.PpssppActivity")
            .setDataAndType(Uri.parse(game.uri), "application/octet-stream")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            ctx.startActivity(intent)
            return true
        } catch (_: Exception) {
        }
    }
    return false
}

/** Opens the PPSSPP store page (Play Store, falling back to the website). */
fun openPpssppDownload(ctx: Context) {
    val market = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=org.ppsspp.ppsspp"))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        ctx.startActivity(market)
    } catch (_: Exception) {
        ctx.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse("https://www.ppsspp.org/download"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}
