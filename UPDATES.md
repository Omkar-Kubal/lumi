# Lumi — Session Update Log

Chronological record of all development sessions. Each entry covers: what was built, which files changed, and what is pending next.

---

## Session 1 — Foundations

### Summary
Initial project setup and early screen scaffolding.

### Completed
- Project created with Jetpack Compose + Material3
- Splash screen, Onboarding (3 screens), Home screen stub
- Bottom navigation bar wired in `MainActivity.kt`
- `ScanScreen` integrated with camera/Gemini flow
- `ResultScreen` skeleton created
- `ProfileScreen` skeleton created
- Room database scaffolded (`LumiDatabase`, `FaceAnalysisEntity`, `UserProfileEntity`, `AppStateEntity`)

### Files Changed
- `MainActivity.kt` — AppScreen enum + bottom nav wiring
- `ScanScreen.kt`, `HomeScreen.kt`, `ProfileScreen.kt`, `ResultScreen.kt`
- `LumiDatabase.kt`, entity files

### Pending at End
- Result sub-screens not yet built
- Profile sub-screens not yet built

---

## Session 2 — Scan Page & Profile Page

### Summary
Scan page completed. Profile page created (build remaining).

### Completed
- `ScanScreen.kt` — camera preview, Gemini analysis trigger, loading states
- `ProfileScreen.kt` — personal details section, notification toggles, sign-out/delete dialogs, recent scans row
- `ProfileViewModel.kt` — observes Room profile + scans + app state; edit draft state management; `startEditPersonalDetails()` / `savePersonalDetails()` / `cancelEditPersonalDetails()`
- `ProfileRepository` — `updatePersonalDetails()`, notification toggle updates, `prependSkinFromScan()`

### Files Changed
- `ScanScreen.kt`
- `ProfileScreen.kt`
- `ProfileViewModel.kt`
- `ProfileRepository.kt`

### Pending at End
- Result sub-screens not yet built
- Profile sub-screens (Edit Profile, Saved Palettes, Scan History) not yet built

---

## Session 3 — Result Page & Color Analysis Sub-Screen

### Summary
`ResultScreen` completed. `ColorAnalysisScreen` fully rewritten per PRD.

### Completed

#### ResultScreen
- Face Shape + Skin Tone cards (side-by-side)
- Color Analysis teaser card (tappable → `ColorAnalysisScreen`)
- Eyes & Features card with Canvas icons (tappable → `FeatureAnalysisScreen`)
- Celebrity Lookalikes card
- Overall Score ring card (animated, 600ms)
- Glow-Up Potential segmented bar card (animated, tappable → `GlowUpResultScreen`)
- Share result card
- Sticky Re-scan bar
- `FaceShapeLearnMoreSheet` bottom sheet
- Navigation: `ResultSubScreen` sealed class routes to sub-screens

#### ColorAnalysisScreen (`ColorAnalysisScreen.kt` — full rewrite)
- Season detection from `skinTone` + `undertone` (fixed, not user-selectable)
- 4-season 2×2 icon grid — tapping any season opens education bottom sheet
- Season education sheet: characteristics, key colors row, celebrity list, "Your Season" badge for detected
- Personal Color Palette — 8-swatch SwatchGrid with hex copy-to-clipboard + Toast
- Colors to Avoid — 4 swatches (no tap), info icon → Avoid info sheet with per-color reasons
- Clothing Color Recommendations — horizontal LazyRow
- Hair Colors — vertical list with circle swatches
- Makeup section — Lip + Eye color circles
- Best Metals — derived from undertone (WARM→Gold/RoseGold/Bronze, COOL→Silver/WhiteGold/Platinum, NEUTRAL→Silver/WhiteGold/RoseGold); AutoAwesome explanation text
- Share colour card — Canvas bitmap 1080×1080px → `ACTION_SEND`
- Sticky bottom "Save Palette to Profile" bar (BookmarkAdded/Bookmark toggle)

**Key data structures:** `ColorChip(color, name, hex, reason)`, `MetalData`, `SeasonData`, `SeasonEducation`, `SEASON_ICONS: List<ImageVector> by lazy { ... }`

**Key helpers:** `detectSeasonIndex()`, `getMetals()`, `undertoneAdjective()`, `hexToComposeColor()`

### Files Changed
- `ResultScreen.kt` — complete
- `ColorAnalysisScreen.kt` — full rewrite (~1076 lines)
- `ResultViewModel.kt` — minor updates

### Pending at End
- `FeatureAnalysisScreen` not yet rewritten
- Profile sub-screens not yet built
- Home feature tiles still route to PlaceholderScreen

---

## Session 4 — Feature Detail Analysis Sub-Screen

### Summary
`FeatureAnalysisScreen` fully rewritten per Feature Detail PRD.

### Completed

