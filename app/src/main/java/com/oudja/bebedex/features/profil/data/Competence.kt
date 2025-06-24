package com.oudja.bebedex.features.profil.data

data class Competence(
    val nom: String,
    val theme: String = "",
    val xp: Int = 10,
    var acquise: Boolean = false
)
