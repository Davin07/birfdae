# Birf Dae — Saffron UI Overhaul: Implementation Plan

**Branch:** `design/saffron-ui-overhaul`
**Base:** `8b78d52` (`origin/master`, v1.0.24 — includes the gradlew LF fix)
**Status:** Design approved. Ready for implementation.
**Prototype of record:** `docs/design/prototype/Birf Dae — UI Concept v2.html`

---

## 0. Read this first

The approved prototype is an **HTML mockup**, not Compose code. Its job was to settle
layout, hierarchy, copy and colour. **Its CSS is not a template to port.** Porting the
CSS structure would produce a Compose app that looks like a website. This plan
rebuilds the same design decisions natively in Jetpack Compose + Material 3.

Three rules that carry the design:

1. **Commit to the surface before the tokens.** Each screen is one archetype:
   Upcoming/Search/Calendar = *Explore* (filters and scanning beat decoration);
   the add wizard and Settings = *Configure* (progressive disclosure, low decoration);
   the birthday card = an *artifact*, not a screen.
2. **Colour comes from tokens, never literals.** One palette file. No `Color(0xFF...)`
   in a screen file. This is the single most important rule in the migration.
3. **The card gradient must stay in a light luminance band.** Measured, not guessed —
   see §5.3. This is the one place where a beautiful design can become unreadable.

---

## 1. What is actually being replaced

| Current | Lines | Disposition |
|---|---|---|
| `ui/theme/Color.kt` | 32 | **Replace wholesale** |
| `ui/theme/Type.kt` | 124 | **Replace wholesale** — currently 100% `FontFamily.Default` |
| `ui/theme/Theme.kt` | 116 | **Rewrite** — keep the insets/status-bar logic, replace the schemes |
| `ui/components/LuminaComponents.kt` | 699 | **Delete** — 12 composables, glassmorphism, 90 call sites |
| `ui/components/BirthdayCard.kt` | 355 | **Rewrite** as the share artifact |
| 6 screen files + `BirthdayApp.kt` | ~4,250 | **Restyle**, preserving all ViewModel wiring |

**Migration surface:** 30 distinct `Lumina*` symbols across ~90 call sites. Highest
usage: `LuminaGlassCard` (21), `LuminaHeader` (11), `LuminaBackground` (11),
`LuminaBirthdayCard` (7), `LuminaChip` (6), `LuminaTextField` (5).

**The data layer does not change.** No new entities, no schema migration, no DAO
change. `Birthday` already carries `name`, `birthDate`, `relationship`, `isPinned`,
`notes`, `notificationOffsets`, `notificationTime`, `imageUri`. Everything the new UI
needs already exists:

- **Age** — `SafeDateCalculator.calculateAge()` (`domain/util/SafeDateCalculator.kt:123`)
- **Zodiac** — `ZodiacUtils.getZodiacSign(month, day)` (`domain/util/ZodiacUtils.kt:6`)

These two are the personalisation engine for the shareable card, and they are already
pure, tested domain logic. **No backend is required for any of this.**

---

## 2. The palette — exact values

Generated from source `#C98A12` with Google's own HCT algorithm
(`@material/material-color-utilities`), not hand-picked. Secondary (rosewood `#8F4B3D`)
and tertiary (plum `#844788`) are separate hue families — that separation is what keeps
person avatars visually distinct instead of all rendering as the same gold.

**Light**

```
primary            #815600     primaryContainer     #FFDDB1
onPrimary          #FFFFFF     onPrimaryContainer   #291800
secondary          #8F4B3D     secondaryContainer   #FFDAD3
onSecondary        #FFFFFF     onSecondaryContainer #3A0A02
tertiary           #844788     tertiaryContainer    #FFD6FB
onTertiary         #FFFFFF     onTertiaryContainer  #36003D
error              #BA1A1A     errorContainer       #FFDAD6
onError            #FFFFFF     onErrorContainer     #410002
surface            #FFF8F3     surfaceVariant       #FFDDB1     onSurfaceVariant #614000
onSurface          #291800     surfaceBright        #FFF8F3     surfaceDim      #FFD395
surfaceContainerLowest #FFFFFF  surfaceContainerLow  #FFF1E3     surfaceContainer #FFEBD3
surfaceContainerHigh  #FFE4C2  surfaceContainerHighest #FFDDB1
outline            #A16C00     outlineVariant       #E9C08A
inverseSurface     #442B00     inverseOnSurface     #FFEEDB     inversePrimary  #FFBA49
```

