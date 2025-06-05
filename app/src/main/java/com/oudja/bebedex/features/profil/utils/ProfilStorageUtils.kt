package com.oudja.bebedex.features.profil.utils

import android.content.Context

object ProfilStorageUtils {

    fun saveLastSeenLevel(context: Context, level: Int) {
        val prefs = context.getSharedPreferences("bebedex_prefs", Context.MODE_PRIVATE)
        prefs.edit().putInt("last_seen_level", level).apply()
    }

    fun loadLastSeenLevel(context: Context): Int {
        val prefs = context.getSharedPreferences("bebedex_prefs", Context.MODE_PRIVATE)
        return prefs.getInt("last_seen_level", 1)
    }
}
