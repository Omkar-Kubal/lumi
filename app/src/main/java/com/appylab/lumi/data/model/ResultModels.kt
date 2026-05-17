package com.appylab.lumi.data.model

enum class GlowUpPotential { LOW, MEDIUM, HIGH }

sealed class ResultError {
    object NotFound : ResultError()
    object LoadFailed : ResultError()
}

fun glowUpPotentialFrom(score: Int): GlowUpPotential = when {
    score >= 75 -> GlowUpPotential.HIGH
    score >= 50 -> GlowUpPotential.MEDIUM
    else        -> GlowUpPotential.LOW
}

fun verdictLabelFrom(score: Int): String = when {
    score >= 90 -> "Stunning!"
    score >= 75 -> "Great!"
    score >= 60 -> "Good!"
    score >= 40 -> "Building!"
    else        -> "Potential!"
}

fun verdictBodyFrom(score: Int): String = when {
    score >= 90 -> "You have exceptional natural features."
    score >= 75 -> "You have amazing features and great glow-up potential."
    score >= 60 -> "Solid foundation with great room to enhance."
    score >= 40 -> "Your glow-up journey is just getting started."
    else        -> "Everyone starts somewhere — let's work on your glow."
}