**Dark**

```
primary            #FFBA49     primaryContainer     #614000
onPrimary          #442B00     onPrimaryContainer   #FFDDB1
secondary          #FFB4A5     secondaryContainer   #6C2D22     onSecondaryContainer #FFDAD3
tertiary           #F7AEF7     tertiaryContainer    #6A2F6E     onTertiaryContainer  #FFD6FB
error              #FFB4AB     errorContainer       #93000A     onErrorContainer     #FFDAD6
surface            #1E1100     surfaceVariant       #614000     onSurfaceVariant     #FFBA49
onSurface          #FFDDB1     surfaceBright        #503400     surfaceDim           #1E1100
surfaceContainerLowest #170C00  surfaceContainerLow  #291800     surfaceContainer     #2E1C00
surfaceContainerHigh  #3C2600  surfaceContainerHighest #4A2F00
outline            #C28408     outlineVariant       #614000
inverseSurface     #FFDDB1     inverseOnSurface     #442B00     inversePrimary      #815600
```

> **`error` is a deviation from the generator.** Running the HCT algorithm over a single
> hue makes `error` come out saffron, because it is derived from the source hue. A
> celebration app that shows saffron for a validation error is useless as an error
> signal. The values above are real M3 reds (`#BA1A1A` light / `#FFB4AB` dark). Use
> them. Do not "fix" this back to the generated value.

**Verification already done** (in the prototype, both themes):
666 text/background pairings, **0 WCAG AA failures** — minimum 6.12:1 light, 7.24:1 dark.

---

## 3. Typography — the biggest single quality win

`Type.kt` currently sets `fontFamily = FontFamily.Default` on **all 13 styles**. The
app already depends on `ui-text-google-fonts:1.6.0` (`app/build.gradle.kts:122`) but has
no font resources at all — `app/src/main/res/font/` **does not exist**. So this is not a
new dependency; it is activating one that is already there.

**Fraunces** (display serif, variable: `opsz` 9–144, `wght` 400–800, `SOFT` 0–100,
`WONK` 0–1) for headlines and the card; **Figtree** for everything else.

| Role | Font | Size | Weight | Notes |
|---|---|---|---|---|
| `displayLarge` | Fraunces | 45 | 600 | `opsz 144, SOFT 40, WONK 1` |
| `displayMedium` | Fraunces | 36 | 600 | `opsz 90, SOFT 45, WONK 1` |
| `displaySmall` | Fraunces | 30 | 600 | `opsz 60, SOFT 44, WONK 1` |
| `headlineLarge` | Fraunces | 28 | 600 | |
| `headlineMedium` | Fraunces | 24 | 600 | |
| `headlineSmall` | Fraunces | 20 | 600 | screen titles |
| `titleLarge` | Figtree | 22 | 700 | |
| `titleMedium` | Figtree | 16 | 700 | |
| `titleSmall` | Figtree | 14 | 700 | person names |
| `bodyLarge` | Figtree | 16 | 400 | |
| `bodyMedium` | Figtree | 14 | 400 | |
| `bodySmall` | Figtree | 12 | 400 | supporting copy |
| `labelLarge` | Figtree | 14 | 700 | buttons |
| `labelMedium` | Figtree | 12 | 700 | chips, nav labels |
| `labelSmall` | Figtree | 11 | 700 | the "Made with" line |

**Fetching the fonts.** `ui-text-google-fonts` is cert-pinned and fails offline. Two steps:

1. Create `app/src/main/res/font/` and download the static TTFs from Google Fonts into
   it (Fraunces 600; Figtree 400, 500, 700). **Prefer static instances over the
   variable font** — a variable TTF in `res/font` gives no axis control through
   `Font()`, and the SOFT/WONK axes are what make the display type feel considered.
2. Declare them in a new `ui/theme/Fonts.kt` as two `FontFamily`s, and reference
   those in `Type.kt`.

```kotlin
// ui/theme/Fonts.kt
val Fraunces = FontFamily(
    Font(R.font.fraunces_semibold, FontWeight.SemiBold),
)
val Figtree = FontFamily(
    Font(R.font.figtree_regular, FontWeight.Normal),
    Font(R.font.figtree_medium,   FontWeight.Medium),
    Font(R.font.figtree_bold,     FontWeight.Bold),
)
```

