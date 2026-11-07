# Project: Birf Dae Saffron UI Overhaul

## Architecture
- **Design System Layer**:
  - `ui/theme/Color.kt`: M3 Saffron tonal palette derived from seed `#C98A12` with isolated M3 red error colors (`#BA1A1A` Light / `#FFB4AB` Dark).
  - `ui/theme/Theme.kt`: `BirthdayReminderAppTheme` applying Saffron color schemes, managing window insets, status bar, and navigation bar appearance.
  - `ui/theme/Fonts.kt`: Font family definitions for Fraunces (display) and Figtree (body/labels) with graceful fallback to `FontFamily.Default`.
  - `ui/theme/Type.kt`: 15 M3 typography styles configured with Fraunces and Figtree.
- **Component Layer (`ui/components/BirfDae/`)**:
  - `SaffronTokens.kt`: Spacing (4..32dp), touch target floor (44dp min), corner radii (12..99dp), elevation.
  - 12 M3 Composables: `SaffronBackground`, `SectionHeader`, `SurfaceCard`, `PersonRow`, `CountdownPill`, `SaffronChip`, `SaffronTextField`, `PersonAvatar`, `SaffronSearchField`, `SaffronBadge`, `SaffronDateField`, `SaffronButton`.
- **Artifact Layer**:
  - `ui/components/BirthdayCard.kt`: Exportable viral birthday card artifact with deterministic clamped gradient (`luminance >= 0.24`), quote re-roller, tone selector, and WhatsApp / `ACTION_SEND` share handler.
- **Screen & Navigation Layer**:
  - Explore Archetype: `BirthdayListScreen`, `CalendarScreen`, `SearchScreen`.
  - Configure Archetype: `AddEditBirthdayScreen`, `NotificationSettingsScreen`, `BackupScreen`.
  - Shell: `BirthdayApp.kt` bottom dock navigation.
  - Preserved contracts: All `StateFlow<UiState>`, ViewModel event callbacks, Room DAOs, and Compose Navigation routes.

## Feature Inventory
| # | Feature | Description | Milestone | Source |
|---|---------|-------------|-----------|--------|
| 1 | Saffron M3 Color Palette | Light and Dark tonal palettes derived from `#C98A12` with proper M3 error reds | M1 | R1, IMPLEMENTATION-PLAN §1.2 |
| 2 | System Bar & Inset Preservation | Window insets and status/navigation bar coloring in `Theme.kt` | M1 | R1, AGENTS.md |
| 3 | Fraunces & Figtree Typography | 15 typography styles with Fraunces (display) and Figtree (body), with fallback | M1 | R1, IMPLEMENTATION-PLAN §1.3 |
| 4 | SaffronTokens Scale | Spacing, 44dp touch target floor, corner radii, and elevation tokens | M2 | R2, IMPLEMENTATION-PLAN §1.4 |
| 5 | BirfDae 12-Component Library | 12 M3 components in `ui/components/BirfDae/` replacing Lumina | M2 | R2, IMPLEMENTATION-PLAN §1.4 |
| 6 | Lumina Component Elimination | Replace all ~90 call sites across screens and delete `LuminaComponents.kt` | M2 | R2, Acceptance Criteria |
| 7 | Explore Archetype Screens | Restyle `BirthdayListScreen`, `CalendarScreen`, `SearchScreen` | M3 | R3, Prototype States up, cal, sea |
| 8 | Calendar Dynamic Month Offset Fix | Compute leading blanks using `(dayOfWeek.value - 1)` for Monday-start | M3 | R3, Survey Finding §1.5 |
| 9 | Configure Archetype Screens | Restyle `AddEditBirthdayScreen` (3 steps), `NotificationSettingsScreen`, `BackupScreen` | M3 | R3, Prototype States a1-a3, set, bk |
| 10 | Navigation Dock Restyling | Update `BirthdayApp.kt` floating dock nav to Saffron design | M3 | R3, Prototype Dock Nav |
| 11 | Birthday Card Generative Gradient | Linear gradient seeded by `(month * 31 + day)` clamped to `luminance >= 0.24` | M4 | R4, IMPLEMENTATION-PLAN §5 |
| 12 | Birthday Card Personalisation & Quotes | Age/zodiac line from domain utilities, re-rollable quotes without color shift | M4 | R4, IMPLEMENTATION-PLAN §5 |
| 13 | WhatsApp Share with Fallback | WhatsApp intent with automatic fallback to Android `ACTION_SEND` chooser | M4 | R4, IMPLEMENTATION-PLAN §5 |
| 14 | Honest Data Display | Display real `createdAt` and dates without fabricated streak counts | M4 | R4, ORIGINAL_REQUEST R4 |
| 15 | BirthdayCardTest Unit Test Suite | 372-seed luminance, determinism, text formatting, and share fallback tests | M4 | Acceptance Criteria |
| 16 | Empty List State | 78dp cake orb, "No birthdays yet", single CTA, "Why it matters" rows | M5 | R5, Prototype State empty |
| 17 | Inline Form Validation | Non-destructive error state with error border, warning icon, preserved inputs | M5 | R5, Prototype State err |
| 18 | Warm Overdue State | `errorContainer` banner, "Send a belated wish", honest dismissal "Not this year" | M5 | R5, Prototype State late |
| 19 | Zero Color Literals Guardrail | Ensure 0 occurrences of `Color(0x...)` in `ui/screens/` | M5 | R5, Acceptance Criteria |
| 20 | Full Build & Lint Verification | Clean `./gradlew.bat build`, `testDebugUnitTest`, `ktlintCheck`, `lintDebug` | M5 | Acceptance Criteria |

