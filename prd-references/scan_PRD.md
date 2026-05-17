# Scan Screen — Product Requirements Document

**Document:** `scan_PRD.md`
**Version:** 1.0
**Status:** Draft
**Parent PRD:** GlamUp MVP PRD v1.0
**Date:** May 2026

---

## 1. Purpose

The Scan screen is the primary value-delivery screen of the Glam Up app. It guides the user through capturing a valid selfie using real-time on-device feedback, validates image quality locally, and triggers the AI analysis pipeline post-capture. Everything visible to the user during the camera preview is 100% on-device — no network calls occur until the capture button is tapped and all validations pass.

---

## 2. Screen Goal

- Guide user to capture a high-quality, well-positioned selfie with minimal friction.
- Validate face position, distance, lighting, and image clarity entirely on-device.
- Provide clear, real-time visual feedback so the user self-corrects without instruction.
- Trigger Gemini AI analysis only after a validated image is confirmed.
- Allow gallery upload as an alternative to live capture.
- Allow scan type selection (Face / Color / Glow-Up) before capture.

---

## 3. AI Dependency

**On-screen (camera preview): None.**
**Post-capture: One Gemini API call.**

| Element | AI? | Source |
|---|---|---|
| Face oval guide overlay | No | Canvas, static shape |
| Facial landmark dots | No | ML Kit Face Detection, on-device |
| Landmark connecting lines | No | Canvas drawn from ML Kit output |
| Corner bracket guide | No | Static Canvas overlay |
| Good lighting chip | No | Local luminance calculation |
| Move closer chip | No | ML Kit face bounding box size ratio |
| Center your face chip | No | ML Kit face centroid offset |
| Positioned status bar | No | Local logic — all checks passing |
| Image quality checklist | No | ML Kit + Laplacian variance + luminance |
| Capture button | No | CameraX ImageCaptureUseCase |
| Gallery button | No | MediaStore PickVisualMedia |
| Flip camera button | No | CameraX CameraSelector |
| Scan type selector | No | Session state / NavArg |
| Privacy footer | No | Static string |
| Help button | No | Static FAQ bottom sheet |
| **Gemini API call** | **Yes** | **Fires post-capture, after all validations pass** |

---

## 4. Entry Points

| Source | ScanType passed | Gate check |
|---|---|---|
| Home — Start Scan CTA | `FULL_ANALYSIS` | Free scan check + auth check |
| Home — Scan tab (bottom nav) | `FULL_ANALYSIS` | Same as above |
| Home — Color feature tile | `COLOR_ONLY` | PRO entitlement check |
| Home — Glow-Up feature tile | `GLOWUP_ONLY` | PRO entitlement check |
| Results screen — Re-scan CTA | `FULL_ANALYSIS` | Free scan check |

**Gate logic on entry (evaluated in `ScanViewModel.init`):**
- Guest user → redirect to Auth screen, return destination = Scan.
- FREE user + `free_scan_used = true` + `ScanType = FULL_ANALYSIS` → redirect to Paywall, `context = "scan"`.
- FREE user + `ScanType = COLOR_ONLY or GLOWUP_ONLY` → redirect to Paywall, `context = feature name`.
- PRO user → proceed. No gate.
- Checks happen before camera is initialised. User never sees camera if gated.

---

## 5. Layout — Sections

```
┌─────────────────────────────────────┐
│  [×]    Scan your face         [?]  │  ← Top bar
│         We'll analyse and           │
│         personalise for you         │
├─────────────────────────────────────┤
│  [Lighting chip]    [Distance chip] │  ← Floating status chips
│                     [Centre chip]   │
│                                     │
│         ┌ - - - - - - ┐             │  ← Corner bracket guide
│       ┌───────────┐               │
│       │   Oval    │               │  ← Face oval guide
│       │  guide +  │               │
│       │ landmarks │               │
│       └───────────┘               │
│         └ - - - - - - ┘             │
│                                     │
│  ┌─────────────────────────────┐    │  ← Positioned status bar
│  │ ✓  Great! Your face is      │    │
│  │    well positioned          │    │
│  └─────────────────────────────┘    │
│  ┌─────────────────────────────┐    │  ← Image quality checklist
│  │ Image quality               │    │
│  │ Face detected          ✓    │    │
│  │ Not blurry             ✓    │    │
│  │ Good lighting          ✓    │    │
│  └─────────────────────────────┘    │
├─────────────────────────────────────┤
│  [Gallery]  [Capture]  [Flip cam]   │  ← Camera controls
├─────────────────────────────────────┤
│           Scan type                 │  ← Scan type selector
│  [Face]     [Color]    [Glow-Up]    │
│  🔒 Your images are private         │  ← Privacy footer
└─────────────────────────────────────┘
```

