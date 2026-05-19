# Scan History Browser — Product Requirements Document

**Document:** `scan_history_PRD.md`
**Version:** 1.0
**Status:** Draft
**Parent:** Profile Screen (`profile_PRD.md`)
**Date:** May 2026

---

## 1. Purpose

The Scan History Browser shows the user's full list of past scans with a progress chart at the top. Users can browse all scans, filter by type, tap to view any past result, compare two scans side by side, and delete scans. FREE users see last 3 scans only. PRO users see full history with compare mode.

---

## 2. AI Dependency

**None.** All data read from Room cache.

| Element | Source | AI? |
|---|---|---|
| Scan list | Room `ScanEntity` list | No |
| Scan thumbnails | Room `ScanEntity.imageUrl` | No |
| Glow scores | Room `FaceAnalysis.glowUpScore` | No |
| Progress chart data | Room — all scan scores for uid | No |
| Compare delta | Derived locally | No |

---

## 3. Access Control

- **All authenticated users** — FREE and PRO.
- FREE users: see last 3 scans. Compare mode locked (PRO only).
- PRO users: full history, compare mode enabled.
- Guest users: no scan history. Cannot reach this screen.

---

## 4. Entry Points

| Source | NavArg |
|---|---|
| Profile screen — Scan History "View all ›" | None |

---

## 5. ScanHistoryViewModel State

```kotlin
data class ScanHistoryUiState(
    val isLoading: Boolean = true,
    val subscriptionTier: SubscriptionTier = SubscriptionTier.FREE,
    val allScans: List<ScanHistoryItem> = emptyList(),
    val filteredScans: List<ScanHistoryItem> = emptyList(),
    val activeFilter: ScanTypeFilter = ScanTypeFilter.ALL,
    val progressData: List<ScanScorePoint> = emptyList(),
    val isCompareMode: Boolean = false,
    val selectedForCompare: List<String> = emptyList(),  // max 2 faceAnalysisIds
    val compareResult: CompareResult? = null,
    val pendingDeleteId: String? = null,
    val recentlyDeleted: ScanHistoryItem? = null,
    val error: ScanHistoryError? = null
)

data class ScanHistoryItem(
    val faceAnalysisId: String,
    val imageUrl: String,
    val scanType: ScanType,
    val createdAt: Long,
    val glowScore: Int?,                // null if score not yet available
    val glowUpPotential: GlowUpPotential?
)

data class ScanScorePoint(
    val date: Long,
    val score: Int,
    val faceAnalysisId: String
)

data class CompareResult(
    val scanA: ScanHistoryItem,         // older scan
    val scanB: ScanHistoryItem,         // newer scan
    val scoreDelta: Int                 // scanB.score - scanA.score
)

enum class ScanTypeFilter { ALL, FULL_ANALYSIS, COLOR_ONLY, GLOWUP_ONLY }

sealed class ScanHistoryError {
    object LoadFailed : ScanHistoryError()
    object DeleteFailed : ScanHistoryError()
}
```

**Data loading:**
```
ScanHistoryViewModel.init
    │
    ├── DataStore.subscriptionTier → tier
    ├── if FREE:
    │   └── ScanEntityDao.getRecent(uid, limit=3) → allScans
    │   else:
    │   └── ScanEntityDao.getAll(uid, orderBy=createdAt DESC) → allScans
    ├── FaceAnalysisDao.getScoreHistory(uid) → progressData
    │   (all scans with non-null score, ordered by createdAt ASC)
    ├── map ScanEntity + FaceAnalysis → ScanHistoryItem list
    ├── filteredScans = allScans (filter = ALL default)
    └── emit ScanHistoryUiState(isLoading=false, ...)
```

---

## 6. Layout — Full Screen

```
┌─────────────────────────────────────────┐
│  [<]   Scan History          [filter]   │  ← Top bar
├─────────────────────────────────────────┤
│  [Progress chart card]                  │  ← Hidden if < 2 scans
├─────────────────────────────────────────┤
│  [Filter chips row]                     │
├─────────────────────────────────────────┤
│  [Scan row card 1]                      │
│  [Scan row card 2]                      │
│  [Scan row card 3]                      │
│  [FREE lock row — if FREE]              │
│  ...                                    │
├─────────────────────────────────────────┤
│  [Compare bar — visible in compare mode]│  ← Sticky bottom
└─────────────────────────────────────────┘
```

