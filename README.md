# Gemini Chat Studio (Android Jetpack Compose)

A production-ready single-screen Android application built with Kotlin, Jetpack Compose, Material 3, and the official Google GenAI SDK (@google/genai / com.google.ai.client.generativeai).

## Features
- Real-time response streaming with Gemini 1.5 Flash
- Multi-turn conversation memory (ChatSession)
- Secure API key encryption via Android Keystore & EncryptedSharedPreferences
- Android 15 Edge-to-Edge display (enableEdgeToEdge)
- Dynamic Material 3 design tokens & Dark Theme

## How to Build the APK
1. Open this project folder in **Android Studio Ladybug (2024.2+)** or later.
2. Allow Gradle sync to complete.
3. Build APK via CLI:
   ```bash
   ./gradlew assembleDebug
   ```
4. The generated APK will be located at:
   `app/build/outputs/apk/debug/app-debug.apk`
