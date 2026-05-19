# Edit Profile Screen — Product Requirements Document

**Document:** `edit_profile_PRD.md`
**Version:** 1.0
**Status:** Draft
**Parent:** Profile Screen (`profile_PRD.md`)
**Date:** May 2026

---

## 1. Purpose

The Edit Profile screen allows the user to update their display name and profile photo. It is a utility screen — no AI dependency, no subscription gate. Accessible to all authenticated users. Guest users cannot reach this screen (no profile to edit).

---

## 2. AI Dependency

**None.**

| Element | Source | AI? |
|---|---|---|
| Current display name | Room `UserProfile.displayName` | No |
| Current photo | Room `UserProfile.photoUrl` | No |
| Save | Room + Firestore write | No |
| Photo pick | MediaStore / CameraX | No |

---

## 3. Access Control

- **All authenticated users.** No subscription gate.
- Guest user: cannot reach this screen. Profile header chevron hidden for guests on Profile screen.

---

## 4. Entry Points

| Source | NavArg |
|---|---|
| Profile screen — user identity row chevron | None (loads current user from auth session) |

---

## 5. Layout

Full screen. Not a bottom sheet — keyboard requires full screen space.

```
┌─────────────────────────────────────────┐
│  [<]     Edit Profile          [Save]   │  ← Top bar
├─────────────────────────────────────────┤
│                                         │
│           [Avatar — 96dp]               │  ← Circular avatar, centred
│        [Change Photo text link]         │
│                                         │
│  Display Name                           │  ← Field label
│  ┌─────────────────────────────────┐    │
│  │  Ayesha Khan              36/40 │    │  ← Text input + char count
│  └─────────────────────────────────┘    │
│                                         │
│  Email                                  │  ← Field label
│  ┌─────────────────────────────────┐    │
│  │  ayesha.khan@email.com    🔒    │    │  ← Read-only, lock icon
│  └─────────────────────────────────┘    │
│                                         │
│  ┌─────────────────────────────────┐    │  ← Change password row
│  │  Change Password           >    │    │
│  └─────────────────────────────────┘    │
│                                         │
│  ┌─────────────────────────────────┐    │  ← Connected accounts row
│  │  [G] Google — Connected         │    │  ← Only if signed in via Google
│  └─────────────────────────────────┘    │
│                                         │
│  [Discard Changes]                      │  ← Text link, bottom
│                                         │
└─────────────────────────────────────────┘
```

---

## 6. EditProfileViewModel State

```kotlin
data class EditProfileUiState(
    val isLoading: Boolean = true,
    val currentPhotoUrl: String? = null,
    val pendingPhotoUri: Uri? = null,       // selected but not yet saved
    val displayName: String = "",
    val displayNameError: String? = null,
    val email: String = "",
    val isGoogleConnected: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: EditProfileError? = null
)

sealed class EditProfileError {
    object SaveFailed : EditProfileError()
    object PhotoUploadFailed : EditProfileError()
    object NetworkUnavailable : EditProfileError()
}
```

**Data loading on entry:**
```
EditProfileViewModel.init
    │
    ├── FirebaseAuth.currentUser → uid, email, providers
    ├── UserProfileDao.getProfile(uid) → displayName, photoUrl
    ├── check providers: isGoogleConnected = providers.contains("google.com")
    └── emit EditProfileUiState(isLoading=false, ...)
```

---

## 7. Avatar Section

**Display:**
- Circular avatar, 96dp diameter.
- Source: `pendingPhotoUri` if set (selected but not saved), else `currentPhotoUrl` (Coil loaded), else initials fallback.
- Priority: `pendingPhotoUri` > `currentPhotoUrl` > initials.

**"Change Photo" tap:**
Opens a bottom sheet with two options:

```
┌─────────────────────────────────────────┐
│  Change Profile Photo                   │
│  ─────────────────────────────────────  │
│  📷  Take Photo                         │
│  🖼️  Choose from Gallery                │
│  ─────────────────────────────────────  │
│  Cancel                                 │
└─────────────────────────────────────────┘
```

**Take Photo:**
- Launches CameraX in a single-photo capture mode (not the full Scan screen — a simplified camera for portrait photo only).
- Front camera default.
- No face validation, no ML Kit overlay — plain camera capture.
- On capture: result `Uri` stored in `EditProfileViewModel.pendingPhotoUri`.
- Avatar previews new photo immediately.
- Photo NOT saved to Room/Firestore until user taps Save.

**Choose from Gallery:**
- `ActivityResultContracts.PickVisualMedia(ImageOnly)`.
- Selected `Uri` stored in `pendingPhotoUri`.
- Avatar previews immediately.
- Not saved until Save tapped.

**Photo validation (both paths):**
- Min dimensions: 100×100px. Below minimum → snackbar "Photo too small — try a larger image."
- Max file size after compression: 2MB. Above → compress to JPEG 80%. If still above 2MB → snackbar "Photo file too large — choose a smaller image."
- No face detection required for profile photo.

---

## 8. Display Name Field

- `OutlinedTextField` in Compose.
- Label: "Display Name".
- Max length: 40 characters.
- Character count shown right-aligned inside or below field: `{length}/40`.
- Count turns error style when length = 40 (at limit).
- Single line. No multiline.
- Keyboard type: `KeyboardType.Text`, `ImeAction.Done`.
- `ImeAction.Done` dismisses keyboard.

**Validation (on Save tap):**
- Empty → `displayNameError = "Name cannot be empty"`. Error shown below field.
- Whitespace only → same error.
- Leading/trailing whitespace → trimmed before save (not shown as error).
- Length > 40 → prevented by `maxLength` constraint. Cannot happen.