Scrollable `LazyColumn`. Progress chart card and filter chips scroll with content (not sticky). Compare bar is sticky — outside scroll area, shown only in compare mode.

---

## 7. Progress Chart Card

**Visibility:** Shown only when `progressData.size >= 2`. Hidden for 0 or 1 scans.

**Layout:** Full-width card with header, chart, and stats row.

**Header:**
- "Glow Score Over Time" label left.
- Info icon (ⓘ) right → bottom sheet: "This chart shows your Glow Score across all completed Face + Skin scans. Re-scan regularly to track your improvement over time."

**Subtext:** "Your glow-up journey over time."

**Line chart (Compose Canvas — no third-party library):**

```
Y: 100
    |                              ○ 92  ← projected
    |                   ● 86
    |        ● 63  ● 69
    | ● 58
    └────────────────────────────────────
      Apr10  Apr24  May8  May18   Jun1
```

**Chart specification:**
- Canvas dimensions: full card width × 160dp height.
- Y axis: 0–100. Four horizontal gridlines at 25, 50, 75, 100. Light stroke, dashed.
- Y axis labels: "25", "50", "75", "100" — left-aligned, 12sp.
- X axis: scan dates, formatted `MMM d`. Evenly distributed. Max 6 labels shown (skip intermediate labels if more than 6 data points).
- Data points: filled circles, 8dp radius. Tappable (see below).
- Connecting line: smooth curve via `drawPath` with cubic bezier control points between consecutive points.
- Latest point: open circle (stroke only, no fill) to differentiate current.
- Projected point:
  - Computed as: `projectedScore = min(lastScore + averageDeltaPerScan, 100)`.
  - `averageDeltaPerScan = (lastScore - firstScore) / (totalScans - 1)`. Use 0 if only 2 scans.
  - Projected date: `lastScanDate + averageDaysBetweenScans`. Average days computed from scan timestamps.
  - Shown as open circle at end of dashed line extending from latest point.
  - Not shown if projected score = last score (no improvement trend) or if result > 100.
- Tooltip: tapping a data point shows a floating tooltip above the point:
  - Format: "{score}/100 • {MMM dd, yyyy}"
  - Tooltip dismissed by tapping anywhere else on chart.

**Stats row below chart:**
- Left: "Total Scans: {n}" — n = `progressData.size`.
- Right: "Avg improvement: +{avg}/scan" — computed as above. Show "—" if 2 scans (no meaningful average yet).

---

## 8. Filter Chips Row

Horizontal scrollable chip row. Always visible regardless of scan count.

**Options:**
```
All  |  Face + Skin  |  Color Analysis  |  Glow-Up
```

**ScanTypeFilter → display label map:**
```
ALL           → "All"
FULL_ANALYSIS → "Face + Skin"
COLOR_ONLY    → "Color Analysis"
GLOWUP_ONLY   → "Glow-Up"
```

**Behaviour:**
- Single select. "All" default.
- Tap chip → `activeFilter = selectedFilter`.
- `filteredScans` recomputed:
  ```kotlin
  filteredScans = if (activeFilter == ALL) allScans
                  else allScans.filter { it.scanType == activeFilter.toScanType() }
  ```
- Filter applied instantly, no loading state.
- If filter produces empty list → show inline empty state within list area:
  - "No {filter label} scans yet."
  - No CTA needed — filter is user-applied.

---

## 9. Scan Row Card

Per scan rendered as a card row.

```
┌──────────────────────────────────────────────────┐
│  [●]   Face + Skin              May 18, 2025      │
│        Glow Score: 86/100                    >    │
│        ████████░░  High potential                 │
└──────────────────────────────────────────────────┘
```

**Card contents:**

| Element | Source | Detail |
|---|---|---|
| Thumbnail | `ScanHistoryItem.imageUrl` | Circular, 48dp, Coil loaded |
| Scan type label | `ScanHistoryItem.scanType` display string | Bold |
| Date | `ScanHistoryItem.createdAt` | `MMM dd, yyyy`, right-aligned |
| Glow Score | `ScanHistoryItem.glowScore` | "{score}/100", shown if non-null |
| Mini progress bar | Score / 100 | Thin horizontal bar, full width of text area |
| Potential label | `ScanHistoryItem.glowUpPotential` | "High / Medium / Low potential" |
| Chevron | Static | Right side |

