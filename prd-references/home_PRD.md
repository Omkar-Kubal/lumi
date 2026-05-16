# Home Screen — Product Requirements Document

**Document:** `home_PRD.md`
**Version:** 1.0
**Status:** Draft
**Parent PRD:** GlamUp MVP PRD v1.0
**Date:** May 2026

---

## 1. Purpose

The Home screen is the central hub of the Glam Up app. It surfaces the scan entry point, cached analysis results, feature navigation, editorial content, and subscription upsell. It is the first screen seen after onboarding and on every subsequent app open.

**Design principle:** Home is 100% AI-independent. Every element is derived from local data sources — Room DB, DataStore, static assets, or subscription state. Zero network calls are made on Home load.

---

## 2. Screen Goal

- Funnel users to the Scan screen (primary conversion action).
- Surface the last scan summary to reinforce app value on repeat opens.
- Provide clear entry points to all four core feature areas.
- Show editorial content (tip, trending) to create daily open habit.
- Drive FREE → PRO conversion via contextual upsell banner.

---

## 3. AI Dependency

**None.** All data on this screen is local.

| Section | Data Source | AI Required |
|---|---|---|
| Greeting + name | Room UserProfile | No |
| Greeting subtitle | Static string map (time-of-day) | No |
| Notification bell badge | Local unread count (DataStore) | No |
| Start scan CTA | Navigation only | No |
| Last scan summary — score | `glow_up_score` from Room FaceAnalysis | No (cached) |
| Last scan summary — verdict | Local score-to-string map | No |
| Last scan sub-metrics row | Face shape, skin tone, undertone, eye shape from Room FaceAnalysis | No (cached) |
| Feature tiles | Static navigation, subscription state from DataStore | No |
| Daily beauty tip | Static JSON asset, deterministic rotation | No |
| Trending Now carousel | Static/editorial JSON asset | No |
| Upsell banner | Subscription tier from DataStore | No |
| Bottom navigation | Local state | No |

---

## 4. Sections — Detailed Specification

### 4.1 Header

**Layout:** Horizontal row. Avatar left, greeting centre-left, notification bell right.

**Avatar**
- Source: `UserProfile.photoUrl` loaded via Coil, circular crop, 48dp diameter.
- Fallback: initials avatar (first letter of display name) if no photo URL.
- Tapping avatar navigates to Profile screen.

**Greeting text**
- Line 1: "Good morning," / "Good afternoon," / "Good evening," — derived from device local time.
  - Morning: 05:00–11:59
  - Afternoon: 12:00–16:59
  - Evening: 17:00–04:59
- Line 2: `UserProfile.displayName` in large bold type. Sparkle icon (✦) appended as a decorative static asset, not dynamic.
- Line 3 (subtitle): Static string, selected by time-of-day bracket:

| Time bracket | Subtitle |
|---|---|
| Morning | "Let's enhance your natural glow" |
| Afternoon | "Ready to perfect your look?" |
| Evening | "Time for your evening routine" |

- Subtitle does **not** change based on scan history or user data. Static copy only.

**Notification bell**
- Icon with badge dot when unread notification count > 0.
- Unread count stored in DataStore key `unread_notification_count` (Int).
- Count set server-side via Firebase Cloud Messaging data payload on push receive.
- Count reset to 0 when user taps bell (navigates to Notifications list screen — post-MVP placeholder in MVP).
- Badge hidden when count = 0.

---

### 4.2 Start Scan CTA

**Layout:** Full-width pill button, below header.

**Label:** "Start your scan" with scan icon (face-detect outline).

**Behaviour:**
- FREE user, free scan not yet used → navigate to Scan screen, `ScanType.FULL_ANALYSIS`.
- FREE user, free scan already used (`free_scan_used = true` in DataStore) → navigate to Paywall, `context = "scan"`.
- PRO user → navigate to Scan screen, `ScanType.FULL_ANALYSIS`.
- Guest user (no auth) → navigate to Auth screen with return destination = Scan.

**State:** Button always visible and always enabled. No disabled state on Home.

