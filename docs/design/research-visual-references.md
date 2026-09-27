# Design Reference Catalog — Birthday Reminder App (India, M3 / Compose)

All values below are **extracted from source of truth**, not recalled:
- Type scale, shape scale, motion springs, baseline palette: `androidx/androidx` `androidx-main`
  (`compose/material3/material3/src/commonMain/kotlin/androidx/compose/material3/tokens/`)
- M3 prose specs: mirror of m3.material.io → `github.com/Glavo/md3-reference-hub`
  (snapshot `2026-05-21`, lastmod `2026-05-06`)
- Palettes: generated with `@material/material-color-utilities` HCT/TonalPalette, then
  WCAG 2.2 contrast-audited programmatically.

---

## 1. Material 3 Expressive — what actually shipped

Announced Google I/O **2025-05-13** (Android Show I/O). Sources:
- https://m3.material.io/blog/building-with-m3-expressive
- https://arstechnica.com/gadgets/2025/05/google-reveals-vibrant-material-3-expressive-coming-soon-to-a-pixel-near-you
- https://www.theverge.com/news/664316/android-material-three-expressive-design-ui-io

**Research backing (Google's own numbers):** 46 studies, 18,000+ participants.
- Key UI elements found **up to 4× faster** in expressive screens.
- Expressive designs score higher on playfulness, energy, creativity, friendliness.
- "Expressive designs are preferred by people of all ages" (Google's claim).

**Four new/updated style systems:**

| System | What it is |
|---|---|
| Motion-physics | Spring tokens replace fixed-duration tweens. *Spatial* springs for movement, *effects* springs for color/opacity. |
| Emphasized typography | 15 new type styles added to the existing 15 baseline → 30 total. |
| Expanded shape library | 35 shapes + shape morph animation. |
| Vibrant color schemes | Wider accessible palettes. |

**14 components new/updated:** App bars · Button groups (NEW) · Common buttons · Extended FAB ·
FAB menu (NEW) · FABs · Icon buttons · Loading indicator (NEW) · Navigation bar · Navigation rail ·
Progress indicators · Sliders · Split button (NEW) · Toolbars (NEW)

**7 expressive tactics (use these as a checklist):**
1. Use a variety of shapes (mix round + square for tension)
2. Apply rich and nuanced colors (hierarchy via surface tones)
3. Guide attention with typography (emphasized styles)
4. Contain content for emphasis
5. Add fluid and natural motion
6. Leverage component flexibility
7. Combine tactics to create **hero moments** ← this is the "wow" lever

### Library status (verified, 2026-09-09 release table)
- **Stable: `androidx.compose.material3:material3:1.4.0`**
- **Expressive: `1.5.0-alpha28`** (Expressive was alpha-gated for a long time)
- `material3-adaptive-navigation-suite:1.5.0-alpha27`
- Source: https://developer.android.com/jetpack/androidx/releases/compose-material3
- ⚠️ **Flagging: the `1.5.0-alpha*` artifacts are alpha.** Stable 1.4.0 does **not** contain
  Expressive components/`MotionScheme`/`MaterialShapes`. Whether to take the alpha dep or
  hand-roll the springs is a real project decision — flagging rather than assuming.

### `MaterialShapes` — all 35 (from the Kotlin API reference)
`Arch, Arrow, Boom, Bun, Burst, Circle, ClamShell, Clover4Leaf, Clover8Leaf, Cookie12Sided,
Cookie4Sided, Cookie6Sided, Cookie7Sided, Cookie9Sided, Diamond, Fan, Flower, Gem, Ghostish,
Heart, Oval, Pentagon, Pill, PixelCircle, PixelTriangle, Puffy, PuffyDiamond, SemiCircle,
Slanted, SoftBoom, SoftBurst, Square, Sunny, Triangle, VerySunny`
Source: https://developer.android.google.cn/reference/kotlin/androidx/compose/material3/MaterialShapes
(`@ExperimentalMaterial3ExpressiveApi`, added in `1.5.0-alpha29`)

**Birthday-app relevance:** `Sunny`, `VerySunny`, `Burst`, `SoftBurst`, `Clover4Leaf`,
`Flower`, `Cookie9Sided`, `Puffy`, `Heart`, `Boom`. Use on **avatars and hero cards only** —
M3's own guidance is "use shapes sparingly to provide a stronger emphasis and moments of delight."

---

## 2. Type scale — exact tokens

From `TypeScaleTokens.kt` (`// VERSION: v0_103`). These are **sp**, not arbitrary.

### BASELINE (the 15 originals)
| Style | Size | Line height | Tracking | Weight |
|---|---|---|---|---|
| displayLarge | 57.sp | 64.sp | **-0.2.sp** | Regular |
| displayMedium | 45.sp | 52.sp | 0.sp | Regular |
| displaySmall | 36.sp | 44.sp | 0.sp | Regular |
| headlineLarge | 32.sp | 40.sp | 0.sp | Regular |
| headlineMedium | 28.sp | 36.sp | 0.sp | Regular |
| headlineSmall | 24.sp | 32.sp | 0.sp | Regular |
| titleLarge | 22.sp | 28.sp | 0.sp | Regular |
| titleMedium | 16.sp | 24.sp | 0.2.sp | Medium |
| titleSmall | 14.sp | 20.sp | 0.1.sp | Medium |
| bodyLarge | 16.sp | 24.sp | 0.5.sp | Regular |
| bodyMedium | 14.sp | 20.sp | 0.2.sp | Regular |
| bodySmall | 12.sp | 16.sp | 0.4.sp | Regular |
| labelLarge | 14.sp | 20.sp | 0.1.sp | Medium |
| labelMedium | 12.sp | 16.sp | 0.5.sp | Medium |
| labelSmall | 11.sp | 16.sp | 0.5.sp | Medium |

### EMPHASIZED (new in Expressive — 15 more)
| Style | Size | Line | Tracking | Weight |
|---|---|---|---|---|
| displayLargeEmphasized | 57.sp | 64.sp | 0.sp | **Medium** |
| displayMediumEmphasized | 45.sp | 52.sp | 0.sp | Medium |
| displaySmallEmphasized | 36.sp | 44.sp | 0.sp | Medium |
| headlineLargeEmphasized | 32.sp | 40.sp | 0.sp | Medium |
| headlineMediumEmphasized | 28.sp | 36.sp | 0.sp | Medium |
| headlineSmallEmphasized | 24.sp | 32.sp | 0.sp | Medium |
| titleLargeEmphasized | 22.sp | 28.sp | 0.sp | Medium |
| titleMediumEmphasized | 16.sp | 24.sp | 0.15.sp | **Bold** |
| titleSmallEmphasized | 14.sp | 20.sp | 0.1.sp | Bold |
| bodyLargeEmphasized | 16.sp | 24.sp | 0.15.sp | Medium |
| bodyMediumEmphasized | 14.sp | 20.sp | 0.25.sp | Medium |
| bodySmallEmphasized | 12.sp | 16.sp | 0.4.sp | Medium |
| labelLargeEmphasized | 14.sp | 20.sp | 0.1.sp | **Bold** |
| labelMediumEmphasized | 12.sp | 16.sp | 0.5.sp | Bold |
| labelSmallEmphasized | 11.sp | 16.sp | 0.5.sp | Bold |

**Key difference:** emphasized is **not a bigger size** — it's a **heavier weight + tighter tracking**.
Swap in via token name: `displayLarge` → `displayLargeEmphasized`.

**Where Material says to use emphasized:** selection, actions, headlines, editorial treatments;
works on badges, primary buttons, extended FAB, selected list items, selected menu items.
Not applied by default — you opt in.

**Units:** sp on Android; letter spacing is tracked in `em` (tracking_px / size_sp).

### Editorial treatments (M3's term for "wow" type moments)
> "standalone, showcase moments driven by type… In editorial treatments, type can freely
> dominate the screen."

Three canonical uses: **celebrating content**, **voice of the user**, **bespoke functionality**.
This is *exactly* the slot for "Happy Birthday, Aarav — turning 30".

Guard rails: token-ize them, match tone to task, don't mix clashing styles in one layout,
don't mimic personalization theming.

### Typography caveats from M3 (real, not filler)
- **Don't go very light at small sizes** — low-res displays struggle. Use light only at display size.
- **Don't go very heavy at small sizes** — hurts readability.
- **Grade axis** (weight-independent thickening, doesn't reflow) — use **negative grade in dark mode**
  because the same text looks heavier on dark.
- **Optical size** must match type size (Literata runs opsz 7→72).
- Customizing the type scale means you stop receiving Material's token updates.

**Default fonts:** Roboto (default), Roboto **Flex** (variable: Slant, Width, Weight, Grade,
Optical Size + advanced XOPQ/YOPQ/XTRA/YTUC/YTLC/YTAS/YTDE/YTFI), Roboto Serif, Roboto Mono, Noto Sans.
Roboto Flex width axis runs **25 → 150**; grade +150 / −200.
**Font fallback chain:** Roboto Flex → Roboto → Noto Sans.

### M3 supports **two** typefaces
> "The **brand** typeface is used for larger type styles, like Headline and Display, to focus on
> expression. The **plain** typeface is used for smaller type styles, like Body and Label."

This is your hook for a warm celebration app: **serif display + clean sans body.**

---

## 3. Font recommendations (all verified in Google Fonts, with real axis ranges)

Axis ranges below are from Google's own `fonts/metadata/fonts` feed, not from memory.

| Font | Category | Axes (min/def/max) | Role |
|---|---|---|---|
| **Fraunces** ⭐ | Serif | SOFT 0/0/100 · WONK 0/0/1 · opsz 9/14/144 · wght 100/400/900 | **Display.** Warm, soft-serif, "farmers-market sign" character |
| **Figtree** ⭐ | Sans Serif | wght 300/400/900 | **Body/UI.** Geometric-humanist, friendly, not Inter |
| **Gloock** | Serif | static | High-contrast display alt. |
| **Instrument Serif** | Serif | static | Single-weight display alt. |
| **Bricolage Grotesque** | Sans | opsz 12/14/96 · wdth 75/100/100 · wght 200/400/800 | Quirky display |
| **Dancing Script** | Handwriting | wght 400/400/700 | Signature/message accent |
| Literata | Serif | opsz 7/14/72 · wght 200/400/900 | Warm body serif |
| Nunito | Sans | wght 200/400/**1000** | Rounded, very warm |
| Anybody | Display | wdth 50/100/150 · wght 100/400/900 | Extreme wdth for hero numbers |
| Fredoka | Sans | wdth 75/100/125 · wght 300/400/700 | Playful rounded |

**Recommended pairing — "warm but premium":**
- **Brand/display: Fraunces.** Set `opsz` to the *rendered* size (e.g. 57 for displayLarge), `wght` 500–600,
  and nudge `SOFT` up (~30–50) for roundness and `WONK` 0–1 for the quirky alternates.
  The `WONK` axis is the single most under-used "warmth" dial on Google Fonts.
- **Plain/body: Figtree**, wght 400/500. Broad 300–900 range means you can stay on ONE family
  for the whole UI and only swap the display face.
- **Accent only: Dancing Script**, for the handwritten "from" line on a card. Never for labels
  or body — M3 explicitly warns against mixing.

Devanagari note: for Hindi, `Anek Devanagari` (wdth 75–100, wght 200–400) or `Tiro Devanagari Hindi`
(static serif). Not verified as visually paired with Fraunces — flagging as unvalidated.

---

## 4. Color

### The tired default, for reference
M3 baseline palette, `PaletteTokens.kt` (v0_210):
`primary #6750A4` · `primaryContainer #EADDFF` · `tertiary #7D5260` · `tertiaryContainer #FFD8E4`
`neutral98 #FEF7FF` · `neutralVariant90 #E7E0EC` · `outline #79747E`
This is the purple you want to avoid. (The Verge noted the Expressive concept images lean
"bright purples and pinks" — that is the aesthetic Google is chasing, not the one you want.)

### Verified competitor palette — Birday (the 4.8★, 50K+ downloads, M3-Expressive birthday app)
Real values from `app/src/main/res/values/colors.xml`:
`aqua #5abf95` · `brown #c55a2c` · `blue #4285f4` · `green #3ddc84` · `yellow #f4b400` ·
`orange #ff7522` · `red #df5252`; light surface `#f5f5f5`, dark surface `#2f2f2f`.

**I contrast-tested all seven. White text on every accent fill fails WCAG AA:**

| Accent | white-on-accent | verdict |
|---|---|---|
| green `#3ddc84` | 1.78:1 | fail |
| yellow `#f4b400` | 1.85:1 | fail |
| aqua `#5abf95` | 2.25:1 | fail |
| orange `#ff7522` | 2.69:1 | fail |
| blue `#4285f4` | 3.56:1 | fail |
| red `#df5252` | 3.84:1 | fail |
| brown `#c55a2c` | 4.31:1 | fail (closest) |

**This is a concrete, exploitable gap.** The category leader ships seven accent options and
*none* of them can carry white body text. If your app's accent pairs correctly, that alone is
a differentiator on the Play listing.

### Recommended: **"Terracotta"**, source `#B4632A`
Generated with HCT + TonalPalette (the real algorithm), then contrast-audited. **0 AA failures.**

**LIGHT** — `lightColorScheme(`
```
primary            #944A12   onPrimary            #FFFFFF
primaryContainer   #FFDBC8   onPrimaryContainer   #321300
secondary          #755847   onSecondary          #FFFFFF
secondaryContainer #FFDBC8   onSecondaryContainer #2B1709
tertiary           #616133   onTertiary           #FFFFFF
tertiaryContainer  #E8E5AC   onTertiaryContainer  #1D1D00
error              #BA1A1A   onError              #FFFFFF
errorContainer     #FFDAD6   onErrorContainer     #410002
surface            #FFF8F5   onSurface            #201A17
surfaceVariant     #F4DED3   onSurfaceVariant     #52443C
surfaceBright      #FFF8F5   surfaceDim           #E4D8D2
surfaceContainerLowest #FFFFFF   surfaceContainerLow  #FEF1EB
surfaceContainer       #F8EBE6   surfaceContainerHigh #F2E6E0
surfaceContainerHighest #ECE0DB
outline            #85746B   outlineVariant       #D7C2B8
inverseSurface     #362F2B   inverseOnSurface     #FBEEE9
inversePrimary     #FFB68A   surfaceTint          #944A12
```

**DARK** — `darkColorScheme(`
```
primary            #FFB68A   onPrimary            #522300
primaryContainer   #743500   onPrimaryContainer   #FFDBC8
secondary          #E5BFA9   onSecondary          #432B1C
secondaryContainer #5C4131   onSecondaryContainer #FFDBC8
tertiary           #CBC992   onTertiary           #323209
tertiaryContainer  #49491E   onTertiaryContainer  #E8E5AC
error              #FFB4AB   onError              #690005
errorContainer     #93000A   onErrorContainer     #FFDAD6
surface            #18120F   onSurface            #ECE0DB
surfaceVariant     #52443C   onSurfaceVariant     #D7C2B8
surfaceBright      #3F3834   surfaceDim           #18120F
surfaceContainerLowest #120D0A   surfaceContainerLow  #201A17
surfaceContainer       #241E1B   surfaceContainerHigh #2F2925
surfaceContainerHighest #3A3330
outline            #9F8D84   outlineVariant       #52443C
inverseSurface     #ECE0DB   inverseOnSurface     #362F2B
inversePrimary     #944A12   surfaceTint          #FFB68A
```

**Measured contrast (computed, not claimed):**

| Pair | Light | Dark |
|---|---|---|
| onPrimary / primary | 6.47:1 | 7.70:1 |
| onPrimaryContainer / primaryContainer | 13.20:1 | 7.20:1 |
| onSecondaryContainer / secondaryContainer | 13.19:1 | 7.19:1 |
| onTertiaryContainer / tertiaryContainer | 13.22:1 | 7.20:1 |
| onErrorContainer / errorContainer | 13.26:1 | 7.24:1 |
| onSurface / surface | 16.37:1 | 14.35:1 |
| onSurfaceVariant / surfaceVariant | 7.21:1 | 5.46:1 |
| onSurfaceVariant / surfaceContainer | 8.00:1 | 9.63:1 |
| onSurface / surfaceContainerHigh | 14.06:1 | 11.10:1 |
| onPrimaryFixedVariant / primaryFixed | 7.20:1 | — |
| inverseOnSurface / inverseSurface | 11.58:1 | 10.17:1 |
| primary / surface (3:1 UI) | ✅ | ✅ |
| outline / surface (3:1 UI) | ✅ | ✅ |

### Four alternates, also AA-clean in both modes
| Name | Source | Light primary | Dark primary | Light container | Light surface |
|---|---|---|---|---|---|
| **Marigold / Saffron** ⭐ | `#C98A12` | `#815600` | `#FEBA4B` | `#FFDDB1` | `#FFF8F3` |
| **Rosewood** | `#8E4A3C` | `#8F4B3D` | `#FFB4A5` | `#FFDAD3` | `#FFF8F6` |
| **Festive Red (India)** | `#B3261E` | `#9C4238` | `#FFB4AA` | `#FFDAD5` | `#FFF8F7` |
| Kerala Green | `#2E6B4F` | `#2C694D` | `#95D4B2` | `#B0F1CD` | `#F8FAF6` |

Saffron reads the most "celebration" without going neon; Rosewood is the most sophisticated.
Both avoid purple entirely.

### Color rules from M3 worth enforcing
- 26 standard roles in 6 groups (primary, secondary, tertiary, error, surface, outline).
- `on*` roles are text/icons **on** their paired parent. `*Container` are **fills, never text**.
- `*Variant` roles = lower-emphasis alternatives.
- The system guarantees **3:1 minimum** on built-in pairings.
- ⚠️ **Improper pairing breaks accessibility as the system contrast level changes.** E.g. using
  `primaryContainer` where `primary` belongs makes text illegible at high system contrast.
  Don't hand-pick roles for looks.

### WCAG 2.2 reference
- Normal text: **4.5:1**. Large-scale text: **3:1**.
- https://www.w3.org/WAI/WCAG22/Understanding/contrast-minimum
- (Large-scale ≈ 18pt / 24px regular, or 14pt / 18.66px bold.)

---

## 5. Shape

**M3 corner-radius scale (10 steps)** — from `ShapeTokens.kt` / m3 spec:

| Step | Value |
|---|---|
| None | 0.dp |
| Extra small | 4.dp |
| Small | 8.dp |
| Medium | 12.dp |
| Large | 16.dp |
| Large increased | 20.dp |
| Extra large | 28.dp |
| Extra large increased | 32.dp |
| Extra extra large | 48.dp |
| Full | `CircleShape` |

Compose field names: `CornerExtraSmall, CornerSmall, CornerMedium, CornerLarge,
CornerLargeIncreased, CornerExtraLarge, CornerExtraLargeIncreased, CornerValueExtraExtraLarge, CornerFull`.
`Shapes(...)` extras include per-corner variants (`CornerLargeTop`, `CornerLargeStart`,
`CornerLargeEnd`, `CornerExtraLargeTop`, …) for menus / split buttons ("inner corners").

**Optical roundness — the rule most people get wrong:**
> "Outer radius − padding = inner radius. For example: 48dp − 14dp = 34dp"

Nested cards must NOT share a corner radius, or the corners read unbalanced.

Also: the shape style family can go **rounded → cut** (straight corners) — use for editorial
contrast, but add padding so a large cut corner doesn't clip content.

**Spacing:** base unit **8dp** (`md.sys.measurement.space100`), range 0×–9×.
Nested units that exist: 2, 4, 6, 10dp. Compose-only token set.
New naming: **padding / margin / gap** + horizontal / vertical / leading / trailing / top / bottom
(legacy tokens used "space").

---

## 6. Motion — the real numbers

`MotionScheme` is selected on the theme. Two built-ins: `standard()` and `expressive()`.
Use `expressive()` — Material's recommendation for prominent UI elements.

### Expressive springs (`ExpressiveMotionTokens.kt`)
```
defaultSpatial  dampingRatio 0.8f   stiffness  380.0f
fastSpatial     dampingRatio 0.6f   stiffness  800.0f
slowSpatial     dampingRatio 0.8f   stiffness  200.0f
defaultEffects  dampingRatio 1.0f   stiffness 1600.0f
fastEffects     dampingRatio 1.0f   stiffness 3800.0f
slowEffects     dampingRatio 1.0f   stiffness  800.0f
```

### Standard springs (`StandardMotionTokens.kt`)
```
defaultSpatial  dampingRatio 0.9f   stiffness  700.0f
fastSpatial     dampingRatio 0.9f   stiffness 1400.0f
slowSpatial     dampingRatio 0.9f   stiffness  300.0f
defaultEffects  dampingRatio 1.0f   stiffness 1600.0f
fastEffects     dampingRatio 1.0f   stiffness 3800.0f
slowEffects     dampingRatio 1.0f   stiffness  800.0f
```
**The whole delta is the damping ratio: 0.8 (expressive, visible overshoot) vs 0.9 (standard,
nearly critically damped).** That single float is what "feels springy."

### Web conversion (from m3 spec) — if you need ms/durations
| Spring | cubic-bezier | Duration |
|---|---|---|
| Expressive fast spatial | `0.42, 1.67, 0.21, 0.90` | 350ms |
| Expressive default spatial | `0.38, 1.21, 0.22, 1.00` | 500ms |
| Expressive slow spatial | `0.39, 1.29, 0.35, 0.98` | 650ms |
| Expressive fast effects | `0.31, 0.94, 0.34, 1.00` | 150ms |
| Expressive default effects | `0.34, 0.80, 0.34, 1.00` | 200ms |
| Expressive slow effects | `0.34, 0.88, 0.34, 1.00` | 300ms |
| Standard fast spatial | `0.27, 1.06, 0.18, 1.00` | 350ms |
| Standard default spatial | `0.27, 1.06, 0.18, 1.00` | 500ms |
| Standard slow spatial | `0.27, 1.06, 0.18, 1.00` | 750ms |
| Standard fast/default/slow effects | same as expressive | 150/200/300ms |

Compose API surface: `MotionScheme.fastSpatialSpec()`, `defaultSpatialSpec()`, `slowSpatialSpec()`,
`fastEffectsSpec()`, `defaultEffectsSpec()`, `slowEffectsSpec()`.
**Spatial for movement, effects for color/alpha** — the effects springs never bounce spatially.

### Other premium-motion patterns in the Expressive toolkit
- **Shape morph** — square → circle, any shape → any shape, with a spring. Applied to image
  crops, avatars, FABs. (`androidx.graphics.shapes`, `Morph`.)
- **FlexibleBottomAppBar** — `BottomAppBarDefaults.exitAlwaysScrollBehavior()`;
  `FlexibleFixedHorizontalArrangement`.
- **FloatingActionButtonMenu** + `ToggleFloatingActionButton` + `Modifier.animateFloatingActionButton()`.
- **Floating toolbars** — `VerticalFloatingToolbar` (`floatingToolbarVerticalNestedScroll`),
  `HorizontalFloatingToolbar` with `FloatingToolbarDefaults.VibrantFloatingActionButton`.
- **Haptics tied to motion** — Google's pitch is explicitly "springy animations … with haptics to
  underline your actions." A confetti burst with a haptic tick is the on-brand moment.
- **Confetti library:** Konfetti (DanielMartinus), ~3.4k★, has a Jetpack Compose module.
  https://github.com/DanielMartinus/Konfetti — Birday already uses it.
- **Shimmer** (facebook/shimmer-android) — Birday ships it, optional.

---

## 7. Birthday / celebration UX — what the good ones do

### Birday — the reference app (4.8★, 4.92K reviews, 50K+ downloads, 30+ locales, open source)
https://github.com/m-i-n-a-r/birday · https://play.google.com/store/apps/details?id=com.minar.birday

What it does well, and what's worth stealing:
- **Notifications on the day, at a user-chosen time** (not a fixed 9am) + optional grouping for
  same-day events. **Up to 21 days before, multiple lead times.** For a gift-buying app, lead
  time is the actual product — 21 days is the number.
- **Favorites/ignore tri-state** — the single most important retention mechanic in this category.
- **"Quick glance" row** — next 10 days in one strip. Answer to "what's today?".
- **Timeline grouped by month or alphabetically**, plus a **yearly overview**.
- **Zodiac + average-age stats** once you have >5 events. This is the shareable, delightful
  surface — exactly M3's "celebrating content" editorial treatment.
- **Confetti + shimmer + animated notification icon**, each individually disableable.
- **Favorite/ignore lets you see detailed info + a countdown per person.**
- **AMOLED black theme** as a separate option (not just dark).
- **12 selectable accents, no restart required.**
- **Animated notification icon** — a genuinely great touch most apps lack.
- **Cloud auto-backup** via Play Services.
- Configurable home-screen widgets (minimal + complete).
- Full M3 Expressive / Monet support.

What it gets wrong (your openings):
- **Accessibility**: all 7 accent colors fail AA with white text (measured above).
- Surfaces are neutral grays (`#f5f5f5` / `#2f2f2f`) — **no warmth at all** despite being a
  celebration app. Big opening for a warm palette.
- Feature sprawl (stats, zodiac, CSV/JSON, 30 locales) competing with the core
  "who's next → wish them" loop.
- Confetti/shimmer are global settings, not attached to *moments* — the joy is a toggle rather
  than a designed beat.

### Apple — iOS 27 birthday UX (real, shipped in betas)
https://appleinsider.com/articles/26/08/11/ios-27-celebrates-your-friends-birthdays-with-fireworks
- During a Phone call, if the contact card has today's birthday → **an animated fireworks banner**.
- Works both when you call **and** when they call you.
- **Provenance microcopy: "Birthday — Found in Contacts."** Apple will not show a fact without
  telling you where it came from. Steal this: always label contact-sourced birthdays.
- Tap → deep-links to the full contact card so the date is verifiable.
- ⚠️ Critique worth designing against: the article's own objection is that you need a reminder
  *before* the day to buy a gift. A same-day-only trigger is too late. Combine Apple's delight
  with Birday's lead time.

### Google Contacts / Calendar
- Google Calendar shows a **Birthdays calendar** but the original complaint that launched Birday
  was that it **doesn't send an automatic notification on the day**. That's still the gap.
- Google Contacts later extended reminder notifications **from birthdays to all saved dates** —
  birthdays are one instance of a general "important date" concept. Supports modeling
  birthdays / anniversaries / name-days / death anniversaries as one `EventType` (Birday does).
- Contacts widgets are Material You, circular avatar-led, 1×1 / 2×1 / 3×1 sizes.
  https://9to5google.com/2023/04/05/google-contacts-material-you-widgets

### WhatsApp / Instagram
- The sticker's job is **in-the-moment, low-friction, already-inside-the-conversation**:
  type a name, get a rendered card, hit send. No account, no setup.
- WhatsApp's 2025 move was **creating custom sticker packs in-app** (April 2025) — user-authored
  art is the engagement loop.
- ⚠️ **Honest gap in my research:** I could not find a first-party WhatsApp/Instagram
  *birthday-specific* UX teardown. Searches returned SEO/agency listicles, not engineering or
  design writeups. Treat specifics here as unverified; the *pattern* (zero-setup,
  in-context, one-tap) is what's transferable.

### Case study (real, with competitor matrix)
https://medium.com/design-bootcamp/case-study-designing-an-app-to-help-people-remember-important-birthdays-cce2487da68e
Documents the Facebook / Android-app / Slackbot landscape and concludes on **zero-setup
import from contacts** as the critical friction reducer. Also notes Facebook **removed birthday
export** for privacy — so don't plan a Facebook import.

---

## 8. Competitive design language context (2025–2026)

- **Samsung One UI 8.5** (beta Dec 2025, rolling out 2026 with Galaxy S26) is importing
  iOS 26 "Liquid Glass" cues: floating back buttons, floating nav bars with rounded sides in the
  dialer, transparency in Gallery, **3D "pop top" buttons in Calculator**. Not full glass.
  https://9to5google.com/2025/12/09/samsung-one-ui-8-5-liquid-glass-inspiration
- **Xiaomi HyperOS** — 100+ refined system animations, redesigned home screen, new icon set,
  HyperOS 3 leaks point to the boldest overhaul yet. I did not extract concrete token values
  from Xiaomi's pages; treat the "100+ animations" figure as their own marketing claim.
- **One UI 8** (rollout began 2025-10-03) is primarily AI + large-screen UX, not a visual
  overhaul. https://news.samsung.com/global/samsung-begins-official-rollout-of-one-ui-8-to-galaxy-devices
- **Edge-to-edge is mandatory**: targeting SDK 35+ on Android 15+ enforces it.
  https://developer.android.com/develop/ui/views/layout/edge-to-edge
- **The Verge's read on M3 Expressive**: "bright purples and pinks … appeal to a younger
  demographic," and a skeptic's note that it "still has a little more to do" for teens. Worth
  knowing the aesthetic is aimed at Gen Z, not at a 30–55 Indian user buying a gift.

---

## 9. Uncertainties — flagged, not guessed

1. **M3 Expressive is still alpha** (`1.5.0-alpha28`); stable is `1.4.0`. Anything about
   "the current state of the art" is bleeding-edge. Decide alpha dep vs. hand-rolled springs.
2. **WhatsApp/Instagram birthday UX** — no first-party teardown found. Pattern-level only.
3. **Devanagari pairing** — `Anek Devanagari` / `Tiro Devanagari Hindi` exist on Google Fonts and
   have real axis ranges, but I did not visually verify they pair well with Fraunces.
4. **HyperOS token values** — not extracted, only marketing claims from Xiaomi's own pages.
5. **m3.material.io numeric token tables are JS-rendered** and did not extract. I sourced every
   number from AndroidX source + the md3-reference-hub markdown mirror instead, which is
   *more* authoritative, but the mirror is a third-party snapshot (2026-05-21).
6. I could not pull live screenshots of Birday/Google Contacts widgets in this environment, so
   layout descriptions come from source code + store listings, not from viewing the pixels.
