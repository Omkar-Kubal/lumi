package com.appylab.lumi.data.model

import org.json.JSONArray

// ── Enums ─────────────────────────────────────────────────────────────────────

enum class GlowUpImageStatus { PENDING, GENERATING, COMPLETE, FAILED }

enum class ImpactLevel { HIGH, MEDIUM, LOW }

// ── Domain models ─────────────────────────────────────────────────────────────

data class ImprovementArea(
    val area: String,
    val impact: ImpactLevel,
    val scorePotential: Int,
    val illustrationAsset: String
)

data class StepGuide(
    val area: String,
    val goal: String,
    val recommendations: List<String>
)

data class ScanScorePoint(
    val date: Long,
    val score: Int,
    val faceAnalysisId: Long
)

// ── Error ─────────────────────────────────────────────────────────────────────

sealed class GlowUpError {
    object NotFound      : GlowUpError()
    object AccessDenied  : GlowUpError()
}

// ── JSON helpers ──────────────────────────────────────────────────────────────

fun parseImprovementAreas(json: String): List<ImprovementArea> {
    if (json.isBlank() || json == "[]") return emptyList()
    return try {
        val arr = JSONArray(json)
        (0 until arr.length()).map { i ->
            val obj = arr.getJSONObject(i)
            ImprovementArea(
                area = obj.optString("area", ""),
                impact = runCatching {
                    ImpactLevel.valueOf(obj.optString("impact", "MEDIUM").uppercase())
                }.getOrDefault(ImpactLevel.MEDIUM),
                scorePotential = obj.optInt("score_potential", 0),
                illustrationAsset = obj.optString("illustration", "default")
            )
        }
    } catch (_: Exception) { emptyList() }
}

fun parseStepGuides(json: String): List<StepGuide> {
    if (json.isBlank() || json == "[]") return emptyList()
    return try {
        val arr = JSONArray(json)
        (0 until arr.length()).map { i ->
            val obj = arr.getJSONObject(i)
            val recArr = obj.optJSONArray("recommendations") ?: JSONArray()
            StepGuide(
                area = obj.optString("area", ""),
                goal = obj.optString("goal", ""),
                recommendations = (0 until recArr.length()).map { recArr.getString(it) }
            )
        }
    } catch (_: Exception) { emptyList() }
}

fun improvementAreasToJson(areas: List<ImprovementArea>): String {
    val arr = JSONArray()
    areas.forEach { area ->
        arr.put(
            org.json.JSONObject().apply {
                put("area", area.area)
                put("impact", area.impact.name)
                put("score_potential", area.scorePotential)
                put("illustration", area.illustrationAsset)
            }
        )
    }
    return arr.toString()
}

fun stepGuidesToJson(guides: List<StepGuide>): String {
    val arr = JSONArray()
    guides.forEach { guide ->
        arr.put(
            org.json.JSONObject().apply {
                put("area", guide.area)
                put("goal", guide.goal)
                put("recommendations", JSONArray(guide.recommendations))
            }
        )
    }
    return arr.toString()
}
