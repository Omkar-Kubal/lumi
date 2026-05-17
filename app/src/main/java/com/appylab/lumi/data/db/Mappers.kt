package com.appylab.lumi.data.db

import com.appylab.lumi.data.model.CelebrityMatch
import com.appylab.lumi.data.model.FaceAnalysis
import org.json.JSONArray

fun FaceAnalysisEntity.toModel(): FaceAnalysis {
    val matches = runCatching {
        val arr = JSONArray(celebrityMatchesJson)
        List(arr.length()) { i ->
            val obj = arr.getJSONObject(i)
            CelebrityMatch(
                rank = obj.getInt("rank"),
                name = obj.getString("name"),
                similarityPct = obj.getInt("similarityPct")
            )
        }
    }.getOrDefault(emptyList())

    return FaceAnalysis(
        id = id,
        userId = userId,
        glowUpScore = glowUpScore,
        faceShape = faceShape,
        skinTone = skinTone,
        undertone = undertone,
        eyeShape = eyeShape,
        imageUrl = imageUrl,
        timestamp = timestamp,
        browType = browType,
        noseShape = noseShape,
        lipType = lipType,
        faceShapeDescription = faceShapeDescription,
        undertoneDescription = undertoneDescription,
        celebrityMatches = matches
    )
}
