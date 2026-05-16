# Android Mobile Design System

## Overview

Source CSS: `ui-reference/index.css`

This design system translates the web tokens from `index.css` into an Android-first Material Design 3 system. The brand posture is crisp, minimal, and high contrast: near-white and true-black surfaces, a vivid rose primary color, soft rose-tinted cards and outlines, Geist typography, compact radius, and shallow utilitarian elevation.

Android implementation should preserve the identity, not pixel-copy the web CSS. Use Material 3 semantic roles, Compose components, Android navigation patterns, edge-to-edge layout, dark mode, accessibility scaling, and adaptive behavior across compact, medium, and expanded window size classes.

## Colors

Dynamic color policy: avoid full dynamic color replacement by default. The rose primary `#ff637e` is a strong brand signal and should remain stable. Android 12+ dynamic color may be offered only as an optional personalization mode if the app still keeps rose for key brand moments such as selected navigation, primary actions, and important status accents.

Use a custom Material 3 color scheme generated from the primary seed `#ff637e`, then override neutral roles to stay close to the source CSS. Do not hardcode CSS hex values inside individual components; expose them through Compose `ColorScheme` and app semantic tokens.

### Source Token Mapping

| CSS token | Light | Dark | Android role |
|---|---:|---:|---|
| `--primary` | `#ff637e` | `#ff637e` | `primary`, selected states, highest-emphasis actions |
| `--primary-foreground` | `#ffffff` | `#000000` | `onPrimary` |
| `--background` | `#fcfcfc` | `#000000` | `surface`, app background |
| `--foreground` | `#000000` | `#ffffff` | `onSurface` |
| `--card` | `#fff1f2` | `#09090b` | `surfaceContainerLow`, prominent card fill |
| `--card-foreground` | `#000000` | `#ffffff` | `onSurface` |
| `--popover` | `#fcfcfc` | `#121212` | `surfaceContainerHigh`, menus, sheets, dialogs |
| `--secondary` | `#ebebeb` | `#222222` | `secondaryContainer`, tonal secondary controls |
| `--secondary-foreground` | `#000000` | `#ffffff` | `onSecondaryContainer` |
| `--muted` | `#f5f5f5` | `#1d1d1d` | `surfaceContainer` |
| `--muted-foreground` | `#525252` | `#a4a4a4` | `onSurfaceVariant` |
| `--accent` | `#ebebeb` | `#333333` | `surfaceContainerHighest`, low-emphasis accent fill |
| `--destructive` | `#e54b4f` | `#ff5b5b` | `error` |
| `--destructive-foreground` | `#ffffff` | `#000000` | `onError` |
| `--border` | `#ffccd3` | `#3f3f46` | `outlineVariant`, rose-tinted dividers in light mode |
| `--input` | `#ebebeb` | `#333333` | text field container or `surfaceContainerHighest` |
| `--ring` | `#000000` | `#a4a4a4` | focus indicator, `outline` |
| `--chart-1` | `#ffae04` | `#ffae04` | chart warning/orange |
| `--chart-2` | `#2d62ef` | `#2671f4` | chart blue/info |
| `--chart-3` | `#a4a4a4` | `#747474` | chart neutral mid |
| `--chart-4` | `#e4e4e4` | `#525252` | chart neutral low |
| `--chart-5` | `#747474` | `#e4e4e4` | chart neutral high |

### Role Guidance

Primary actions use `primary` with `onPrimary`. In light mode, use white text on rose. In dark mode, prefer black text on rose as specified by the source tokens, but verify contrast for every text size and fall back to white only if the generated MD3 palette fails a contrast check.

Surfaces should remain restrained and mostly neutral. Use `surface` for screen background, `surfaceContainer` for grouped sections, and the rose-tinted `surfaceContainerLow` card color in light mode for important cards or repeated items. Avoid filling whole screens with rose.

Borders and outlines should be subtle. In light mode, use `#ffccd3` for brand-tinted outlines around cards, fields, and selected containers. In dark mode, use neutral zinc outlines from `#3f3f46` and reserve rose for active or selected states.

Semantic states must not rely on color alone. Error/destructive actions use `error` plus an icon, label, or confirmation pattern. Success, warning, and info states should use stable semantic tokens derived from the chart palette: orange for warning, blue for info, neutral for inactive or historical data. Add labels or patterns in charts for accessibility.

## Typography

The source declares `Geist, sans-serif`, `Geist Mono, monospace`, and `Georgia, serif`.

Use Geist as the primary app font if bundled or available as a downloadable font. If Geist is unavailable, fall back to `Roboto` or the platform default sans-serif. Use Geist Mono only for technical identifiers, codes, timestamps, logs, or tabular data. Do not use Georgia as a default Android UI font; reserve it only for editorial content where a serif voice is intentionally needed.

