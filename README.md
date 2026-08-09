# Kinbo
Kinbo — AI-powered collaborative shopping list and household planning app.

Built with Kotlin + Jetpack Compose based on the Kinbo PRD (v1.0 MVP).

## Build
Requires JDK 17+ (built/tested with JDK 21) and Android SDK (compileSdk 34, build-tools 34.0.0).

```bash
# Set environment (adjust paths as needed)
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export ANDROID_HOME=/opt/android-sdk
export PATH=$JAVA_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH

./gradlew assembleDebug
```

The debug APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

Run lint:
```bash
./gradlew lintDebug
```

## Project structure
- `app/src/main/java/com/kinbo/app/`
  - `model/` — data models (ShoppingList, ShoppingItem, User, Budget, etc.)
  - `data/` — repository, ViewModel, rule-based AI shopping assistant + seed data
  - `ui/theme/` — Material 3 color/typography/shapes, light & dark themes
  - `ui/components/` — reusable components (progress ring, cards, chips, avatar pile)
  - `ui/navigation/` — routes, bottom tabs, NavHost
  - `ui/screens/` — Splash, Onboarding, Login, Signup, Home, ShoppingList, AddItem,
    CreateList, AiAssistant, Budget, Analytics, Notifications, Settings, Profile
- `app/src/main/res/` — strings, themes, colors, adaptive launcher icons
