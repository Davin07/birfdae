# Birf Dae — Saffron UI Overhaul: Agent Handoff Document

> Updated: 2026-09-27, after Hermes took over implementation.
> Supersedes the previous state: Milestones 1–3 are now **done and committed**.

---

## Current state

Branch: `design/saffron-ui-overhaul` (base `origin/master` @ 8b78d52, v1.0.24)

| # | Milestone | Commit | State |
|---|-----------|--------|-------|
| 1 | Design tokens + typography | `4d47f3f` | Done, verified |
| 2 | `BirfDae` component suite | `581ba67` | Done, 18 components |
| 3 | Screen migration, Lumina retired | `2100759`, `ff59731` | Done, all 6 screens + nav |
| 4 | Birthday Card (the viral artifact) | — | **Not started** |
| 5 | Overdue / validation edge states | — | **Not started** |

`./gradlew build` is clean: ktlint, Android lint, 107 unit tests, 0 failures.

Acceptance criteria currently met:
- [x] `gradlew build` + `test` pass clean
- [x] `grep -r Lumina app/src` → 0 results; `LuminaComponents.kt` deleted
- [x] no `Color(0x` in `ui/screens`
- [x] no `FontFamily.Default` under `ui`
- [x] dark theme verified on a real device (emulator-5554, Pixel 7, Android 36)
- [ ] `gradientFor()` luminance ≥ 0.24 across all 372 date seeds — **Milestone 4**
- [ ] WhatsApp present/absent share test — **Milestone 4**
- [ ] all 13 prototype states matched in native Compose — **Milestones 4 & 5**

---

## What exists now

**`ui/theme/`** — `Color.kt` (Saffron light + dark M3 schemes, error reds
`#BA1A1A`/`#FFB4AB` intentionally non-generated), `Type.kt` (Fraunces display +
Figtree body across all 15 M3 roles), `Fonts.kt`, `Theme.kt`
(`dynamicColor = false`).

**`ui/components/birfdae/`** — note the **lowercase** package; ktlint rejects
`BirfDae`.

| File | Contents |
|---|---|
| `SaffronTokens.kt` | spacing 4/8/12/16/20/24/32, radii, 44dp touch floor, motion scale |
| `SaffronPrimitives.kt` | `SaffronBackground`, `SurfaceCard`, `SectionHeader`, `SaffronChip`, `SaffronButton`, `EmptyState`, `SectionLabel`, `Divider` |
| `PersonComponents.kt` | `PersonAvatar`, `PersonRow`, `accentFor()`, `initialsOf()` |
| `InputComponents.kt` | `SaffronTextField`, `SearchField`, `DatePickerField`, `AvatarPicker` |

**Screens** — `BirthdayListScreen`, `SearchScreen`, `CalendarScreen`,
`NotificationSettingsScreen`, `AddEditBirthdayScreen` and the bottom nav in
`BirthdayApp.kt` all run on the new components. `BackupScreen` was already on
Material 3 `Scaffold` and needed no swap.

---

## Bugs found and fixed during the migration

All invisible in the old design, all found by reading or running the code:

1. `CalendarScreen` drew day numbers and month arrows in `Color.White` —
   invisible in light mode.
2. `LuminaChip` selected state was primary-on-20%-alpha-primary (~2.4:1).
   `SaffronChip` is a solid primary fill with `onPrimary` ink.
3. Bottom nav selected item had the same 2.4:1 failure; now a
   `primaryContainer` pill with `onPrimaryContainer`.
4. `LuminaHeader` centred a 40sp title inside a `Box`, colliding with the back
   button. Replaced by a Row-based `SectionHeader`.
5. `MainActivity` hard-coded `darkTheme = true` — the app was dark-only and
   ignored the system setting. Now `isSystemInDarkTheme()`.
6. `PersonAvatar` on a `primaryContainer` surface picked the same container
   colour and disappeared. Now takes explicit `containerColor`/`contentColor`.
7. `CalendarDayCell` painted a second `background()` over its computed
   container for the "today" case, so the birthday dot kept gold ink on a
   rosewood cell. Container and ink are now resolved together.
8. `initialsOf("123")` returned `"123"` instead of `"?"` — caught by unit test.

---

## Deliberate decisions — do not undo

- **No streak copy.** The prototype showed "you have remembered 11 of her last
  11". There is no reminder-history table — only `createdAt` — so that claim
  would be invented. The home hero states only name, date, age turning and
  reminder time. Milestone 4 should prefer truthful copy like
  "on your list since {year}".
- **Stable `Brush.linearGradient`, not `MeshGradientPainter`.** The app is on
  Compose BOM 2024.02.01; the mesh painter needs alpha Material 3. One
  decorative effect does not justify that upgrade.
- **Card gradient stays in a light luminance band.** Darkest stop ≥ 0.24
  relative luminance so dark ink clears WCAG AA. Vary hue with birth data,
  never lightness.
- **Share is a WhatsApp deep link** with a `resolveActivity` guard — never
  assume WhatsApp is installed.
- **Personalisation reuses `SafeDateCalculator.calculateAge` and
  `ZodiacUtils.getZodiacSign`.** No backend was added.
- **Content changes made in Milestone 3:** wizard step 3 relabelled
  "Notify" → "Personalize" to match what it does; step 2's two stat cards
  became one "THIS UNLOCKS — Turning 53 · Pisces" panel; search gained an
  empty state distinguishing "no people" from "no matches"; home splits into
  "Next up" (≤30 days) and "Later this year".

---

## Next: Milestone 4, the birthday card

The shareable artifact is still missing. Sketch:

- New `BirthdayCardScreen` rendering the card.
- Gradient from the person's birth month/day — hue only, clamped to the safe
  luminance band.
- Content: name, age turning, zodiac, date, the user's own message.
- WhatsApp share button, `resolveActivity` guard, copy-to-clipboard fallback.
- Produce the share image natively: `Canvas` → `Bitmap` → cache file →
  `FileProvider` URI. Do not screenshot the view.
- Unit test over all 372 month/day seeds asserting darkest-stop luminance
  ≥ 0.24, and a determinism test.

Then Milestone 5: overdue state (warm-urgent, "Send a belated wish" +
"Not this year", both required) and inline validation errors wired to
`BirthdayValidator.kt` with data preserved.

---

## Environment notes

- Branch from `origin/master` after `git fetch` — **never** local `master`.
  Local master is stale and its `gradlew` has CRLF, which breaks the build
  with a mangled `lasspath` error.
- `ffmpeg` is needed to downscale screenshots before vision analysis and
  requires a `C:/...` path; a `/c/...` MSYS path is "No such file or
  directory" for the native binary.
- The `design_review` AVD (Android 36, Pixel 7, 1080×2400) is retained. Cold
  boot ≈ 60s. `adb -s emulator-5554 emu kill` to shut down.
- `RequestNotificationPermission` shows on first launch; grant with
  `adb shell pm grant com.birthdayreminder android.permission.POST_NOTIFICATIONS`
  before screenshotting.
- `KEYCODE_BACK` on the root screen exits the app, so avoid it when just
  trying to dismiss a dialog.
