package com.appylab.lumi.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.appylab.lumi.data.db.LumiDatabase
import com.appylab.lumi.data.model.FaceAnalysis
import com.appylab.lumi.data.model.GlowUpPotential
import com.appylab.lumi.data.model.ResultError
import com.appylab.lumi.data.model.SubscriptionTier
import com.appylab.lumi.data.model.glowUpPotentialFrom
import com.appylab.lumi.data.model.verdictBodyFrom
import com.appylab.lumi.data.model.verdictLabelFrom
import com.appylab.lumi.data.repository.ResultRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class ResultUiState(
    val isLoading: Boolean = true,
    val analysis: FaceAnalysis? = null,
    val subscriptionTier: SubscriptionTier = SubscriptionTier.FREE,
    val glowUpPotential: GlowUpPotential = GlowUpPotential.MEDIUM,
    val verdictLabel: String = "",
    val verdictBody: String = "",
    val isGeneratingShareCard: Boolean = false,
    val error: ResultError? = null
)

class ResultViewModel(application: Application) : AndroidViewModel(application) {

    private val db = LumiDatabase.getInstance(application)
    private val repository = ResultRepository(
        faceAnalysisDao = db.faceAnalysisDao(),
        appStateDao = db.appStateDao()
    )

    private val _isGeneratingShare = MutableStateFlow(false)

    val uiState: StateFlow<ResultUiState> = combine(
        repository.observeLatestAnalysis(),
        repository.observeSubscriptionTier(),
        _isGeneratingShare
    ) { analysis, tier, generating ->
        if (analysis == null) {
            ResultUiState(
                isLoading = false,
                error = ResultError.NotFound
            )
        } else {
            val score = analysis.glowUpScore
            ResultUiState(
                isLoading = false,
                analysis = analysis,
                subscriptionTier = tier,
                glowUpPotential = glowUpPotentialFrom(score),
                verdictLabel = verdictLabelFrom(score),
                verdictBody = verdictBodyFrom(score),
                isGeneratingShareCard = generating
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ResultUiState(isLoading = true)
    )

    fun shareResult(context: Context) {
        val analysis = uiState.value.analysis ?: return
        val isPro = uiState.value.subscriptionTier == SubscriptionTier.PRO

        viewModelScope.launch(Dispatchers.Default) {
            _isGeneratingShare.update { true }
            try {
                val bitmap = generateShareCard(analysis, isPro)
                withContext(Dispatchers.IO) {
                    val file = File(context.cacheDir, "lumi_result.jpg")
                    file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
                    val uri = FileProvider.getUriForFile(
                        context, "${context.packageName}.provider", file
                    )
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/jpeg"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "Share your results"))
                }
            } finally {
                _isGeneratingShare.update { false }
            }
        }
    }

    private fun generateShareCard(analysis: FaceAnalysis, isPro: Boolean): Bitmap {
        val size = 1080
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val bgPaint = Paint().apply { color = android.graphics.Color.parseColor("#FCFCFC") }
        canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), bgPaint)

        val rosePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#FF637E")
        }

        // App wordmark
        val wordmarkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#FF637E")
            textSize = 72f
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("Lumi", 80f, 120f, wordmarkPaint)

        // Divider
        val divPaint = Paint().apply { color = android.graphics.Color.parseColor("#FFCCD3") }
        canvas.drawRect(80f, 140f, size - 80f, 143f, divPaint)

        // Score ring (simple circle)
        val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#F5F5F5")
            style = Paint.Style.STROKE
            strokeWidth = 30f
        }
        val cx = 220f; val cy = 380f; val r = 150f
        canvas.drawCircle(cx, cy, r, ringPaint)

        val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#FF637E")
            style = Paint.Style.STROKE
            strokeWidth = 30f
            strokeCap = Paint.Cap.ROUND
        }
        val oval = android.graphics.RectF(cx - r, cy - r, cx + r, cy + r)
        canvas.drawArc(oval, -90f, 360f * (analysis.glowUpScore / 100f), false, progressPaint)

        // Score number
        val scorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#0A0A0A")
            textSize = 120f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("${analysis.glowUpScore}", cx, cy + 40f, scorePaint)
        val smallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#525252")
            textSize = 42f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("/100", cx, cy + 90f, smallPaint)

        // Verdict
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#0A0A0A")
            textSize = 72f
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText(analysis.glowUpScore.let { verdictLabelFrom(it) }, 430f, 320f, labelPaint)
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#525252")
            textSize = 42f
        }
        canvas.drawText(analysis.faceShape.ifEmpty { "—" } + " Face Shape", 430f, 410f, bodyPaint)
        canvas.drawText(analysis.skinTone.ifEmpty { "—" } + " • " + analysis.undertone.ifEmpty { "—" }, 430f, 470f, bodyPaint)

        // Divider
        canvas.drawRect(80f, 580f, size - 80f, 583f, divPaint)

        // Skin tone swatches
        val swatchColors = listOf("#F5DCCA", "#E8C4A0", "#C68642", "#8D5524", "#4A2912")
        swatchColors.forEachIndexed { i, hex ->
            val swatchPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.parseColor(hex) }
            canvas.drawCircle(120f + i * 100f, 660f, 38f, swatchPaint)
        }

        // Watermark for FREE
        if (!isPro) {
            val wPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.parseColor("#A4A4A4")
                textSize = 38f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("Powered by Lumi • getlumi.app", size / 2f, 1040f, wPaint)
        }

        return bitmap
    }
}
