# Birf Dae — Saffron UI Overhaul: Agent Handoff Document

> Updated: 2026-09-28. **All five milestones are done, and the plan-vs-prototype
> fidelity pass that followed is also committed.** Milestone completion is not
> design completion: the card was missing three features the plan called for,
> and several screens still diverged from the approved concept.

---

## Current state

Branch: `design/saffron-ui-overhaul` (base `origin/master` @ 8b78d52, v1.0.24)

| # | Milestone | Commit | State |
|---|-----------|--------|-------|
| 1 | Design tokens + typography | `4d47f3f` | Done, verified |
| 2 | `BirfDae` component suite | `581ba67` | Done, 18 components |
| 3 | Screen migration, Lumina retired | `2100759`, `ff59731` | Done, all 6 screens + nav |
| 4 | Birthday Card (the viral artifact) | `12fd209` | Done |
| 5 | Overdue / validation edge states | `1d3a2dd` | Done, verified on device |
| 6 | Card tone chips, re-roll, save image | `db85fba` | Done, verified on device |
| 7 | Screen fidelity vs the approved concept | `507b978`, `3aab686`, `e04da65`, `f010459` | Done, verified on device |

`./gradlew build` is clean: ktlint, Android lint, **174 unit tests**, 0 failures.
`./gradlew connectedDebugAndroidTest` is **76 tests, 0 failures** — the whole
instrumented source set now compiles and runs, which it did not before.

Acceptance criteria currently met:
- [x] `gradlew build` + `test` pass clean
- [x] `grep -r Lumina app/src` → 0 results; `LuminaComponents.kt` deleted
- [x] no `Color(0x` in `ui/screens`
- [x] no `FontFamily.Default` under `ui`
- [x] dark theme verified on a real device (emulator-5554, Pixel 7, Android 36)
- [x] darkest gradient stop clears the luminance floor across all 372 seeds
- [x] WhatsApp absent path exercised - PNG written, no crash
- [x] overdue state, validation errors — Milestone 5
- [x] Room 3→4 migration verified on a real device: 12 existing rows preserved,
      `skippedYear` defaults to -1
- [x] all three wizard steps validate before advancing; Continue stays disabled
      with the reason shown
- [x] card: Warm / Funny / Sincerest / Short tone chips, words-only re-roll, and
      a real MediaStore save to `Pictures/Birf Dae/`
- [x] card gradient constrained to a 96° arc centred on hue 10°, so no birthday
      lands on lilac
- [x] Settings reads as flat rows under Reminders / Your data / Appearance
- [x] per-person reminder overrides exist and are reachable from Settings
- [x] overdue collapses to one card plus a tappable remainder, so the hero and
      the upcoming rows stay on screen
- [x] Search shows everyone on load instead of reporting an empty list

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

Found in Milestone 4, and only by inspecting the exported PNG rather than the
app — all three were invisible on screen:

9. The card name collided with the "HAPPY BIRTHDAY" eyebrow. At 132px a name's
   ascent is ~100px, so adding the previous line's returned height overlapped
   them. The header is now on an explicit grid.
10. Secondary text had an olive cast. `cardInk * 0.72f` scales the alpha byte of
    a packed ARGB int as well as the colour, leaving it semi-transparent so it
    picked up green from the pink card. Both surfaces blend toward the
    gradient's lightest stop now.
11. The card read "from Birf Dae · Birf Dae" — both the composable and the
    renderer appended the app name to a sender that was already it.

Lesson: for anything exported, pull the real file off the device and look at
it. The app preview is not the artifact.

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

## The card (Milestone 4, done)

`ui/card/` — `CardGradient` (the invariant), `BirthdayCardArtifact` (on-screen),
`CardImageRenderer` (the exported 1080x1350 PNG), `CardSharer` (intents).
Route `birthday_card/{birthdayId}`, reached from a share button on every row.

Keep these together: the composable and the renderer read the same gradient and
the same `attributionLine`, which is the only reason preview and share match.

## The overdue + validation states (Milestone 5, done)

- `domain/model/OverdueBirthday.kt` — occurrence, day count, `ageTurned`. A
  date already passed this year, and only that.
- `domain/usecase/SkipBirthdayForYearUseCase.kt` — persists the year through
  the existing repository update path; no new DAO query.
- `ui/components/birfdae/OverdueComponents.kt` — the warm-plum card. It reads
  as urgent, not alarming: "OVERDUE" / "3 DAYS AGO" / "YESTERDAY".
- Room schema 3→4 adds `Birthday.skippedYear` (`-1` = never skipped).
  Verified on-device against a real database, not just via MigrationTestHelper.
- `AddEditBirthdayViewModel.nextStep()` validates the step being left before
  advancing, and routes the message to the field that caused it via
  `withFieldError`.

`overdue` is a **separate list** on `BirthdayListUiState`, not a negative
`daysUntilNext` in `birthdays`. The two feed different UI: overdue renders the
plum card, the regular list renders a person row.

