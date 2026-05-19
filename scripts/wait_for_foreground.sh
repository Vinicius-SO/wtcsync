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
