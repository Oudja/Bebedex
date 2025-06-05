package com.oudja.bebedex.features.profil.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.oudja.bebedex.R
import com.oudja.bebedex.features.profil.data.Competence
import java.io.BufferedReader
import java.io.InputStreamReader
import androidx.compose.runtime.snapshots.SnapshotStateList

object CompetenceStorageUtils {

    fun saveCompetences(context: Context, competences: List<Competence>) {
        val prefs = context.getSharedPreferences("bebedex_prefs", Context.MODE_PRIVATE)
        val json = Gson().toJson(competences)
        prefs.edit().putString("competences", json).apply()
    }

    fun loadSavedCompetences(context: Context): List<Competence> {
        val prefs = context.getSharedPreferences("bebedex_prefs", Context.MODE_PRIVATE)
        val json = prefs.getString("competences", null)
        return if (json != null) {
            val type = object : TypeToken<List<Competence>>() {}.type
            Gson().fromJson(json, type)
        } else emptyList()
    }

    fun loadCompetencesFromCSV(context: Context): List<Competence> {
        val competences = mutableListOf<Competence>()
        val inputStream = context.resources.openRawResource(R.raw.competences)
        val reader = BufferedReader(InputStreamReader(inputStream))

        reader.useLines { lines ->
            lines.forEach { line ->
                val parts = line.split(";")
                if (parts.size >= 1) {
                    val nom = parts[0].trim()
                    if (nom.isNotEmpty()) {
                        competences.add(Competence(nom = nom))
                    }
                }
            }
        }

        return competences
    }

    fun resetProgress(context: Context, competences: SnapshotStateList<Competence>) {
        competences.forEachIndexed { index, competence ->
            competences[index] = competence.copy(acquise = false)
        }
        saveCompetences(context, competences)
        saveProgress(context, 1, 0)
    }

    fun saveProgress(context: Context, level: Int, xp: Int) {
        val prefs = context.getSharedPreferences("bebedex_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putInt("level", level)
            putInt("experience", xp)
            apply()
        }
    }

    fun loadProgress(context: Context): Pair<Int, Int> {
        val prefs = context.getSharedPreferences("bebedex_prefs", Context.MODE_PRIVATE)
        val level = prefs.getInt("level", 1)
        val xp = prefs.getInt("experience", 0)
        return level to xp
    }
}