---

## 9. Email Field

- Read-only `OutlinedTextField`.
- Value: `FirebaseAuth.currentUser?.email`.
- Trailing icon: lock icon.
- Field background muted to signal non-editable.
- No interaction. No tap handler.
- Label: "Email".

---

## 10. Change Password Row

- Shown only for email/password auth users. Hidden for Google-only sign-in.
- Detection: `FirebaseAuth.currentUser?.providerData` — if no `password` provider, hide row.
- Row: label "Change Password" + chevron `>`.
- Tap behaviour:
  1. `FirebaseAuth.sendPasswordResetEmail(email)`.
  2. Success → snackbar: "Password reset email sent to {email}."
  3. Failure → snackbar: "Failed to send reset email — try again."
  4. No navigation. User stays on Edit Profile screen.
- No loading state needed — Firebase call is fast. Show snackbar on completion.

---

## 11. Connected Accounts Row

- Shown only if `isGoogleConnected = true`.
- Row: Google "G" icon + "Google — Connected" label.
- No tap action in MVP. Display only.
- Post-MVP: tapping could allow unlinking Google account.

---

## 12. Save Logic

Triggered by top bar "Save" button.

```
tap Save
    │
    ├── validate displayName (not empty, not whitespace only)
    │   → if invalid: show field error, stop
    │
    ├── isSaving = true → Save button shows loading spinner
    │
    ├── if pendingPhotoUri != null:
    │   ├── compress photo to JPEG 80%
    │   ├── upload to Firebase Storage: users/{uid}/profile.jpg
    │   ├── get download URL
    │   └── newPhotoUrl = downloadUrl
    │   else: newPhotoUrl = currentPhotoUrl
    │
    ├── UserProfileDao.updateProfile(uid, displayName.trim(), newPhotoUrl)
    ├── Firestore.update("users/{uid}", {
    │       displayName: displayName.trim(),
    │       photoUrl: newPhotoUrl
    │   })
    │
    ├── isSaving = false
    ├── saveSuccess = true
    └── navigate back to Profile screen
```

**On save failure:**
- Firebase Storage upload fail → `EditProfileError.PhotoUploadFailed` → snackbar: "Photo upload failed — profile name saved without new photo." Saves name only, skips photo.
- Firestore write fail → `EditProfileError.SaveFailed` → snackbar: "Update failed — try again." Room already updated (optimistic). Firestore retried on next app open via offline persistence.
- Network unavailable → `EditProfileError.NetworkUnavailable` → snackbar: "No internet — changes saved locally and will sync when online." Room written, Firestore queued via offline persistence.

---

## 13. Discard Changes

- Text link at bottom of screen: "Discard Changes".
- Only shown when `pendingPhotoUri != null` or `displayName` has been modified from original value.
- Hidden when no changes made.

**Tap — Discard Changes:**
- If changes exist: confirmation dialog:
  - Title: "Discard changes?"
  - Body: "Your unsaved changes will be lost."
  - Buttons: "Keep Editing" (dismiss) / "Discard" (destructive).
  - On Discard: navigate back to Profile. No save.
- Back chevron also triggers same dialog if changes exist.
- Back chevron with no changes: navigate back directly, no dialog.

**Change detection:**
```kotlin
val hasChanges: Boolean
    get() = pendingPhotoUri != null ||
            displayName.trim() != originalDisplayName.trim()
```

---

## 14. Keyboard Behaviour

- Screen scrolls up when keyboard appears to keep active field visible.
- Uses `Modifier.imePadding()` on root composable.
- `WindowCompat.setDecorFitsSystemWindows(window, false)` required (already set app-wide per master PRD).

---

## 15. Navigation Map

| Trigger | Destination | Condition |
|---|---|---|
| Back chevron | Profile screen | No changes |
| Back chevron | Discard dialog | Changes exist |
| Save button | Profile screen | After successful save |
| Change Password tap | Stay on screen + snackbar | Email auth users only |
| Discard Changes link | Discard dialog → Profile | Changes exist |
| Discard confirm | Profile screen | After discard |

---

## 16. Acceptance Criteria

1. Screen loads with current display name and photo pre-populated within 200ms.
2. Avatar previews new photo immediately after selection (before save).
3. Photo picker bottom sheet shows correct two options. Gallery and camera both functional.
4. Photos below 100×100px rejected with correct snackbar.
5. Photos above 2MB compressed before upload. Compression applied correctly.
6. Display name character count updates live. Shows error style at 40 chars.
7. Empty / whitespace-only name shows field error on Save tap. No save attempted.
8. Email field is non-interactive. Lock icon visible.
9. Change Password row hidden for Google-only users. Shown for email/password users.
10. Password reset email sent correctly. Snackbar shown on success and failure.
11. Google Connected row shown only when Google provider detected.
12. Save button shows loading state during save operation.
13. Successful save navigates back to Profile. Profile screen reflects updated name and photo immediately.
14. Photo upload failure saves name only. Correct snackbar shown.
15. Discard Changes link visible only when changes exist. Hidden otherwise.
16. Back navigation with unsaved changes triggers discard dialog. Back with no changes navigates directly.
17. Keyboard appears without obscuring the active field (imePadding applied).
18. Guest users cannot reach this screen. Profile chevron hidden for guests.

---

## 17. Out of Scope

- Username (@ handle) — display name only.
- Account linking / unlinking (Google ↔ email).
- Two-factor authentication setup.
- In-app photo cropping / editing tool.
- Multiple profile photos or photo gallery.
- Display name profanity filter.

---

*End of document — Edit Profile PRD v1.0*
