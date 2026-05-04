# QuickTalk 5G — Push-To-Talk (PTT) Android

Autor: Karolina Płonowska
---

## 1. Krótkie streszczenie
QuickTalk 5G to aplikacja mobilna typu Push‑To‑Talk (PTT) napisana w Kotlinie dla platformy Android. Umożliwia niskolatencyjną transmisję dźwięku między urządzeniami w sieci lokalnej (tryb PTT) oraz prosty tryb VoIP‑like do komunikacji głosowej. Projekt powstał jako zadanie na studia i zawiera pełny zestaw funkcjonalności potrzebnych do demonstracji działania PTT: nagrywanie audio, pakowanie ramek, wysyłanie przez UDP, odbiór i odtwarzanie oraz wykrywanie urządzeń w sieci.

Repo zawiera implementację aplikacji klienckiej (moduł `app`) z kompletem zasobów UI, stylami i kodem.

## 2. Najważniejsze funkcje
- Push‑To‑Talk (PTT) — naciśnij i mów, puszczenie kończy nadawanie.
- Tryb VoIP‑like — streaming audio przez UDP.
- Odkrywanie urządzeń w sieci lokalnej (lista wykrytych hostów; użytkownik wybiera urządzenie).
- Tryb jasny/ciemny (Dark Mode) z możliwością przełączania w UI.
- Obsługa uprawnień czasu wykonania dla mikrofonu.

## 3. Technologia i wymagania
- Język: Kotlin
- Platforma: Android (AndroidX)
- Kompilator: Gradle (zmienne wersje w plikach konfiguracji)
- Wymagania deweloperskie: Android Studio, Android SDK (platform‑tools, adb)
- Minimalne API: (określone w `app/build.gradle`) — projekt używa nowoczesnych ustawień (minSdk 26 w module `app`)

## 4. Struktura projektu — kluczowe pliki
- `app/src/main/java/com/pans/quicktalk5g/AudioSender.kt` — kod nagrywania mikrofonu, fragmentacji i wysyłki UDP.
- `app/src/main/java/com/pans/quicktalk5g/AudioReceiver.kt` — odbiór UDP i odtwarzanie przez `AudioTrack`.
- `app/src/main/AndroidManifest.xml` — deklaracja uprawnień i aktywności.
- `app/src/main/res/` — zasoby UI: layouty, ikony, style i kolory.
- `app/build.gradle`, `build.gradle`, `settings.gradle` — konfiguracja budowania.


## 5. Jak zbudować i uruchomić (skrót)
1. Otwórz projekt w Android Studio i poczekaj na synchronizację Gradle.
2. Upewnij się, że masz ustawione `ANDROID_HOME` / SDK i `adb` jest dostępny w PATH (potrzebne do instalacji na urządzeniu fizycznym).
3. W terminalu projektu uruchom:

```bash
./gradlew assembleDebug
./gradlew installDebug
```

4. Alternatywnie pobierz wygenerowany APK z `app/build/outputs/apk/debug/app-debug.apk` i zainstaluj ręcznie:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Uwaga: jeśli widzisz w logach błąd typu "adb not found", zainstaluj Android SDK Platform‑Tools i dodaj do PATH (na macOS np. dodaj do `~/.zshrc`).

## 6. Typowe problemy i ich naprawa
- Błąd Gradle: brak `AndroidManifest.xml` w `app/src/main/` — upewnij się, że plik istnieje i zawiera poprawny XML.
- SAXParseException "Premature end of file" — oznacza uszkodzony/pusty plik XML w `res/values/` lub innym zasobie; otwórz ostatnio modyfikowane pliki XML i popraw.
- Render errors w Android Studio (np. cykliczne style) — sprawdź `res/values/styles.xml` / `themes.xml` i usuń cykliczne odwołania parent → child.
- Crashe po przełączeniu motywu na dark mode — sprawdź, czy wszystkie atrybuty stylów są dostępne w obu motywach i czy nie następuje dostęp do null podczas recreation Activity.


