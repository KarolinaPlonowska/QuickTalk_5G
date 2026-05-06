# QuickTalk 5G — Push‑To‑Talk (PTT) Android

Autor: Karolina Płonowska / Author: Karolina Płonowska
---

## Krótkie wprowadzenie / Short introduction

QuickTalk 5G to aplikacja mobilna typu Push‑To‑Talk (PTT) napisana w Kotlinie dla platformy Android. Umożliwia szybki, niskolatencyjny przesył dźwięku między urządzeniami w sieci lokalnej (PTT) oraz prosty tryb VoIP‑like.

QuickTalk 5G is an Android Push‑To‑Talk (PTT) app written in Kotlin. It enables low‑latency audio transmission between devices on a local network (PTT) and also provides a simple VoIP‑like mode.

---

## Najważniejsze funkcje / Key features

- Push‑To‑Talk (PTT): naciśnij i mów, puszczenie kończy nadawanie.
- VoIP‑like streaming przez UDP.
- Wykrywanie urządzeń w sieci i lista do wyboru.
- Tryb jasny / ciemny (Light / Dark mode) z przełącznikiem.
- Obsługa uprawnień mikrofonu (runtime permissions).

- Push‑To‑Talk (PTT): press to talk, release to stop.
- VoIP‑like UDP streaming.
- Network device discovery with user selection list.
- Light / Dark themes with toggle.
- Microphone runtime permissions handling.

---

## Struktura projektu — kluczowe pliki / Project structure — key files

- `app/src/main/java/com/pans/quicktalk5g/AudioSender.kt` — nagrywanie mikrofonu, fragmentacja i wysyłka UDP.
- `app/src/main/java/com/pans/quicktalk5g/AudioReceiver.kt` — odbiór UDP i odtwarzanie przez `AudioTrack`.
- `app/src/main/AndroidManifest.xml` — deklaracje uprawnień i aktywności.
- `app/src/main/res/` — layouty, ikony, style i kolory.
- `app/build.gradle`, `build.gradle` — konfiguracja budowania.

- `app/src/main/java/com/pans/quicktalk5g/AudioSender.kt` — microphone capture, fragmentation and UDP send.
- `app/src/main/java/com/pans/quicktalk5g/AudioReceiver.kt` — UDP receive and playback via `AudioTrack`.
- `app/src/main/AndroidManifest.xml` — permissions & activities.
- `app/src/main/res/` — layouts, icons, styles, colors.
- `app/build.gradle`, `build.gradle` — build configuration.

---

## Wymagania / Requirements

- Android Studio + Android SDK (Platform‑tools, adb)
- Gradle (wersja określona w `gradle/wrapper/` i `build.gradle`)
- Java / JDK zgodna z konfiguracją projektu

- Android Studio + Android SDK (Platform‑tools, adb)
- Gradle (see `gradle/wrapper/` and `build.gradle` for exact versions)
- Java / JDK matching project configuration

---

## Jak zbudować i uruchomić / Build & Run (skrót)

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



---

