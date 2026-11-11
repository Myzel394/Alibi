![Alibi, Witness every moment](readme_content/banner.webp)

# Alibi

<p float="left" align="center">
    <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/01.webp" width="30%" />
    <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/02.webp" width="30%" />
    <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/03.webp" width="30%" />
    <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/04.webp" width="30%" />
    <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/05.webp" width="30%" />
    <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/06.webp" width="30%" />
</p>

Alibi keeps recording audio/video in the background and saves the last 30 minutes at your request.
Everything is completely configurable. No internet connection required.

## Recording controls and file management

- Enable **Volume-key video shortcuts (Up: front / Down: rear)** in Settings, grant camera and notification permissions, and enable **Alibi recording shortcut** in Android Accessibility settings. Hold Volume Up for 0.8 seconds to start front-camera video; hold Volume Up again to stop. Volume Down starts/stops video with the configured rear camera. Short presses adjust volume. A different shortcut during an active recording reports that the recorder is busy.
- Recording starts directly from the accessibility service without opening the app. One vibration confirms recording has started, two confirm stopping, and three indicate failure. The service filters key events without requesting screen contents. Additional vendor background-start permissions may be required; Settings provides an entry point for Xiaomi/HyperOS. Unaccepted start requests expire after six seconds.
- **Rear camera** in Settings selects the system default, an available logical camera, or an exposed physical lens for the next rear-camera recording. Camera labels include approximate 35 mm equivalent focal length and camera ID where available. Only CameraX-accessible cameras are listed. Physical routing uses [Camera2Interop](https://developer.android.com/reference/androidx/camera/camera2/interop/Camera2Interop.Extender) on Android 9+, and recording configurations remain device-dependent.
- Enable the floating recording dot and grant display-over-other-apps permission to show a red dot near the top edge while recording audio/video. Pausing or stopping removes it. The system status-bar notification uses a monochrome icon.
- The folder button opens a recording library for audio/video fragments and merged exports, with playback, sharing, refresh, and confirmed deletion. It includes internal storage, app media storage, and custom folders with retained access permissions. Deletion is blocked during recording or merging, and active fragments are protected from playback/sharing. Previous fragments are archived before a new session, preserving merged exports.

Background Volume Down recording, start/stop haptics, and the recording dot were tested on a Xiaomi 14 Ultra running Android 16. Front-camera shortcuts and multiple rear-camera selection also passed owner-reported physical-device acceptance. Other vendors and lock-screen behavior need separate validation.

## Building

Use JDK 17 or 21 and Android SDK 36. The build uses Gradle 8.13, AGP 8.13.2, Kotlin 2.3.21, Java 17 bytecode, and compile/target SDK 36; minimum SDK remains 24. Dependencies are centralized in `gradle/libs.versions.toml`, with the Kotlin Compose Compiler plugin, Compose BOM, and CameraX 1.6.2. The retired FFmpegKit 5.1 dependency is replaced by the community-maintained Maven Central artifact `com.antonkarpenko:ffmpeg-kit-full-gpl:2.2.3`.

```bash
./gradlew assembleDebug testDebugUnitTest
```

# Download

[<img src="readme_content/google-play-badge.png" alt="Get it on Google Play" height="80">](https://play.google.com/store/apps/details?id=app.myzel394.alibi)
[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" alt="Get it on F-Droid" height="80">](https://f-droid.org/packages/app.myzel394.alibi)
[<img src="readme_content/github-badge.webp" alt="Get it on GitHub" height="80">](https://github.com/Myzel394/Alibi/releases)

# Supporting Alibi

You can support Alibi in various ways:

## Contribute to the project

Add a new feature or fix bugs.

## Add translations

[Translate Alibi into your language using Crowdin](https://crowdin.com/project/alibi), so that other
people can use it more easily.

## Donate

It might sound crazy, but if you would just donate $ 1, it would totally mean the world to me, since
it's a really small amount and if everyone did that, I could focus on Alibi and my other open
source projects. :)

You can donate via [GitHub Sponsors](https://github.com/sponsors/Myzel394) or via [crypto currencies](https://github.com/Myzel394/contact-me?tab=readme-ov-file#donations).