---

### 4.3 Last Scan Summary Card

**Visibility:** Shown only when `FaceAnalysis` records exist in Room for the current user. Hidden for new users with no scan history.

**Empty state (new user):** CTA card shown in place: "No scan yet — tap Start your scan to begin." Single action button linking to Scan screen.

**Card contents when scan exists:**

#### 4.3.1 Selfie thumbnail
- Source: `FaceAnalysis.imageUrl` loaded via Coil, fixed aspect ratio 1:1, rounded corners.
- Tapping thumbnail navigates to Results screen for that scan.

#### 4.3.2 Score
- Label: "Glow Score"
- Value: `FaceAnalysis.glowUpScore` displayed as `{score} / 100`.
- Font: Large (28sp), bold.

#### 4.3.3 Verdict line
- Derived locally from score. No AI call. Map:

| Score range | Verdict text |
|---|---|
| 90 – 100 | "Glowing! Your look is on point ✦" |
| 75 – 89 | "Great! Your glow is coming through ✦" |
| 60 – 74 | "Good foundation — let's build on this" |
| 40 – 59 | "Potential unlocked — your glow is growing" |
| 0 – 39 | "Let's work on your glow together" |

- Sparkle icon (✦) appended only for scores ≥ 75. Static asset, not dynamic.

#### 4.3.4 Sub-metrics row
Horizontal row of 4 metric chips. Each chip: icon + label + value. All values sourced from cached `FaceAnalysis` in Room. No AI call.

| Chip | Icon | Value source | Display value |
|---|---|---|---|
| Face Shape | face-outline icon | `FaceAnalysis.faceShape` | e.g. "Oval" |
| Skin Tone | circle-fill icon | `FaceAnalysis.skinTone` | e.g. "Medium" |
| Undertone | palette icon | `FaceAnalysis.undertone` | e.g. "Warm" |
| Eye Shape | eye-outline icon | `FaceAnalysis.eyeShape` | e.g. "Almond" |

- Values are enum labels mapped to display strings (e.g. `WARM → "Warm"`, `ALMOND → "Almond"`).
- Row is horizontally scrollable if labels overflow (unlikely but handled).

#### 4.3.5 "View results" link
- Top-right of card header.
- Navigates to Results screen for the most recent scan.

---

### 4.4 Feature Tiles Grid

**Layout:** 2×2 grid of equal-size cards.

| Position | Feature | Subtitle |
|---|---|---|
| Top-left | Color | "Find your perfect season & tones" |
| Top-right | Glow-Up | "Personalised skincare recommendations" |
| Bottom-left | Makeup | "Looks that enhance your features" |
| Bottom-right | Style | "Outfits that fit your vibe & body" |

**Tap behaviour:**
- PRO user → navigate to respective feature screen.
- FREE user → navigate to Paywall with `context = {feature_name}`.

**Lock indicator:**
- FREE user: lock icon overlaid on Glow-Up, Makeup, Style tiles (Color tile visible in partial form even for FREE — to be confirmed with design). All 4 locked in MVP if FREE.
- PRO user: no lock icon on any tile.

**Prerequisite check:**
- All feature tiles require at least one completed scan to be actionable.
- If PRO user has no scan: tile tap navigates to Scan screen with message "Complete a scan first to access [feature]."
- Toast shown, then navigation to Scan.

**Style tile — MVP note:**
Style (outfits) is a Tier 3 feature not yet built. In MVP, tapping Style shows a "Coming soon" bottom sheet regardless of subscription tier. Do not navigate to Paywall for Style.

---

### 4.5 Daily Beauty Tip Card

**Layout:** Single card with icon, tip text, bookmark icon, and pagination dots.

**Content source:** Static JSON file bundled in app assets (`/assets/beauty_tips.json`). Array of tip objects: `{id, text, category}`.

**Rotation logic:**
- Deterministic selection: `tipIndex = (userId.hashCode() + dayOfYear) % tips.size`
- `dayOfYear` = `LocalDate.now().dayOfYear` (device local date).
- Same user sees same tip all day. Different users may see different tips on the same day.
- No network call. No randomness (reproducible across app restarts).

