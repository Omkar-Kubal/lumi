# Glow-Up Screen — Product Requirements Document

**Document:** `glowup_PRD.md`
**Version:** 1.0
**Status:** Draft
**Parent:** Results Screen (`result_PRD.md`)
**Date:** May 2026

---

## 1. Purpose

The Glow-Up screen shows an AI-generated transformation of the user's selfie alongside the original, a score with delta from last scan, improvement areas, an actionable step guide per area, and a progress tracker across scans. It is the highest perceived-value screen in the app — primary driver of subscription conversion. All heavy AI work (image generation + analysis) happens asynchronously post-scan. Display time is zero AI calls.

---

## 2. AI Dependency

| Section | AI? | When | Source at display time |
|---|---|---|---|
| Glow-up image (After) | Yes — Gemini Imagen | Post-scan async, before screen opens | Room `GlowUpEntity.glowUpImageUrl` |
| Glow-up score | Yes — Gemini | During scan | Room `FaceAnalysis.glowUpScore` |
| Score delta | No | Derived locally | Room — diff between last 2 scans |
| Improvement areas | Yes — Gemini | During scan | Room `GlowUpEntity.improvementAreas` |
| Step guide per area | Yes — Gemini | During scan | Room `GlowUpEntity.stepGuides` |
| Progress chart data | No | Derived locally | Room — all scan scores for user |
| Share card | No | On demand | Canvas drawn locally |

**Glow-up image generation is async.** It runs as a background `WorkManager` task after scan completes. User can navigate to Glow-Up screen before it is ready — loading state shown in place of After image.

---

## 3. Access Control

- **PRO entitlement required.**
- Gate checked in `GlowUpViewModel.init`.
- FREE user → Paywall. `context = "glow_up"`.
- PRO user, no scan → empty state with Re-scan CTA.

---

## 4. Entry Points

| Source | NavArg |
|---|---|
| Results screen — Glow-Up feature tile | `faceAnalysisId: String` |
| Home — Glow-Up feature tile | `faceAnalysisId: String` (latest) |

---

## 5. Gemini Schema — Glow-Up Fields

Fields required in `FULL_ANALYSIS` and `GLOWUP_ONLY` Gemini response:

```
glow_up_score         : int       — 0–100
improvement_areas     : array     — max 5 objects:
    {
      area          : string    — e.g. "Brows", "Skin Clarity"
      impact        : string    — HIGH | MEDIUM | LOW
      score_potential: int      — e.g. 18 (points improvable)
      illustration  : string    — asset key e.g. "brows", "skin", "eye", "lips", "hair"
    }
step_guides           : array    — one per improvement area:
    {
      area          : string    — matches improvement_areas[n].area
      goal          : string    — 1 sentence goal description
      recommendations: array    — 3–5 action strings
    }
```

**Glow-up image:** Generated via separate Gemini Imagen API call (not part of analysis JSON). Triggered by `GlowUpImageWorker` (WorkManager) after scan. Result URL stored in `GlowUpEntity.glowUpImageUrl`.

---

## 6. GlowUpEntity — Room

```kotlin
@Entity(tableName = "glow_up")
data class GlowUpEntity(
    @PrimaryKey val id: String,
    val faceAnalysisId: String,
    val userId: String,
    val originalImageUrl: String,
    val glowUpImageUrl: String?,        // null until image generation completes
    val glowUpImageStatus: String,      // PENDING | GENERATING | COMPLETE | FAILED
    val score: Int,
    val improvementAreasJson: String,   // JSON array of improvement area objects
    val stepGuidesJson: String,         // JSON array of step guide objects
    val createdAt: Long
)
```

---

## 7. GlowUpImageWorker — WorkManager

```
Triggered by: ScanViewModel on successful Gemini analysis response
Worker type: OneTimeWorkRequest
Constraints: NetworkType.CONNECTED

Worker logic:
    1. Fetch originalImageUrl from GlowUpEntity
    2. Download original image → Base64
    3. Call Gemini Imagen API with prompt:
       "Generate a natural, realistic glow-up version of this person.
        Improve makeup, grooming, and overall appearance while preserving
        identity, facial structure, and skin tone. Photorealistic. No filters."
    4. On success:
       - Save generated image to local storage
       - Update GlowUpEntity.glowUpImageUrl = localPath
       - Update GlowUpEntity.glowUpImageStatus = COMPLETE
    5. On failure:
       - Update GlowUpEntity.glowUpImageStatus = FAILED
       - No retry in MVP — show failed state on screen

Observation: GlowUpViewModel observes GlowUpEntity via Room Flow.
             UI updates automatically when status changes.
```

---

