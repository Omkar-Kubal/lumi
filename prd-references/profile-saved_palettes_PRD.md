# Saved Palettes Screen — Product Requirements Document

**Document:** `saved_palettes_PRD.md`
**Version:** 1.0
**Status:** Draft
**Parent:** Profile Screen (`profile_PRD.md`)
**Date:** May 2026

---

## 1. Purpose

The Saved Palettes screen shows the user's full list of saved color analysis palettes in reverse chronological order. Each palette was generated during a Color Analysis scan and saved by the user. Tapping a palette opens the full Color Analysis screen for that result. PRO-only screen — FREE users cannot save palettes.

---

## 2. AI Dependency

**None.** All data read from Room cache.

| Element | Source | AI? |
|---|---|---|
| Palette list | Room `ColorAnalysisEntity` ordered by `updatedAt` desc | No |
| Swatch previews | First 5 hex values from `personalPalette` JSON field | No |
| Season name + sub-season | Room `ColorAnalysisEntity.season`, `subSeason` | No |
| Updated date | Room `ColorAnalysisEntity.updatedAt` | No |

---

## 3. Access Control

- **PRO entitlement required.**
- FREE user navigating here → redirect to Paywall. `context = "saved_palettes"`.
- Checked in `SavedPalettesViewModel.init` via RevenueCat `CustomerInfo`.

---

## 4. Entry Points

| Source | NavArg |
|---|---|
| Profile screen — Saved Color Palettes "View all ›" | None (loads all palettes for current uid) |

---

## 5. SavedPalettesViewModel State

```kotlin
data class SavedPalettesUiState(
    val isLoading: Boolean = true,
    val palettes: List<PaletteSummary> = emptyList(),
    val pendingDeleteId: String? = null,    // id of palette pending delete confirm
    val error: SavedPalettesError? = null
)

data class PaletteSummary(
    val id: String,
    val faceAnalysisId: String,
    val season: String,                     // display string e.g. "Soft Summer"
    val subSeason: String,                  // e.g. "Cool • Soft • Light"
    val updatedAt: Long,
    val swatchHexes: List<String>           // first 5 from personalPalette
)

sealed class SavedPalettesError {
    object LoadFailed : SavedPalettesError()
    object DeleteFailed : SavedPalettesError()
    object AccessDenied : SavedPalettesError()
}
```

**Data loading:**
```
SavedPalettesViewModel.init
    │
    ├── entitlement check → if FREE: emit AccessDenied → redirect to Paywall
    ├── ColorAnalysisDao.getAllByUser(uid, orderBy=updatedAt DESC)
    │   → List<ColorAnalysisEntity>
    ├── map each entity to PaletteSummary:
    │   ├── parse personalPalette JSON → take first 5 hex strings
    │   ├── format season display string from season + subSeason fields
    │   └── format subSeason attributes string
    └── emit SavedPalettesUiState(isLoading=false, palettes=...)
```

Data loaded as `Flow<List<ColorAnalysisEntity>>` — list updates automatically when Room changes (palette saved or deleted from Color Analysis screen).

---

## 6. Layout

```
┌─────────────────────────────────────────┐
│  [<]      Saved Palettes                │  ← Top bar
├─────────────────────────────────────────┤
│                                         │
│  [Palette card 1]                       │
│  [Palette card 2]                       │
│  [Palette card 3]                       │
│  ...                                    │
│                                         │
│  [Empty state — if no palettes]         │
│                                         │
└─────────────────────────────────────────┘
```

Scrollable `LazyColumn`. No sticky bar. No bottom nav (child screen — top bar back only).

---

## 7. Palette Card

Per palette row rendered as a card.

```
┌─────────────────────────────────────────┐
│  Soft Summer              Sub-season    │
│  Updated May 05, 2025                   │
│                                         │
│  ●  ●  ●  ●  ●                     >   │
│  (5 swatch circles)                     │
└─────────────────────────────────────────┘
```

**Card contents:**

| Element | Source | Detail |
|---|---|---|
| Season name | `PaletteSummary.season` | Bold, top-left |
| Sub-season chip | `PaletteSummary.subSeason` | Small pill chip, top-right of name |
| Updated date | `PaletteSummary.updatedAt` | Formatted `MMM dd, yyyy`, muted, below name |
| Swatch row | `PaletteSummary.swatchHexes` (5) | Circular filled swatches, 24dp diameter each |
| Chevron | Static | Right side, vertically centred |

**Swatch rendering:**
- 5 circular swatches in a horizontal row, 24dp each, 6dp spacing.
- Filled with hex colour from `swatchHexes[0..4]`.
- No labels, no interaction on swatches.
- If fewer than 5 hex values in array (edge case — malformed data): render only available swatches. No crash.