**Pagination dots:**
- Show 5 dots. Active dot = current tip position in a 5-tip window.
- Swipe left/right to browse adjacent tips within the day's 5-tip window.
- Window: `tipIndex` to `tipIndex + 4` (mod total tips count).

**Bookmark action:**
- Tapping bookmark saves tip ID to Room `SavedTipEntity`.
- Bookmarked tips accessible from Profile screen under "Saved Tips" (post-MVP).
- In MVP: save to Room silently, show filled bookmark icon as confirmation. No navigation.
- Toggle: tapping filled bookmark removes from Room.

---

### 4.6 Trending Now Carousel

**Layout:** Horizontally scrollable card carousel. Pagination dots below.

**Content source:** Static JSON bundled in app assets (`/assets/trending_looks.json`). Array of look objects: `{id, tag, title, subtitle, imageUrl}`.

**Fields displayed per card:**
- Tag label (e.g. "Trending now") — static string, same for all cards in MVP.
- Title (e.g. "Glass Skin Look")
- Subtitle (e.g. "Dewy, clean & radiant ✦")
- Hero image (loaded via Coil from bundled asset path or CDN URL)
- "Explore looks" button

**"Explore looks" tap behaviour:**
- MVP: navigates to a static WebView or placeholder screen "More looks coming soon."
- Post-MVP: navigates to a curated looks feed.

**Update cadence:** JSON updated with app releases in MVP. No remote config or CMS in MVP.

---

### 4.7 Upsell Banner

**Visibility:** Shown only for FREE tier users. Hidden for PRO users.

**Layout:** Horizontal card at bottom of scrollable content, above bottom nav. Crown icon left, copy centre, Upgrade button right, dismiss (×) top-right.

**Copy:**
- Headline: "Unlock your full glow"
- Subtext: "Get advanced insights, saved scans, and personalised recommendations."

**Upgrade button:** Navigates to Paywall, `context = "home_banner"`.

**Dismiss behaviour:**
- Tapping × sets DataStore key `home_banner_dismissed_session = true`.
- Banner hidden for remainder of session (until app process restart).
- Banner re-appears on next cold start.
- Not permanently dismissible in MVP — re-shows every cold start for FREE users.

---

### 4.8 Bottom Navigation

Four tabs: Home, Scan, Results, Profile.

| Tab | Icon | Destination |
|---|---|---|
| Home | home-filled | HomeScreen (current) |
| Scan | scan-face-outline | ScanScreen |
| Results | bar-chart-outline | ResultsScreen (latest scan) or empty state |
| Profile | person-outline | ProfileScreen |

**Badge:** Results tab shows a badge dot if a scan has completed but the Results screen has not been viewed since. Badge cleared on Results screen entry. Badge state stored in DataStore key `results_unviewed` (Boolean).

**Scan tab shortcut:** Tapping Scan tab from Home is equivalent to tapping the Start Scan CTA — same gate logic applies (free scan check, auth check).

---

## 5. Screen States

| State | Condition | Behaviour |
|---|---|---|
| New user, no scan | `scanHistory.isEmpty()` | Hide last scan card. Show empty-state prompt card. Show upsell banner if FREE. |
| Returning FREE user, scan exists | `free_scan_used = true`, tier = FREE | Show last scan card. Show lock on feature tiles. Show upsell banner. |
| Returning PRO user | tier = PRO | Show last scan card. No locks. No upsell banner. |
| Guest user | No Firebase UID | Show Home with empty scan card. Scan tap → Auth screen. Feature tile tap → Auth screen. |
| Offline | No network | All sections load from Room/assets. No error shown. Subtle offline indicator optional (not required in MVP). |

---

## 6. Data Loading Strategy

All data loaded in `HomeViewModel` on screen entry via `init` block. No lazy loading on scroll.