| Material 3 role | Size | Weight | Use |
|---|---:|---:|---|
| `headlineSmall` | 24sp | 500 | Screen-level headings when a large app bar is not used |
| `titleLarge` | 22sp | 500 | Top app bar titles and major detail titles |
| `titleMedium` | 16sp | 600 | Section headings, card titles |
| `titleSmall` | 14sp | 600 | Dense labels and compact section headers |
| `bodyLarge` | 16sp | 400 | Primary reading text |
| `bodyMedium` | 14sp | 400 | Secondary text, list supporting text |
| `bodySmall` | 12sp | 400 | Metadata and helper text |
| `labelLarge` | 14sp | 600 | Button labels |
| `labelMedium` | 12sp | 600 | Chips, badges, small controls |

Letter spacing should remain `0em`, matching `--tracking-normal`. Do not introduce negative tracking on Android. Use `sp` for all text and support system font scaling up to 200%. Layouts must reflow at large font sizes rather than clipping or truncating important content.

## Layout

The source spacing token is `--spacing: 0.25rem`, equivalent to a 4px web sub-grid. Translate this to Android as an 8dp base grid with 4dp allowed for fine alignment, icon padding, and text baseline adjustments.

| Context | Android value |
|---|---:|
| Compact horizontal screen margin | 16dp |
| Medium and expanded horizontal margin | 24dp |
| Related item gap | 8dp |
| Section gap | 16dp or 24dp |
| Component internal padding | 12dp to 16dp |
| Minimum interactive target | 48x48dp |
| Adjacent target separation | 8dp minimum |

Use edge-to-edge layouts. The app background should draw behind transparent system bars, while interactive content must respect `WindowInsets.safeContent`, `WindowInsets.systemBars`, `WindowInsets.navigationBars`, `WindowInsets.ime`, and `WindowInsets.displayCutout` as appropriate. Never hardcode status bar, navigation bar, keyboard, or cutout heights.

Use Android window size classes:

| Width class | Layout behavior |
|---|---|
| Compact, below 600dp | Single-pane screens, bottom `NavigationBar` for 3-5 top-level destinations, 16dp margins |
| Medium, 600-839dp | Use `NavigationRail`, 8-column grid, optional list-detail or supporting pane |
| Expanded, 840dp and up | Use two-pane canonical layouts where content benefits, 12-column grid, permanent drawer only for broad hierarchies |

Top-level navigation should use a bottom `NavigationBar` on compact screens when there are 3-5 destinations. Use tabs for sibling views inside one destination. Use a modal navigation drawer only for larger hierarchies, not as the default phone pattern. Support predictive back for all nested destinations, dialogs, sheets, and custom transitions.

## Elevation & Depth

The source shadows are shallow: mostly `0px 1px 2px` with opacity between `0.09` and `0.18`. On Android, express depth primarily with Material 3 tonal elevation and surface container roles instead of heavy physical shadows.

| Web shadow intent | Android translation |
|---|---|
| `shadow-xs`, `shadow-sm` | Level 1 tonal elevation or outlined card |
| `shadow`, `shadow-md` | Level 2 tonal elevation for active surfaces |
| `shadow-lg`, `shadow-xl` | Use sparingly for menus, sheets, dragged items, or temporary overlays |
| `shadow-2xl` | Avoid except modal emphasis or special drag states |

In light mode, prefer the rose-tinted border and soft filled surfaces over visible shadows. In dark mode, use tonal surfaces (`surfaceContainerHigh` and `surfaceContainerHighest`) for separation because black shadows are not useful on black backgrounds.

## Shapes

The source radius is `0.5rem`, which maps to an 8dp default corner radius.

| CSS radius token | Approx. Android shape |
|---|---:|
| `--radius-sm` | 4dp |
| `--radius-md` | 6dp, round to 8dp unless precision is important |
| `--radius-lg` | 8dp |
| `--radius-xl` | 12dp |

Use 8dp for cards, menus, fields, and compact containers. Use 12dp for dialogs and prominent surfaces. Use 16dp for modal bottom sheets and navigation drawers when additional softness is needed. Use full shape for avatars, circular icon buttons, switches, and progress indicators.

Avoid overly pill-shaped surfaces for general containers; reserve pill shapes for chips, segmented controls, search fields, and FAB variants where Material 3 expects them.

## Components

Use `androidx.compose.material3` components as the default implementation layer.

Top app bars should be small for utility screens, center-aligned only for simple single-level destinations, and medium or large only when the title carries meaningful context. Apply scroll behavior so large and medium bars collapse on content scroll.

Navigation uses bottom `NavigationBar` on compact screens for 3-5 destinations, `NavigationRail` on medium widths, and permanent drawer only for expanded screens with broad hierarchy. Selected states use rose primary; inactive icons and labels use `onSurfaceVariant`.

Buttons follow emphasis hierarchy. Use filled `Button` for the single primary action, `FilledTonalButton` or `OutlinedButton` for secondary actions, and `TextButton` for low-emphasis inline commands. Destructive actions should usually be `TextButton` or `OutlinedButton` in confirmation surfaces unless the destructive action is the confirmed final step.

