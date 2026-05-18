# Profile Screen — Product Requirements Document

**Document:** `profile_PRD.md`
**Version:** 1.0
**Status:** Draft
**Parent PRD:** GlamUp MVP PRD v1.0
**Date:** May 2026

---

## 1. Purpose

The Profile screen is the user's personal hub — account identity, subscription status, scan history, saved content, personal details, and app settings. It is entirely local. Zero AI calls are made on this screen. All data is read from Room, DataStore, or RevenueCat cache.

---

## 2. Screen Goal

- Surface account identity and subscription status clearly.
- Give users access to their scan history and saved content.
- Allow editing of personal details that influence analysis personalisation.
- House notification preferences and account actions.
- Drive FREE → PRO conversion via locked content sections.

---

## 3. AI Dependency

**None.** Every element is local.

| Section | Source | AI? |
|---|---|---|
| User identity | Room `UserProfile` | No |
| Subscription status | RevenueCat `CustomerInfo` cached locally | No |
| Scan history | Room `ScanEntity` list | No |
| Saved routines | Room `RoutineEntity` list | No |
| Saved color palettes | Room `ColorAnalysisEntity` list | No |
| Personal details | Room `UserProfile` | No |
| Notification preferences | DataStore | No |
| Rate / Share App | Static URLs | No |
| Delete / Sign out | Firebase Auth + Room | No |

---

## 4. Entry Points

| Source | NavArg |
|---|---|
| Bottom nav — Profile tab | None |
| Home screen — avatar tap | None |

No NavArgs required. Screen always loads current authenticated user's data.

---

## 5. Layout — Section Order

Scrollable `LazyColumn`. No sticky bars. Top bar fixed.

```
┌─────────────────────────────────────┐
│        Profile              [gear]  │  ← Top bar (fixed)
├─────────────────────────────────────┤
│  Scrollable content:                │
│                                     │
│  1.  User identity row              │
│  2.  Subscription card              │
│  3.  Scan history card              │
│  4.  Saved content row              │
│      [Saved Routines] [Palettes]    │
│  5.  Personal details card          │
│  6.  Notification preferences card  │
│  7.  App actions card               │
│      (Rate App / Share App)         │
│  8.  Account actions card           │
│      (Delete Account / Sign Out)    │
│                                     │
├─────────────────────────────────────┤
│  [Home] [Scan] [Results] [Profile]  │  ← Bottom nav
└─────────────────────────────────────┘
```

---

## 6. Top Bar

**Centre:** "Profile" title.
**Right:** Settings gear icon.
- Tapping gear scrolls `LazyColumn` to Notification Preferences section (Section 11).
- No separate Settings screen. Gear is a scroll shortcut only.
- Implemented via `LazyListState.animateScrollToItem(index)` targeting the notification section index.

---

## 7. Section 1 — User Identity Row

**Layout:** Horizontal row. Avatar left, name + email centre, chevron right.

**Avatar:**
- Source: `UserProfile.photoUrl` via Coil, circular crop, 64dp.
- Fallback: initials avatar (first letter of `displayName`), generated locally as Canvas bitmap. No network call for fallback.

**Name + email:**
- Display name: `UserProfile.displayName`, large bold.
- Email: `UserProfile.email`, regular weight, muted.

**Chevron `>`:**
- Navigates to Edit Profile screen (see Section 7.1).

### 7.1 Edit Profile Screen

Separate screen, navigated to from chevron. Not a bottom sheet — full screen to accommodate keyboard.

**Fields:**
- Display name — text input, max 40 characters.
- Profile photo — tapping avatar opens `ActivityResultContracts.PickVisualMedia(ImageOnly)`. Selected image saved to local storage, URL updated in `UserProfile.photoUrl`.

**Save button:**
- Validates display name not empty.
- Updates Room `UserProfile` and Firebase Firestore `users/{uid}` document.
- Navigates back to Profile on success.
- Snackbar on failure: "Update failed — try again."

**Note:** Email is not editable in MVP. Shown as read-only in Edit Profile screen.

---

## 8. Section 2 — Subscription Card

**Layout:** Full-width card.

### 8.1 PRO user (active subscription)

**Contents:**
- Left: crown icon inside circular background.
- Centre:
  - Headline: "Premium Plan" + "Active" badge pill (right of headline).
  - Body: "You have full access to all features."
  - Renewal line: "Renews on {date}" — formatted from RevenueCat `CustomerInfo.entitlements["pro_access"].expirationDate`.
  - Date format: `MMMM dd, yyyy` (e.g. "June 18, 2025").
