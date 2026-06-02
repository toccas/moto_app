# ============================================================
#  MotoTracker — Build & Install su Watch + Telefono
# ============================================================

$ADB    = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
$GRADLE = ".\gradlew.bat"

$WATCH_IP   = "192.168.1.6"
$WATCH_PORT = "37321"

$APK_WEAR = "wear\build\outputs\apk\debug\wear-debug.apk"
$APK_APP  = "app\build\outputs\apk\debug\app-debug.apk"

function Write-Step($msg) {
    Write-Host "`n>>> $msg" -ForegroundColor Cyan
}

function Check-Device($serial, $name) {
    $result = & $ADB -s $serial get-state 2>$null
    if ($result -ne "device") {
        Write-Host "ATTENZIONE: $name non connesso ($serial)" -ForegroundColor Yellow
        return $false
    }
    Write-Host "$name connesso." -ForegroundColor Green
    return $true
}

# ── 1. Connetti il watch via Wi-Fi ──────────────────────────
Write-Step "Connessione al Galaxy Watch ($WATCH_IP`:$WATCH_PORT)..."
& $ADB connect "${WATCH_IP}:${WATCH_PORT}" | Out-Null

# ── 2. Rileva dispositivi ────────────────────────────────────
Write-Step "Rilevamento dispositivi..."
$devices = & $ADB devices | Select-String "device$"
Write-Host ($devices | Out-String)

$watchSerial = "${WATCH_IP}:${WATCH_PORT}"
$phoneSerial = (& $ADB devices | Select-String "device$" |
    Where-Object { $_ -notmatch $WATCH_IP -and $_ -notmatch "adb-tls" } |
    Select-Object -First 1) -replace "\s+device", "" -replace "^\s+",""

$hasWatch = Check-Device $watchSerial "Galaxy Watch 5"
$hasPhone = if ($phoneSerial) { Check-Device $phoneSerial "Telefono" } else { $false }

if (-not $hasWatch -and -not $hasPhone) {
    Write-Host "`nNessun dispositivo trovato. Collega telefono via USB e assicurati che il watch sia sulla stessa Wi-Fi." -ForegroundColor Red
    exit 1
}

# ── 3. Build ─────────────────────────────────────────────────
Write-Step "Build in corso (Gradle)..."
if ($hasWatch -and $hasPhone) {
    & $GRADLE assembleDebug
} elseif ($hasWatch) {
    & $GRADLE :wear:assembleDebug
} else {
    & $GRADLE :app:assembleDebug
}

if ($LASTEXITCODE -ne 0) {
    Write-Host "`nBuild fallita." -ForegroundColor Red
    exit 1
}
Write-Host "Build completata." -ForegroundColor Green

# ── 4. Installa sul Watch ────────────────────────────────────
if ($hasWatch) {
    Write-Step "Installazione su Galaxy Watch 5..."
    & $ADB -s $watchSerial install -r $APK_WEAR
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Watch: installazione OK" -ForegroundColor Green
        & $ADB -s $watchSerial shell am start -n "com.moto.tracker/.MainActivity" | Out-Null
    } else {
        Write-Host "Watch: installazione fallita" -ForegroundColor Red
    }
}

# ── 5. Installa sul Telefono ─────────────────────────────────
if ($hasPhone) {
    Write-Step "Installazione su Telefono ($phoneSerial)..."
    & $ADB -s $phoneSerial install -r $APK_APP
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Telefono: installazione OK" -ForegroundColor Green
        & $ADB -s $phoneSerial shell am start -n "com.moto.tracker/.phone.MainActivity" | Out-Null
    } else {
        Write-Host "Telefono: installazione fallita" -ForegroundColor Red
    }
}

Write-Host "`n Fatto! Le app sono installate e avviate." -ForegroundColor Green
