#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ADB="${ADB:-$HOME/Library/Android/sdk/platform-tools/adb}"
EMU="${EMU:-$HOME/Library/Android/sdk/emulator/emulator}"
AVD="${AVD:-Medium_Phone_API_36.1}"
SERIAL="${SERIAL:-emulator-5554}"
APK_PATH="${APK_PATH:-$ROOT_DIR/app/build/outputs/apk/debug/app-debug.apk}"
APP_ID="${APP_ID:-com.musclefit.app}"
EMULATOR_LOG_GUI="/tmp/${AVD}.gui.log"
EMULATOR_LOG_HEADLESS="/tmp/${AVD}.headless.log"

log() {
  echo "[$(date '+%H:%M:%S')] $*"
}

require_bin() {
  local path="$1"
  local name="$2"
  if [[ ! -x "$path" ]]; then
    echo "Missing $name at: $path"
    echo "You can override via env: $name=/custom/path"
    exit 1
  fi
}

wait_for_device_online() {
  local max_try="${1:-120}"
  local i state
  for ((i = 1; i <= max_try; i++)); do
    state="$("$ADB" -s "$SERIAL" get-state 2>/dev/null || true)"
    if [[ "$state" == "device" ]]; then
      return 0
    fi
    "$ADB" reconnect offline >/dev/null 2>&1 || true
    sleep 2
  done
  return 1
}

wait_for_device_stable() {
  local max_try="${1:-120}"
  local needed_consecutive="${2:-4}"
  local i state ok=0
  for ((i = 1; i <= max_try; i++)); do
    state="$("$ADB" -s "$SERIAL" get-state 2>/dev/null || true)"
    if [[ "$state" == "device" ]]; then
      ok=$((ok + 1))
      if (( ok >= needed_consecutive )); then
        return 0
      fi
    else
      ok=0
      "$ADB" reconnect offline >/dev/null 2>&1 || true
    fi
    sleep 2
  done
  return 1
}

wait_for_boot_complete() {
  local max_try="${1:-180}"
  local i b1 b2 anim
  for ((i = 1; i <= max_try; i++)); do
    b1="$("$ADB" -s "$SERIAL" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')"
    b2="$("$ADB" -s "$SERIAL" shell getprop dev.bootcomplete 2>/dev/null | tr -d '\r')"
    anim="$("$ADB" -s "$SERIAL" shell getprop init.svc.bootanim 2>/dev/null | tr -d '\r')"
    if [[ "$b1" == "1" && "$b2" == "1" && "$anim" == "stopped" ]]; then
      return 0
    fi
    sleep 2
  done
  return 1
}

kill_target_emulator() {
  "$ADB" -s "$SERIAL" emu kill >/dev/null 2>&1 || true
  pkill -f "qemu-system-aarch64.*$AVD" >/dev/null 2>&1 || true
  pkill -f "$EMU.*-avd $AVD" >/dev/null 2>&1 || true
  sleep 2
}

start_emulator_gui() {
  log "Starting emulator (GUI, cold boot)..."
  nohup "$EMU" -avd "$AVD" -no-snapshot-load -netdelay none -netspeed full >"$EMULATOR_LOG_GUI" 2>&1 &
}

start_emulator_headless() {
  log "Starting emulator (headless fallback)..."
  nohup "$EMU" -avd "$AVD" -no-window -gpu swiftshader_indirect -no-snapshot-load -netdelay none -netspeed full >"$EMULATOR_LOG_HEADLESS" 2>&1 &
}

ensure_adb_server() {
  log "Starting adb server..."
  if "$ADB" start-server >/dev/null 2>&1; then
    return 0
  fi
  log "adb start failed, restarting adb daemon..."
  "$ADB" kill-server >/dev/null 2>&1 || true
  sleep 1
  "$ADB" start-server >/dev/null
}

ensure_online_booted_device() {
  log "Checking current device state..."
  if wait_for_device_stable 6 2; then
    if wait_for_boot_complete 90; then
      log "Existing emulator is stable and boot completed."
      return 0
    fi
  fi

  log "No stable booted device, restarting target emulator..."
  kill_target_emulator
  start_emulator_gui
  if wait_for_device_stable 120 4 && wait_for_boot_complete 180; then
    log "GUI emulator is stable and boot completed."
    return 0
  fi

  log "GUI emulator is unstable, switching to headless fallback..."
  kill_target_emulator
  start_emulator_headless
  if wait_for_device_stable 120 4 && wait_for_boot_complete 180; then
    log "Headless emulator is stable and boot completed."
    return 0
  fi

  return 1
}

install_with_retry() {
  local max_try="${1:-3}"
  local i
  for ((i = 1; i <= max_try; i++)); do
    if "$ADB" -s "$SERIAL" install -r -t "$APK_PATH"; then
      return 0
    fi
    log "Install failed, retry $i/$max_try..."
    "$ADB" reconnect offline >/dev/null 2>&1 || true
    wait_for_device_stable 20 2 || true
    sleep 3
  done
  return 1
}

require_bin "$ADB" "ADB"
require_bin "$EMU" "EMU"

ensure_adb_server

if ! ensure_online_booted_device; then
  echo "Unable to get a stable booted emulator."
  echo "Check logs: $EMULATOR_LOG_GUI and $EMULATOR_LOG_HEADLESS"
  exit 1
fi

log "Building debug APK..."
cd "$ROOT_DIR"
./gradlew :app:assembleDebug

if [[ ! -f "$APK_PATH" ]]; then
  echo "APK not found: $APK_PATH"
  exit 1
fi

log "Installing APK with retry..."
if install_with_retry 3; then
  log "Install success."
  log "Launching app..."
  "$ADB" -s "$SERIAL" shell monkey -p "$APP_ID" -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1 || true
  exit 0
fi

log "Fallback: clean reinstall..."
"$ADB" -s "$SERIAL" uninstall "$APP_ID" >/dev/null 2>&1 || true
"$ADB" -s "$SERIAL" install -t "$APK_PATH"
log "Install success."
"$ADB" -s "$SERIAL" shell monkey -p "$APP_ID" -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1 || true
