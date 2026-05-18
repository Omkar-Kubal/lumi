package com.appylab.lumi.data.api

import android.graphics.BitmapFactory
import android.util.Base64
import com.appylab.lumi.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Wraps all Gemini API calls for the app.
 *
 * - [analyzeFace] — sends the face image to `gemini-2.0-flash` (vision + JSON output)
 *   and returns a structured [GeminiFaceResult].
 *
 * - [generateGlowUpImage] — sends the original image + transformation prompt to
 *   `gemini-2.0-flash-exp-image-generation` via REST and returns the raw image bytes.
 *
 * Both functions return `null` gracefully on any API or parse failure.
 */
class GeminiService {

    // Gemini 2.0 Flash with vision and forced JSON response
    private val analysisModel = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey    = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {
            responseMimeType = "application/json"
        }
    )

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Analyzes the face captured in [imageBytes] (JPEG).
     * Returns [GeminiFaceResult] on success, null on any failure.
     *
     * Should be called from an IO dispatcher (suspend function).
     */
    suspend fun analyzeFace(imageBytes: ByteArray): GeminiFaceResult? = runCatching {
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            ?: return null

        val response = analysisModel.generateContent(
            content {
                image(bitmap)
                text(ANALYSIS_PROMPT)
            }
        )

        parseAnalysisResponse(response.text ?: return null)
    }.getOrNull()

    /**
     * Sends [originalImageBytes] (JPEG) to Gemini image generation and returns
     * the generated glow-up image as raw bytes, or null on failure.
     *
     * Uses direct REST because the Android SDK does not expose image output yet.
     */
    fun generateGlowUpImage(originalImageBytes: ByteArray): ByteArray? = runCatching {
        val base64 = Base64.encodeToString(originalImageBytes, Base64.NO_WRAP)
        val body   = buildImageGenBody(base64)

        val url  = URL("https://generativelanguage.googleapis.com/v1beta/models/$IMAGE_GEN_MODEL:generateContent?key=${BuildConfig.GEMINI_API_KEY}")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            doOutput        = true
            connectTimeout  = 30_000
            readTimeout     = 90_000
        }
        conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }

        if (conn.responseCode != 200) return null
        val responseJson = conn.inputStream.use { it.readBytes() }.toString(Charsets.UTF_8)
        parseImageResponse(responseJson)
    }.getOrNull()

    // ── Request builders ──────────────────────────────────────────────────────

    private fun buildImageGenBody(base64Image: String): String {
        val inlineData = JSONObject().put("mime_type", "image/jpeg").put("data", base64Image)
        val imgPart    = JSONObject().put("inline_data", inlineData)
        val textPart   = JSONObject().put("text", GLOW_UP_PROMPT)
        val parts      = JSONArray().put(imgPart).put(textPart)
        val contentObj = JSONObject().put("parts", parts)
        val contents   = JSONArray().put(contentObj)
        val genConfig  = JSONObject().put("responseModalities", JSONArray().put("IMAGE"))
        return JSONObject()
            .put("contents", contents)
            .put("generationConfig", genConfig)
            .toString()
    }

    // ── Response parsers ──────────────────────────────────────────────────────

    private fun parseAnalysisResponse(json: String): GeminiFaceResult? = runCatching {
        val o = JSONObject(json)
        GeminiFaceResult(
            glowUpScore          = o.optInt("glow_up_score", 75).coerceIn(0, 100),
            faceShape            = o.optString("face_shape", "OVAL").uppercase(),
            faceShapeDescription = o.optString("face_shape_description", ""),
            skinTone             = o.optString("skin_tone", "MEDIUM").uppercase(),
            undertone            = o.optString("undertone", "NEUTRAL").uppercase(),
            undertoneDescription = o.optString("undertone_description", ""),
            eyeShape             = o.optString("eye_shape", "ALMOND").uppercase(),
            browType             = o.optString("brow_type", "DEFINED").uppercase(),
            noseShape            = o.optString("nose_shape", "STRAIGHT").uppercase(),
            lipType              = o.optString("lip_type", "FULL").uppercase(),
            improvementAreasJson = o.optJSONArray("improvement_areas")?.toString() ?: "[]",
            stepGuidesJson       = o.optJSONArray("step_guides")?.toString() ?: "[]",
            celebrityMatchesJson = o.optJSONArray("celebrity_matches")?.toString() ?: "[]"
        )
    }.getOrNull()

    private fun parseImageResponse(json: String): ByteArray? = runCatching {
        val candidates = JSONObject(json).getJSONArray("candidates")
        val parts      = candidates.getJSONObject(0)
            .getJSONObject("content")
            .getJSONArray("parts")
        for (i in 0 until parts.length()) {
            val part = parts.getJSONObject(i)
            if (part.has("inline_data")) {
                val b64 = part.getJSONObject("inline_data").getString("data")
                return Base64.decode(b64, Base64.NO_WRAP)
            }
        }
        null
    }.getOrNull()

    // ── Companion: prompts + constants ────────────────────────────────────────

    companion object {
        private const val IMAGE_GEN_MODEL = "gemini-2.0-flash-exp-image-generation"

        // language=JSON — ask Gemini to return pure JSON, no markdown
        const val ANALYSIS_PROMPT = """Analyze this face image and return ONLY a valid JSON object — no markdown, no code fences, no explanation.

Required fields:
{
  "glow_up_score": <integer 0-100>,
  "face_shape": <"OVAL"|"ROUND"|"HEART"|"SQUARE"|"DIAMOND"|"OBLONG"|"TRIANGLE">,
  "face_shape_description": "<1-2 sentences>",
  "skin_tone": <"FAIR"|"LIGHT"|"MEDIUM"|"TAN"|"DEEP">,
  "undertone": <"WARM"|"COOL"|"NEUTRAL">,
  "undertone_description": "<1-2 sentences>",
  "eye_shape": <"ALMOND"|"ROUND"|"HOODED"|"MONOLID"|"UPTURNED"|"DOWNTURNED">,
  "brow_type": <"DEFINED"|"SPARSE"|"ARCHED"|"STRAIGHT"|"THICK">,
  "nose_shape": <"STRAIGHT"|"WIDE"|"NARROW"|"BUTTON"|"ROMAN">,
  "lip_type": <"FULL"|"THIN"|"BOW_SHAPED"|"WIDE"|"HEART">,
  "improvement_areas": [
    { "area": "<name>", "impact": <"HIGH"|"MEDIUM"|"LOW">, "score_potential": <int>, "illustration": <"skin"|"brows"|"eye"|"lips"|"hair"|"jawline"|"contour"|"default"> }
  ],
  "step_guides": [
    { "area": "<same area name>", "goal": "<one sentence>", "recommendations": ["<action>","<action>","<action>"] }
  ],
  "celebrity_matches": [
    { "rank": 1, "name": "<name>", "similarityPct": <int 50-85> },
    { "rank": 2, "name": "<name>", "similarityPct": <int 45-75> },
    { "rank": 3, "name": "<name>", "similarityPct": <int 40-70> }
  ]
}

Rules:
- improvement_areas: max 5 items, ordered HIGH → LOW impact
- Every area in improvement_areas must have a matching step_guides entry
- Be specific and personalised to the actual face visible in the image
- Return ONLY the JSON object"""

        const val GLOW_UP_PROMPT = "Generate a natural, realistic glow-up transformation of this person. Improve their makeup, grooming, skin clarity and overall appearance while strictly preserving their identity, facial structure, skin tone and natural features. Photorealistic. No filters. The result must look like the same person at their absolute best."
    }
}

// ── Result model ──────────────────────────────────────────────────────────────

data class GeminiFaceResult(
    val glowUpScore: Int,
    val faceShape: String,
    val faceShapeDescription: String,
    val skinTone: String,
    val undertone: String,
    val undertoneDescription: String,
    val eyeShape: String,
    val browType: String,
    val noseShape: String,
    val lipType: String,
    val improvementAreasJson: String,
    val stepGuidesJson: String,
    val celebrityMatchesJson: String
)