---

## The text-first hero and reminder history (Milestone 8, done)

The approved concept opened Home with a sentence about today, not a card. It
also promised "You've remembered 11 of her last 11", which is a claim about the
user, so the storage to make it true was built alongside it.

- **Room schema 4→5** adds `reminder_events` — `(birthdayId, year)` unique, so
  one birthday has one row per year. `acknowledgedAt` and `sharedAt` are
  independent and additive: a share never clears an acknowledgement.
- **Acknowledged is the streak; a share is not.** `GetReminderStreakUseCase`
  counts `acknowledgedAt` only. Sharing records intent to share, which the app
  cannot verify, so it feeds a separate `sharedYears` counter and nothing else.
- **The denominator is earned, not asserted.** Tracked years start at
  `BirthdayYear.firstEligibleYear`: the first birthday the app *could* have
  reminded about. Added in March with a December birthday, that year counts;
  added in June with a March birthday, the first chance is next March. Every
  year after that counts whether or not a row exists — that is what makes a
  missing row mean "not remembered" rather than "not yet due".
- **A short history says so.** `ReminderStreak.isMeaningful` requires two
  tracked years. Below that the hero reads "On your list since 2016" rather than
  dressing a fresh install as a track record. Nothing in `HomeHeroCopy` can
  produce a streak line the data does not support.
- **Notification taps now work.** `MainActivity` reads `birthday_id` and
  `birthday_year` on both `onCreate` and `onNewIntent`, records the
  acknowledgement, and navigates to that person's card. Previously the intent
  carried the id and nothing read it: the tap was a dead end that opened Home.
  The year extra exists because an advance reminder fired in December belongs
  to the following January's birthday.
- `HeroBirthdayCard` is deleted. The hero is text (`HomeHeroSection`), and the
  list no longer holds a person back to fill a card.

Two bugs the device found, both now regression-tested:

- **A birthday falling today was also "overdue".** `OverdueCalculator` used
  `!isAfter(today)`, so a same-day date produced a 0-day overdue entry and the
  person appeared in the overdue card, the hero, and the list at once. Now
  `isBefore(today)`. Three existing tests encoded the old contract and were
  corrected rather than worked around.
- **A same-name person repeated under a "Next up" heading** right below a hero
  that had just announced them. The hero's people are excluded from the list,
  which is now the only thing that exclusion is for.

**Do not read the database to check whether a write landed without the WAL.**
`adb exec-out run-as ... cat databases/<db>` returns the main file only; a
recent write is still in `-wal` and looks absent. Force-stopping first does not
help — Room has not checkpointed. Pull all three files and read with the `-wal`
and `-shm` beside the main db. This cost a long false bug hunt.

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
- **The Room database is `databases/birthday_reminder_database`**, with a
  `-wal`/`-shm` pair. Not `birthday_reminder.db` — writing to a name like that
  creates a stray file the app never reads, and the UI keeps showing the old
  data with no error anywhere.
- **Do not trust `adb exec-out run-as ... cat` on a live WAL database** to check
  what the app wrote; the copy can come back truncated and read as "the write
  never happened". Assert persistence through an instrumented test against a
  real Room DB, or by restarting the app and looking at the UI. Both confirmed
  the skip write is correct.
- The instrumented source set had **never compiled** before this work, so its
  tests asserted pre-redesign copy. Hilt-backed Compose tests need a host
  activity in `src/debug` (not `src/androidTest`) plus an Application-swapping
  `HiltTestRunner`; see the `android-cli-dev-workflow` skill's
  `references/hilt-compose-testing.md`.
- `connectedDebugAndroidTest` **uninstalls the app** when it finishes, and it
  drives the emulator while it runs. Every seeded database and every screenshot
  is invalid afterwards: reinstall, relaunch once so Room creates the file, then
  seed, then relaunch again. A "no data" reading right after a test run is this,
  not a lost write.
- **A rejected `write_file` is silent if you do not read the result.** The tool
  refuses to overwrite a file last seen through a paginated `read_file`, and
  reports it in the return value only. A splice built with `read_file` +
  `write_file` looked like it succeeded and silently changed nothing; check
  `verified` / `error` on every write, or use `patch` for surgical edits.
- **ktlintFormat runs first and strips imports it thinks are unused**, which
  includes imports for code you are about to add. Add the import *after* the
  usage exists, or re-add it and compile immediately, or you chase an
  "unresolved reference" that was a formatting side effect.
- **Mocking a suspend function with thirteen defaulted parameters is a trap.**
  `whenever(mock.suspendFn(...))` cannot work, because building the stub would
  invoke the suspend call; use `wheneverBlocking { }` and `verifyBlocking(mock)
  { }` from mockito-kotlin, with every field passed by name so its type is
  constrained.
- `stateIn(WhileSubscribed)` returns the initial value until something collects,
  so a test reading `.value` sees empty. Collect the flow in the test rather
  than changing the ViewModel to suit the test.