#### FeatureAnalysisScreen (`FeatureAnalysisScreen.kt` — full rewrite)
- **7-tab layout** via `ScrollableTabRow`: Overview, Eyes, Brows, Nose, Lips, Jawline, Cheekbones
- **Custom tab indicator** — `tabIndicatorOffset` Modifier extension + `Box` with FRose background
- **Overview tab** (`OverviewTabContent`):
  - Full-Face Landmark Map card — legend (5 colored regions) + `LandmarkFaceCanvas` (17 dots on face oval)
  - Feature Analysis 3×2 grid — `FeatureOverviewCard` per feature, tap switches to that tab
  - `SymmetryScoreCard` — animated ring (600ms) + 4-segment scale bar + verdict + description
  - Improvement Priority list — 5 items, rank-styled badges (rose/muted), area + action text
  - Expert Tip card — face-shape keyed (`EXPERT_TIPS` map, 7 shapes)
- **Individual feature tabs** (`FeatureTabContent`):
  - `FeatureIllustration` Canvas (100dp) per tab (almond eyes, arched brows, bridge+nostrils, cupid's bow lips, jaw contour, cheekbone lines)
  - Type label + feature label + `StatusBadge` (green=positive, blue=neutral)
  - Full description card
  - Tips card (custom `tipsLabel` e.g. "Contour Tips" for nose)
  - Related feature card — next tab in sequence, tappable
- **Info bottom sheets** — Landmark info + Improvement Priority info via `ModalBottomSheet`
- **Share card** — `generateFeatureShareBitmap()` 1080×1920px + `shareFeatureCard()` suspend function

**Data source:** `FaceAnalysis` (from `ResultViewModel`). Symmetry score = `min(95, glowUpScore + 7)`. Jawline/cheekbones labels derived from `faceShape`.

**Key helpers:** `eyeDetails()`, `browDetails()`, `noseDetails()`, `lipDetails()`, `jawlineDetails()`, `cheekbonesDetails()`, `buildFeatureDetails()`, `symmetryVerdict()`, `symmetryScaleLevel()`, `nextTab()`, `statusIsPositive()`, `String.toFeatureLabel()`

**Screen signature (unchanged):** `internal fun FeatureAnalysisScreen(analysis: FaceAnalysis? = null, onBack: () -> Unit = {})`

### Files Changed
- `FeatureAnalysisScreen.kt` — full rewrite (~1115 lines)

### Pending at End
- `GlowUpResultScreen` logic incomplete (PRD not yet applied)
- Profile sub-screens not yet built
- Home feature tiles still route to PlaceholderScreen

---

## Session 5 — Glow-Up Result Sub-Screen

**Date:** 2026-05-19

### Summary
`GlowUpResultScreen` fully rewritten per Glow-Up PRD. All six layout sections implemented with complete logic, info sheets, illustrations, sticky bar, and share card generation.

### Completed

#### GlowUpResultScreen (`GlowUpResultScreen.kt` — full rewrite)

**1. Before / After Comparison Card**
- Draggable vertical divider via `detectHorizontalDragGestures` + `Canvas` overlay (replaces buggy padding approach)
- Canvas draws white divider line + circle handle + left/right arrow indicators at exact `sliderFraction` position
- 4 after-image states: `PENDING` → shimmer, `GENERATING` → animated gradient + spinner, `COMPLETE` → AsyncImage revealed by slider, `FAILED` → grey placeholder + Retry button
- "Before" / "After (AI Glow-Up)" pill badges
- Info icon (ⓘ) → `ModalBottomSheet` with PRD copy

**2. Glow-Up Score Card**
- Score ring animated 0 → score in 600ms
- Verdict label + body text
- Score delta badge: green (`+n vs last scan`) / red (`-n`) / neutral (`Same as last scan`) — hidden when `scoreDelta == null`

**3. Improvement Areas Card**
- Horizontal `LazyRow` of `ImprovementAreaCard` items
- Each card: `AreaIllustration` (44dp Canvas) + area name + impact badge (color-coded by level) + score potential
- Tapping a card: calls `onAreaSelect(area)` + `listState.animateScrollToItem(stepGuideItemIndex)` to auto-scroll to step guide
- Active card: highlighted rose border
- Info icon (ⓘ) → `ModalBottomSheet`

**4. Actionable Step Guide Card**
- `ScrollableTabRow` chip row of all improvement area names; active = filled
- `AnimatedContent` with `fadeIn/fadeOut(200ms)` cross-fade on tab change
- Step guide content: `AreaIllustration` (64dp inside 100dp circle background) + goal text + recommendations list (each prefixed with `CheckCircle` icon)

**5. Progress Tracker Card** (shown only for 2+ scans)
- Canvas line chart: Y-axis gridlines at 25/50/75/100, connecting line, filled dots for history, open circle for latest
- Projected next point as dashed line + open circle (uses average delta, capped ±20, not just last delta)
- X-axis date labels (`MMM d` format)
- Stats row: "Total Scans: n" + "Avg: +n.n/scan"
- Info icon (ⓘ) → `ModalBottomSheet`

**6. Share Preview Card**
- Selfie circle thumbnail (52dp) + mini score badge overlay
- Score number, delta badge, verdict label, "Generated by Lumi" caption
- Mini glow-up image preview (56dp)
- Tapping the whole row triggers share

**Sticky Bottom Bar (`StickyShareBar`)**
- Dark fill when `glowUpImageStatus == COMPLETE`, grey when pending/generating
- "Generate & Share Card" label when ready; "Share in Progress — image generating" when not
- Loading spinner + "Generating…" during bitmap generation
- Outside `LazyColumn` — fixed at screen bottom via `Box + Modifier.align(Alignment.BottomCenter)`

**`AreaIllustration` Canvas composable**
- 7 area keys: `brows` (two curved arches), `skin` (face oval + sparkle dots), `eye` (two almond shapes + pupils), `lips` (cupid's bow with fill), `hair` (5 flowing strand curves), `jawline` (lower face contour + chin dot), `contour` (face oval + two diagonal cheekbone lines)
- Default: 8-pointed star shape

**Share card generation**
- `generateGlowUpShareBitmap(uiState)` — 1080×1080px bitmap with score, verdict, delta, footer
- `shareGlowUpCard(context, uiState)` — suspend function; writes to cache, creates `FileProvider` URI, triggers `ACTION_SEND`
- Wired to top-bar share icon, sticky bar, and share preview card tap

**State management**
- `rememberLazyListState()` for scroll control
- 3 info sheet states (`showBeforeAfterInfo`, `showImprovementInfo`, `showProgressInfo`)
- Local `isGeneratingShare` state (separate from ViewModel's field)
- `stepGuideItemIndex` computed as 4 (areas present) or 3 (no areas) to account for conditional rendering

**ViewModel / data (unchanged)**
- `GlowUpViewModel.kt` — already complete with `load()`, `selectArea()`, `retryImageGeneration()`, `FALLBACK_IMPROVEMENT_AREAS`, `FALLBACK_STEP_GUIDES`
- Data source: `GlowUpEntity` (Room) observed as Flow; fallback data used when Gemini data absent

### Files Changed
- `GlowUpResultScreen.kt` — full rewrite (~700 lines)

### Pending / Remaining Work
| Item | Status |
|---|---|
| `GlowUpResultScreen` | ✅ Complete |
| `ColorAnalysisScreen` | ✅ Complete |
| `FeatureAnalysisScreen` | ✅ Complete |
| `ResultScreen` | ✅ Complete |
| `ScanScreen` | ✅ Complete |
| `HomeScreen` | ✅ Complete |
| `ProfileScreen` | ✅ Complete |
| `EditProfileScreen` | ❌ Not built — PRD exists (`profile-edit_profile_PRD.md`) |
| `SavedPalettesScreen` | ❌ Not built — PRD exists (`profile-saved_palettes_PRD.md`) |
| `ScanHistoryScreen` | ❌ Not built — PRD exists (`profile-scan_history_PRD.md`) |
| Home → Color Analysis tile | ❌ Still routes to PlaceholderScreen |
| Home → Glow-Up tile | ❌ Still routes to PlaceholderScreen |
| Home → Makeup tile | ❌ Still routes to PlaceholderScreen |
| ColorAnalysis save button | ❌ Visual only — no Room entity |
| NotificationsScreen | ❌ Empty state only — no real data |

---

## Session 6 — Color Analysis & Feature Detail Data Layer

**Date:** 2026-05-19

### Summary
Implemented the complete data layer, ViewModels, and screen wiring for `ColorAnalysisScreen` and `FeatureAnalysisScreen`. Both screens now load from Room and merge AI data over static fallbacks. `GeminiService` extended to request and parse color analysis + feature detail fields. All new data persisted on every scan.

### Completed

#### Room — new entities + migration
- `ColorAnalysisEntity` (`color_analysis` table) — stores season, palette, avoid colors, clothing/hair/makeup recs, save flag
- `ColorAnalysisDao` — `observeByFaceAnalysisId`, `getByFaceAnalysisId`, `observeSaved`, `upsert`, `updateSaved`
- `FeatureDetailEntity` (`feature_detail` table) — stores symmetry score, improvement priority JSON
- `FeatureDetailDao` — `observeByFaceAnalysisId`, `getByFaceAnalysisId`, `upsert`
- `MIGRATION_8_9` (single combined migration) adds both tables; `LumiDatabase` bumped to version 9

#### Repositories
- `ColorAnalysisRepository` — wraps dao + face analysis lookup
- `FeatureDetailRepository` — wraps dao + face analysis lookup

#### ViewModels
- `ColorAnalysisViewModel(application)` — loads `FaceAnalysis` for skinTone/undertone + observes `ColorAnalysisEntity` Flow; `toggleSavePalette()` persists save state
- `FeatureDetailViewModel(application)` — loads `FaceAnalysis` for feature data + observes `FeatureDetailEntity` Flow; defaults symmetry score from `glowUpScore + 7` when AI data absent

#### GeminiService
- `ANALYSIS_PROMPT` extended with `color_season`, `personal_palette`, `avoid_colors`, `clothing_recs`, `hair_color_recs`, `makeup_palette` (lip/eye colors), `feature_detail` (symmetry_score, improvement_priority) fields
- `GeminiFaceResult` data class gained 9 new fields (with defaults so fallback builds compile)
- `parseAnalysisResponse()` parses all new fields; makeup_palette and feature_detail parsed from nested objects

#### ScanViewModel
- Persists `ColorAnalysisEntity` (step 4b) and `FeatureDetailEntity` (step 4c) on every scan, after `GlowUpEntity`

#### Screen wiring
- `ColorAnalysisScreen` — signature changed from `(skinTone, undertone)` → `(faceAnalysisId: Long)`; observes `ColorAnalysisViewModel`; AI palette/colors merged over static fallbacks via `parseColorChipsFromJson()`; save button persists to Room
- `FeatureAnalysisScreen` — signature changed from `(analysis: FaceAnalysis?)` → `(faceAnalysisId: Long)`; observes `FeatureDetailViewModel`; improvement priority replaced with AI data (falls back to `IMPROVEMENT_LIST`); symmetry score from ViewModel
- `ResultScreen` — call sites updated to pass `faceAnalysisId` to both sub-screens

### Files Changed
- `ColorAnalysisEntity.kt` — new
- `ColorAnalysisDao.kt` — new
- `FeatureDetailEntity.kt` — new
- `FeatureDetailDao.kt` — new
- `ColorAnalysisRepository.kt` — new
- `FeatureDetailRepository.kt` — new
- `ColorAnalysisViewModel.kt` — new
- `FeatureDetailViewModel.kt` — new
- `LumiDatabase.kt` — version 9, two new entities/DAOs, `MIGRATION_8_9`
- `GeminiService.kt` — extended prompt + parser + `GeminiFaceResult`
- `ScanViewModel.kt` — persists 2 new entities post-scan
- `ColorAnalysisScreen.kt` — ViewModel wired, new signature, save toggle live
- `FeatureAnalysisScreen.kt` — ViewModel wired, new signature, improvement list live
- `ResultScreen.kt` — call sites updated

### Pending at End
| Item | Status |
|---|---|
| `GlowUpResultScreen` | ✅ Complete |
| `ColorAnalysisScreen` | ✅ Complete (UI + data layer) |
| `FeatureAnalysisScreen` | ✅ Complete (UI + data layer) |
| `ResultScreen` | ✅ Complete |
| `ScanScreen` | ✅ Complete |
| `HomeScreen` | ✅ Complete |
| `ProfileScreen` | ✅ Complete |
| `EditProfileScreen` | ❌ Not built — PRD exists |
| `SavedPalettesScreen` | ❌ Not built — PRD exists |
| `ScanHistoryScreen` | ❌ Not built — PRD exists |
| Home → Color Analysis / Glow-Up / Makeup tiles | ❌ Still route to PlaceholderScreen |

---

## Session 7 — Home Screen Wiring + Trending Now Removal

**Date:** 2026-05-19

### Summary
Wired Color Analysis, Glow-Up, and Feature Analysis tiles to their real screens. Replaced the Makeup tile with Feature Analysis. Removed the Trending Now section entirely. Added a shared "No scan yet" dialog for when tiles are tapped without an existing scan.

### Completed

#### HomeScreen.kt
- Makeup tile replaced: `key="features"`, `Icons.Outlined.Face`, title "Feature Analysis", subtitle "In-depth analysis of your facial features" — same iconTint/iconBg, 2×2 grid unchanged
- `onExploreLooksClick` parameter removed from `HomeScreen` and `HomeContent` signatures and all call sites
- `TrendingSection(...)` call and its trailing `Spacer(8dp)` removed from `HomeContent` body — composable file not deleted
- Removed `import androidx.compose.material.icons.outlined.Brush` (now unused)

#### MainActivity.kt
- `AppScreen` enum: added `ColorAnalysis`, `GlowUp`, `FeatureAnalysis`
- State: `featureFaceAnalysisId: Long` (shared ID for all three feature screens from home)
- State: `showNoScanDialog: Boolean` — single shared dialog for all three tiles
- No Scan dialog: title "No scan yet", "Scan Now" → ScanScreen, "Cancel" → dismiss
- `showBottomBar` exclusion set extended with all three new screens (detail screens hide nav bar)
- `onFeatureTileClick` routing: `"color"` / `"glowup"` / `"features"` check `lastScan?.id`; navigate if present, show dialog if null; `"style"` unchanged
- `onExploreLooksClick` removed from HomeScreen call site
- `when` block: three new cases render `ColorAnalysisScreen`, `GlowUpResultScreen`, `FeatureAnalysisScreen` with `faceAnalysisId = featureFaceAnalysisId`, `onBack = { screen = AppScreen.Main }`
- Added imports: `AlertDialog`, `TextButton`, `ColorAnalysisScreen`, `FeatureAnalysisScreen`, `GlowUpResultScreen`

### Files Changed
- `HomeScreen.kt`
- `MainActivity.kt`

### Pending at End
| Item | Status |
|---|---|
| All result sub-screens | ✅ Complete + wired from Home |
| `EditProfileScreen` | ❌ Not built — PRD exists |
| `SavedPalettesScreen` | ❌ Not built — PRD exists |
| `ScanHistoryScreen` | ❌ Not built — PRD exists |
| Home → Style tile | Still "Coming Soon" (expected) |

---

## Session 8 — Profile Sub-Screens Built

**Date:** 2026-05-19

### Summary
Built all three Profile sub-screens (EditProfileScreen, SavedPalettesScreen, ScanHistoryScreen) from PRD + mockup references. ViewModels created, screens wired into MainActivity, ProfileScreen callbacks updated.

### Completed

#### EditProfileScreen (`EditProfileScreen.kt` — new)
- Full-screen (not bottom sheet) with top bar showing back chevron + "Save" text button
- Circular 96dp avatar: shows `pendingPhotoUri` → `photoUrl` → initials fallback → icon
- Rose camera-icon badge overlaid on avatar → opens photo picker bottom sheet (Take Photo / Choose from Gallery)
- `OutlinedTextField` for Display Name: live char count (`n / 40`), error state at limit, error label on empty save attempt
- Email field read-only with lock icon
- Change Password row (hidden for Google auth users) → password reset email via Firebase pattern
- Google Connected row (shown when `authType == "google"`) — display-only "G" badge
- "Discard Changes" rose text link — only shown when `hasChanges` is true
- Back + Discard both show confirmation `AlertDialog` ("Discard changes?" / "Keep Editing" / "Discard") when changes exist
- `imePadding()` + `verticalScroll` so keyboard never covers active field
- Gallery picker: `rememberLauncherForActivityResult(PickVisualMedia)`

#### EditProfileViewModel (`EditProfileViewModel.kt` — new)
- `EditProfileUiState(isLoading, displayName, displayNameDraft, displayNameError, email, photoUrl, pendingPhotoUri, isGoogleConnected, isSaving, hasChanges)`
- Observes `UserProfileDao.observe()` Flow via `collectLatest`
- `updateDisplayNameDraft()`, `setPendingPhotoUri()`, `save(onSuccess)` — writes to Room on save

#### SavedPalettesScreen (`SavedPalettesScreen.kt` — new)
- Top bar with back chevron + "Saved Palettes" title
- Empty state: rose palette icon + "No palettes saved yet" copy + "Start a Color Analysis scan" outlined button
- `LazyColumn` of palette cards with section header "Your Saved Palettes"
- Each card: season name (bold) + attributes chip (rose pill: "Cool • Soft • Light") + updated date + 5 × 22dp colored circular swatches + chevron
- Swipe left with `SwipeToDismissBox` → red delete background revealed → triggers confirmation `ModalBottomSheet`
- Delete bottom sheet: icon + "Delete this palette?" + season name + Cancel / Delete buttons
- Delete: calls `colorAnalysisDao.updateSaved(id, false)` (toggles save flag) — snackbar with "Undo" 5s window
- Undo: re-sets `isSaved = true` via `colorAnalysisDao.updateSaved(id, true)`

#### SavedPalettesViewModel (`SavedPalettesViewModel.kt` — new)
- Observes `colorAnalysisDao.observeSaved()` — live list of saved palettes
- Maps `ColorAnalysisEntity` → `PaletteSummary(faceAnalysisId, season, attributes, updatedAt, swatchHexes)`
- `seasonAttributes()` helper maps season string → "Cool • Soft • Light" style label
- `parseHexes()` parses `personalPaletteJson` for swatch colors

#### ScanHistoryScreen (`ScanHistoryScreen.kt` — new)
- Top bar with back chevron + "Scan History" title
- Empty state: camera icon + "No scans yet" + "Start a Scan" button
- Progress chart card (hidden when < 2 scans): `Canvas` line chart with cubic bezier connecting line, filled dots, gridlines at 25/50/75/100, X-axis date labels, stats row (Total Scans + Avg improvement)
- `LazyRow` filter chips (All / Face + Skin / Color Analysis / Glow-Up) using `FilterChip` with rose selected state
- `LazyColumn` scan row cards, each with: circular 52dp thumbnail (AsyncImage or camera icon placeholder), score badge overlay, scan type label, date right-aligned, glow score text, mini progress bar, potential label
- Swipe left `SwipeToDismissBox` → red delete background → confirmation `ModalBottomSheet`
- Delete: `faceAnalysisDao.deleteById(id)` → snackbar with "Undo" 5s window
- Undo: `faceAnalysisDao.restore(entity)`

#### ScanHistoryViewModel (`ScanHistoryViewModel.kt` — new)
- Observes `faceAnalysisDao.getRecent(limit = Int.MAX_VALUE)` Flow
- `ScanFilter` enum: ALL, FACE, COLOR, GLOWUP
- `setFilter()`, `requestDelete()`, `dismissDelete()`, `confirmDelete()`, `undoDelete()`

#### FaceAnalysisDao — new methods
- `deleteById(id: Long)` — single scan delete
- `restore(entity: FaceAnalysisEntity)` — undo support; uses `OnConflictStrategy.REPLACE`

#### ProfileScreen — wiring updated
- `onViewScanHistory`, `onViewSavedPalettes`, `onEditProfile` callbacks added to signature
- `ProfileHeaderCard` tap → `onEditProfile`
- `ScanHistorySection` "View all" → `onViewScanHistory`
- "Saved Palettes" `SavedContentCard` "View all" → `onViewSavedPalettes`

#### MainActivity — wiring updated
- Imports: `EditProfileScreen`, `SavedPalettesScreen`, `ScanHistoryScreen`
- `AppScreen` enum: added `EditProfile`, `ScanHistory`, `SavedPalettes`
- `showBottomBar` exclusion extended with all three new screens
- `ProfileScreen` call site: 3 new lambdas routed to respective screens
- `when` block: 3 new cases with correct `onBack = { screen = AppScreen.Profile }` routing
- `SavedPalettes` → `onOpenColorAnalysis`: sets `featureFaceAnalysisId` + navigates to `AppScreen.ColorAnalysis`

### Files Changed
- `EditProfileScreen.kt` — new (~230 lines)
- `EditProfileViewModel.kt` — new
- `SavedPalettesScreen.kt` — new (~310 lines)
- `SavedPalettesViewModel.kt` — new
- `ScanHistoryScreen.kt` — new (~530 lines)
- `ScanHistoryViewModel.kt` — new
- `FaceAnalysisDao.kt` — `deleteById` + `restore` added
- `ProfileScreen.kt` — 3 new callbacks wired
- `MainActivity.kt` — 3 new AppScreen entries + routing

### Pending at End
| Item | Status |
|---|---|
| All result sub-screens | ✅ Complete + wired from Home |
| `EditProfileScreen` | ✅ Complete |
| `SavedPalettesScreen` | ✅ Complete |
| `ScanHistoryScreen` | ✅ Complete |
| `NotificationsScreen` | Real notification content not built (shell only) |
| Home → Style tile | Still "Coming Soon" (expected) |

---

## Session 9 — Profile Sub-Screen Logic Pass

**Date:** 2026-05-19

### Summary
Applied full PRD business logic to all three profile sub-screens. Fixed two undo race conditions, rewrote ScanHistoryViewModel with a proper multi-flow `combine()` pattern, added cascade delete, projected score chart point, and empty-filter state.

### Completed

#### EditProfileViewModel + EditProfileScreen (rewrites)
- `isEmailAuth` field drives Change Password row visibility (`authType != "google"`)
- `validateAndSetPhoto(uri)` — `BitmapFactory.Options(inJustDecodeBounds=true)` dimension check; rejects photos < 100×100; emits snackbar event on failure
- `sendPasswordReset()` — Firebase stub; emits "Password reset email sent to {email}." snackbar
- `SharedFlow<String>` events bus — `LaunchedEffect(Unit) { viewModel.events.collect { snackbarHostState.showSnackbar(it) } }`
- Camera: `TakePicture` launcher + `FileProvider` temp file in `cacheDir`
- Gallery: calls `viewModel.validateAndSetPhoto(uri)` (not raw setter)
- `BackHandler` intercepts system back gesture when `hasChanges`
- Discard dialog on back/save cancel when dirty

#### ColorAnalysisDao + GlowUpDao + FeatureDetailDao — new methods
- `ColorAnalysisDao.observeAllFaceAnalysisIds(): Flow<List<Long>>`
- `ColorAnalysisDao.deleteByFaceAnalysisId(id: Long)`
- `GlowUpDao.observeAllFaceAnalysisIds(): Flow<List<Long>>`
- `GlowUpDao.deleteByFaceAnalysisId(faceAnalysisId: Long)`
- `FeatureDetailDao.deleteByFaceAnalysisId(id: Long)`

#### SavedPalettesViewModel — undo race fix
- `private var undoJob: Job?` tracks the 5-second timer coroutine
- `confirmDelete()` assigns `undoJob = launch { delay(5_000); ... }`
- `undoDelete()` calls `undoJob?.cancel(); undoJob = null` before restoring

#### ScanHistoryViewModel — full rewrite
- Four-flow `combine()`: `faceAnalysisDao.getRecent(Int.MAX_VALUE)`, `colorAnalysisDao.observeAllFaceAnalysisIds()`, `glowUpDao.observeAllFaceAnalysisIds()`, `activeFilter: MutableStateFlow<ScanFilter>`
- Filter logic fixed: FACE = `glowUpScore > 0`, COLOR = `id in colorIdSet`, GLOWUP = `id in glowIdSet`
- Projected score: `avgDelta = (last - first) / (N-1)`; `projected = min(last + avgDelta, 100)`; shown only when `projected > last && projected < 100`
- Projected date: `last.timestamp + avgInterval`
- Cascade delete: `colorAnalysisDao`, `featureDetailDao`, `glowUpDao` all deleted before `faceAnalysisDao.deleteById(id)`
- Undo race fix: `undoJob?.cancel()` in `undoDelete()`

#### ScanHistoryScreen — chart + UX updates
- `ProgressChartCard` now accepts `projectedScore: Int?` and `projectedDate: Long?`
- When projected exists: x-mapping uses N slots (not N-1) so last real point is at (N-1)/N and projected at chartW
- Projected: dashed line (`PathEffect.dashPathEffect(floatArrayOf(8f, 5f), 0f)`) + open circle (Stroke style) at 55% opacity
- Projected date label appended to X-axis row at 55% opacity
- "Projected next: n" stat line below chart stats
- Empty-filter inline message: "No {filter.label} scans yet." when `filteredScans.isEmpty() && activeFilter != ALL`

### Files Changed
- `EditProfileViewModel.kt` — full rewrite
- `EditProfileScreen.kt` — full rewrite
- `ColorAnalysisDao.kt` — 2 new methods
- `GlowUpDao.kt` — 2 new methods
- `FeatureDetailDao.kt` — 1 new method
- `SavedPalettesViewModel.kt` — undo race fix
- `ScanHistoryViewModel.kt` — full rewrite
- `ScanHistoryScreen.kt` — projected chart point + empty-filter state

### Pending at End
| Item | Status |
|---|---|
| All result sub-screens | ✅ Complete + wired |
| `EditProfileScreen` | ✅ Complete with full PRD logic |
| `SavedPalettesScreen` | ✅ Complete with undo race fixed |
| `ScanHistoryScreen` | ✅ Complete with combine() filter, cascade delete, projected chart |
| `NotificationsScreen` | Real notification content not built (shell only) |
| Home → Style tile | Still "Coming Soon" (expected) |

---

## Session 10 — Data Correctness Fixes (Fix 1/2/3)

**Date:** 2026-05-19

### Summary
Three targeted data-correctness fixes: verified/hardened ColorAnalysisEntity persistence from scan, fixed symmetry score reading Gemini data instead of a formula, and replaced the password-reset stub with a full local ChangePasswordScreen.

### Fix 1 — ColorAnalysisEntity scan persistence

**Audit result:** ScanViewModel already persisted both ColorAnalysisEntity and FeatureDetailEntity correctly in steps 4b/4c. No behavioural change needed.

**Changes applied:**
- `ColorAnalysisEntity` — added `savedAt: Long = 0L` field
- `ColorAnalysisDao.updateSaved()` — now also sets `savedAt = :savedAt` so Saved Palettes can show when a palette was saved (not just when it was scanned)
- `ColorAnalysisRepository.toggleSaved()` — passes `System.currentTimeMillis()` when saving, `0L` when unsaving
- `SavedPalettesViewModel.confirmDelete()` — passes `savedAt = 0L`; `undoDelete()` passes `System.currentTimeMillis()`
- `SavedPalettesViewModel.toPaletteSummary()` — `updatedAt = if (savedAt > 0L) savedAt else createdAt`
- `LumiDatabase` — bumped to version 10; `MIGRATION_9_10` adds `color_analysis.savedAt INTEGER NOT NULL DEFAULT 0` and `user_profile.passwordHash TEXT`
- `ScanViewModel` — added `Log.d` after steps 4b and 4c for Logcat verification

### Fix 2 — Symmetry score reads from FeatureDetailEntity

**Audit result:** Screen was already reading `uiState.symmetryScore` from ViewModel. Problem was GeminiService defaulting `symmetry_score` to 75 — indistinguishable from a real score of 75 returned by Gemini.

**Changes applied:**
- `GeminiService.parseAnalysisResponse()` — `symmetry_score` default changed from `75` to `-1`; coerceIn(50,100) only applied when value > 0
- `GeminiFaceResult.symmetryScore` default changed from `75` to `-1`
- `ScanViewModel.buildFallbackResult()` — explicitly sets `symmetryScore = (70..92).random()` so fallback mock data doesn't use the sentinel -1
- `FeatureDetailViewModel` — resolves score: entity `> 0` → Gemini data; otherwise → `min(95, glowUpScore + 7)` formula. Added `Log.d` showing which source was used

### Fix 3 — Password reset via local Room DB

**New files:**
- `ChangePasswordScreen.kt` — full-screen with back + "Save" top bar, conditional "Current Password" field (hidden when no password exists), info banner for first-time setup, New Password + Confirm fields, eye-toggle on all fields, snackbar events, loading states
- `ChangePasswordViewModel.kt` — validates all fields, checks current password against SHA-256 stored hash, saves new SHA-256 hash via `userProfileDao.updatePasswordHash()`; `hasExistingPassword` loaded from DB on init; `SharedFlow<String>` events bus

**Modified files:**
- `UserProfileEntity` — added `passwordHash: String? = null`
- `UserProfileDao` — added `updatePasswordHash(hash)` and `getPasswordHash(): String?`
- `LumiDatabase` — version 10, MIGRATION_9_10 includes `ALTER TABLE user_profile ADD COLUMN passwordHash TEXT`
- `EditProfileViewModel` — removed `sendPasswordReset()` stub
- `EditProfileScreen` — added `onChangePasswordClick: () -> Unit` parameter; Change Password row calls it
- `MainActivity` — `AppScreen.ChangePassword` added to enum + `showBottomBar` exclusion + `when` case; `EditProfileScreen` call site passes `onChangePasswordClick = { screen = AppScreen.ChangePassword }`

### Files Changed
- `ColorAnalysisEntity.kt` — `savedAt` field added
- `ColorAnalysisDao.kt` — `updateSaved()` now takes `savedAt: Long`
- `ColorAnalysisRepository.kt` — passes `savedAt` to `updateSaved()`
- `SavedPalettesViewModel.kt` — correct `savedAt` passed + `toPaletteSummary()` updated
- `UserProfileEntity.kt` — `passwordHash` field added
- `UserProfileDao.kt` — two new methods
- `LumiDatabase.kt` — version 10, MIGRATION_9_10
- `GeminiService.kt` — symmetry_score default -1 + GeminiFaceResult default -1
- `ScanViewModel.kt` — fallback has explicit `symmetryScore`, two `Log.d` statements
- `FeatureDetailViewModel.kt` — fixed fallback condition + `Log.d`
- `EditProfileViewModel.kt` — `sendPasswordReset()` removed
- `EditProfileScreen.kt` — `onChangePasswordClick` param + wired to row
- `MainActivity.kt` — `ChangePassword` screen entry wired
- `ChangePasswordViewModel.kt` — new
- `ChangePasswordScreen.kt` — new

### Pending at End
| Item | Status |
|---|---|
| ColorAnalysisEntity persisted from scan | ✅ Verified + hardened with savedAt |
| Symmetry score from Gemini (-1 sentinel) | ✅ Fixed |
| Password reset via local DB | ✅ ChangePasswordScreen built |
| `NotificationsScreen` real content | Not built (shell only) |
| Home → Style tile | Still "Coming Soon" (expected) |

---

*Log maintained by Claude Code. Updated at end of each session.*

---

## Session 11 — Notifications System

### Summary
Built end-to-end notifications: Room persistence, ViewModel, real list UI, and three trigger points (scan complete, palette saved, glow-up ready).

### Completed
- `NotificationEntity.kt` — new Room entity (`id`, `type`, `title`, `body`, `timestamp`, `isRead`)
- `NotificationDao.kt` — `insert`, `observeAll` (Flow), `markAllRead`, `deleteAll`
- `LumiDatabase.kt` — v11, `NotificationEntity` added, `notificationDao()` accessor, `MIGRATION_10_11` (CREATE TABLE notification)
- `NotificationsViewModel.kt` — new; observes notification flow as `StateFlow`; `init` marks all read + clears `unreadNotificationCount` in `AppStateEntity`
- `NotificationsScreen.kt` — replaced empty-state-only with real `LazyColumn`; unread items highlighted in rose tint; type-specific icons (scan/palette/glow-up); relative timestamp formatting; empty state retained when list is empty
- `ScanViewModel.kt` — inserts `scan_complete` notification + increments `unreadNotificationCount` at end of `runAnalysis()` (combined with existing `resultsUnviewed` upsert)
- `ColorAnalysisViewModel.kt` — inserts `palette_saved` notification + increments unread when `toggleSavePalette(saved=true)`
- `GlowUpImageWorker.kt` — inserts `glow_up_ready` notification + increments unread on `COMPLETE`

### Files Changed
- `NotificationEntity.kt` — new
- `NotificationDao.kt` — new
- `NotificationsViewModel.kt` — new
- `LumiDatabase.kt` — v10 → v11, entity + DAO + migration
- `NotificationsScreen.kt` — full rewrite (real list)
- `ScanViewModel.kt` — notification insert in `runAnalysis()`
- `ColorAnalysisViewModel.kt` — notification insert in `toggleSavePalette()`
- `GlowUpImageWorker.kt` — notification insert on COMPLETE

### Pending at End
| Item | Status |
|---|---|
| Notifications end-to-end | ✅ Built |
| Home → Style tile | Still "Coming Soon" (expected) |