> **If the download fails**, keep the `FontFamily.Default` fallback and continue with
> colour and layout. Do not block the whole migration on font files.

> **A note on the variable axes.** The prototype sets `SOFT`/`WONK` via CSS. Compose
> cannot animate those on a static TTF. The *effect* is achieved by choosing static
> instances cut at a suitable `SOFT` value. Do not try to port the CSS
> `font-variation-settings` string into Compose — it does not exist there.

---

## 4. Dependencies — one deliberate, contained change

**Decision: stay on the stable Compose BOM. Do not take the M3 alpha.**

`app/build.gradle.kts:85` pins `compose-bom:2024.02.01`. `MeshGradientPainter` does not
exist there — it arrived in Compose UI 1.12 (2025). Taking the alpha to get it would
drag in a year of M3 breaking changes across the whole app for one decorative effect.

The mesh gradient is replaced instead by a **clamped multi-stop `Brush.linearGradient`
whose stops are derived from the person's birth data** (§5.3). Visually this lands very
close to the mesh, is fully controllable, and keeps the app on stable releases.

**No dependency changes are required.** If the font step in §3 is done, the build is
unchanged.

---

## 5. Screen-by-screen

### 5.1 Design system (`ui/theme/`, `ui/components/`)

Create **`ui/components/BirfDae/`** as the new system. Delete `LuminaComponents.kt` in
the same commit that stops its last call site (see §7, stage 3).

Required pieces, each replacing a named Lumina symbol:

| New | Replaces | Notes |
|---|---|---|
| `SaffronBackground` | `LuminaBackground` | Flat `surface`. **No gradient, no blur.** |
| `SectionHeader` | `LuminaHeader` | Title + optional trailing action |
| `SurfaceCard` | `LuminaGlassCard` (21 uses) | `surfaceContainer`, 20dp radius, **no** `backdrop-filter` |
| `PersonRow` | `LuminaBirthdayCard` | Avatar + name + date + countdown |
| `SaffronChip` | `LuminaChip` | 44dp min height/width |
| `SaffronTextField` | `LuminaTextField` | 48dp min height |
| `PersonAvatar` | `LuminaAvatar` | Tinted from primary/secondary/tertiary |
| `SaffronSearchField` | `LuminaSearchBar` | |
| `SaffronBadge` | `LuminaBadge` | |
| `SaffronDateField` | `LuminaDatePickerField` | wraps existing `DatePickerField.kt` |
| `SaffronButton` | — | primary / secondary / outlined / text |
| `CountdownPill` | — | days-until, the "6 days" numeral in Fraunces |

**`SaffronTokens` object** — a single file holding the spacing scale (4/8/12/16/20/24/32),
radii (12/15/17/20/24), and the 44dp touch-target floor. **Every magic number in the
new code must come from here.**

### 5.2 Upcoming (`BirthdayListScreen.kt`)

*Explore surface.* The approved hero replaces the current list header:

- Eyebrow (`✦ Today`) → headline → one supporting line → **Send a wish** primary CTA
- **The "11 of her last 11" streak is the emotional core.** It is computed from
  `notificationOffsets` / history — see §8 for the honest fallback when there is no
  history yet. Do not fabricate it. See §6.
- "Next up" (≤30 days) and "Later this year" sections, using `PersonRow`
- Bottom nav: **Upcoming · Calendar · + · Search · Settings** (matches `BirthdayApp.kt`)

### 5.3 The birthday card — the viral artifact (`BirthdayCard.kt`)

This screen *is* the product's growth mechanism, so it gets the most care.

- A 300dp-min card with a **birth-data-derived gradient**
- Big name in Fraunces, `opsz`-appropriate weight
- `age · zodiac · birth date` line, e.g. *"53 years · Pisces · born 2 March"*
- A short warm line, **re-rollable** — re-rolls the *words only*, never the colours
- Primary CTA: **Share on WhatsApp**; secondary: Copy text / Save image
- Tone chips: Warm / Funny / Sincerest / Short

**The gradient rule (read twice — this is a measured constraint, not taste).**

In the prototype the quote initially measured **1.71:1** against the dark end of the
gradient. The automated contrast audit *did not catch it*, because it only reads solid
background colours. The fix was to keep the whole gradient inside a light band:

```
light: #FFECCD → #FFD696 → #F7B560 → #E29242
dark:  #48260A → #55300F → #6B3E08 → #774713
```

