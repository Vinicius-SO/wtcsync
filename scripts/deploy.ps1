param(
  [string]$ReferenceScreen = "home"
)

$APP_ID = "br.com.fiap.wtcsync"
$MAIN_ACTIVITY = "br.com.fiap.wtcsync.MainActivity"
$MAX_DIFF = 500
$ANDROID_HOME = "$env:USERPROFILE\AppData\Local\Android\Sdk"
$JAVA_HOME = "C:\Program Files\Java\jdk-21"

$env:ANDROID_HOME = $ANDROID_HOME
$env:JAVA_HOME = $JAVA_HOME
$env:Path = "$JAVA_HOME\bin;$ANDROID_HOME\platform-tools;$env:USERPROFILE\.maestro\bin\maestro\bin;C:\Program Files\ImageMagick-7.1.2-Q16-HDRI;$env:Path"

git add -A
git stash push -m "stable-before-deploy"

gradlew.bat installDebug

adb logcat -c
adb shell am start -n "${APP_ID}/${MAIN_ACTIVITY}"

$timeout = 30; $elapsed = 0
while ($elapsed -lt $timeout) {
    $focused = adb shell dumpsys window | Select-String "mCurrentFocus" | Select-String $APP_ID
    if ($focused) { Write-Host "App is in foreground."; break }
    Start-Sleep -Seconds 1; $elapsed++
}
if ($elapsed -ge $timeout) { Write-Error "Timeout."; exit 1 }

foreach ($d in @("screenshots\current","screenshots\diff")) {
    if (!(Test-Path $d)) { New-Item -ItemType Directory -Path $d | Out-Null }
}
adb exec-out screencap -p > screenshots\current\current.png

if (Test-Path "screenshots\reference\${ReferenceScreen}.png") {
    $diff = & magick compare -metric AE -fuzz 5% "screenshots\reference\${ReferenceScreen}.png" screenshots\current\current.png "screenshots\diff\${ReferenceScreen}_diff.png" 2>&1
    Write-Host "Pixel diff: $diff"
    if ([int]$diff -gt $MAX_DIFF) { Write-Error "Visual diff exceeded threshold."; exit 1 }
}

if (Test-Path ".maestro") {
    maestro test .maestro/ | Tee-Object -FilePath logs\maestro.txt
}

adb logcat -d > logs\logcat.txt

Write-Host "Deploy and validation complete."