**Season name display string construction:**
```kotlin
// season = "SUMMER", subSeason = "Soft Summer"
// display: subSeason value if non-empty, else season enum display string
val seasonDisplay = if (subSeason.isNotBlank()) subSeason else season.toDisplayString()
// subSeason attributes chip: from ColorAnalysisEntity.attributes JSON array
// joined: "Cool • Soft • Light"
val attributesDisplay = attributes.joinToString(" • ")
```

**Card tap:** Navigates to Color Analysis screen. NavArg: `colorAnalysisId = palette.id`.

**Swipe to delete:**
- Swipe left reveals red delete action (standard `SwipeToDismiss` in Compose).
- Delete icon + "Delete" label on revealed action.
- Swiping to full reveal triggers confirmation dialog (does not auto-delete).
- Confirmation dialog:
  - Title: "Delete this palette?"
  - Body: "This will permanently remove your Soft Summer palette. This cannot be undone."
  - Season name inserted dynamically from palette being deleted.
  - Buttons: "Cancel" (dismiss, card snaps back) / "Delete" (destructive).

**Delete logic on confirm:**
```
pendingDeleteId = palette.id
    │
    ├── ColorAnalysisDao.deleteById(palette.id)
    ├── Room Flow updates → palette removed from list automatically
    ├── pendingDeleteId = null
    └── snackbar: "Palette deleted" with "Undo" action (5s window)

Undo logic:
    └── if Undo tapped within 5s:
        ├── ColorAnalysisDao.insert(deletedEntity)  ← entity held in memory
        └── list repopulates automatically via Flow
```

**Undo implementation:**
- Deleted `ColorAnalysisEntity` held in `SavedPalettesViewModel.recentlyDeletedPalette` for 5s.
- After 5s: `recentlyDeletedPalette = null` (entity no longer restorable).
- Snackbar dismissed after 5s if Undo not tapped.

---

## 8. Empty State

Shown when `palettes.isEmpty()` after loading completes.

```
┌─────────────────────────────────────────┐
│                                         │
│       [ic_empty_palettes.xml]           │
│                                         │
│       No saved palettes yet             │
│                                         │
│   Complete a Color Analysis scan to     │
│   discover and save your colour season  │
│                                         │
│       [Start Color Scan]                │
│                                         │
└─────────────────────────────────────────┘
```

- Illustration: `ic_empty_palettes.xml` static asset.
- Headline: "No saved palettes yet".
- Body: "Complete a Color Analysis scan to discover and save your colour season."
- CTA button: "Start Color Scan" → navigates to Scan screen, `ScanType.COLOR_ONLY`.

---

## 9. Sorting

- Default order: `updatedAt DESC` (most recently updated first).
- No user-controlled sort in MVP.
- `updatedAt` updated when palette is re-saved from Color Analysis screen (user taps Save Palette again for a new scan — creates new entity, does not overwrite existing).

---

## 10. Pagination

- Load all palettes for user in one query in MVP. No pagination.
- Practical limit: users will have very few palettes (1 per color scan, 3 scans/day limit). No performance concern.
- Post-MVP: add pagination if list grows beyond 50 items.

---

## 11. Navigation Map

| Trigger | Destination | Condition |
|---|---|---|
| Back chevron | Profile screen | Always |
| Palette card tap | Color Analysis screen (`colorAnalysisId`) | Always |
| Swipe delete → confirm | Stay on screen, palette removed | Always |
| Swipe delete → cancel | Stay on screen, card snaps back | Always |
| Undo snackbar | Stay on screen, palette restored | Within 5s of delete |
| CTA (empty state) | Scan screen (`COLOR_ONLY`) | Empty state only |
| FREE user | Paywall placeholder | On entry |

---

## 12. Acceptance Criteria

1. FREE users redirected to Paywall on entry without loading palette data.
2. List loads within 200ms from Room. Flow updates list automatically when palettes change.
3. Each palette card shows correct season name, sub-season attributes chip, formatted date, and 5 swatches.
4. Swatch hex colours render correctly. Fewer than 5 swatches handled without crash.
5. Card tap navigates to Color Analysis screen with correct `colorAnalysisId`.
6. Swipe left reveals delete action. Does not auto-delete — requires confirmation dialog.
7. Confirmation dialog shows correct palette name dynamically.
8. Cancel on dialog snaps card back to original position.
9. Confirm delete removes palette from Room. List updates automatically via Flow.
10. Snackbar with Undo shown for 5s after delete.
11. Undo within 5s restores palette to Room. List repopulates immediately.
12. Undo after 5s has no effect (entity discarded from memory).
13. Empty state shown when no palettes exist. CTA navigates to Scan screen with `COLOR_ONLY` type.
14. No bottom navigation bar on this screen (child screen, top bar back only).

---

## 13. Out of Scope

- Rename palette.
- Reorder / drag to sort palettes.
- Bulk delete.
- Export all palettes as PDF or image.
- Compare two palettes side by side.
- Palette sharing directly from this list (share available from Color Analysis screen only).

---

*End of document — Saved Palettes PRD v1.0*