---

## 6. Camera Setup

**Library:** CameraX (`androidx.camera`)

**Use cases bound:**
- `PreviewUseCase` — live viewfinder rendered to `PreviewView`.
- `ImageCaptureUseCase` — triggered on capture button tap.
- `ImageAnalysisUseCase` — feeds frames to ML Kit at throttled rate (10fps).

**Configuration:**
```
CameraSelector     : DEFAULT_FRONT_CAMERA (default on screen open)
ImageCapture       : JPEG quality 85, target resolution 1080×1080
ImageAnalysis      : STRATEGY_KEEP_ONLY_LATEST, target resolution 480×480
                     (lower res for ML Kit — speed over quality)
Lifecycle          : bound to viewLifecycleOwner
```

**Camera release:** `ProcessCameraProvider.unbindAll()` called in `onStop()`. No camera held in background.

**Flip camera:**
- Toggles between `DEFAULT_FRONT_CAMERA` and `DEFAULT_BACK_CAMERA`.
- Re-binds all use cases on toggle.
- State persisted in `ScanViewModel.cameraLens` (not in DataStore — session only).
- All validation logic identical regardless of lens selected.

---

## 7. Real-Time Overlay — ML Kit Pipeline

**Library:** ML Kit Face Detection (`com.google.mlkit:face-detection`)

**Detector configuration:**
```
FaceDetectorOptions:
  performanceMode     = FAST
  landmarkMode        = ALL_LANDMARKS
  classificationMode  = NONE  (no smile/eye-open detection needed)
  minFaceSize         = 0.15  (ignore tiny faces in background)
  enableTracking      = false (single-frame analysis, no tracking needed)
```

**Frame throttle:** `ImageAnalysisUseCase` executor throttled to 10fps via `Executors.newSingleThreadExecutor()`. ML Kit result delivered on analysis thread, state update posted to main thread via `StateFlow`.

### 7.1 Validation Checks (run per frame)

All four checks evaluated independently. Results combined into `FrameValidationState`.

**Check 1 — Face detected**
- Condition: `faces.size == 1`
- Fail (0 faces): show "Position your face in the oval" hint. Oval guide stays white.
- Fail (>1 face): show "Only one face please" hint.
- Pass: proceed to checks 2–4.

**Check 2 — Face distance (size)**
- Metric: face bounding box width as fraction of frame width. `faceWidthRatio = boundingBox.width / frameWidth`
- Too far: `faceWidthRatio < 0.30` → "Move closer / Ideal distance" chip shown.
- Too close: `faceWidthRatio > 0.75` → "Move back a little" chip shown.
- Pass range: `0.30 ≤ faceWidthRatio ≤ 0.75`

**Check 3 — Face centred**
- Metric: offset of face bounding box centre from frame centre.
  ```
  faceCentreX = boundingBox.centerX
  frameCentreX = frameWidth / 2
  offsetRatioX = |faceCentreX - frameCentreX| / frameWidth

  faceCentreY = boundingBox.centerY
  frameCentreY = frameHeight / 2
  offsetRatioY = |faceCentreY - frameCentreY| / frameHeight
  ```
- Fail: `offsetRatioX > 0.15` or `offsetRatioY > 0.15` → "Center your face / Keep your face in the oval" chip shown.
- Pass: both offset ratios ≤ 0.15.

**Check 4 — Lighting**
- Metric: average luminance of pixels within face bounding box.
  - Crop face region from `ImageProxy` YUV frame (Y channel = luminance).
  - Compute mean of all Y-channel pixel values within bounding box.
- Too dark: `meanLuminance < 80` → "Low lighting / Find better lighting" chip shown.
- Too bright: `meanLuminance > 220` → "Too bright / Reduce light behind you" chip shown.
- Pass range: `80 ≤ meanLuminance ≤ 220`.

### 7.2 Combined Status

All 4 checks evaluated → `FrameValidationState`:

