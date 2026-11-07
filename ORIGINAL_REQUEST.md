# Original User Request

## Initial Request — 2026-09-27T10:56:41Z

Execute the complete Saffron UI Overhaul for Birf Dae end-to-end in Jetpack Compose and Material 3, replacing the existing Lumina design system according to the specifications in `docs/design/IMPLEMENTATION-PLAN.md` and the approved prototype `docs/design/prototype/Birf Dae — UI Concept v2.html`.

Working directory: D:\Hmmm\Projects\Vibe\birfdae
Integrity mode: development

Reference material:
- Implementation plan & Definition of Done: `docs/design/IMPLEMENTATION-PLAN.md`
- Approved prototype: `docs/design/prototype/Birf Dae — UI Concept v2.html`
- Palette candidates and accessibility records: `docs/design/prototype/Birf Dae — Palette Options.html`
- Project conventions & build commands: `AGENTS.md`

## Requirements

### R1. Design System Tokens & Typography (Stages 1-2 & 6)
- Implement the generated M3 Saffron tonal palette in `ui/theme/Color.kt` and `ui/theme/Theme.kt` with exact light and dark token values derived from `#C98A12` with proper M3 error colors (`#BA1A1A` light / `#FFB4AB` dark).
- Preserve existing status bar, navigation bar, and window insets handling in `Theme.kt`.
- Activate typography with Fraunces (display) and Figtree (body/labels) in `Type.kt` and `Fonts.kt`, sourcing static TTFs in `app/src/main/res/font/` (with fallback to default font family if offline).

### R2. BirfDae Component Suite & Lumina Retirement (Stage 3)
- Construct the `ui/components/BirfDae/` component library (`SaffronBackground`, `SectionHeader`, `SurfaceCard`, `PersonRow`, `CountdownPill`, `SaffronChip`, `SaffronTextField`, `PersonAvatar`, `SaffronSearchField`, `SaffronBadge`, `SaffronDateField`, `SaffronButton`) driven by a central `SaffronTokens` scale for spacing, touch target floor (44dp), and radii.
- Systematically replace all ~90 Lumina component call sites across the application.
- Completely remove `ui/components/LuminaComponents.kt` once all usages are eliminated.

### R3. Screen Restyling & Archetypes (Stage 4)
- Restyle all screens (`BirthdayListScreen`, `AddEditBirthdayScreen`, `CalendarScreen`, `SearchScreen`, `SettingsScreen`, `BackupScreen`) and `BirthdayApp.kt` to the approved Explore and Configure archetypes.
- Retain all existing ViewModel state flows, event callbacks, Room data bindings, and Compose navigation wiring.
- Calculate dynamic calendar month offsets from the actual first day of each month.

### R4. The Birthday Card Viral Artifact (Stage 5)
- Re-architect `BirthdayCard.kt` as an exportable share artifact featuring a birth-data-derived linear gradient clamped to luminance >= 0.24 (guaranteeing >= 4.5:1 text contrast).
- Provide age and zodiac calculations from existing domain utilities (`SafeDateCalculator`, `ZodiacUtils`), re-rollable quotes without color shifts, and tone selection chips.
- Implement WhatsApp sharing with automatic fallback to standard Android share chooser (`ACTION_SEND`) if WhatsApp is absent.
- Ensure no fabricated streak or reminder history is shown, displaying only honest data (`createdAt` or birthdate).

### R5. Edge States & Guardrails (Stage 7)
- Implement approved edge states: empty list state with single clear action, non-destructive inline validation errors on form fields, and warm overdue state with honest dismissal options.
- Strictly ensure zero color literals (`Color(0x...)`) remain in any screen files.

## Acceptance Criteria

### Automated Verification & Code Health
- [ ] `./gradlew.bat build` and `./gradlew.bat test` pass cleanly with zero compilation errors, test failures, or lint violations.
- [ ] `grep -r Lumina app/src` yields 0 results and `LuminaComponents.kt` is deleted.
- [ ] `grep -rn "Color(0x" app/src/main/java/com/birthdayreminder/ui/screens` returns zero occurrences.
- [ ] Unit tests in `BirthdayCardTest` verify that `gradientFor(date)` produces darkest stop luminance >= 0.24 across all 372 calendar date seeds.
- [ ] Unit tests verify deterministic gradient generation, age/zodiac text mapping, and WhatsApp share intent fallback logic.

### Visual & Functional Conformance
- [ ] All 13 screen states rendered in both Light and Dark themes conform to the approved prototype in `Birf Dae — UI Concept v2.html`.
- [ ] Form wizard progress, date picker interaction, search filtering, and navigation transitions function seamlessly with existing ViewModels.
- [ ] Hero component displays honest reminder data without fabricated streak counts.