Icon buttons must use familiar Material icons and meaningful content descriptions. Use filled or tonal icon buttons only when the icon action is high emphasis. Provide tooltips for icon-only controls on large screens and long-press affordance on touch devices.

FABs represent one primary screen action. Use one FAB maximum per screen, placed above the navigation bar and guarded by bottom insets. Prefer extended FABs when an icon alone is unclear; collapse on scroll where appropriate. Never use a FAB for destructive actions.

Cards should be filled or outlined, not heavily elevated. Use the light rose card fill for important grouped content and neutral containers for routine lists. Avoid nesting cards inside cards. Repeated list rows should stay scannable with 56-72dp heights and optional inset dividers.

Text fields and search use Material 3 `OutlinedTextField`, `TextField`, or `SearchBar` depending on prominence. Field containers map to the input token, focus indicators map to ring/outline, and errors use `error` plus field-level error semantics.

Chips are appropriate for filters, tags, suggestions, and compact actions. Selected chips use rose primary container treatment or a visible check icon. Do not rely on color alone for selection.

Dialogs, bottom sheets, menus, and popovers use `surfaceContainerHigh` or `surfaceContainerHighest`, 12-16dp shape, scrim, focus trapping, and clear dismiss paths. Dialogs should have at most two primary decisions plus dismissal. Use modal bottom sheets for mobile task flows that need more room than a menu.

Snackbars use inverse surface colors, concise one- or two-line text, and at most one short action such as Undo. Do not use snackbars for critical or persistent information.

Loading states should use skeleton containers or linear/circular progress indicators depending on layout. Empty states should be brief and actionable. Error states should include a recovery action, not only an error color. Search states should cover idle, focused, suggestions, loading, no results, and results.

Settings rows should use standard Android list structure: title, optional summary, trailing control or navigation chevron, 56-72dp row height, and clear grouping. Avoid custom desktop-style settings panels on compact phones.

## Motion & Behavior

Use Material motion defaults for transitions, state changes, shared axis navigation, and container transforms. Keep animation subtle to match the shallow elevation and minimal surface language.

Support predictive back on Android 13+ and later. Back gestures should preview the destination or dismissal state for navigation, bottom sheets, dialogs, and full-screen flows.

Respect reduced motion. If accessibility reduced motion is enabled, remove decorative motion and shorten necessary state transitions. Never make progress, navigation, or comprehension depend solely on animation.

Use native Android share sheets, permission flows, notification permission prompts, and settings deep links. Do not recreate platform dialogs or custom share sheets.

## Accessibility & System Integration

Every interactive element must have a minimum 48x48dp touch target and at least 8dp between adjacent touch targets. Smaller icons may remain visually small only if their hit area is expanded with Compose minimum interactive sizing.

Maintain at least 4.5:1 contrast for normal text, 3:1 for large text and non-text UI indicators. Test contrast in both light and dark themes, including rose primary with the configured `onPrimary`.

TalkBack support is required. Provide content descriptions for icon-only actions, merge related content where appropriate, mark section headings, expose custom actions for swipe-only behavior, and provide state descriptions for toggles, filters, selected chips, loading, empty, and error states.

Focus order should match visual reading order and support keyboard, switch access, and rotary or directional input where relevant. Dialogs and sheets must trap focus while open and restore focus when dismissed.

Handle system bars and IME insets explicitly. Content can draw edge-to-edge, but controls, text fields, bottom bars, snackbars, sheets, and FABs must avoid overlap with status bars, navigation bars, gesture areas, display cutouts, and the software keyboard.

Support light and dark theme from system settings. Do not mix light tokens into dark UI or dark tokens into light UI. Test with high font scale, dark mode, gesture navigation, three-button navigation, split-screen, foldables, and tablets.

## Do's and Don'ts

Do use rose `#ff637e` as the stable brand accent for primary actions, selected navigation, and important active states.

Do keep screens mostly neutral, crisp, and high contrast, with rose used deliberately rather than as a full-screen wash.

Do map colors to Material 3 roles and Compose theme tokens before using them in components.

Do use Geist or a close Android sans-serif fallback with zero letter spacing and accessible `sp` sizing.

Do use 8dp corners as the default shape language, with 12-16dp only for larger modal surfaces.

Do design compact, medium, and expanded layouts from the same content model instead of stretching phone layouts across tablets.

Do handle edge-to-edge, IME, status bar, navigation bar, and display cutout insets with `WindowInsets`.

Do pair all semantic colors with text, icons, labels, or patterns.

Don't copy CSS pixels directly into Android `dp` values.

Don't rely on web shadows for depth; use tonal elevation and surface containers first.

Don't use bottom navigation for fewer than 3 or more than 5 top-level destinations.

Don't hide critical actions behind icon-only controls without labels, descriptions, or tooltips.

Don't use color-only chart legends, validation errors, selection states, or destructive warnings.

Don't hardcode system bar heights or assume one navigation mode.

Don't replace the brand palette with dynamic color unless the user explicitly enables a personalization mode.