- Right: "Manage Billing ›" button (dark fill).
  - Tap: opens Google Play subscription management URL via `Intent.ACTION_VIEW`.
  - URL: `https://play.google.com/store/account/subscriptions`

### 8.2 FREE user

**Contents:**
- Left: crown icon (outlined, not filled).
- Centre:
  - Headline: "Free Plan"
  - Body: "Upgrade to unlock full analysis and all features."
- Right: "Upgrade Now" button (dark fill).
  - Tap: Paywall placeholder. `context = "profile_subscription"`.

### 8.3 Data loading

RevenueCat `CustomerInfo` fetched via `Purchases.getCustomerInfo()` on screen entry. Result cached — no blocking load. If fetch fails, use last known tier from DataStore. Show stale data silently rather than error.

---

## 9. Section 3 — Scan History Card

**Layout:** Full-width card. Header row + horizontal thumbnail strip.

**Header row:**
- Left: "Scan History" label.
- Right: "View all ›" link.
  - FREE user: shows last 3 scans inline. "View all" navigates to Scan History list screen (see Section 9.1).
  - PRO user: shows last 5 scans inline. "View all" same destination.

**Thumbnail strip:**

Per thumbnail:
- Square image card (Coil, from `ScanEntity.imageUrl`), rounded corners, fixed size 80×80dp.
- Date below image: formatted `MMM dd, yyyy`.
- Scan type label below date.

**Scan type label map:**

| ScanType enum | Display label |
|---|---|
| `FULL_ANALYSIS` | "Face + Skin" |
| `COLOR_ONLY` | "Color Analysis" |
| `GLOWUP_ONLY` | "Glow-Up" |

**Tap on thumbnail:** Navigates to Result screen with `faceAnalysisId` of that scan.

**FREE user — scan count gate:**
- Only last 3 scans shown in strip.
- If more than 3 exist: 4th slot replaced with a locked card showing lock icon + "View all with Pro".
- Tapping locked slot → Paywall placeholder.

**Empty state (no scans):**
- Strip replaced with single row: "No scans yet — start your first scan." with Scan CTA button.

### 9.1 Scan History List Screen

Full-screen list of all scans, reverse chronological.

**Each row:** Thumbnail (40×40dp circular) + scan type label + date + chevron. Tap → Result screen.

**FREE user:** Shows last 3 rows. Row 4+ replaced with "Unlock full history with Pro" banner. No pagination needed — 3 rows maximum for FREE.

**PRO user:** Full list, paginated (20 per page via Room `LIMIT/OFFSET`). Swipe to delete row — confirmation dialog — deletes `ScanEntity` and associated `FaceAnalysis` from Room.

---

## 10. Section 4 — Saved Content Row

**Layout:** Two equal-width cards side by side.

### 10.1 Saved Routines Card (left)

**Data source:** Room `RoutineEntity` list, ordered by `savedAt` descending.

**Header row:**
- Left: "Saved Routines" label.
- Right: "View all ›" in accent. Navigates to Saved Routines list (post-MVP placeholder — "Coming soon" bottom sheet in MVP).

**FREE user:**
- Card body: lock illustration + "Save and access your makeup routines with Pro." + "Upgrade" button.
- No routine rows shown.
- Upgrade → Paywall placeholder. `context = "saved_routines"`.

**PRO user:**
- Up to 2 routine rows shown inline.
- Per row: routine icon + routine name + "Updated {date}" subtext + chevron.
- Tap row → Makeup Routine screen for that routine (post-MVP placeholder).
- If no saved routines: "No routines saved yet. Complete a scan to get your routine."

**Routine name source:** `RoutineEntity.name` — set when user saves routine from Makeup Routine screen. If no custom name set, default to scan type + date (e.g. "Day Routine — May 12, 2025").

### 10.2 Saved Color Palettes Card (right)

**Data source:** Room `ColorAnalysisEntity` list, ordered by `updatedAt` descending.

**Header row:**
- Left: "Saved Color Palettes" label.
- Right: "View all ›". Same placeholder behaviour as routines.

**FREE user:**
- Card body: lock illustration + "Save your seasonal color palettes with Pro." + "Upgrade" button.
- No palette rows shown.

**PRO user:**
- Up to 2 palette rows shown inline.
- Per row:
  - Palette name (e.g. "Soft Autumn") bold.
  - "Updated {date}" subtext.
  - Swatch row: 5 small circular swatches from `ColorAnalysisEntity.personalPalette` (first 5 hex values).
  - Chevron.
- Tap row → Color Analysis screen for that palette (post-MVP placeholder).
- If no saved palettes: "No palettes saved yet. Complete a color scan to see your palette."

---

