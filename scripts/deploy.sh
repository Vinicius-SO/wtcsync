#!/bin/bash
set -e

APP_ID="br.com.fiap.wtcsync"
MAIN_ACTIVITY="br.com.fiap.wtcsync.MainActivity"
REFERENCE_SCREEN="${1:-home}"
MAX_DIFF=500

git add -A
git stash push -m "stable-before-deploy"

export ANDROID_HOME="/c/Users/vhiso/AppData/Local/Android/Sdk"
export JAVA_HOME="/c/Program Files/Java/jdk-21"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$PATH"
export MAESTRO_DIR="$HOME/.maestro"
export IMAGEMAGICK_DIR="/c/Program Files/ImageMagick-7.1.2-Q16-HDRI"
export PATH="$IMAGEMAGICK_DIR:$MAESTRO_DIR/bin/maestro/bin:$PATH"

./gradlew installDebug

adb logcat -c
adb shell am start -n "${APP_ID}/${MAIN_ACTIVITY}"

bash scripts/wait_for_foreground.sh "$APP_ID" 30

mkdir -p screenshots/current screenshots/diff
adb exec-out screencap -p > screenshots/current/current.png

if [ -f "screenshots/reference/${REFERENCE_SCREEN}.png" ]; then
  DIFF=$(magick compare -metric AE -fuzz 5% \
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

if [ -d ".maestro" ] && [ "$(ls -A .maestro)" ]; then
  maestro test .maestro/ 2>&1 | tee logs/maestro.txt
fi

adb logcat -d > logs/logcat.txt

echo "Deploy and validation complete."
