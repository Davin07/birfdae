# Local Android Dev Setup (CLI-only, no Android Studio)

**Project:** `birfdae` — BirthdayReminderApp
**Repo:** `D:\Hmmm\Projects\Vibe\birfdae`
**Goal:** a reliable build → test → install → observe loop with no Android Studio installed.

---

## 0. Verified starting state

Everything below was checked on this machine, not assumed.

| Thing | State |
|---|---|
| Android SDK | `D:\Programs\Android` — platforms 34 + 36, build-tools 34/36/36.1-rc1, platform-tools 36.0.2, emulator, system-images |
| `local.properties` | already correct: `sdk.dir=D:\\Programs\\Android` |
| SDK licences | already accepted (`licenses/android-sdk-license`) |
| `cmdline-tools` | installed at `D:\Programs\Android\cmdline-tools\latest` (Android CLI 1.0.16406183). `sdkmanager` now deprecated in favour of the `android` CLI |
| AVDs | **zero** (`~/.android/avd/` is empty) |
| System image | `D:\Programs\Android\system-images\android-36\google_apis_playstore\x86_64` (2.3 GB) — lives in the **SDK**, not Studio |
| JDK on PATH | Oracle **JDK 24** (`C:\Program Files\Java\jdk-24`), via `...\Oracle\Java\javapath` |
| `JAVA_HOME` | **unset** |
| Only JDK 17/21 on disk | Studio's bundled **JBR 21.0.8** (`D:\Programs\Android Studio\jbr`) |
| `ANDROID_HOME` | unset |
| `adb` on PATH | no |
| Android Studio | 2025.2.2, installed app (registry entry + `D:\Programs\Android Studio\uninstall.exe`), 3.1 GB |
| Studio caches | `%LOCALAPPDATA%\Google\AndroidStudio2025.2.2` = 675 MB, plus a stale `AndroidStudio2025.1.2` = 829 MB |
| Package managers | `winget`, `choco` 2.6.0 |
| `scrcpy` | not installed |
| Cloudflare WARP | installed, **currently Disconnected** |
| Build toolchain | Gradle 8.9 (wrapper), AGP 8.2.2, Kotlin 1.9.22, ktlint 12.1.0, `compileSdk`/`targetSdk` 34, `minSdk` 21, `jvmTarget` 17 |
| CI | GitHub Actions, Temurin **17**, runs `./gradlew lint`, ktlint, build, unit tests |

**Already proven working on this machine:** `:app:assembleDebug` ✅ (38s), `testDebugUnitTest` ✅ (2m 50s), `ktlintCheck` ✅ (12s), `:app:assembleRelease` with R8 + resource shrinking ✅ (4m 08s).

### Two things you must know before starting

1. **Studio's JBR is your only JDK 17/21.** Java 24 is on PATH but is outside AGP 8.2.2 / Kotlin 1.9.22's supported range (they are JDK-17-era; CI pins Temurin 17). **Install the JDK before uninstalling Studio**, or you will have no supported JDK left.
2. **Use `gradlew.bat`, not `./gradlew`** — until Phase 3 fixes it. See Phase 3 for the root cause.

### About the virtual device you were worried about

Uninstalling Studio **will not** delete your system image. The system image lives in the SDK at `D:\Programs\Android\system-images` — a completely separate tree from the app at `D:\Programs\Android Studio`. You also currently have **zero AVDs** defined, so there is no virtual device definition to lose either.

The one real risk is different: Studio records its SDK path as `D:\Programs\Android`, so its uninstaller **may offer to delete the Android SDK**. Decline that. See Phase 8.

---

## Status summary (as of 2026-09-26)