```
HomeViewModel.init
  ├── loadUserProfile()         → Room UserProfileDao.getProfile(uid)
  ├── loadLastScan()            → Room FaceAnalysisDao.getLatest(uid)
  ├── loadSubscriptionTier()    → DataStore key: subscription_tier
  ├── loadNotificationCount()   → DataStore key: unread_notification_count
  ├── loadDailyTip()            → Assets beauty_tips.json + deterministic index
  ├── loadTrendingLooks()       → Assets trending_looks.json
  └── loadBannerDismissed()     → DataStore key: home_banner_dismissed_session
```

All flows combined into a single `HomeUiState` data class via `combine()`. UI observes one StateFlow. No multiple loading spinners — single loading state for the whole screen, resolves in < 100ms (all local).

---

## 7. HomeUiState Model

```kotlin
data class HomeUiState(
    val isLoading: Boolean = true,
    val userProfile: UserProfile? = null,
    val lastScan: FaceAnalysis? = null,
    val greetingSubtitle: String = "",
    val subscriptionTier: SubscriptionTier = SubscriptionTier.FREE,
    val unreadNotificationCount: Int = 0,
    val dailyTip: BeautyTip? = null,
    val trendingLooks: List<TrendingLook> = emptyList(),
    val showUpsellBanner: Boolean = false,
    val bannerDismissedForSession: Boolean = false
)
```

---

## 8. Navigation Map

| Trigger | Destination | Condition |
|---|---|---|
| Avatar tap | ProfileScreen | Always |
| Bell tap | NotificationsScreen (placeholder) | Always |
| Start Scan button | ScanScreen | PRO or free scan available |
| Start Scan button | PaywallScreen (`context=scan`) | FREE + free scan used |
| Start Scan button | AuthScreen | Guest user |
| View results link | ResultsScreen (latest scan id) | Scan exists |
| Feature tile tap | Respective feature screen | PRO + scan exists |
| Feature tile tap | PaywallScreen (`context=feature`) | FREE user |
| Feature tile tap | ScanScreen (with toast) | PRO + no scan yet |
| Style tile tap | Coming Soon bottom sheet | Always (MVP) |
| Explore looks button | Placeholder screen | Always (MVP) |
| Upgrade button (banner) | PaywallScreen (`context=home_banner`) | Always |
| Results tab | ResultsScreen | Always |
| Scan tab | ScanScreen or Paywall or Auth | Same logic as Start Scan CTA |

---

## 9. Acceptance Criteria

1. Home screen renders fully within 200ms on cold start (all local data sources).
2. Greeting subtitle updates correctly based on device local time across all three brackets.
3. Last scan card hidden when no scan exists; empty-state prompt shown in its place.
4. Glow Score verdict maps correctly to all five score ranges.
5. Sub-metrics row displays correct values from Room FaceAnalysis (not hardcoded).
6. Daily tip is deterministic: same user on same day always sees same tip after app restart.
7. Bookmark saves tip to Room; bookmark icon toggles state correctly.
8. Upsell banner hidden for PRO users; shown for FREE users on every cold start.
9. Banner dismiss hides banner for session; reappears on next cold start.
10. Style tile shows "Coming soon" bottom sheet for all users.
11. Feature tile lock icons shown for FREE users; absent for PRO users.
12. Notification badge shows when `unread_notification_count > 0`; clears on bell tap.
13. Results tab badge shows when `results_unviewed = true`; clears on Results screen entry.
14. No network calls made during Home screen load.
15. Offline mode: all sections load from cache with no crash and no error screen.

---

## 10. Out of Scope — Home Screen MVP

- AI-generated personalised greeting subtitle.
- AI-generated daily tip (Gemini-powered).
- Live trending content from a CMS or backend API.
- Skin health sub-metrics (Hydration / Texture / Pores / Spots) — requires new Gemini schema fields, deferred to post-MVP.
- Style feature full implementation — "Coming soon" placeholder in MVP.
- Notifications list screen — bell navigates to placeholder.
- Saved Tips screen — bookmarks saved to Room but not yet surfaced in UI beyond icon toggle.

---

*End of document — Home Screen PRD v1.0*
