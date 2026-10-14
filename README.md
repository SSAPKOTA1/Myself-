# Snake

A personal Android Snake game: Kotlin, Jetpack Compose, DataStore. No ads, no network access, no analytics
(the app declares **no permissions at all**).

- Swipe anywhere to steer, tap to pause/resume
- 20x30 board, snake starts at length 3 in the centre moving right
- +10 points per food, speed-up every 5 food (200 ms → 80 ms per step at most)
- Optional wrap-around walls (switch on the main menu, off by default)
- High score and the wrap setting survive app restarts (Jetpack DataStore)
- The game pauses automatically when you leave the app

Min SDK 26 (Android 8.0), target/compile SDK 37.

---

## Build and install the debug APK on your phone over USB

### 1. One-time computer setup

You need two things: **JDK 17 or newer** and the **Android SDK** (with the *Android SDK Platform 37* and
*Build-Tools* packages). The easiest way to get both is Android Studio:

1. Install [Android Studio](https://developer.android.com/studio) (it bundles a suitable JDK).
2. Open it once and let the setup wizard download the Android SDK.
3. *Tools → SDK Manager → SDK Platforms*: tick **Android API 37** and press *Apply*.
   (Gradle will also offer to download missing pieces on the first build.)

If you build from the command line only, tell Gradle where the SDK lives, either by exporting
`ANDROID_HOME` or by creating a `local.properties` file in the project root:

```properties
# macOS example – Linux is usually ~/Android/Sdk, Windows C\:\\Users\\you\\AppData\\Local\\Android\\Sdk
sdk.dir=/Users/you/Library/Android/sdk
```

(Android Studio writes this file for you when you open the project.)

### 2. Enable Developer options and USB debugging on the phone

1. Open **Settings → About phone** (on some phones *Settings → About device → Software information*).
2. Tap **Build number** seven times. You'll see "You are now a developer!" (you may need to enter your
   screen-lock PIN).
3. Go back to **Settings → System → Developer options** (on Samsung: *Settings → Developer options*; some
   phones show it at the top level of Settings).
4. Turn **Developer options** on, then turn on **USB debugging**.
5. Xiaomi/Redmi/POCO only: also turn on **Install via USB** (it may require signing in to a Mi account).

### 3. Connect the phone

1. Plug the phone into the computer with a USB cable that supports data (some cables are charge-only).
2. If the phone asks for the USB mode, choose **File transfer / Android Auto** (not "charging only").
3. A dialog **"Allow USB debugging?"** appears on the phone. Tick *Always allow from this computer* and
   tap **Allow**.
4. Check that the computer sees the phone. `adb` lives in `<Android SDK>/platform-tools`:

   ```sh
   adb devices
   ```

   You should see one line ending in `device`. If it says `unauthorized`, accept the dialog on the phone;
   if the list is empty, see *Troubleshooting* below.

### 4. Build and install

**Option A – Android Studio:** open this folder (*File → Open*), wait for the Gradle sync to finish, pick your
phone in the device drop-down at the top, and press the green **Run ▶** button.

**Option B – command line** (from the project root; use `gradlew.bat` instead of `./gradlew` on Windows):

```sh
# Build the APK and install it straight onto the connected phone
./gradlew :app:installDebug
```

or build the file first and install it yourself:

```sh
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

The first build downloads Gradle and the dependencies, so it takes a few minutes and needs internet; later
builds are fast. Then find **Snake** in your app drawer. The debug APK is automatically signed with a debug
key, which is fine for your own phone. `-r` keeps your saved high score when you reinstall a newer build.

> Prefer not to use `adb`? Copy `app/build/outputs/apk/debug/app-debug.apk` to the phone, open it from the
> Files app, and allow "Install unknown apps" for that app when Android asks.

### Troubleshooting

| Problem | Fix |
| --- | --- |
| `adb devices` shows nothing | Try another cable/USB port, make sure USB mode isn't "charging only", toggle USB debugging off/on, and on Windows install your phone maker's USB driver. |
| `unauthorized` | Unlock the phone, accept the "Allow USB debugging?" prompt (or revoke authorisations in Developer options and reconnect). |
| `SDK location not found` | Create `local.properties` with `sdk.dir=...` or set `ANDROID_HOME` (see step 1). |
| `Failed to find target with hash string 'android-37'` / platform missing | Install *Android API 37* in the SDK Manager. |
| `INSTALL_FAILED_USER_RESTRICTED` (Xiaomi etc.) | Enable *Install via USB* in Developer options. |
| `INSTALL_FAILED_UPDATE_INCOMPATIBLE` | Uninstall the old copy first (`adb uninstall com.example.snake`) – this resets the high score. |
| Gradle says it needs a newer Java | Use JDK 17+; in Android Studio set *Settings → Build Tools → Gradle → Gradle JDK* to the bundled JBR. |
| Play Protect "unrecognised app" warning | Tap *Install anyway* – it's your own build. |

---

## Running the unit tests

The game rules live in the plain-Kotlin `:game` module, so the tests don't need the Android SDK or a phone:

```sh
./gradlew :game:test
```

HTML report: `game/build/reports/tests/test/index.html`.

---

## Architecture

```
game/   Pure Kotlin/JVM module – no Android imports, fully unit tested
  Point.kt, Direction        value types for cells and headings
  GameConfig.kt              board size, scoring, speed curve, wrap-around flag
  Board.kt                   bounds, wrap-around, empty-cell lookup
  GameState.kt               immutable snapshot of a game (snake, food, score, status…)
  SnakeGame.kt               the rules engine: turn(), tick(), restart()
  SwipeDetector.kt           finger movement → Direction
  FixedStepTimer.kt          drift-free deadline arithmetic for the game loop

app/    Android module (Compose UI)
  SnakeViewModel.kt          owns the SnakeGame, runs the coroutine game loop, exposes UiState
  data/SnakeRepository.kt    DataStore: high score + wrap-around setting
  ui/SnakeApp.kt             root composable, gesture handling (swipe / tap)
  ui/BoardCanvas.kt          Canvas drawing of board, food and snake
  ui/Overlays.kt             menu, pause and game-over screens
  MainActivity.kt            edge-to-edge dark window, auto-pause in onPause
```

Data flows one way: gestures → `SnakeViewModel` → `SnakeGame` → immutable `GameState` → `StateFlow<UiState>` →
Compose. The UI never mutates game state.