Measured with the dark overlay applied, the same text now reads **6.47:1 light / 11.26:1
dark**. That is the budget: **any generated gradient must keep its darkest stop at
luminance ≥ 0.24 so dark ink clears 4.5:1.**

**Deriving a per-person gradient.** Rotate hue from the birth data; never vary lightness
much:

```kotlin
fun gradientFor(birthDate: LocalDate): List<Color> {
    val seed   = (birthDate.monthValue * 31 + birthDate.dayOfMonth)   // 1..372
    val hueRot = (seed % 12) * 30f                                  // 0..330
    val chroma = 0.06f + (seed % 5) * 0.012f                         // 0.06..0.11
    // Build 4 stops in a FIXED lightness band, then rotate hue only.
    return LightnessBand.of(hueRot, chroma)  // clamp: never exceed the band
}
```

> **The failure mode to avoid:** letting per-person variation drift into darker/lighter
> ranges. That is exactly what produced the 1.71:1 failure. Variety must come from
> **hue**, never from lightness.

**Share** — WhatsApp deep link (decided):

```kotlin
val text = "Amma turns 53 today 🎂 $cardUrl"
val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/?text=${Uri.encode(text)}"))
context.startActivity(Intent.createChooser(intent, "Share on WhatsApp"))
```

Guard with `resolveActivity`; if WhatsApp is absent, fall back to `ACTION_SEND` and
surface a clear message. **Do not crash on a device without WhatsApp.**

### 5.4 Add wizard (`AddEditBirthdayScreen.kt`)

*Configure surface.* Keep the existing 3-step structure and all ViewModel wiring — the
step indicator, `uiState.step`, and `nextStep()`/`previousStep()` logic are correct and
must not be rewritten. Restyle to the approved design:

- Step 1 Identity: name field, relationship chips, optional photo
- Step 2 Date: date field, quick-pick chips, and the **"This unlocks 53 years · Pisces"**
  preview card (this is what sells entering the year)
- Step 3 Personalization: remind toggle, lead-time chips, tone chips
  *(note: step 3 is Personalization, not notifications — match the existing code)*

### 5.5 Calendar, Search, Settings, Backup, Notification settings

Restyle to tokens. Two specific notes:

- **Calendar** — Sept 2026 starts on a **Tuesday**. The grid must compute the leading
  blanks from the real first-of-month weekday, not a hardcoded offset.
- **Settings** — the "Use wallpaper colours" row is the dynamic-colour switch. Default
  **off**; the brand palette is fixed. `dynamicColor` is already `false` in `Theme.kt`.

### 5.6 Edge states (from the approved prototype)

Three states that a real app must have and a mockup usually hides:

- **First run / empty** — cake mark, one sentence, one CTA. No fake data.
- **Validation** — inline error under the field, **Continue stays disabled**, and typed
  data is preserved. Wire this to the existing
  `domain/validation/BirthdayValidator.kt` (`ERROR_NAME_REQUIRED` at line 25, the check
  at line 93) rather than inventing new validation. The use case
  `domain/usecase/AddBirthdayUseCase.kt` is the other half of that path.
- **Overdue** — the day-after state. Warm-urgent, not alarming red. Offers
  "Send a belated wish" *and* "Not this year". The second option matters: it is the
  honest exit from guilt-tripping.

---

## 6. The one thing not to build yet

The prototype's hero says *"You've remembered 11 of her last 11."* **The app has no
history table and no schema for that.** `Birthday` records `createdAt` but not
"reminders that were actually acknowledged".

**Do not fabricate the streak.** Ship the hero without it, or with an honest variant:

- `"53 today · 2 March"` — always true
- `"On your list since 2024"` — true from `createdAt`

When a streak *is* wanted, it needs a `reminder_events` table, a migration, and a
definition of "remembered" (notification delivered? opened?). That is a separate piece
of work with a real product decision in it. **Flag it; do not fake it.**

---

## 7. Staged delivery

Each stage compiles and is independently reviewable. Do not attempt this in one pass —
90 call sites is too much to verify at once.

**Stage 1 — Tokens (no visual change yet)**
Add `Color.kt` / `Type.kt` / `Fonts.kt` / `Spacing.kt`. Wire the new `Typography` into
`Type.kt`. Build. Nothing visual changes until the schemes swap.

**Stage 2 — Theme swap**
Replace the schemes in `Theme.kt`. Keep the existing status-bar / insets logic
(`Theme.kt:98-105`) untouched — it is correct. Build, screenshot, **compare against the
prototype**. Expect it to look wrong: the components still use Lumina tokens. That is
fine and expected.