## Milestones
| # | Name | Scope | Dependencies | Status |
|---|------|-------|-------------|--------|
| 1 | Design System Tokens & Typography | `Color.kt`, `Theme.kt`, `Fonts.kt`, `Type.kt` | none | DONE |
| 2 | BirfDae Component Suite & Lumina Retirement | `ui/components/BirfDae/`, migration of call sites, delete `LuminaComponents.kt` | M1 | IN_PROGRESS |
| 3 | Screen Restyling & Archetypes | `BirthdayListScreen`, `CalendarScreen` (offset fix), `SearchScreen`, `AddEditBirthdayScreen`, `NotificationSettingsScreen`, `BackupScreen`, `BirthdayApp.kt` | M2 | PLANNED |
| 4 | Birthday Card Viral Artifact & Sharing | `BirthdayCard.kt`, `BirthdayCardGradient`, `BirthdayCardTest.kt` | M2 | PLANNED |
| 5 | Edge States, Guardrails & Full Verification | Empty states, inline validation, overdue hero, zero color literals, final builds/tests | M3, M4 | PLANNED |

## Interface Contracts
### Design System ↔ UI Components
- `MaterialTheme.colorScheme`: Access standard M3 tokens (`primary`, `surface`, `surfaceContainer`, `error`, etc.).
- `MaterialTheme.typography`: Access Fraunces (`displayLarge`..`headlineSmall`) and Figtree (`titleLarge`..`labelSmall`).
- `SaffronTokens`: Dimensions, shapes, spacing, and 44dp touch target floor.

### Components ↔ Screens
- `SaffronBackground(content: @Composable () -> Unit)`
- `SectionHeader(title: String, modifier: Modifier = Modifier, actionText: String? = null, onActionClick: (() -> Unit)? = null)`
- `SurfaceCard(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, containerColor: Color = MaterialTheme.colorScheme.surfaceContainer, content: @Composable ColumnScope.() -> Unit)`
- `PersonRow(name: String, dateString: String, subtitle: String? = null, daysUntil: Int, tone: PersonAvatarTone = PersonAvatarTone.Primary, isPinned: Boolean = false, imageUri: String? = null, onClick: () -> Unit, modifier: Modifier = Modifier)`
- `CountdownPill(daysUntil: Int, isHot: Boolean = false, isSoon: Boolean = false, modifier: Modifier = Modifier)`
- `SaffronChip(selected: Boolean, onClick: () -> Unit, label: String, modifier: Modifier = Modifier)`
- `SaffronTextField(value: String, onValueChange: (String) -> Unit, label: String? = null, placeholder: String? = null, isError: Boolean = false, errorMessage: String? = null, modifier: Modifier = Modifier, singleLine: Boolean = true, leadingIcon: @Composable (() -> Unit)? = null, trailingIcon: @Composable (() -> Unit)? = null)`
- `PersonAvatar(name: String, imageUri: String? = null, tone: PersonAvatarTone = PersonAvatarTone.Primary, size: Dp = 44.dp, modifier: Modifier = Modifier)`
- `SaffronSearchField(value: String, onValueChange: (String) -> Unit, placeholder: String = "Search", onClear: (() -> Unit)? = null, modifier: Modifier = Modifier)`
- `SaffronBadge(text: String, modifier: Modifier = Modifier, containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh, contentColor: Color = MaterialTheme.colorScheme.onSurface)`
- `SaffronDateField(selectedDate: LocalDate?, onDateSelected: (LocalDate) -> Unit, label: String = "Birth date", isError: Boolean = false, errorMessage: String? = null, modifier: Modifier = Modifier)`
- `SaffronButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, variant: SaffronButtonVariant = SaffronButtonVariant.Primary, size: SaffronButtonSize = SaffronButtonSize.Regular, enabled: Boolean = true, icon: ImageVector? = null)`

### Birthday Card ↔ Sharing & Testing
- `BirthdayCardGradient.gradientFor(month: Int, day: Int): List<Color>` (clamped to luminance >= 0.24).
- `BirthdayCardGradient.gradientFor(date: LocalDate): List<Color> = gradientFor(date.monthValue, date.dayOfMonth)`.
- `BirthdayCardShareHelper.createShareIntent(context: Context, text: String, isWhatsAppInstalled: Boolean): Intent`.

## Code Layout
- `app/src/main/java/com/birthdayreminder/ui/theme/`:
  - `Color.kt`
  - `Theme.kt`
  - `Fonts.kt`
  - `Type.kt`
- `app/src/main/java/com/birthdayreminder/ui/components/BirfDae/`:
  - `SaffronTokens.kt`
  - `SaffronBackground.kt`
  - `SectionHeader.kt`
  - `SurfaceCard.kt`
  - `PersonRow.kt`
  - `CountdownPill.kt`
  - `SaffronChip.kt`
  - `SaffronTextField.kt`
  - `PersonAvatar.kt`
  - `SaffronSearchField.kt`
  - `SaffronBadge.kt`
  - `SaffronDateField.kt`
  - `SaffronButton.kt`
- `app/src/main/java/com/birthdayreminder/ui/components/`:
  - `BirthdayCard.kt`
- `app/src/main/java/com/birthdayreminder/ui/screens/`:
  - `BirthdayListScreen.kt`
  - `CalendarScreen.kt`
  - `SearchScreen.kt`
  - `AddEditBirthdayScreen.kt`
  - `NotificationSettingsScreen.kt`
  - `BackupScreen.kt`
- `app/src/main/java/com/birthdayreminder/ui/`:
  - `BirthdayApp.kt`
- `app/src/test/java/com/birthdayreminder/ui/components/`:
  - `BirthdayCardTest.kt`