## 8. GlowUpViewModel State

```kotlin
data class GlowUpUiState(
    val isLoading: Boolean = true,
    val originalImageUrl: String? = null,
    val glowUpImageUrl: String? = null,
    val glowUpImageStatus: GlowUpImageStatus = GlowUpImageStatus.PENDING,
    val score: Int = 0,
    val scoreDelta: Int? = null,        // null if only 1 scan exists
    val verdictLabel: String = "",
    val verdictBody: String = "",
    val improvementAreas: List<ImprovementArea> = emptyList(),
    val selectedArea: String = "",      // active tab in step guide
    val activeStepGuide: StepGuide? = null,
    val progressData: List<ScanScorePoint> = emptyList(),
    val isGeneratingShareCard: Boolean = false,
    val error: GlowUpError? = null
)

enum class GlowUpImageStatus { PENDING, GENERATING, COMPLETE, FAILED }

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
    val faceAnalysisId: String
)

enum class ImpactLevel { HIGH, MEDIUM, LOW }

sealed class GlowUpError {
    object NotFound : GlowUpError()
    object AccessDenied : GlowUpError()
}
```

**Data loading:**
```
GlowUpViewModel.load(faceAnalysisId)
    │
    ├── entitlement check
    ├── GlowUpDao.getByFaceAnalysisId(id) → entity (as Flow — live updates)
    ├── FaceAnalysisDao.getScoreHistory(userId) → progressData
    ├── derive scoreDelta:
    │     if progressData.size >= 2:
    │       scoreDelta = progressData.last().score - progressData[last-1].score
    │     else: scoreDelta = null
    ├── derive verdictLabel + verdictBody from score (same map as result_PRD)
    ├── set selectedArea = improvementAreas[0].area (first area default)
    └── emit GlowUpUiState(isLoading=false, ...)
```

---

## 9. Layout — Section Order

Scrollable `LazyColumn`. Sticky bottom bar (Generate & Share Card) outside scroll.

```
┌─────────────────────────────────────────┐
│  [<]  Feature Glow Up          [share]  │
│       See your transformation and       │
│       track your progress               │
├─────────────────────────────────────────┤
│  1. Before / After comparison card      │
│  2. Glow-Up Score card                  │
│  3. Improvement Areas card              │
│  4. Actionable Step Guide card          │
│  5. Progress Tracker card               │
│  6. Share Your Glow-Up section          │
├─────────────────────────────────────────┤
│  [Generate & Share Card sticky bar]     │
└─────────────────────────────────────────┘
```

---

## 10. Section 1 — Before / After Comparison Card

**Layout:** Full-width card. Draggable vertical divider.

**Header row:**
- "Before / After Comparison" label (left).
- Info icon (ⓘ) right → bottom sheet: "This glow-up preview is AI-generated using your personalised recommendations. Results shown are for inspiration only."
- Subtext below header: "Drag the slider to compare."

**Image comparison view:**
- Fixed height: 320dp.
- Left side: original selfie (`originalImageUrl`, Coil loaded).
- Right side: glow-up image (`glowUpImageUrl`, Coil loaded).
- Draggable vertical divider (white circle handle with `<` `>` arrows) positioned at centre (50%) by default.
- Drag gesture: `pointerInput` with `detectHorizontalDragGestures`. Updates `sliderPosition` (0f–1f) in ViewModel.
- Left badge: "Before" pill, top-left corner of image.
- Right badge: "After (AI Glow-Up)" pill, top-right corner of image.

**Before image:** Always available — `FaceAnalysis.imageUrl` from Room.

**After image states:**

| `glowUpImageStatus` | Displayed |
|---|---|
| `PENDING` | Shimmer placeholder right side. No divider shown. |
| `GENERATING` | Animated gradient right side + "Generating your glow-up..." label. |
| `COMPLETE` | Full glow-up image. Divider visible and draggable. |
| `FAILED` | Grey placeholder + "Generation failed" + retry button. Retry re-queues `GlowUpImageWorker`. |

**Footer text below image:** "AI-generated glow-up using your personalised recommendations." — static, always shown.

---

## 11. Section 2 — Glow-Up Score Card

**Layout:** Full-width card. Three-column internal layout.

### 11.1 Left column — Score ring
- Circular progress ring (same as result_PRD Overall Score).
- Score number centred inside ring.
- "/100" below score inside ring.
- Ring animates 0 → score on first render (600ms).

### 11.2 Centre column — Verdict + delta
- Verdict label (bold): derived from score (same map as result_PRD).
- Verdict body text (regular).
- Score delta badge: shown only if `scoreDelta != null`.
  - Positive delta: "+{n} vs last scan" — filled badge.
  - Negative delta: "-{n} vs last scan" — different fill.
  - Zero delta: "Same as last scan" — neutral badge.

