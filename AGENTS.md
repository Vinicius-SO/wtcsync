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