| Phase | State |
|---|---|
| 1 — JDK 17 + `JAVA_HOME` | **Done** — Temurin 17.0.20.101, Gradle confirms `Launcher JVM: 17.0.20.1` |
| 2 — `cmdline-tools` + `ANDROID_HOME` + PATH | **Done** — `android info` → `sdk: D:\Programs\Android`, `adb` 36.0.2 |
| 3 — Repo fix (`gradlew`, `.gitattributes`, CI) | **Done** — [PR #6](https://github.com/Davin07/birfdae/pull/6) open, all CI green, **not yet merged** |
| 4 — Phone over USB | Not started |
| 5 — Phone over wireless adb | Not started |
| 6 — Day-to-day dev loop | Not started (commands already verified working) |
| 7 — `scrcpy` | Not started |
| 8 — Uninstall Android Studio | Not started — **blocked on Phase 8 pre-flight only** |
| 9 — Reclaim SDK disk | Optional |
| 10 — Final verification | Not started |

**Next up: Phase 4** — plug the phone in, enable USB debugging, and confirm `adb devices -l`
sees it. Everything before that is verified working.

> **Note on stale shells:** this document was written from a long-running terminal session that
> predates the environment changes. If `adb` or `android` appear "not found" there, open a
> **new** terminal — PATH and the `ANDROID_*` variables are correctly set in the registry.

---

## Progress checklist

- [x] Phase 1 — Install JDK 17, set `JAVA_HOME` — **DONE**
- [x] Phase 2 — `ANDROID_HOME` + PATH (cmdline-tools already installed) — **DONE**
- [x] Phase 3 — Fix the repo: `gradlew` bug, `.gitattributes`, PR — **DONE** (PR #6 open, **awaiting merge**)
- [ ] Phase 4 — Connect the phone over USB
- [ ] Phase 5 — Switch the phone to wireless adb
- [ ] Phase 6 — Learn the day-to-day dev loop
- [ ] Phase 7 — Install `scrcpy` for screen mirroring
- [ ] Phase 8 — Uninstall Android Studio
- [ ] Phase 9 — Optional: reclaim disk from unused SDK parts
- [ ] Phase 10 — Final verification with Studio gone

---

# Phase 1 — Install JDK 17 and set JAVA_HOME

> **STATUS: COMPLETE (verified).** Temurin 17.0.20.101 installed at
> `C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot`, `JAVA_HOME` set, and Gradle
> reports `Launcher JVM: 17.0.20.1 (Eclipse Adoptium 17.0.20.1+1)`. `assembleDebug` passes on it.

### Why
Gradle needs a JDK in the 17–21 range for this toolchain. Your only one is inside Studio, which is going away.

### Step 1.1 — Install Temurin 17

Open a terminal (a UAC prompt will appear — approve it):

```bash
winget install EclipseAdoptium.Temurin.17.JDK --accept-package-agreements
```

Verified available: `EclipseAdoptium.Temurin.17.JDK` version `17.0.20.101`.

> Do **not** use choco's `temurin` package — it is now a rolling "latest" and currently installs JDK **25**, which is not what you want.
> JDK 21 (`EclipseAdoptium.Temurin.21.JDK`) also works — JBR 21 built this project successfully — but 17 matches CI exactly. Prefer 17.

### Step 1.2 — Find where it landed

Default is `C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot`. Confirm the exact folder name:

```bash
ls -d "/c/Program Files/Eclipse Adoptium"/*
```

You should see one `jdk-17.0.20.101-hotspot` directory. Note the exact name — you need it in the next step.

### Step 1.3 — Set JAVA_HOME

Check whether the Temurin MSI already set it for you:

```bash
echo "JAVA_HOME=[$JAVA_HOME]"
MSYS_NO_PATHCONV=1 cmd /c "echo %JAVA_HOME%"
```

If it is empty or wrong, set it for your user account:

```bash
MSYS_NO_PATHCONV=1 setx JAVA_HOME "C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot"
```

`setx` is safe for single variables like this. **Never use `setx PATH`** — it silently truncates at 1024 characters and will corrupt your PATH. Phase 2 handles PATH properly.

### Step 1.4 — Understand what JAVA_HOME actually controls

This is the important nuance:

- **Gradle uses `JAVA_HOME`.** The wrapper checks `JAVA_HOME` before falling back to `PATH`. So once `JAVA_HOME` points at Temurin 17, your builds use 17 regardless of what `java` resolves to.
- **Your shell's `java` will probably still be 24**, because Oracle's `javapath` is on the system PATH. That is cosmetic for Gradle purposes.

If you *also* want `java -version` to report 17 in your shell, you must remove or deprioritise this system PATH entry:

```
C:\Program Files\Common Files\Oracle\Java\javapath
```

Only do that if nothing else on your machine needs JDK 24. Otherwise leave it alone.

### Step 1.5 — Verify

**Close and reopen your terminal** (and restart the Hermes terminal session) so the new variable is picked up, then:

```bash
cd /d/Hmmm/Projects/Vibe/birfdae
MSYS_NO_PATHCONV=1 cmd /c "gradlew.bat --version"
```

**You should see** in the output:

```
Launcher JVM:  17.0.20 (Eclipse Adoptium ...)
Daemon JVM:    C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot
```

If it still says 21 and `D:\Programs\Android Studio\jbr`, `JAVA_HOME` did not take effect — recheck Step 1.3.

### Step 1.6 — Confirm the build still works

```bash
MSYS_NO_PATHCONV=1 cmd /c "gradlew.bat :app:assembleDebug"
```

Expect `BUILD SUCCESSFUL`. Do not proceed to Phase 8 until this passes.

---

# Phase 2 — cmdline-tools (OPTIONAL), ANDROID_HOME and PATH

> **STATUS: COMPLETE (verified).** All of it is in place:
>
> | Item | Value |
> |---|---|
> | `cmdline-tools` | installed at `D:\Programs\Android\cmdline-tools\latest` (Android CLI 1.0.16406183) |
> | `ANDROID_HOME` | `D:\Programs\Android` |
> | `ANDROID_SDK_ROOT` | `D:\Programs\Android` |
> | PATH | `D:\Programs\Android\platform-tools` and `D:\Programs\Android\cmdline-tools\latest\bin` both present |
>
> Confirmed by `android info` → `sdk: D:\Programs\Android` and `adb version` → 36.0.2.
>
> **Nothing left to do in this phase.** The steps below are kept for reference and for
> re-verification.

### Status: partly done, partly optional

`cmdline-tools` is **already installed** on this machine (see "Completed on this machine" below). Only the environment variables and PATH entries remain.

**Do you need cmdline-tools if you are not using an emulator? No.** Verified on this repo:

| Task | Needs cmdline-tools? |
|---|---|
| `:app:assembleDebug` / `installDebug` | **No** — proven working |
| `:app:assembleRelease` (R8 + resource shrinking) | **No** — proven working, 4m 08s |
| `testDebugUnitTest`, `ktlintCheck`, `lintDebug` | **No** — lint ships inside AGP |
| `adb`, wireless debugging, `logcat`, `scrcpy` | **No** — that is `platform-tools`, already installed |
| Managing SDK packages (add/remove/update) | **Yes** — no CLI alternative once Studio is gone |
| Reading obfuscated release stack traces | **Yes** — `retrace` (release build has `isMinifyEnabled = true`) |

AGP bundles R8 internally and auto-downloads missing SDK packages during a build when a licence is accepted, so the entire build path is self-sufficient.

### Step 2.0 — Already completed on this machine

`cmdline-tools` **is installed** at `D:\Programs\Android\cmdline-tools\latest` (Android CLI 1.0.16406183). Nothing to download.

> ### ⚠️ Important: `sdkmanager` is deprecated
>
> This 2026 release prints a deprecation warning on every invocation:
>
> ```
> WARNING: The SDK Manager CLI tool (sdkmanager) is deprecated. Android CLI will be used instead.
> The 'android' binary can also be found in the cmdline-tools directory, and 'android sdk' is
> the replacement for 'sdkmanager'.
> ```
>
> The replacement is a new unified `android` binary. It still works, so nothing is broken, but
> prefer the new one where an equivalent exists:
>
> | Deprecated | Replacement |
> |---|---|
> | `sdkmanager --list_installed` | `android sdk list` |
> | `sdkmanager <package>` (install) | `android sdk install <package>` |
> | `sdkmanager --licenses` | **No longer needed** — see the licence note below |
> | `avdmanager list avd` | `android emulator list` |
> | `avdmanager create avd` | `android emulator create` |
>
> Check what is installed with either:
>
> ```bash
> android sdk list
> android info
> ```

### Step 2.1 — Your SDK licences are already fine (no action needed)

Earlier drafts of this plan flagged the licence file as non-canonical. **On inspection it is fine** and needs no change:

```
D:\Programs\Android\licenses\android-sdk-license       -> 24333f8a63b6825ea9c5514f83c2829b004d1fee
D:\Programs\Android\licenses\android-sdk-preview-license -> 84831b9409646a918e30573bab4c9c91346d8abd
```

`24333f8a…` is the current `android-sdk-license` hash and is what modern AGP checks. The two older hashes (`8933bad1…`, `d56f5187…`) are for legacy packages that this project will never need. `sdkmanager --licenses` is now a no-op anyway:

```
Warning: The --licenses option is no longer needed.
```

**Leave the licence files alone.** If a future build ever complains that licences have not been accepted, that is a different problem, and `android sdk install <package>` will report it clearly.

### Step 2.2 — Set ANDROID_HOME (this one is NOT optional)

Without it, the new `android` CLI guesses wrong. Verified:

```bash
android info
# sdk: C:\Users\davin\AppData\Local\Android\Sdk      <-- WRONG, does not exist
```

Gradle itself is unaffected (it reads `local.properties`), but `android`, `adb`, and most other tools will look in the wrong place.

```bash
MSYS_NO_PATHCONV=1 setx ANDROID_HOME "D:\Programs\Android"
MSYS_NO_PATHCONV=1 setx ANDROID_SDK_ROOT "D:\Programs\Android"
```

Then re-verify:

```bash
android info
# sdk: D:\Programs\Android                             <-- correct
```

You can also override per-invocation without setting anything:

```bash
android --sdk="D:\Programs\Android" info
```

### Step 2.3 — Add to PATH (use the GUI, not setx)

**This is the one genuinely required step** — without it, `adb` and `android` are not recognised.

Open **System Properties → Environment Variables**:

- Press `Win`, type `environment variables`, choose **Edit the system environment variables**
- Click **Environment Variables…**
- In the **top box (User variables for davin)**, select **Path** → **Edit** → **New**, and add these two entries one at a time:

```
D:\Programs\Android\platform-tools
D:\Programs\Android\cmdline-tools\latest\bin
```

- OK out of all dialogs

> **Do not use `setx PATH` for this.** Your PATH is long enough that `setx`'s 1024-character truncation limit would silently destroy it. The GUI edits the raw value safely.
>
> If you would rather script it, this preserves the unexpanded registry value:
>
> ```powershell
> $p = (Get-ItemProperty -Path 'HKCU:\Environment' -Name Path).Path
> [Environment]::SetEnvironmentVariable('Path', $p + ';D:\Programs\Android\platform-tools;D:\Programs\Android\cmdline-tools\latest\bin', 'User')
> ```

### Step 2.4 — Verify

**Open a fresh terminal** (PATH changes only apply to new processes), then:

```bash
which adb android
adb version
android info
android sdk list
```

**You should see:**
- `adb version` → `Android Debug Bridge version 1.0.41`, `Version 36.0.2-...`
- `android info` → `sdk: D:\Programs\Android`
- `android sdk list` → lists `build-tools/34.0.0`, `build-tools/36.0.0`, `platform-tools`, `platforms/android-34`, `platforms/android-36`, `system-images/...`, `cmdline-tools/latest`

### Step 2.5 — (Only if you ever want an emulator)

Not needed for the physical-phone workflow:

```bash
avdmanager create avd -n pixel_api36 -k "system-images;android-36;google_apis_playstore;x86_64" -d pixel_6
```

Or with the new CLI: `android emulator create --help`

**Resource warning:** an emulator costs 2–4 GB of RAM and real CPU. On a machine where you are avoiding Studio for resource reasons, this is the single biggest thing to skip.

### Reference: if you ever DO need to (re)install cmdline-tools

There is **no winget or choco package** for cmdline-tools — winget's `Google.AndroidCLI` is an unrelated Google tool, and winget only ships Android Studio itself. Download and extract with one command in Git Bash:

```bash
cd "$TMPDIR" \
  && curl -sLO https://dl.google.com/android/repository/commandlinetools-win-16111833_latest.zip \
  && unzip -q commandlinetools-win-16111833_latest.zip \
  && mkdir -p "D:/Programs/Android/cmdline-tools" \
  && mv cmdline-tools "D:/Programs/Android/cmdline-tools/latest" \
  && rm commandlinetools-win-16111833_latest.zip
```

**The `latest` nesting is mandatory — this was verified empirically.** Extracting to `cmdline-tools\` directly fails with:

```
Error: Could not determine SDK root.
Error: Either specify it explicitly with --sdk_root= or move this package into its expected location: <sdk>\cmdline-tools\latest\
```

Get the current build number from the [Android Studio download page → "Command line tools only"](https://developer.android.com/studio#command-line-tools-only).

---


# Phase 3 — Fix the repo

> **STATUS: COMPLETE — PR #6 open, all CI checks pass.**
> https://github.com/Davin07/birfdae/pull/6
>
> Committed on branch `fix/gradle-wrapper-and-lf-endings` (2 commits). Fixes the `gradlew`
> corruption, adds `.gitattributes`, removes the CI shim, fixes the `setup-android` packages
> input, and corrects the `AGENTS.md` Windows syntax. **The PR still needs to be merged.**

### Background: the `gradlew` bug

`./gradlew` was broken — on Linux and macOS too, not just here. **This is now fixed; see the
commit below for the original state.**

`gradlew` line 241 read:

```sh
printf '%s\n' "$@"
```

Upstream Gradle's wrapper has:

```sh
printf '%s\n' "$DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS"
```

Because the script already prepends the launcher's own arguments at line 207–211, using `"$@"` there makes the argument list get **duplicated**. `bash -x` shows it:

```
+ exec java -Dorg.gradle.appname=gradlew -classpath .../gradle-wrapper.jar \
    org.gradle.wrapper.GradleWrapperMain projects --offline \
    -Dorg.gradle.appname=gradlew -classpath .../gradle-wrapper.jar ...   ← duplicated
```

The wrapper's own `-classpath <jar>` then leaks through to Gradle's command-line parser, which reads the short option `-c` with an inline value of `lasspath`, producing:

```
The specified settings file 'D:\Hmmm\Projects\Vibe\birfdae\lasspath' does not exist.
```

That is why `.github/workflows/android.yml` lines 130–134 contain a hand-written replacement `gradlew` — CI has been working around this same bug.

Separately, your working-tree `gradlew` has **CRLF** line endings (246 CR / 246 LF) while the committed blob is pure LF, because `core.autocrlf=true` and there is **no `.gitattributes`**. A shell script with CRLF endings is a second source of breakage.

### Step 3.0 — Protect your in-flight work first

> **Done** — the `SearchViewModel.kt` change was reverted as requested, so the branch contains
> only the wrapper/CI/docs changes.

You have uncommitted work:

```
 M app/src/main/java/com/birthdayreminder/ui/viewmodel/SearchViewModel.kt
```

That is a `DeleteBirthdayUseCase` / `ErrorHandler` refactor that predates this setup work. Phase 3 must not disturb it. Either commit it on its own branch, or stash it:

```bash
cd /d/Hmmm/Projects/Vibe/birfdae
git stash push -m "wip: SearchViewModel error handling" -- app/src/main/java/com/birthdayreminder/ui/viewmodel/SearchViewModel.kt
```

(Remember to `git stash pop` afterwards.)

### Step 3.1 — Create the branch

Your convention is that every change to the default branch goes through a pull request — never a direct push to `master`.

```bash
git checkout -b fix/gradle-wrapper-and-lf-endings
```

### Step 3.2 — Fix `gradlew` line 241

Change:

```sh
        printf '%s\n' "$@" |
```

to:

```sh
        printf '%s\n' "$DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS" |
```

Leave everything else in that block alone, including the `eval "set -- $(...) '"$@"'"` structure.

### Step 3.3 — Add `.gitattributes`

Create `.gitattributes` at the repo root:

```gitattributes
# Shell scripts must stay LF, or they break on every Unix and in CI.
gradlew    text eol=lf
*.sh       text eol=lf
*.bash     text eol=lf

# Windows batch files must stay CRLF.
*.bat      text eol=crlf
*.cmd      text eol=crlf

# Binary files — never touch.
*.png      binary
*.jpg      binary
*.jar      binary
*.keystore binary
*.apk      binary
```

### Step 3.4 — Re-materialise `gradlew` with LF endings

Adding `.gitattributes` does not retroactively rewrite the checked-out file:

```bash
git rm --cached gradlew
git checkout -- gradlew
```

Verify it is now LF-only:

```bash
tr -dc '\r' < gradlew | wc -c
```

**You should see** `0`.

### Step 3.5 — Verify the fix works in bash

```bash
./gradlew projects
```

**You should see** a project tree listing with `BUILD SUCCESSFUL` — not a `lasspath` error.

Then confirm the Windows path still works:

```bash
MSYS_NO_PATHCONV=1 cmd /c "gradlew.bat projects"
```

### Step 3.6 — Remove the CI workaround — **DONE**

In `.github/workflows/android.yml`, delete the step at lines 130–134:

```yaml
      - name: Make gradlew executable
        run: |
          echo '#!/bin/sh' > gradlew
          echo 'exec java -Dorg.gradle.appname=gradlew -classpath gradle/wrapper/gradle-wrapper.jar org.gradle.wrapper.GradleWrapperMain "$@"' >> gradlew
          chmod +x ./gradlew
```

It exists purely to paper over the bug you just fixed. Keep the preceding `gradle/wrapper-validation-action` step — that one is still doing real work.

> If you would rather not touch CI in this PR, skip this step. The wrapper fix alone is safe.

### Step 3.7 — Fix the wrong Windows guidance in `AGENTS.md`

`AGENTS.md` currently says:

```
- **Full build**: `./gradlew build` (includes compilation, tests, and linting)
  - On Windows: `./gradlew.bat build`
```

`./gradlew.bat` is invalid syntax — you do not prefix a `.bat` with `./`. Correct the three Windows lines to:

```
  - On Windows: `.\gradlew.bat build`
```

### Step 3.8 — Run the full check locally

```bash
./gradlew ktlintCheck
./gradlew testDebugUnitTest
./gradlew :app:assembleDebug
```

All three should be `BUILD SUCCESSFUL`. Note that `ktlintCheck` and `testDebugUnitTest` were already green before this change — you are confirming no regression.

### Step 3.9 — Commit and raise the PR

```bash
git add gradlew .gitattributes AGENTS.md
git add .github/workflows/android.yml   # only if you did step 3.6
git commit -m "Fix corrupted gradle wrapper script and pin line endings

gradlew used \"\$@\" where upstream uses the JVM opts variables, which
duplicated the launcher arguments and leaked the wrapper's own
-classpath flag into Gradle's CLI (parsed as -c lasspath). Added
.gitattributes so gradlew stays LF and .bat files stay CRLF."
git push -u origin fix/gradle-wrapper-and-lf-endings
```

Then open a PR against `master` (via `gh pr create` or the web UI) and merge it.

---

# Phase 4 — Connect the phone over USB

> **STATUS: Not started — this is the next step.** Needs you and the physical phone.

### Step 4.1 — Enable Developer Options on the phone

1. Settings → **About phone**
2. Tap **Build number** seven times
3. Enter your PIN/password if asked
4. You should see "You are now a developer!"

(On Xiaomi/Redmi it is Settings → About phone → tap **MIUI version** 7×. On Samsung it is Settings → About phone → Software information → tap **Build number** 7×.)

### Step 4.2 — Enable USB debugging

Settings → **System → Developer options** → turn on **USB debugging**.

### Step 4.3 — Plug in and authorise

1. Connect the phone with a **data-capable** USB cable (many charge-only cables will not work — if nothing appears, try a different cable first)
2. On the phone, if a USB-mode notification appears, choose **File transfer / MTP**
3. Run:

```bash
adb devices -l
```

**First run — you should see:**

```
List of devices attached
ABCD1234    unauthorized
```

4. A dialog appears **on the phone**: "Allow USB debugging?" → tick "Always allow from this computer" → **Allow**

5. Run it again:

```bash
adb devices -l
```

**You should see** your device with `device` (not `unauthorized`), plus model info.

> Your `~/.android/adbkey` already exists from a previous setup, so this authorisation is a one-time step per phone.

### Step 4.4 — Troubleshooting if it stays `unauthorized`

```bash
adb kill-server
adb devices
```

If that fails, on the phone go to Developer options → **Revoke USB debugging authorizations**, then unplug and replug. Also ensure Windows Firewall allowed `adb.exe` (a prompt appears the first time — allow it on **Private** networks).

### Step 4.5 — Confirm the app installs

```bash
cd /d/Hmmm/Projects/Vibe/birfdae
MSYS_NO_PATHCONV=1 cmd /c "gradlew.bat installDebug"
```

**You should see** `BUILD SUCCESSFUL` and `Installed on 1 device`. The app should appear on your phone.

Launch it from the command line if you like:

```bash
adb shell am start -n com.birthdayreminder/.MainActivity
```

---

# Phase 5 — Switch to wireless adb

> **STATUS: Not started.** Do this only after Phase 4 works over USB.

Pick **one** method. Method A works on every Android version; Method B is nicer but needs Android 11+.

### ⚠️ Before you start: turn Cloudflare WARP off

WARP is installed on this machine and tunnels your traffic through Cloudflare. While it is connected, LAN adb pairing and discovery **will fail** in confusing ways. Confirm it is disconnected:

```bash
MSYS_NO_PATHCONV=1 "/c/Program Files/Cloudflare/Cloudflare WARP/warp-cli.exe" status
```

**You should see** `Status update: Disconnected`. If not, run `warp-cli disconnect`.

### Method A — Cable-assisted (any Android version)

Keep the USB cable plugged in for the first three steps.

1. Confirm USB still works: `adb devices`
2. Put adb into TCP mode:
   ```bash
   adb tcpip 5555
   ```
   Expect `restarting in TCP mode port: 5555`
3. Find the phone's IP address. On the phone: Settings → Wi-Fi → tap the connected network → **IP address** (something like `192.168.1.42`).
   Or read it from adb:
   ```bash
   adb shell ip route
   ```
   Look for `src 192.168.x.x`.
4. Connect:
   ```bash
   adb connect 192.168.1.42:5555
   ```
   Expect `connected to 192.168.1.42:5555`
5. **Unplug the USB cable**
6. Verify:
   ```bash
   adb devices
   ```
   **You should see** `192.168.1.42:5555    device`

### Method B — Pairing code (Android 11+)

No cable needed at all.

1. On the phone: Developer options → **Wireless debugging** → turn **ON**
2. Tap **Pair device with pairing code**. A dialog shows an IP, a **port**, and a 6-digit code — e.g. `192.168.1.42:37105` / `123456`
3. On the PC:
   ```bash
   adb pair 192.168.1.42:37105
   ```
   It prompts for the code — type the 6 digits and press Enter. Expect `Successfully paired`.
4. Now go back to the main **Wireless debugging** screen. It shows a *different* IP and port under "IP address & Port" — e.g. `192.168.1.42:5555`. **This port is not the pairing port.**
5. Connect using that one:
   ```bash
   adb connect 192.168.1.42:5555
   ```
6. Verify:
   ```bash
   adb devices
   ```

### Gotchas that will bite you

| Symptom | Cause / fix |
|---|---|
| `failed to connect` / times out | WARP is on → `warp-cli disconnect` |
| `failed to connect` | PC and phone on different subnets, or guest/isolated Wi-Fi with client isolation, or router AP-isolation enabled |
| Worked yesterday, fails today | Phone rebooted, or its IP changed. Re-pair, or re-run Method A over USB |
| `adb devices` shows the old entry as `offline` | `adb disconnect <ip>:port` then reconnect |
| Two devices listed | Both USB and wireless are live. Target one explicitly: `adb -s 192.168.1.42:5555 ...`, or set `ANDROID_SERIAL=192.168.1.42:5555` before running Gradle |

### Optional: a helper script

The pairing port is ephemeral and the connect port changes on reboot, so this gets tedious. A small `scripts/adb-wireless.cmd` (gitignored, or committed — your call) that takes the IP, runs `adb connect`, and prints `adb devices` saves a lot of retyping.

---

# Phase 6 — The day-to-day dev loop

All commands run from `D:\Hmmm\Projects\Vibe\birfdae`.

### Build and install to the phone

```bash
MSYS_NO_PATHCONV=1 cmd /c "gradlew.bat installDebug"
```

This compiles, packages, and installs in one go. This is your inner loop.

### Tests and lint

```bash
MSYS_NO_PATHCONV=1 cmd /c "gradlew.bat testDebugUnitTest"    # ~2m50s
MSYS_NO_PATHCONV=1 cmd /c "gradlew.bat ktlintCheck"          # ~12s
MSYS_NO_PATHCONV=1 cmd /c "gradlew.bat ktlintFormat"         # auto-fix Kotlin style
MSYS_NO_PATHCONV=1 cmd /c "gradlew.bat lintDebug"            # Android lint
```

### Watch the logs

Filter to just your app:

```bash
adb logcat --pid=$(adb shell pidof -s com.birthdayreminder)
```

All logs, colourised:

```bash
adb logcat -v color
```

Only errors and warnings:

```bash
adb logcat *:E
```

> The `--pid=$(...)` form needs bash. In `cmd.exe` use `adb logcat | findstr birthdayreminder`.

### Relaunch / reset the app

```bash
adb shell am start -n com.birthdayreminder/.MainActivity    # launch
adb shell am force-stop com.birthdayreminder                # kill
adb shell pm clear com.birthdayreminder                     # wipe app data
```

### Screenshots and screen recording without Studio

```bash
adb exec-out screencap -p > shot.png
adb shell screenrecord /sdcard/demo.mp4     # Ctrl-C to stop, then adb pull
```

### Gradle daemon notes

Your `gradle.properties` sets `org.gradle.jvmargs=-Xmx2048m` and `org.gradle.parallel=true`. That is reasonable. If memory pressure is still an issue:

- Stop the daemon between sessions: `gradlew.bat --stop`
- Lower the heap: edit `org.gradle.jvmargs` to `-Xmx1536m`
- Keep `org.gradle.configuration-cache=false` as-is for now — enabling it changes build behaviour and is a separate experiment

---

# Phase 7 — Install scrcpy (screen mirroring)

### Why
Studio's device mirroring is the single most useful Studio feature for phone-based development. `scrcpy` replaces it, is a few MB, and is far lighter than an emulator.

### Step 7.1 — Install

```bash
winget install Genymobile.scrcpy --accept-package-agreements
```

Verified available: `Genymobile.scrcpy` version 4.1.

### Step 7.2 — Run

With the phone connected (USB or wireless):

```bash
scrcpy
```

### Step 7.3 — Lighter flags for a resource-constrained machine

```bash
scrcpy --max-size 1024 --max-fps 30 --no-audio
```

Useful extras:

- `--turn-screen-off` — mirror while the phone's own screen stays dark (saves phone battery)
- `--stay-awake` — keep the phone awake while plugged in
- `--keyboard=uhid` — send real keyboard input, better for typing

---

# Phase 8 — Uninstall Android Studio

> **STATUS: Not started. All three pre-flight checks below now pass**, so this can proceed
> whenever you are ready.

### Pre-flight

**Do not start this phase until all of these are true:**

- [x] Phase 1 complete — `gradlew.bat --version` reports Temurin 17.0.20.1
- [x] `gradlew.bat :app:assembleDebug` succeeds
- [x] Phase 2 complete — `adb` / `android` on PATH
- [ ] **PR #6 merged** (blocks nothing in Phases 4-7, but should land before Phase 8)

If you skip Phase 1, you will end up with no supported JDK.

### Step 8.1 — Close Studio

Make sure it is not running:

```bash
MSYS_NO_PATHCONV=1 tasklist //FI "IMAGENAME eq studio64.exe"
```

### Step 8.2 — Run the uninstaller

```bash
MSYS_NO_PATHCONV=1 "D:\Programs\Android Studio\uninstall.exe"
```

Or use **Settings → Apps → Installed apps → Android Studio → Uninstall**.

### Step 8.3 — ⚠️ Decline the SDK removal

Studio's uninstaller lists optional components. Because Studio has `D:\Programs\Android` recorded as its SDK path, it **may offer to remove the Android SDK**.

**If you see an Android SDK entry, untick it / decline.** Your SDK — including the 2.3 GB system image, the platforms, and your accepted licences — lives there and must survive.

(There is no AVD to worry about: `~/.android/avd/` is empty.)

### Step 8.4 — Verify the SDK survived

```bash
ls "D:/Programs/Android"
adb version
sdkmanager --list_installed
```

**You should see** `build-tools`, `cmdline-tools`, `emulator`, `licenses`, `platform-tools`, `platforms`, `system-images` — all still present.

### Step 8.5 — Reclaim the leftover caches

The uninstaller leaves settings and index caches behind:

```bash
rm -rf "$LOCALAPPDATA/Google/AndroidStudio2025.2.2"
rm -rf "$LOCALAPPDATA/Google/AndroidStudio2025.1.2"
rm -rf "$APPDATA/Google/AndroidStudio2025.2.2"
rm -rf "$APPDATA/Google/AndroidStudio2025.1.2"
rm -rf "$HOME/.android/studio"
```

**Space recovered:** 3.1 GB (app) + 675 MB + 829 MB (index caches) + ~2.6 MB (settings) ≈ **4.6 GB**.

> **Do not delete `~/.gradle`.** That is the Gradle dependency cache and the wrapper distribution — deleting it forces a full re-download. Keep `~/.android/adbkey` too, or you will have to re-authorise your phone.

### Step 8.6 — Optional: remove the PATH entry if Studio added one

Check for a `STUDIO_JDK` or Studio-related variable in Environment Variables and remove it if present.

---

# Phase 9 — Optional: reclaim disk from unused SDK parts

Only if you are **not** using the emulator. You chose physical-phone development, so these are likely dead weight:

| Path | Size | Safe to delete if… |
|---|---|---|
| `D:\Programs\Android\system-images` | 2.3 GB | you are not running an emulator |
| `D:\Programs\Android\emulator` | 1.1 GB | same |
| `D:\Programs\Android\skins` | 15 MB | same |
| `D:\Programs\Android\sources` | 242 MB | you are not using an IDE that browses Android sources |

**Total: ~3.7 GB.**

Re-downloadable at any time with `sdkmanager "system-images;android-36;google_apis_playstore;x86_64"` and `sdkmanager emulator`, so this is reversible — just slow.

To delete:

```bash
rm -rf "D:/Programs/Android/system-images"
rm -rf "D:/Programs/Android/emulator"
rm -rf "D:/Programs/Android/skins"
```

> Keep `platforms`, `build-tools`, `platform-tools`, `licenses`, and `cmdline-tools`. Those are required to build.

---

# Phase 10 — Final verification with Studio gone

Run this whole sequence. Every line must succeed with no Studio on the machine.

```bash
cd /d/Hmmm/Projects/Vibe/birfdae

# 1. Wrapper works both ways
./gradlew --version
MSYS_NO_PATHCONV=1 cmd /c "gradlew.bat --version"

# 2. JDK is 17, not Studio's JBR
MSYS_NO_PATHCONV=1 cmd /c "gradlew.bat --version"   # expect: Launcher JVM: 17.0.20

# 3. Clean build
MSYS_NO_PATHCONV=1 cmd /c "gradlew.bat clean :app:assembleDebug"

# 4. Tests and lint
MSYS_NO_PATHCONV=1 cmd /c "gradlew.bat testDebugUnitTest"
MSYS_NO_PATHCONV=1 cmd /c "gradlew.bat ktlintCheck"

# 5. SDK tooling
adb devices
sdkmanager --list_installed

# 6. Deploy to the phone
MSYS_NO_PATHCONV=1 cmd /c "gradlew.bat installDebug"
adb shell am start -n com.birthdayreminder/.MainActivity
```

If all six pass, the CLI setup is complete and Studio is fully replaceable for this project.

---

# Appendix A — What you give up by dropping Studio

Be clear-eyed about this. Studio is not just an IDE wrapper for everything.

| Feature | Substitute | Verdict |
|---|---|---|
| Compose `@Preview` rendering | **none** | **The biggest real loss.** No CLI equivalent exists. |
| Layout Inspector | `adb shell uiautomator dump` | Much weaker — XML dump, no live inspection |
| Visual profiler (CPU/memory) | `adb shell dumpsys`, `simpleperf` | Much weaker |
| GUI debugger | `gradlew.bat --debug-jvm` + IntelliJ CE debugger | Workable for JVM-side logic |
| Device mirroring | **`scrcpy`** | Fully replaced ✅ |
| SDK package manager | **`sdkmanager`** | Fully replaced ✅ |
| AVD manager | **`avdmanager`** | Fully replaced ✅ |
| Logcat viewer | **`adb logcat`** | Fully replaced, arguably better ✅ |
| Gradle sync UI | `gradlew.bat` output | Fully replaced ✅ |
| Kotlin editing / refactoring | IntelliJ IDEA CE 2024.3.4.1 (already installed at `D:\Programs\IntelliJ IDEA Community Edition 2024.3.4.1`) | Kotlin + Gradle work; **no Android plugin**, so no Compose preview |

**Honest summary:** for building, testing, linting, installing, and shipping — the CLI loses nothing. For UI-heavy work on Compose screens, losing `@Preview` and Layout Inspector is a genuine cost.

**Also worth knowing:** uninstalling frees **disk, not RAM**. The RAM win comes from not *launching* Studio. D: has 494 GB free, so disk is not your constraint. If you later want Compose Previews back, reinstalling Studio is the only way — but you can also just leave it installed and never launch it, at zero runtime cost. Removing it is entirely reasonable; this is just so you decide with full information.

---

# Appendix B — Troubleshooting quick reference

| Symptom | Cause | Fix |
|---|---|---|
| `The specified settings file '...\lasspath' does not exist` | corrupted `gradlew` | **fixed in PR #6** — merge it; meanwhile use `gradlew.bat` |
| `gradlew: /bin/sh^M: bad interpreter` | CRLF in `gradlew` | **fixed in PR #6** — `.gitattributes` pins LF |
| `Could not determine SDK root` | `cmdline-tools` not nested under `latest` | must be `cmdline-tools\latest\bin\`, not `cmdline-tools\bin\` |
| `android info` shows `C:\Users\...\AppData\Local\Android\Sdk` | `ANDROID_HOME` unset | Phase 2.2, or pass `--sdk="D:\Programs\Android"` |
| `Unsupported class file major version 68` | build ran on Java 24 | Phase 1 — set `JAVA_HOME` |
| `sdkmanager: command not found` | PATH not applied to this shell | open a fresh terminal (PATH is correct in the registry) |
| `adb: command not found` | platform-tools not on PATH | already on PATH — open a fresh terminal |
| Phone shows `unauthorized` | RSA prompt not accepted | Phase 4.4 |
| `adb connect` times out | WARP on, or subnet/AP isolation | Phase 5 pre-flight |
| `INSTALL_FAILED_UPDATE_INCOMPATIBLE` | app signed with a different debug key | `adb uninstall com.birthdayreminder` then reinstall |
| `No connected devices!` from `installDebug` | adb sees no device | `adb devices`; re-pair wireless |
| Wrong device targeted | USB and wireless both live | `ANDROID_SERIAL=<serial> gradlew.bat installDebug` |
| Gradle daemon OOM | heap too small / too many daemons | `gradlew.bat --stop`, raise `org.gradle.jvmargs` |

---

# Appendix C — Files this plan touches

| File | Change | Phase |
|---|---|---|
| `gradlew` | fix line 241 (`"$@"` → JVM opts vars) | 3.2 — **done** |
| `.gitattributes` | **new** — pin LF for shell scripts, CRLF for `.bat` | 3.3 — **done** |
| `.github/workflows/android.yml` | removed the `gradlew` shim; **also** fixed the `setup-android` packages input (pre-existing CI failure) | 3.6 — **done** |
| `AGENTS.md` | fix `./gradlew.bat` → `gradlew.bat` | 3.7 — **done** |
| `docs/local-dev-setup.md` | **this document** (new) | — |

Outside the repo (not version-controlled): `JAVA_HOME`, `ANDROID_HOME`, `ANDROID_SDK_ROOT`, user PATH, `D:\Programs\Android\cmdline-tools\latest\`, installed JDK, `scrcpy`.

**Reverted on request:** `app/src/main/java/com/birthdayreminder/ui/viewmodel/SearchViewModel.kt` — the uncommitted `DeleteBirthdayUseCase` / `ErrorHandler` refactor was discarded before branching, per your instruction.