### 11.3 Right column — Category bars
Four mini progress bars (cut from full spec — **removed per simplification decision**).
**Replaced with:** Single line "vs last scan" stat only. Right column removed entirely.
Layout becomes two-column: score ring left, verdict + delta right.

**Simplification rationale:** Sub-category bars (Skin +20, Brows +18 etc.) require Gemini to return per-category scores — schema complexity not justified. Score + delta is sufficient signal.

---

## 12. Section 3 — Improvement Areas Card

**Data source:** `improvementAreas` — max 5 `ImprovementArea` objects.

**Layout:** Full-width card. Horizontal scroll of improvement area cards.

- Header: "Improvement Areas (Priority)" + info icon.
- Info icon bottom sheet: "These areas were identified as having the highest impact on your overall glow-up score. Tap each to see your personalised guide."
- Horizontal scroll of cards (5 max).

**Per improvement area card:**
```
┌──────────────────┐
│  [illustration]  │  ← static asset per area
│  Brows      >    │  ← area name + chevron
│ [High Impact]    │  ← impact badge
│  +18 Potential   │  ← score potential
└──────────────────┘
```

**Illustration asset map (static SVGs):**
```
"brows"    → ic_improve_brows.xml
"skin"     → ic_improve_skin.xml
"eye"      → ic_improve_eye.xml
"lips"     → ic_improve_lips.xml
"hair"     → ic_improve_hair.xml
"jawline"  → ic_improve_jawline.xml
"contour"  → ic_improve_contour.xml
```
Fallback for unrecognised area key: `ic_improve_default.xml`.

**Impact badge label map:**
```
HIGH   → "High Impact"
MEDIUM → "Medium Impact"
LOW    → "Low Impact"
```

**Tap on area card:**
- Updates `GlowUpViewModel.selectedArea` to tapped area name.
- Scrolls `LazyColumn` to Actionable Step Guide section automatically via `LazyListState.animateScrollToItem()`.
- Active area card: highlighted border.

---

## 13. Section 4 — Actionable Step Guide Card

**Data source:** `activeStepGuide` — `StepGuide` for `selectedArea`.

**Layout:** Full-width card.

**Area tab row:**
- Horizontal scrollable chip row of all improvement area names.
- Active chip: filled. Others: outline.
- Tapping chip updates `selectedArea` in ViewModel.
- Tab row and card content update together.

**Step guide content (changes per selected area):**

```
┌─────────────────────────────────────────┐
│  [area illustration — large, left]      │
│                                         │
│  Your Goal                              │
│  [goal text — 1 sentence]               │
│                                         │
│  Top Recommendations                    │
│  ✓ [recommendation 1]                   │
│  ✓ [recommendation 2]                   │
│  ✓ [recommendation 3]                   │
│  ✓ [recommendation 4]  (if present)     │
│  ✓ [recommendation 5]  (if present)     │
└─────────────────────────────────────────┘
```

- Area illustration: same static asset as improvement area card, shown larger (120dp).
- Goal: `StepGuide.goal` text.
- Recommendations: `StepGuide.recommendations` list, each prefixed with ✓ checkmark icon.
- Max 5 recommendations shown. No overflow handling needed (Gemini returns max 5).
- **No product recommendations in this section** (Makeup screen dropped — no product dependency).

**Transition animation:** Card content cross-fades (200ms) when `selectedArea` changes.

---

## 14. Section 5 — Progress Tracker Card

**Visibility:** Shown only when `progressData.size >= 2`. Hidden for single scan.

**Layout:** Full-width card.

- Header: "Progress Tracker" + info icon.
- Info icon bottom sheet: "This chart shows your Glow Score across all your scans. Re-scan regularly to track your improvement."
- Subtext: "Your glow-up journey over time."

**Line chart:**
- X axis: scan dates, formatted `MMM d` (e.g. "Apr 10").
- Y axis: score 0–100, 4 gridlines at 25/50/75/100.
- Line: smooth curve connecting data points.
- Each data point: filled circle, 8dp radius.
- Latest data point: open circle (ring only) to indicate "current".
- Tapping a data point: shows tooltip above point with exact score + date.
- Projected next point shown as dashed line extending from latest point to a predicted score (derived as `lastScore + averageDelta`, capped at 100). Shown as open circle at end of dashed line.

**Chart implementation:** Compose Canvas `drawLine`, `drawCircle`, `drawPath`. No third-party chart library — keeps APK size down.