**Score not available** (COLOR_ONLY or GLOWUP_ONLY scans may not have a face score):
- Glow Score row hidden.
- Mini progress bar hidden.
- Potential label hidden.
- Scan type + date shown only.

**Card tap:**
- Normal mode: navigate to Result screen with `faceAnalysisId`.
- Compare mode: toggle selection (see Section 11).

**Swipe to delete:**
- Swipe left → red delete action revealed.
- Same undo pattern as Saved Palettes:
  - Confirmation dialog → delete from Room → snackbar "Scan deleted" with 5s Undo.
  - `recentlyDeleted` held in ViewModel for Undo.
- Deleting a scan deletes both `ScanEntity` and associated `FaceAnalysis`, `GlowUpEntity`, `FeatureDetailEntity`, `ColorAnalysisEntity` for that `faceAnalysisId`.
- Cannot delete in compare mode (swipe disabled while `isCompareMode = true`).

**Deletion cascade:**
```kotlin
@Transaction
suspend fun deleteScanAndRelated(faceAnalysisId: String) {
    scanEntityDao.deleteById(faceAnalysisId)
    faceAnalysisDao.deleteById(faceAnalysisId)
    glowUpDao.deleteByFaceAnalysisId(faceAnalysisId)
    featureDetailDao.deleteByFaceAnalysisId(faceAnalysisId)
    colorAnalysisDao.deleteByFaceAnalysisId(faceAnalysisId)
}
```
Room `@Transaction` annotation ensures atomic deletion.

---

## 10. FREE User Gate Row

Shown as the 4th item in the list when `subscriptionTier == FREE` and `allScans.size >= 3`.

```
┌──────────────────────────────────────────────────┐
│  🔒  Your full scan history is locked            │
│      Upgrade to Pro to access all scans          │
│      [Upgrade to Pro]  button                    │
└──────────────────────────────────────────────────┘
```

