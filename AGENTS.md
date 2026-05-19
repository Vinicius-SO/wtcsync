
# Build & Validation Cycle

## Device

```
Dispositivo: 2303ERA42L (Android 14)
ADB:        /c/Users/vhiso/AppData/Local/Android/Sdk/platform-tools/adb.exe
JDK 21:     /c/Program Files/Java/jdk-21
ImageMagick:/c/Program Files/ImageMagick-7.1.2-Q16-HDRI/magick.exe
```

## Quick Build & Install

```bash
export JAVA_HOME="/c/Program Files/Java/jdk-21"
export PATH="$PATH:/c/Users/vhiso/AppData/Local/Android/Sdk/platform-tools"
./gradlew installDebug
```

## Full Cycle (build → install → launch)

```bash
# 1. Build + install
export JAVA_HOME="/c/Program Files/Java/jdk-21"
export PATH="$PATH:/c/Users/vhiso/AppData/Local/Android/Sdk/platform-tools"
./gradlew installDebug

# 2. Force stop + relaunch
adb shell am force-stop br.com.fiap.wtcsync
sleep 1
adb shell am start -n br.com.fiap.wtcsync/.MainActivity

# 3. Wait for foreground (timeout 30s)
APP_ID="br.com.fiap.wtcsync"
TIMEOUT=30
ELAPSED=0
while [ $ELAPSED -lt $TIMEOUT ]; do
  FOCUSED=$(adb shell dumpsys window | grep -i "mCurrentFocus" | grep "$APP_ID")
  if [ -n "$FOCUSED" ]; then echo "App is running"; break; fi
  sleep 1
  ELAPSED=$((ELAPSED + 1))
done
```

## Screenshot

```bash
adb exec-out screencap -p > screenshots/current/current.png
```

## Testing Without Firebase Auth

To bypass login and go straight to the home screen, change `startDestination` in
`app/src/main/java/br/com/fiap/wtcsync/ui/Navigation.kt`:

```kotlin
NavHost(navController = navController, startDestination = "home") {
```

**Don't forget to revert to `"entry"` before shipping.**

## Comparing with Pencil Design

1. Take screenshot via ADB
2. Compare visually with the design in Pencil (`C:\Users\vhiso\Downloads\WtcSync`)
3. Iterate on code → rebuild → screenshot → repeat
=======
# AGENTS.md

## Objective

This project uses OpenCode as the primary development environment for incrementally evolving an existing Android application, validated directly on a physical device via ADB.

Design references are provided through the **Pencil MCP**, which gives the agent access to the full design system — components, screens, tokens, and specs — without requiring manual exports.

The agent follows this iterative cycle:

1. Consult the Pencil MCP for the design reference of the target screen or component.
2. Create a git checkpoint of the current stable state.
3. Implement or modify the source code to match the design.
4. Build and install the APK on the connected Android device.
5. Launch the application and wait for UI stabilization via polling.
6. Capture a screenshot of the current state.
7. Compare the screenshot against the Pencil design reference.
8. Fix any visual or behavioral deviations.
9. Repeat until the implementation matches the design.

---

## Required Tools

- Java Development Kit (JDK 17+)
- Android SDK Command-Line Tools
- Gradle Wrapper (`gradlew`)
- Android Debug Bridge (ADB)
- Maestro CLI *(to be installed and configured in this project)*
- Git
- ImageMagick *(for pixel diff comparison)*
- Pencil MCP *(design reference source)*

---

## Environment Validation

Before starting any task, verify all tools are available:

```bash
java -version
adb version
./gradlew --version
git --version
convert --version       # ImageMagick
maestro --version       # Will fail until Maestro is set up — see Maestro Setup below
```

### Device Validation

```bash
adb devices
```

Must return at least one device with status `device`. If none is listed, halt and notify the user before proceeding.

---

## Pencil MCP — Design Reference

The agent must consult the Pencil MCP **before implementing any screen or component**. It is the single source of truth for:

- Layout and spacing (margins, paddings, grid)
- Typography (font family, size, weight, line height)
- Color tokens (backgrounds, surfaces, text, brand, state colors)
- Component specs (buttons, inputs, cards, navigation, etc.)
- Screen flows and states (empty, loading, error, filled)

### Workflow

1. Query the Pencil MCP for the target screen or component.
2. Extract relevant specs: colors, dimensions, typography, component hierarchy.
3. Implement in code.
4. After deploying, capture a screenshot and compare it visually against the Pencil reference.
5. Iterate until the implementation matches the design within the accepted visual threshold.

> Never assume design details. If a spec is ambiguous or missing in Pencil, ask the user before implementing.

---

## Maestro Setup (First-Time)

Maestro is not yet configured in this project. The agent must set it up before the first test run.

### Install Maestro CLI

```bash
curl -Ls "https://get.maestro.mobile.dev" | bash
```

Verify:

```bash
maestro --version
```

### Create Flow Directory

```bash
mkdir -p .maestro
```

### Example Starter Flow