```kotlin
data class FrameValidationState(
    val faceDetected: Boolean,
    val multipleFaces: Boolean,
    val distanceStatus: DistanceStatus,   // TOO_CLOSE, TOO_FAR, OK
    val centreStatus: CentreStatus,       // OFF_CENTRE, OK
    val lightingStatus: LightingStatus,   // TOO_DARK, TOO_BRIGHT, OK
    val allPassed: Boolean = faceDetected
        && !multipleFaces
        && distanceStatus == OK
        && centreStatus == OK
        && lightingStatus == OK
)
```

---

## 8. Overlay Drawing

All drawn on a transparent `Canvas` composable layered over `PreviewView`.

### 8.1 Corner bracket guide
- Four L-shaped corner brackets at fixed positions (10% inset from screen edges).
- Static. Does not animate. Always visible.
- Drawn as white strokes, 3dp width, rounded caps.

### 8.2 Face oval guide
- Ellipse centred on screen. Fixed dimensions: width = 55% of screen width, height = 72% of screen height.
- Stroke only (not filled), 2dp width.
- Colour state:
  - All checks passing → white (ready)
  - Any check failing → white (neutral — no red/green per design direction, confirmed no color scheme reference)
- Dashed stroke on top arc (hairline guide, as visible in mockup). Solid stroke on sides and bottom.

### 8.3 Landmark dots
- Rendered only when `faceDetected = true`.
- ML Kit returns 2D landmark positions in image coordinates → transformed to screen coordinates via `Matrix` mapping `imageSize → previewSize`.
- Landmarks rendered: all available ML Kit face landmarks (eyes, nose, mouth corners, ears, cheeks).
- Standard landmarks: filled circles, 4dp radius, white fill.
- Key feature landmarks (nose tip, mouth corners): filled circles, 4dp radius, accent fill (pink — per mockup).
- Connecting lines drawn between landmark groups:
  - Eye outlines (left eye landmarks connected in order)
  - Nose bridge (top to tip)
  - Mouth outline
  - Jawline (ear to ear via chin)
  - Brow lines
- Line weight: 1.5dp, white, 60% alpha.

### 8.4 Coordinate transformation
```kotlin
val matrix = Matrix()
matrix.setRectToRect(
    RectF(0f, 0f, imageWidth.toFloat(), imageHeight.toFloat()),
    RectF(0f, 0f, previewWidth.toFloat(), previewHeight.toFloat()),
    Matrix.ScaleToFit.FILL
)
// For front camera: also apply horizontal flip
if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
    matrix.postScale(-1f, 1f, previewWidth / 2f, previewHeight / 2f)
}
val screenPoint = floatArrayOf(landmark.position.x, landmark.position.y)
matrix.mapPoints(screenPoint)
```

---

## 9. Floating Status Chips

Three independent chips positioned as floating cards over the camera preview (top-left and top-right regions per mockup).

Each chip: icon + primary label + secondary label (subtext).

| Chip | Position | Icon | Primary label | Subtext | Shown when |
|---|---|---|---|---|---|
| Lighting | Top-left | sun icon | "Good lighting" | "Keep it up!" | Lighting OK |
| Lighting | Top-left | sun-dim icon | "Low lighting" | "Find better lighting" | Too dark |
| Lighting | Top-left | sun-bright icon | "Too bright" | "Reduce light behind you" | Too bright |
| Distance | Top-right upper | scan-face icon | "Move closer" | "Ideal distance" | Too far |
| Distance | Top-right upper | scan-face icon | "Move back" | "Ideal distance" | Too close |
| Distance | Top-right upper | *(hidden)* | — | — | Distance OK |
| Centre | Top-right lower | centre-focus icon | "Center your face" | "Keep your face in the oval" | Off-centre |
| Centre | Top-right lower | *(hidden)* | — | — | Centred OK |

**Lighting chip:** Always visible (shows positive or corrective state). Never hidden.
**Distance + Centre chips:** Hidden when condition passes. Shown only when correction needed.
**Animation:** Chips fade in/out with 150ms alpha animation on visibility change.

---

## 10. Positioned Status Bar

Full-width pill below the oval guide. Sits above the image quality checklist.

**States:**

| State | Icon | Text |
|---|---|---|
| All checks passing | ✓ (filled circle) | "Great! Your face is well positioned" |
| Any check failing | — (no icon, hidden entirely) | *(bar not shown)* |

