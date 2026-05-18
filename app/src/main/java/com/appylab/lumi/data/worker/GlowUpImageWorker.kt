package com.appylab.lumi.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.appylab.lumi.data.api.GeminiService
import com.appylab.lumi.data.db.LumiDatabase
import com.appylab.lumi.data.model.GlowUpImageStatus
import java.io.File
import java.io.FileOutputStream

/**
 * WorkManager worker that generates a glow-up image via Gemini image generation.
 *
 * Input data key: [KEY_FACE_ANALYSIS_ID] (Long)
 *
 * Flow:
 *  1. Reads [GlowUpEntity] for the given faceAnalysisId from Room
 *  2. Marks status → GENERATING (UI picks up the live update)
 *  3. Loads the original image bytes from local storage
 *  4. Calls [GeminiService.generateGlowUpImage] (blocking REST call on IO)
 *  5. Saves the returned bytes to internal storage
 *  6. Updates [GlowUpEntity] with the new file path and status COMPLETE
 *  7. On any failure → status FAILED (no retry in MVP; UI shows retry button)
 *
 * The [GlowUpViewModel] observes the entity via Room Flow, so the UI updates
 * automatically when the status changes.
 */
class GlowUpImageWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_FACE_ANALYSIS_ID = "face_analysis_id"
    }

    override suspend fun doWork(): Result {
        val faceAnalysisId = inputData.getLong(KEY_FACE_ANALYSIS_ID, -1L)
        if (faceAnalysisId == -1L) return Result.failure()

        val db        = LumiDatabase.getInstance(applicationContext)
        val glowUpDao = db.glowUpDao()
        val entity    = glowUpDao.getByFaceAnalysisId(faceAnalysisId) ?: return Result.failure()

        // Signal UI that generation has started
        glowUpDao.updateImageStatus(faceAnalysisId, null, GlowUpImageStatus.GENERATING.name)

        return try {
            // Load original image bytes
            val originalBytes = File(entity.originalImageUrl).readBytes()

            // Call Gemini image generation (blocking — CoroutineWorker runs on Dispatchers.IO)
            val generatedBytes = GeminiService().generateGlowUpImage(originalBytes)
                ?: throw IllegalStateException("Gemini returned no image bytes")

            // Save generated image to internal storage
            val outputFile = File(applicationContext.filesDir, "glowup_${faceAnalysisId}.jpg")
            FileOutputStream(outputFile).use { it.write(generatedBytes) }

            glowUpDao.updateImageStatus(
                faceAnalysisId = faceAnalysisId,
                url            = outputFile.absolutePath,
                status         = GlowUpImageStatus.COMPLETE.name
            )
            Result.success()
        } catch (e: Exception) {
            glowUpDao.updateImageStatus(
                faceAnalysisId = faceAnalysisId,
                url            = null,
                status         = GlowUpImageStatus.FAILED.name
            )
            Result.failure()
        }
    }
}