Create `.maestro/smoke.yaml` as a baseline smoke test:

```yaml
appId: <APPLICATION_ID>
---
- launchApp
- assertVisible:
    id: ".*"
    timeout: 5000
```

Expand flows as features are validated. Store one `.yaml` file per screen or feature.

### Running Maestro Tests

```bash
maestro test .maestro/
```

Or a specific flow:

```bash
maestro test .maestro/smoke.yaml
```

Always capture output:

```bash
maestro test .maestro/ 2>&1 | tee logs/maestro.txt
```

---

## Stable State Checkpointing

Before any code change, create a git checkpoint:

```bash
git add -A
git stash push -m "stable-before-<task-name>"
```

If 3 consecutive builds fail, rollback to the last stable state:

```bash
git stash pop
```

---

## Build and Deploy Cycle

### 1. Build and Install

```bash
./gradlew installDebug
```

Windows:

```bat
gradlew.bat installDebug
```

On build failure: analyze the output, fix, and retry. After 3 consecutive failures, rollback and halt.

### 2. Discover Application ID

```bash
grep "applicationId" app/build.gradle
```

### 3. Launch Application

```bash
adb shell am start -n <APPLICATION_ID>/<MAIN_ACTIVITY>
```

Fallback if main activity is unknown:

```bash
adb shell monkey -p <APPLICATION_ID> -c android.intent.category.LAUNCHER 1
```

### 4. Wait for UI Stabilization via Polling

Use `scripts/wait_for_foreground.sh` (see Deployment Scripts below) instead of a fixed `sleep`.

### 5. Capture Screenshot

```bash
mkdir -p screenshots/current
adb exec-out screencap -p > screenshots/current/current.png
```

---

## Visual Comparison Against Pencil

After capturing a screenshot:

1. Retrieve the reference frame from the Pencil MCP for the target screen.
2. Export or access the reference image (save to `screenshots/reference/<screen>.png`).
3. Run pixel diff:

```bash
DIFF=$(compare -metric AE -fuzz 5% \
  screenshots/reference/<screen>.png \
  screenshots/current/current.png \
  screenshots/diff/diff.png 2>&1 || true)

echo "Pixel diff: $DIFF"

MAX_DIFF=500
if [ "$DIFF" -gt "$MAX_DIFF" ]; then
  echo "Visual diff exceeded threshold ($DIFF > $MAX_DIFF). Iterating..."
  exit 1
fi
```

4. Inspect `screenshots/diff/diff.png` to identify which areas diverge.
5. Cross-reference with Pencil specs to understand the expected values.
6. Fix the implementation and repeat.

---

## Continuous Validation Loop

```
stable_checkpoint = git_stash()
attempts = 0
MAX_ATTEMPTS = 10

while result != expected and attempts < MAX_ATTEMPTS:
    attempts++

    # Consult design reference
    design_spec = pencil_mcp.get_screen(<target_screen>)

    # Implement
    update_source_code(design_spec)

    # Build
    if not gradlew_installDebug():
        if attempts % 3 == 0:
            git_stash_pop(stable_checkpoint)
        continue

    # Deploy
    launch_app()
    poll_for_foreground()

    # Validate
    capture_screenshot()
    pixel_diff = compare_with_pencil_reference()
    maestro_result = maestro_test()  # once .maestro/ flows exist
    analyze_logs()

    if pixel_diff <= MAX_DIFF and maestro_result == passed:
        result = expected
    else:
        fix_issues()

if attempts >= MAX_ATTEMPTS:
    report_failure_and_halt()
```

> After `MAX_ATTEMPTS` without convergence, the agent must stop and report the current diff and logs to the user.

---

## Screenshot Directory

```
screenshots/
├── reference/     # Exported from Pencil MCP (design baseline)
├── current/       # Screenshots from the current run
└── diff/          # Pixel diff output images
```

---

## Log Collection

Clear logcat before each launch to avoid noise from previous runs:

```bash
adb logcat -c
adb shell am start -n <APPLICATION_ID>/<MAIN_ACTIVITY>
adb logcat -d > logs/logcat.txt
```

Filter by package:

```bash
adb logcat | grep <APPLICATION_ID>
```

---

## Suggested Project Structure

```
project-root/
├── app/
├── .maestro/
│   └── smoke.yaml
├── screenshots/
│   ├── reference/
│   ├── current/
│   └── diff/
├── logs/
├── scripts/
│   ├── deploy.sh
│   ├── deploy.ps1
│   └── wait_for_foreground.sh
├── gradlew
├── gradlew.bat
└── AGENTS.md
```

---

## Deployment Scripts

### scripts/wait_for_foreground.sh

```bash
#!/bin/bash
APP_ID=$1
TIMEOUT=${2:-30}
ELAPSED=0

while [ $ELAPSED -lt $TIMEOUT ]; do
  FOCUSED=$(adb shell dumpsys window | grep -i "mCurrentFocus" | grep "$APP_ID")
  if [ -n "$FOCUSED" ]; then
    echo "App is in foreground."
    exit 0
  fi
  sleep 1
  ELAPSED=$((ELAPSED + 1))
done

echo "Timeout: App did not reach foreground in ${TIMEOUT}s."
exit 1
```

