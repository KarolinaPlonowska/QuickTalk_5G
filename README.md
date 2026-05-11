# QuickTalk 5G — Push‑To‑Talk (PTT) Android

QuickTalk 5G is an Android Push‑To‑Talk (PTT) app written in Kotlin. It enables low‑latency audio transmission between devices on a local network (PTT) and also provides a simple VoIP‑like mode.

---

## Key features

- Push‑To‑Talk (PTT): press to talk, release to stop,
- VoIP‑like UDP streaming,
- Network device discovery with user selection list,
- Light / Dark themes with toggle,
- Microphone runtime permissions handling.

---

## Project structure — key files

- `app/src/main/java/com/pans/quicktalk5g/AudioSender.kt` — microphone capture, fragmentation and UDP send,
- `app/src/main/java/com/pans/quicktalk5g/AudioReceiver.kt` — UDP receive and playback via `AudioTrack`,
- `app/src/main/AndroidManifest.xml` — permissions & activities,
- `app/src/main/res/` — layouts, icons, styles, colors,
- `app/build.gradle`, `build.gradle` — build configuration.

---

## Requirements

- Android Studio + Android SDK (Platform‑tools, adb),
- Gradle see `gradle/wrapper/` and `build.gradle` for exact versions,
- Java / JDK matching project configuration.

---

## Build & Run

1. Open the project in Android Studio and let Gradle sync.
2. Ensure `adb` and SDK tools are in your PATH (on macOS add them to `~/.zshrc`).
3. From project root run:

```bash
./gradlew assembleDebug
./gradlew installDebug   # requires a connected device or emulator
```

4. Or install APK manually:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## Demo


<img width="1920" height="1080" alt="1" src="https://github.com/user-attachments/assets/74ea9147-11ed-4294-a948-f76896db2292" />
<br>

<img width="1920" height="1080" alt="2" src="https://github.com/user-attachments/assets/9fa8b6fd-f18d-43cb-a0a5-6985270b07d7" />
<br>

<img width="1920" height="1080" alt="3" src="https://github.com/user-attachments/assets/2e5ed7e1-e7c2-4005-8677-7b79821b236d" />
<br>

<img width="1920" height="1080" alt="4" src="https://github.com/user-attachments/assets/81619e50-64a8-4717-a105-866b09227836" />
<br>

<img width="1920" height="1080" alt="5" src="https://github.com/user-attachments/assets/114bb550-450f-44b4-8597-61d0765df0a4" />
<br>

<img width="1920" height="1080" alt="6" src="https://github.com/user-attachments/assets/8886cf8a-e007-489a-938f-231d0b2f0bf4" />
<br>

<img width="1920" height="1080" alt="7" src="https://github.com/user-attachments/assets/e3c1eea9-3c86-4064-a8ea-0a87f23ba455" />
<br>

<img width="1920" height="1080" alt="8" src="https://github.com/user-attachments/assets/86eccd84-1939-4ad5-a5a9-964cc90838e1" />

---