**Stage 3 — New component set**
Create `ui/components/BirfDae/`. Migrate **one screen** (start with `SearchScreen` —
smallest) end-to-end. Then migrate the rest. Delete `LuminaComponents.kt` only when
`grep -r Lumina app/src` returns nothing.

**Stage 4 — Screens**
Restyle the 6 screens + `BirthdayApp.kt`, one at a time, building after each.

**Stage 5 — The card**
`BirthdayCard.kt` rewrite: gradient, share intent, re-roll. This is the piece with
real logic — do it on its own so it can be tested properly.

**Stage 6 — Fonts**
Download TTFs, wire `Fonts.kt`, re-check every screen's line breaks. Type changes
metrics; expect to re-tune spacing.

**Stage 7 — Edge states + polish**
Empty/validation/overdue. Then the pass described in §9.

> **Order matters:** fonts last, deliberately. Type metrics change and will force
> re-tuning of every screen that came before. Doing them first means doing it twice.

---

## 8. Testing

Existing convention: JUnit 4 + Mockito-Kotlin + Turbine, `*SimpleTest.kt` for
compilation checks.

**Worth writing (real logic, not snapshot noise):**

1. `gradientFor` — **assert every generated gradient's darkest stop has luminance
   ≥ 0.24.** This is the regression test for the 1.71:1 bug. Parameterise over all 372
   valid month/day seeds.
2. `gradientFor` — determinism: the same birth date always yields the same gradient
   (a person must not see their card change between openings).
3. Age and zodiac rendering — `SafeDateCalculator.calculateAge` and
   `ZodiacUtils.getZodiacSign` already exist; test the *mapping into card text*, not the
   functions themselves.
4. Share-intent construction — correct package, correct URI-encoded text, and the
   no-WhatsApp fallback path.

**Do not write** screenshot tests for the visual restyle — they will fail on every
spacing tweak and teach nobody anything.

---

## 9. Definition of done

- [ ] `./gradlew build` passes (includes `ktlintCheck` and `lint`)
- [ ] `grep -r Lumina app/src` returns nothing; `LuminaComponents.kt` deleted
- [ ] `grep -rn "Color(0x" app/src/main/java/com/birthdayreminder/ui/screens` returns
      nothing — no colour literals in screens
- [ ] `grep -rn "FontFamily.Default" app/src/main/java/com/birthdayreminder/ui` returns
      nothing
- [ ] Fraunces and Figtree actually render (screenshot, not assumption)
- [ ] All 13 screens match the approved prototype
- [ ] Every generated card gradient measured ≥ 4.5:1 against its ink
- [ ] Share works on a device with WhatsApp **and** one without
- [ ] No fabricated streak data (§6)
- [ ] Light and dark both checked
- [ ] Dark theme checked on a real device, not only in the preview

---

## 10. Risks

| Risk | Likelihood | Mitigation |
|---|---|---|
| Fonts unavailable offline | Medium | §3 has an explicit fallback; do not block on it |
| Per-person gradients drift dark | **High if unguarded** | Clamp lightness in `gradientFor`; §8 test 1 is the guard |
| Line breaks shift after fonts land | Certain | Fonts are stage 6 for this reason; re-check every screen |
| 90 Lumina call sites missed | Medium | §9's `grep` gate is mechanical, not a judgement call |
| `ui-text-google-fonts` cert-pinning fails | Low | Static TTFs in `res/font` avoid the network entirely |
| Type changed, screenshots stale | Medium | Re-shoot after stage 6, not before |

---

## 11. Reference

- Approved prototype — `docs/design/prototype/Birf Dae — UI Concept v2.html`
  (tabs: Core / Add flow / Settings & backup / Edge cases; light–dark toggle)
- Palette board (12 candidates, all AA) — `docs/design/prototype/Birf Dae — Palette Options.html`
- Screenshots — `docs/design/prototype/shots/`
- Virality research — `docs/design/research-virality.md`
- Visual references — `docs/design/research-visual-references.md`

**The competition is real:** `Yaad` (`com.killerpath.yaad`, Hindi/English/Hinglish)
already ships quiz + coins, and utility-only birthday apps top out around 10K installs.
The differentiator is not the quiz or the coins — it is a card worth forwarding to one
named person. Build that well.