- Bar animates in (slide up + fade) when `allPassed` transitions from false → true.
- Bar animates out (fade) when `allPassed` transitions from true → false.
- Transition debounced: state must be stable for 300ms before triggering animation (prevents flicker).

---

## 11. Image Quality Checklist

Card below the status bar. Always visible. Three rows with live pass/fail state.

| Row | Label | Pass condition |
|---|---|---|
| Face detected | "Face detected" | `faceDetected = true` && `multipleFaces = false` |
| Not blurry | "Not blurry" | Evaluated post-capture only (Laplacian variance ≥ 100). Live preview always shows pending state (grey). |
| Good lighting | "Good lighting" | `lightingStatus == OK` |

**Icon states per row:**
- Pending (live, not yet checked): grey circle outline.
- Pass: filled accent circle with checkmark.
- Fail: grey circle outline (not red — neutral failure per design direction).

**"Not blurry" row behaviour:**
- During live preview: always shows pending (grey). Blur is not computed per-frame (too expensive).
- Post-capture: Laplacian variance computed on captured bitmap. Row updates to pass/fail.
- If fail: blur error shown, user returned to camera preview, checklist resets.

---

## 12. Scan Type Selector

Bottom sheet strip, always visible above privacy footer.

**Label:** "Scan type"

**Three options:**

| Tab | Icon | ScanType enum | Access |
|---|---|---|---|
| Face | face-scan icon | `FULL_ANALYSIS` | FREE + PRO |
| Color | palette icon | `COLOR_ONLY` | PRO only |
| Glow-Up | sparkle icon | `GLOWUP_ONLY` | PRO only |

**Selection behaviour:**
- Default selection: determined by `ScanType` NavArg passed from entry point. If no NavArg, defaults to `FULL_ANALYSIS`.
- Tapping a tab updates `ScanViewModel.selectedScanType`.
- Tapping Color or Glow-Up tab while FREE: show bottom sheet "Unlock Color Analysis / Glow-Up with Pro" with Upgrade CTA. Tab does not visually select. User stays on Face tab.
- Lock icon overlaid on Color and Glow-Up tabs for FREE users.

**Effect on capture:** `selectedScanType` passed as parameter to Gemini API call post-capture. Gemini prompt and expected JSON schema adjusted per scan type.

---

## 13. Camera Controls

Three controls in a horizontal row between checklist and scan type selector.

### 13.1 Gallery button
- Icon: image/gallery icon. Label: "Gallery".
- Launches `ActivityResultContracts.PickVisualMedia(ImageOnly)`.
- Selected image passed through same validation pipeline as captured photo:
  - ML Kit face detection on selected bitmap.
  - Laplacian variance blur check.
  - Luminance lighting check.
- If validation fails: specific error bottom sheet shown with option to pick a different photo. Errors:
  - No face detected → "No face found in this photo — try a clearer selfie."
  - Multiple faces → "Multiple faces detected — use a solo selfie."
  - Too blurry → "Photo is too blurry — try a sharper image."
  - Poor lighting → "Lighting is too dark or bright — try a better-lit photo."
- If validation passes → proceeds to Gemini API call.

### 13.2 Capture button
- Large circular button, centre. Outer ring animates (pulse) when `allPassed = true`.
- Tap behaviour:
  - `allPassed = false`: button tappable but triggers a gentle shake animation + highlights the failing checklist row. Does not capture.
  - `allPassed = true`: triggers `ImageCaptureUseCase.takePicture()`. Proceeds to post-capture validation → Gemini call.
- Auto-capture: fires automatically 1.5s after `allPassed` transitions to true and remains true. Countdown not shown to user. Can be pre-empted by manual tap.
- Auto-capture disabled if user has manually dismissed it once (tapped × or cancel during loading). Stays manual for remainder of session.

### 13.3 Flip camera
- Icon: rotate/flip icon. Label: "Flip camera".
- Toggles `ScanViewModel.cameraLens` between FRONT and BACK.
- Re-binds CameraX use cases.
- Landmark coordinate transform matrix recalculated (mirror removed for back camera).
- All validation checks continue uninterrupted on new camera.

---

## 14. Post-Capture Validation

Executed after `ImageCaptureUseCase.takePicture()` succeeds. Runs before Gemini API call.