### scripts/deploy.sh

```bash
#!/bin/bash
set -e

APP_ID="<APPLICATION_ID>"
MAIN_ACTIVITY="<MAIN_ACTIVITY>"
REFERENCE_SCREEN="${1:-home}"
MAX_DIFF=500

# Checkpoint
git add -A
git stash push -m "stable-before-deploy"

# Build and install
./gradlew installDebug

# Clear logcat and launch
adb logcat -c
adb shell am start -n "${APP_ID}/${MAIN_ACTIVITY}"

# Wait for foreground
bash scripts/wait_for_foreground.sh "$APP_ID" 30

# Capture screenshot
mkdir -p screenshots/current screenshots/diff
adb exec-out screencap -p > screenshots/current/current.png

# Visual diff against Pencil reference
if [ -f "screenshots/reference/${REFERENCE_SCREEN}.png" ]; then
  DIFF=$(compare -metric AE -fuzz 5% \
    "screenshots/reference/${REFERENCE_SCREEN}.png" \
    screenshots/current/current.png \
    "screenshots/diff/${REFERENCE_SCREEN}_diff.png" 2>&1 || true)
  echo "Pixel diff: $DIFF"
  if [ "$DIFF" -gt "$MAX_DIFF" ]; then
    echo "Visual diff exceeded threshold."
    exit 1
  fi
else
  echo "No reference found for '${REFERENCE_SCREEN}'. Skipping pixel diff."
fi

# Run Maestro (if flows exist)
if [ -d ".maestro" ] && [ "$(ls -A .maestro)" ]; then
  maestro test .maestro/ 2>&1 | tee logs/maestro.txt
fi

# Collect logs
adb logcat -d > logs/logcat.txt

echo "Deploy and validation complete."
```

### scripts/deploy.ps1

```powershell
param(
  [string]$ReferenceScreen = "home"
)

$APP_ID = "<APPLICATION_ID>"
$MAIN_ACTIVITY = "<MAIN_ACTIVITY>"
$MAX_DIFF = 500

# Checkpoint
git add -A
git stash push -m "stable-before-deploy"

# Build and install
gradlew.bat installDebug

# Clear logcat and launch
adb logcat -c
adb shell am start -n "${APP_ID}/${MAIN_ACTIVITY}"

# Wait for foreground (polling)
$timeout = 30; $elapsed = 0
while ($elapsed -lt $timeout) {
    $focused = adb shell dumpsys window | Select-String "mCurrentFocus" | Select-String $APP_ID
    if ($focused) { Write-Host "App is in foreground."; break }
    Start-Sleep -Seconds 1; $elapsed++
}
if ($elapsed -ge $timeout) { Write-Error "Timeout."; exit 1 }

# Capture screenshot
foreach ($d in @("screenshots\current","screenshots\diff")) {
    if (!(Test-Path $d)) { New-Item -ItemType Directory -Path $d | Out-Null }
}
adb exec-out screencap -p > screenshots\current\current.png

# Run Maestro (if flows exist)
if (Test-Path ".maestro") {
    maestro test .maestro/ | Tee-Object -FilePath logs\maestro.txt
}

# Collect logs
adb logcat -d > logs\logcat.txt

Write-Host "Deploy and validation complete."
```

---

## OpenCode Agent Instructions

When implementing any feature or UI change, the agent must:

1. **Consult Pencil MCP first** — retrieve the design spec for the target screen or component before writing any code.
2. **Create a git checkpoint** before touching any file.
3. **Modify only the necessary files** — do not refactor unrelated code.
4. **Build the project** — if the build fails 3 times consecutively, rollback and halt.
5. **Launch the app** using `am start` with an explicit intent.
6. **Poll ADB** for foreground state instead of using a fixed sleep.
7. **Capture a screenshot** and compare it against the Pencil reference using pixel diff.
8. **Run Maestro tests** once flows are available in `.maestro/`.
9. **Collect and analyze logcat** output for runtime errors.
10. **Iterate** until visual diff is within threshold and all tests pass — or halt after `MAX_ATTEMPTS`.

### Example Prompts for OpenCode

- "Implement the Home screen according to the Pencil design, install it on the device, and iterate until it matches."
- "Run the deploy script for the login screen and compare the screenshot with the Pencil reference."
- "Fix the visual diff on the dashboard — check the Pencil MCP for the correct card spacing and colors."
- "Add a Maestro flow for the onboarding screen and run it after deploy."

---

## Success Criteria

A task is considered complete only when **all** of the following are true:

- The project builds without errors.
- The app installs and launches correctly on the physical device.
- The app reaches foreground state confirmed via ADB polling.
- The screenshot pixel diff against the Pencil design reference is within the accepted threshold.
- Maestro flows pass (once configured).
- No critical runtime errors appear in logcat.
- The change is committed with a meaningful message.
