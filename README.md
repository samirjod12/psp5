# PSP Shell

A PS5-style launcher for the [PPSSPP](https://github.com/hrydgard/ppsspp) PSP emulator on Android:
animated boot screen, home screen with game tiles, power menu (Rest Mode / Restart / Turn Off) and a shutdown animation.

PSP Shell does not bundle the emulator. It keeps your game library and launches each game in the installed
PPSSPP app (`org.ppsspp.ppsspp` or the Gold edition). If PPSSPP is missing it opens the download page.

## Use

1. Install PPSSPP (Play Store) and this APK.
2. Open PSP Shell, tap the **+** tile (or search icon) and pick an `.iso`, `.cso` or `.pbp` file.
3. Tap **Play Game**. Long-press a tile to remove it. D-pad / A / Start work with a gamepad.

## Build and release

Locally (Android SDK + Gradle 8.7+): `gradle :app:assembleRelease`

On GitHub: push this folder to a repo, then
`git tag v1.0.0 && git push origin v1.0.0`. The workflow in `.github/workflows/build.yml` builds the APK and
attaches `PspShell.apk` to a new GitHub release.

Interface art is original; no Sony or game assets are included.