## 11. Section 5 — Personal Details Card

**Layout:** Full-width card. Header row + list of detail rows.

**Header row:**
- Left: "Personal Details" label.
- Right: "Edit ✎" link in accent.
  - Tapping switches card to edit mode (inline edit, not navigation).

### 11.1 Display mode (read-only)

Five rows, each: icon + label (left) + value (right) + chevron.

| Row | Icon | Label | Value source | Editable |
|---|---|---|---|---|
| Age | calendar icon | "Age" | `UserProfile.age` (Int) | Yes |
| Skin Type | droplet icon | "Skin Type" | `UserProfile.skinType` display string | Yes |
| Skin Tone | circle icon | "Skin Tone" | `UserProfile.skinTone` display string | Yes |
| Undertone | palette icon | "Undertone" | `UserProfile.undertone` display string | Yes |
| Location | pin icon | "Location" | `UserProfile.location` (String) | Yes |

**Default values (first open, before user sets):**
- Age: "—"
- Skin Type: "—"
- Skin Tone: populated from last scan `FaceAnalysis.skinTone` if available, else "—"
- Undertone: populated from last scan `FaceAnalysis.undertone` if available, else "—"
- Location: "—"

**Skin tone and undertone pre-population logic:**
```
On Profile screen load:
  if UserProfile.skinTone == null && lastScan != null:
      UserProfile.skinTone = lastScan.skinTone
      UserProfile.undertone = lastScan.undertone
      persist to Room
```
This happens silently once. User can override via Edit.

### 11.2 Edit mode

Tapping "Edit ✎" transforms card inline:

| Field | Input type |
|---|---|
| Age | `NumberTextField`, max 2 digits, range 10–99 |
| Skin Type | Dropdown / exposed dropdown menu: Oily, Dry, Combination, Normal, Sensitive |
| Skin Tone | Dropdown: Fair, Light, Medium, Tan, Deep |
| Undertone | Dropdown: Warm, Cool, Neutral |
| Location | `TextField`, free text, max 60 characters, optional |

**Save button** at bottom of card in edit mode:
- Validates age is within 10–99 if entered.
- Persists all changed fields to Room `UserProfile` and Firestore `users/{uid}`.
- Switches back to display mode on success.
- Snackbar on save failure: "Update failed — try again."

**Cancel link** next to Save: discards changes, switches back to display mode. No confirmation dialog needed (non-destructive).

**SkinType enum → display string map:**
```
OILY        → "Oily"
DRY         → "Dry"
COMBINATION → "Combination"
NORMAL      → "Normal"
SENSITIVE   → "Sensitive"
```

---

## 12. Section 6 — Notification Preferences Card

**Layout:** Full-width card. Header + 3 toggle rows.

**Header:** Bell icon + "Notification Preferences" label.

**Three toggle rows:**

| Row | Icon | Label | Subtext | DataStore key |
|---|---|---|---|---|
| Reminders | clock icon | "Reminders" | "Scan reminders, daily tips" | `notif_reminders` (Boolean, default true) |
| Promotions | tag icon | "Promotions" | "Offers, new features" | `notif_promotions` (Boolean, default false) |
| Updates | megaphone icon | "Updates" | "App updates, announcements" | `notif_updates` (Boolean, default true) |

**Toggle behaviour:**
- Toggle state change → immediately write to DataStore.
- WorkManager periodic tasks (scan reminders, daily tips) check `notif_reminders` before firing notification. If false, skip.
- Firebase Cloud Messaging topic subscription updated on toggle:
  - `notif_promotions` true → `FirebaseMessaging.subscribeToTopic("promotions")`
  - `notif_promotions` false → `FirebaseMessaging.unsubscribeFromTopic("promotions")`
  - Same for `notif_updates` → topic "updates".
  - Reminders are local only (WorkManager), no FCM topic.

**Android 13+ permission note:**
- If notification permission not granted: all toggles shown as disabled (greyed).
- Banner above toggles: "Enable notifications in Settings to receive updates."
- Banner tap: deep link to app notification settings via `Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)`.

---

## 13. Section 7 — App Actions Card

**Layout:** Full-width card. Two rows separated by a divider.

| Row | Icon | Label | Subtext | Action |
|---|---|---|---|---|
| Rate App | star icon | "Rate App" | "Tell us how we're doing" | Opens Play Store listing via `Intent.ACTION_VIEW` to `market://details?id={packageName}` |
| Share App | share icon | "Share App" | "Share with your friends" | `Intent.ACTION_SEND`, text + Play Store URL. Pre-filled message: "Check out Glam Up — the AI beauty app! [Play Store URL]" |