**Stats row below chart:**
- "Total Scans: {n}" left.
- "Avg improvement: +{avg}/scan" right. Average computed as `(latestScore - firstScore) / (totalScans - 1)`. Show "—" if only 2 scans (delta only).

---

## 15. Section 6 — Share Your Glow-Up

**Layout:** Row above sticky bottom bar.

- Left: selfie circular thumbnail (40dp) + mini score badge overlay.
- Centre column:
  - "My Glow-Up Score" label.
  - Score number bold.
  - Delta badge if present.
  - Verdict label.
  - "Generated by Glam Up" caption.
- Right: preview of share card (small, 60dp wide).

This section is a preview of what the share card will look like. Not interactive — tap anywhere in this section = same as tapping sticky bar.

---

## 16. Sticky Bottom Bar — Generate & Share Card

- Icon: share/upload icon.
- Label: "Generate & Share Card".
- Full-width dark fill bar.

**Tap behaviour:**
1. `isGeneratingShareCard = true` → bar shows loading spinner + "Generating...".
2. Canvas bitmap generated locally (1080×1080px):
   - Left half: original selfie cropped square.
   - Right half: glow-up image cropped square (if COMPLETE), else original with filter.
   - "Before" / "After (AI Glow-Up)" labels overlaid.
   - Score ring bottom-left with score number.
   - Delta badge bottom-centre.
   - App logo + "Generated by Glam Up" footer.
3. `Intent.ACTION_SEND` with `image/jpeg`.
4. System share sheet opens.
5. `isGeneratingShareCard = false`.

**If glow-up image not yet COMPLETE:**
- Bar label changes to "Share in Progress — image generating".
- Bar disabled (greyed, non-tappable).
- Becomes active when `glowUpImageStatus = COMPLETE`.

---

## 17. Empty State

PRO user, no glow-up data:
- Illustration: `ic_empty_glowup.xml`
- Headline: "No glow-up yet"
- Body: "Complete a scan to see your AI-powered glow-up transformation."
- CTA: "Start Scan" → Scan screen, `ScanType.GLOWUP_ONLY`.

---

## 18. Navigation Map

| Trigger | Destination |
|---|---|
| Back chevron | Previous screen |
| Share icon (top bar) | Same as sticky bar — share sheet |
| Info icon (before/after) | Info bottom sheet |
| Info icon (improvement areas) | Info bottom sheet |
| Info icon (progress tracker) | Info bottom sheet |
| Improvement area card tap | Scrolls to step guide + selects tab |
| Step guide tab tap | Updates guide content (no navigation) |
| Data point tap (chart) | Tooltip shown inline |
| Retry (image failed) | Re-queues GlowUpImageWorker |
| Sticky bar tap | Share card generation → share sheet |
| CTA (empty state) | Scan screen |
| FREE user | Paywall placeholder |

---

## 19. Acceptance Criteria

1. FREE users redirected to Paywall without rendering any content.
2. Before image loads immediately from Room local path. No network call.
3. After image shows correct state per `glowUpImageStatus` (pending shimmer, generating animation, complete image, failed placeholder).
4. Divider drag gesture updates image split in real time. Positioned at 50% on load.
5. Score ring animates 0 → score in 600ms on first render.
6. Score delta badge shown only when 2+ scans exist. Correct positive/negative/zero state.
7. Improvement area cards show correct illustration asset per area key. Fallback asset shown for unrecognised keys.
8. Tapping improvement area card scrolls to step guide and activates corresponding tab.
9. Step guide content updates correctly when tab changes. Cross-fade animation plays.
10. No product recommendations appear anywhere in step guide (Makeup dropped).
11. Progress chart hidden for single scan. Shown for 2+ scans.
12. Progress chart data points match Room scan score history for user.
13. Projected point shown as dashed line from latest score.
14. Tapping chart data point shows tooltip with score and date.
15. Stats row shows correct total scans and average improvement.
16. Share card sticky bar disabled when glow-up image status is not COMPLETE.
17. Share card generated as 1080×1080px bitmap. Share sheet opens successfully.
18. GlowUpImageWorker retry re-queues correctly on failure tap. Status updates via Room Flow.
19. Screen loads within 200ms for all non-image content. Image loads async without blocking.
20. Empty state shown for PRO user with no glow-up data.

---

## 20. Out of Scope

- Per-category sub-scores (Skin, Brows, Balance bars) — removed, schema complexity not justified.
- Product recommendations in step guide — Makeup screen dropped.
- Video glow-up preview.
- AR live glow-up overlay.
- Multiple glow-up image variants (one image per scan only).
- Social sharing with deep link back to app.

---

*End of document — Glow-Up PRD v1.0*
