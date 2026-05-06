# QuickTalk 5G — Push‑To‑Talk (PTT) Android

Autor: Karolina Płonowska / Author: Karolina Płonowska
---

## Krótkie wprowadzenie / Short introduction

QuickTalk 5G to aplikacja mobilna typu Push‑To‑Talk (PTT) napisana w Kotlinie dla platformy Android. Umożliwia szybki, niskolatencyjny przesył dźwięku między urządzeniami w sieci lokalnej (PTT) oraz prosty tryb VoIP‑like.

QuickTalk 5G is an Android Push‑To‑Talk (PTT) app written in Kotlin. It enables low‑latency audio transmission between devices on a local network (PTT) and also provides a simple VoIP‑like mode.

---

## Najważniejsze funkcje / Key features

- Push‑To‑Talk (PTT): naciśnij i mów, puszczenie kończy nadawanie (press to talk, release to stop),
- VoIP‑like UDP streaming,
- Wykrywanie urządzeń w sieci i lista do wyboru (Network device discovery with user selection list),
- Tryb jasny / ciemny (Light / Dark mode) z przełącznikiem (Light / Dark themes with toggle),
- Obsługa uprawnień mikrofonu (Microphone runtime permissions handling).

---

## Struktura projektu — kluczowe pliki / Project structure — key files

- `app/src/main/java/com/pans/quicktalk5g/AudioSender.kt` — nagrywanie mikrofonu, fragmentacja i wysyłka UDP (microphone capture, fragmentation and UDP send),
- `app/src/main/java/com/pans/quicktalk5g/AudioReceiver.kt` — odbiór UDP i odtwarzanie przez `AudioTrack` (UDP receive and playback via `AudioTrack`),
- `app/src/main/AndroidManifest.xml` — deklaracje uprawnień i aktywności (permissions & activities),
- `app/src/main/res/` — layouty, ikony, style i kolory (layouts, icons, styles, colors),
- `app/build.gradle`, `build.gradle` — konfiguracja budowania (build configuration).

---

## Wymagania / Requirements

- Android Studio + Android SDK (Platform‑tools, adb),
- Gradle (wersja określona w `gradle/wrapper/` i `build.gradle`) (see `gradle/wrapper/` and `build.gradle` for exact versions),
- Java / JDK zgodna z konfiguracją projektu (matching project configuration).

---

## Jak zbudować i uruchomić

1. Otwórz projekt w Android Studio i pozwól na synchronizację Gradle.
2. Upewnij się, że `adb` i SDK są dostępne w PATH (na macOS dodaj do `~/.zshrc`).
3. W terminalu projektu uruchom:

```bash
./gradlew assembleDebug
./gradlew installDebug   # wymaga podłączonego urządzenia lub emulatora
```

4. Alternatywnie: zainstaluj apk ręcznie:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

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

## Prezentacja / Demo


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