Chevron on right of each row. No confirmation dialogs for either action.

---

## 14. Section 8 — Account Actions Card

**Layout:** Full-width card. Two rows separated by a divider. Visually separated from Section 7 with extra vertical margin to signal destructive actions.

### 14.1 Delete Account row

- Icon: trash icon. Label: "Delete Account". Subtext: "Permanently delete your account and data".
- Chevron right.
- Tap → confirmation bottom sheet:
  - Title: "Delete your account?"
  - Body: "This will permanently delete your account, all scan history, and saved data. This cannot be undone."
  - Two buttons: "Cancel" (dismiss) / "Delete Account" (destructive, accent).
  - Tapping "Delete Account":
    1. Cancel active RevenueCat subscription check (inform user to cancel via Google Play separately — app cannot cancel on their behalf).
    2. Delete all Room entities for `uid` (UserProfile, ScanEntity, FaceAnalysis, RoutineEntity, ColorAnalysisEntity, SavedTipEntity).
    3. Delete Firestore `users/{uid}` document.
    4. `FirebaseAuth.currentUser?.delete()`.
    5. Clear all DataStore keys.
    6. Navigate to Onboarding screen (clear back stack).
  - On failure (Firebase delete fails — requires recent login):
    - Show bottom sheet: "Please sign out and sign back in before deleting your account." (Firebase requires recent authentication for account deletion.)
    - Dismiss. User must re-authenticate then retry.

### 14.2 Sign Out row

- Icon: sign-out icon. Label: "Sign Out". Subtext: "Sign out from your account".
- Chevron right.
- Tap → confirmation dialog:
  - Title: "Sign out?"
  - Body: "You'll need to sign in again to access your results."
  - Buttons: "Cancel" / "Sign Out".
  - On confirm:
    1. `FirebaseAuth.signOut()`.
    2. Clear session DataStore keys (`free_scan_used`, `home_banner_dismissed_session`, `results_unviewed`, `scan_count_today`, `scan_count_date`).
    3. Do NOT clear Room data — preserved for when user signs back in.
    4. Navigate to Auth screen (Onboarding step 6). Clear back stack.

---

## 15. ProfileViewModel State

```kotlin
data class ProfileUiState(
    val isLoading: Boolean = true,
    val userProfile: UserProfile? = null,
    val subscriptionTier: SubscriptionTier = SubscriptionTier.FREE,
    val subscriptionRenewalDate: String? = null,
    val recentScans: List<ScanSummary> = emptyList(),
    val savedRoutines: List<RoutineEntity> = emptyList(),
    val savedPalettes: List<ColorAnalysisEntity> = emptyList(),
    val notifReminders: Boolean = true,
    val notifPromotions: Boolean = false,
    val notifUpdates: Boolean = true,
    val notificationPermissionGranted: Boolean = true,
    val isEditingPersonalDetails: Boolean = false,
    val personalDetailsDraft: PersonalDetailsDraft? = null,
    val error: ProfileError? = null
)

data class ScanSummary(
    val faceAnalysisId: String,
    val imageUrl: String,
    val scanType: ScanType,
    val createdAt: Long
)

data class PersonalDetailsDraft(
    val age: String = "",
    val skinType: SkinType? = null,
    val skinTone: SkinTone? = null,
    val undertone: Undertone? = null,
    val location: String = ""
)

sealed class ProfileError {
    object UpdateFailed : ProfileError()
    object DeleteFailed : ProfileError()
    object SignOutFailed : ProfileError()
}
```

**Data loading on entry:**
```
ProfileViewModel.init
    │
    ├── UserProfileDao.getProfile(uid)              → userProfile
    ├── ScanEntityDao.getRecent(uid, limit=5)        → recentScans
    ├── RoutineEntityDao.getByUser(uid, limit=2)     → savedRoutines
    ├── ColorAnalysisDao.getByUser(uid, limit=2)     → savedPalettes
    ├── Purchases.getCustomerInfo()                  → subscriptionTier + renewalDate
    ├── DataStore notif keys                         → notif preferences
    ├── checkNotificationPermission()                → notificationPermissionGranted
    └── skin tone pre-population check (Section 11.1)
```

All combined into single `StateFlow<ProfileUiState>` via `combine()`.

---

## 16. UserProfile Domain Model — Additions

Fields added vs master PRD to support this screen:

```kotlin
data class UserProfile(
    // existing fields
    val uid: String,
    val email: String,
    val displayName: String,
    val photoUrl: String?,
    val ageGroup: AgeGroup,           // from onboarding — kept for backward compat
    val skinConcerns: List<SkinConcern>,
    val goals: List<BeautyGoal>,
    val createdAt: Long,
    val subscriptionTier: SubscriptionTier,

    // new fields for Profile screen
    val age: Int? = null,             // exact age, replaces ageGroup display
    val skinType: SkinType? = null,   // OILY, DRY, COMBINATION, NORMAL, SENSITIVE
    val skinTone: SkinTone? = null,   // pre-populated from last scan
    val undertone: Undertone? = null, // pre-populated from last scan
    val location: String? = null      // free text, optional
)
```

Room `UserProfileEntity` updated with corresponding nullable columns. Firestore document updated with same fields. Migration script required for existing Room schema (Room auto-migration via `@AutoMigration` annotation).

---

## 17. Navigation Map

| Trigger | Destination | Condition |
|---|---|---|
| Settings gear | Scroll to Notification Preferences | Always |
| User row chevron | Edit Profile screen | Always |
| Subscription — Manage Billing | Google Play subscriptions URL | PRO user |
| Subscription — Upgrade Now | Paywall placeholder | FREE user |
| Scan thumbnail tap | Result screen (`faceAnalysisId`) | Always |
| Scan History — View all | Scan History list screen | Always |
| Scan History — locked slot | Paywall placeholder | FREE user |
| Saved Routines — View all | Coming soon bottom sheet | MVP |
| Saved Routines — row tap | Coming soon bottom sheet | MVP |
| Saved Routines — Upgrade | Paywall placeholder | FREE user |
| Saved Palettes — View all | Coming soon bottom sheet | MVP |
| Saved Palettes — row tap | Coming soon bottom sheet | MVP |
| Saved Palettes — Upgrade | Paywall placeholder | FREE user |
| Notification permission banner | App notification settings (system) | Always |
| Rate App | Play Store listing | Always |
| Share App | System share sheet | Always |
| Delete Account — confirm | Onboarding screen (clear back stack) | After deletion |
| Sign Out — confirm | Auth screen (clear back stack) | After sign out |
| Bottom nav — Home | Home screen | Always |
| Bottom nav — Scan | Scan screen | Always |
| Bottom nav — Results | Result screen | Always |

---

## 18. Acceptance Criteria

1. Screen loads within 200ms on cold navigation. Single `isLoading` state, no per-section loaders.
2. Avatar fallback (initials) renders correctly when `photoUrl` is null. No broken image state.
3. PRO subscription card shows renewal date formatted correctly from RevenueCat expiration date.
4. FREE subscription card shows Upgrade button navigating to Paywall placeholder.
5. Scan history shows last 3 thumbnails for FREE, last 5 for PRO.
6. FREE user scan history 4th slot shows locked card, not a crash or empty space.
7. Scan type label under thumbnail maps correctly for all 3 ScanType values.
8. Saved Routines and Saved Color Palettes sections show lock state for FREE users with no routine/palette rows visible.
9. Personal details pre-populated with last scan skin tone and undertone on first profile view if UserProfile values are null.
10. Edit mode renders all 5 fields as correct input types (number, dropdown, dropdown, dropdown, text).
11. Age validation rejects values outside 10–99. Rejects non-numeric input.
12. Save personal details persists to Room and Firestore. Card returns to display mode on success.
13. Cancel in edit mode discards all changes. No confirmation dialog.
14. Notification toggles disabled and permission banner shown when notification permission not granted (Android 13+).
15. Toggle state change writes to DataStore immediately. FCM topic subscription updated for Promotions and Updates toggles.
16. Delete Account confirmation bottom sheet shows before any deletion. Cancel dismisses without action.
17. Delete Account clears all Room data, Firestore doc, Firebase Auth, DataStore. Navigates to Onboarding.
18. Delete Account failure (requires recent auth) shows re-authentication prompt, no crash.
19. Sign Out preserves Room data. Navigates to Auth screen. Back stack cleared.
20. Settings gear scrolls to Notification Preferences section smoothly via `animateScrollToItem`.
21. Profile tab in bottom nav shows filled icon when on this screen.

---

## 19. Out of Scope — Profile Screen MVP

- Full Paywall screen — placeholder only.
- Saved Routines list screen — "Coming soon" bottom sheet.
- Saved Color Palettes list screen — "Coming soon" bottom sheet.
- Scan History delete (swipe to delete) — PRO feature, post-MVP.
- Two-factor authentication or password change.
- Profile photo crop/edit tool — pick and save only, no in-app cropping.
- Export data / download my data feature.
- Linked accounts (connect Instagram, etc.).
- Customer support chat — mailto link only in MVP.

---

*End of document — Profile Screen PRD v1.0*