```
Captured JPEG
    │
    ▼
Decode to Bitmap
    │
    ▼
Check 1: ML Kit face detection on full-res bitmap
    │   Pass: exactly 1 face detected
    │   Fail: show error, return to camera
    ▼
Check 2: Laplacian variance blur detection
    │   Compute Laplacian on grayscale bitmap
    │   Threshold: variance ≥ 100 = sharp
    │   Fail: "Photo is blurry — retake." return to camera
    ▼
Check 3: Luminance check on face region
    │   Crop face bounding box from bitmap
    │   Mean Y-channel luminance 80–220 = pass
    │   Fail: "Lighting issue — retake." return to camera
    ▼
Check 4: File size
    │   Compress to JPEG 85%
    │   If still > 512KB: resize to 512×512, re-compress
    │   Max size enforced: 512KB
    ▼
All pass → Base64 encode → pass to ScanViewModel.startAnalysis()
```

**Daily scan counter check** (before Gemini call):
- Read DataStore `scan_count_today` (Int) and `scan_count_date` (String, ISO date).
- If `scan_count_date ≠ today` → reset `scan_count_today = 0`, update date.
- If `scan_count_today ≥ 3` → block. Show dialog:
  - PRO: "You've used all 3 scans for today. Come back tomorrow."
  - FREE: "You've used all 3 scans for today. Come back tomorrow or upgrade to Pro."
  - Return to camera. Do not call Gemini.
- If `scan_count_today < 3` → increment counter, proceed to Gemini call.

---

## 15. Loading State (Post-Capture)

Shown after all post-capture validations pass, during Gemini API call.

**Layout:** Full-screen overlay over frozen last camera frame (camera preview paused). Semi-transparent dark scrim. Centred content.

**Animated progress stages** — text updated every 2s regardless of actual API progress:

| Stage | Text |
|---|---|
| 0–2s | "Detecting your face shape..." |
| 2–4s | "Analysing skin tone..." |
| 4–6s | "Mapping your features..." |
| 6–8s | "Building your personalised profile..." |
| 8–10s | "Almost there..." |
| 10s+ | "Just a few more seconds..." (repeated) |

**Cancel button:** Shown after 5s. Label: "Cancel". Tapping:
- Cancels Gemini coroutine job (`job.cancel()`).
- Dismisses overlay.
- Returns to camera preview (camera use cases re-bound).
- Captured image preserved in `ScanViewModel.capturedBitmap` — user can retap capture without re-validating.
- Daily scan counter NOT decremented (scan attempt was made).

**Timeout:** Gemini call cancelled automatically after 15s. Same recovery as manual cancel + snackbar: "Analysis timed out — tap capture to try again."

**On API error (non-timeout):**
- Dismiss overlay.
- Snackbar: "Analysis failed — tap capture to try again."
- Preserve captured image in ViewModel.
- Log error to Firebase Crashlytics with error code and scan type.

---

## 16. Gemini API Call

Triggered by `ScanViewModel.startAnalysis(base64Image, scanType, userProfile)`.

**Model:** `gemini-2.0-flash-lite`
**Max tokens:** 1000
**Temperature:** 0.2 (low — consistent structured output)

**Request construction:**

```
System prompt:
  "You are an expert beauty consultant. Analyse the provided selfie
   and return ONLY valid JSON matching the schema below.
   No markdown, no explanation, no code fences. JSON only."
  + JSON schema (varies by ScanType — see below)

User message:
  {
    image: { base64, mimeType: "image/jpeg" },
    text: "Analyse this face. User profile: age={ageGroup},
           skin concerns={skinConcerns}. Scan type: {scanType}."
  }
```

**Schema by ScanType:**

| ScanType | Schema includes |
|---|---|
| `FULL_ANALYSIS` | All fields: face_shape, skin_tone, undertone, eye_shape, features, celebrity_matches, glow_up_score, improvement_areas, color_season, personal_palette, avoid_colors, makeup_palette, routine_day, routine_night |
| `COLOR_ONLY` | color_season, color_sub_season, personal_palette, avoid_colors, clothing_recs, hair_color_recs, makeup_palette |
| `GLOWUP_ONLY` | glow_up_score, improvement_areas, routine_day (abridged — top 5 steps only) |

**Response parsing:**
- Attempt `JSON.parse()` on raw response string.
- If parse fails: one automatic retry — append to conversation: "Your response was not valid JSON. Return only the JSON object with no other text."
- If second parse fails: `AnalysisError.ParseFailure`. Snackbar + return to camera. Daily scan counter NOT decremented on parse failure.
- On success: map to `FaceAnalysis` domain model via `GeminiResponseMapper`. Store raw JSON in Room `ScanEntity.rawJson`.