- Lock icon left.
- Copy: "Your full scan history is locked" + "Upgrade to Pro to access all scans and compare your progress."
- "Upgrade to Pro" button → Paywall placeholder. `context = "scan_history"`.
- If `allScans.size < 3` for FREE user: gate row not shown (user hasn't hit the limit yet).

---

## 11. Compare Mode (PRO Only)

**Entry:**
- Long press on any scan row → enters compare mode.
- `isCompareMode = true` in ViewModel.
- Checkboxes appear on all scan rows.
- Long-pressed row's checkbox pre-selected.
- Compare sticky bar appears at bottom.

**FREE user long press:**
- Does not enter compare mode.
- Shows bottom sheet: "Compare mode is a Pro feature. Upgrade to compare your scans."
- "Upgrade" button → Paywall placeholder.

**Compare mode UI changes:**
- Top bar title changes to "Select 2 Scans".
- Top bar right button changes to "Cancel" → tapping exits compare mode.
- Swipe to delete disabled on all rows.
- Each scan row shows checkbox left of thumbnail.

**Selection rules:**
- Max 2 rows selectable.
- Tapping a selected row deselects it.
- Tapping a 3rd row when 2 already selected: deselects oldest selection, selects new one.

**Compare sticky bar:**
```
┌─────────────────────────────────────────┐
│  2 scans selected        [Compare]      │  ← when 2 selected
│  Select 1 more scan      [Compare]      │  ← when 1 selected (Compare greyed)
└─────────────────────────────────────────┘
```
- "Compare" button enabled only when exactly 2 scans selected.
- Tap "Compare" → build `CompareResult`, navigate to Compare View screen (Section 12).

---

## 12. Compare View Screen

Full-screen overlay (not a separate nav destination — rendered as a composable on top of Scan History).

**Layout:**
```
┌──────────────────────────────────────────┐
│  [<]     Compare Scans                   │
├─────────────────┬────────────────────────┤
│  May 10, 2025   │  May 18, 2025          │  ← dates
│  [selfie A]     │  [selfie B]            │  ← circular thumbnails 80dp
│  Face + Skin    │  Face + Skin           │  ← scan type
│  Score: 69      │  Score: 86             │  ← scores
│                 │  +17 ↑                 │  ← delta on newer scan
│                 │                        │
│  Potential:     │  Potential:            │
│  Medium         │  High                  │
└─────────────────┴────────────────────────┘
│  [View Full Results →]                   │  ← per column, navigates to Result
│  [View Full Results →]                   │
└──────────────────────────────────────────┘
```

**Two-column layout, equal width.**

**Per column:**
- Date: `MMM dd, yyyy`.
- Selfie circular thumbnail: 80dp, Coil loaded.
- Scan type label.
- Score: "{score}/100", bold large.
- Delta (newer scan column only):
  - Positive: "+{n} ↑" — improvement.
  - Negative: "-{n} ↓" — decline.
  - Zero: "No change".
- Glow-Up Potential label if available.
- "View Full Results →" text link: navigates to Result screen for that scan.

**Column ordering:** Older scan always left, newer scan always right. Determined by `createdAt` comparison.

**Delta calculation:**
```kotlin
val scoreDelta = newerScan.glowScore - olderScan.glowScore
```

**Exit:** Back chevron or Android back gesture → returns to Scan History in compare mode. Compare mode auto-exits after viewing compare result (reset `isCompareMode = false`, clear `selectedForCompare`).

---

## 13. Navigation Map

| Trigger | Destination | Condition |
|---|---|---|
| Back chevron | Profile screen | Always |
| Filter icon (top bar) | Filter bottom sheet | Always |
| Filter chip tap | Filter list inline | Always |
| Scan row tap (normal) | Result screen (`faceAnalysisId`) | Normal mode |
| Scan row tap (compare) | Toggle selection | Compare mode |
| Chart data point tap | Tooltip inline | Always |
| FREE gate "Upgrade" | Paywall placeholder | FREE user |
| Long press row (PRO) | Enter compare mode | PRO only |
| Long press row (FREE) | Upgrade bottom sheet | FREE user |
| Compare button | Compare View | Exactly 2 selected |
| Compare View "View Full Results" | Result screen | Per column |
| Compare View back | Scan History (exit compare mode) | Always |
| Swipe delete → confirm | Stay, scan removed | Normal mode only |
| Undo snackbar | Stay, scan restored | Within 5s |
| CTA (empty state) | Scan screen | Empty state only |

---

## 14. Acceptance Criteria

1. FREE user sees last 3 scans only. Gate row shown as 4th item when 3 scans exist.
2. PRO user sees all scans in reverse chronological order.
3. Progress chart hidden for < 2 scans. Shown with correct data points for 2+ scans.
4. Chart data points match Room scan score history. Projected point computed correctly.
5. Chart tooltip shows correct score and date on data point tap. Dismissed on tap elsewhere.
6. Stats row shows correct total scans and average improvement.
7. Filter chips correctly filter list by scan type. "All" shows full list.
8. Filter producing empty results shows correct inline empty message.
9. Each scan row shows correct thumbnail, scan type label, date, score (if available), mini bar, potential label.
10. Scan row without score (COLOR_ONLY, GLOWUP_ONLY) hides score/bar/potential gracefully.
11. Scan row tap in normal mode navigates to correct Result screen.
12. Swipe to delete shows confirmation dialog. Cancel snaps card back.
13. Delete confirmed removes scan and all associated entities atomically (Room @Transaction).
14. Undo within 5s restores deleted scan. Undo after 5s has no effect.
15. Cannot swipe to delete in compare mode.
16. Long press enters compare mode for PRO users. Shows upgrade bottom sheet for FREE users.
17. Compare mode: checkboxes visible, max 2 selectable, 3rd tap replaces oldest.
18. Compare button disabled with 1 selection. Enabled with exactly 2.
19. Compare View shows older scan left, newer scan right. Delta shown on newer column only.
20. Compare View "View Full Results" navigates to correct Result screen per column.
21. Exiting Compare View resets compare mode state.
22. Screen loads within 200ms. All data from Room, zero network calls.

---

## 15. Out of Scope

- Bulk delete (delete all scans at once).
- Export scan history as PDF or CSV.
- Compare more than 2 scans simultaneously.
- Sort order control (date asc/desc, score high/low).
- Search within scan history.
- Scan annotations or notes per scan.

---

*End of document — Scan History Browser PRD v1.0*