---

## 17. Navigation

| Trigger | Destination |
|---|---|
| × (close) button | Back to Home (or previous screen) |
| ? (help) button | FAQ bottom sheet (static content) |
| Gemini response success | Results screen (with `faceAnalysisId` NavArg) |
| Gemini error after retry | Stay on Scan screen, snackbar shown |
| Daily limit reached | Stay on Scan screen, dialog shown |
| FREE user taps Color/Glow-Up tab | Paywall bottom sheet |
| Gallery pick → validation fail | Stay on Scan screen, error bottom sheet |

---

## 18. ScanViewModel State

```kotlin
data class ScanUiState(
    val isLoading: Boolean = false,
    val cameraLens: Int = CameraSelector.LENS_FACING_FRONT,
    val selectedScanType: ScanType = ScanType.FULL_ANALYSIS,
    val frameValidation: FrameValidationState = FrameValidationState(),
    val loadingStage: String = "",
    val showCancelButton: Boolean = false,
    val capturedBitmap: Bitmap? = null,
    val error: ScanError? = null,
    val scanCount: Int = 0             // today's count, shown as "X of 3 scans used"
)

sealed class ScanError {
    object NoFace : ScanError()
    object MultipleFaces : ScanError()
    object Blurry : ScanError()
    object PoorLighting : ScanError()
    object DailyLimitReached : ScanError()
    object Timeout : ScanError()
    object ParseFailure : ScanError()
    data class ApiError(val code: Int, val message: String) : ScanError()
}
```

---

## 19. Privacy

- "Your images are private and secure" — static label, always visible at bottom of screen.
- Captured images sent to Gemini API over HTTPS only.
- Images not stored server-side by the app. Gemini processes and discards per Google API terms.
- Local copy stored in Room as file path only (not raw bytes) for Results screen display.
- No image shared with third parties beyond Gemini API.
- Privacy policy URL accessible from Help (?) bottom sheet.

---

## 20. Acceptance Criteria

1. Camera opens within 1 second of screen entry on mid-range Android device (Pixel 6a equivalent).
2. ML Kit landmark overlay renders within 500ms of face appearing in frame.
3. All four validation checks update independently and in real time at 10fps.
4. Floating chips appear/disappear with 150ms animation on condition change.
5. Status bar ("Great! Your face is well positioned") appears only when all 4 checks simultaneously pass.
6. Auto-capture fires exactly 1.5s after `allPassed` becomes true. Does not fire if user previously cancelled.
7. Capture button shake animation plays when tapped while `allPassed = false`.
8. Front camera landmark dots correctly mirrored (not flipped) via matrix transform.
9. Flip camera re-binds use cases within 500ms with no preview freeze > 1s.
10. Gallery-imported image passes through identical validation pipeline as captured photo.
11. Blurry image (Laplacian variance < 100) rejected post-capture with correct error message.
12. Multi-face image rejected with correct error message.
13. Daily scan counter correctly blocks 4th attempt. Counter resets at local midnight.
14. Loading overlay stage text advances every 2s regardless of API speed.
15. Cancel button appears at 5s mark. Cancels coroutine. Returns to live camera preview.
16. Gemini timeout (15s) triggers snackbar and returns to camera without crash.
17. JSON parse failure triggers one retry. Two consecutive failures return to camera.
18. On successful API response, navigates to Results screen with correct `faceAnalysisId`.
19. Color and Glow-Up scan type tabs show lock for FREE users. Tapping shows Paywall bottom sheet, not full-screen Paywall.
20. Camera use cases fully released in `onStop()`. No ANR or camera leak.
21. "Your images are private and secure" label always visible above system nav bar.

---

## 21. Out of Scope — Scan Screen MVP

- Video-based skin analysis (requires continuous frame analysis post-capture).
- AR makeup try-on overlay.
- QR code or product scan via camera.
- Multiple photo submission (single selfie only in MVP).
- Portrait mode / depth sensor integration.
- Audio guidance / accessibility voice-over for positioning (post-MVP accessibility pass).
- Server-side scan throttle (client-side only in MVP — add server enforcement post-launch).

---

*End of document — Scan Screen PRD v1.0*
