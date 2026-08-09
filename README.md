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

## Real-time collaboration

The app ships with a **backend-agnostic repository layer**. Out of the box it runs in
**local/offline mode** (data lives in memory on the device). Real-time multi-device
collaboration is fully coded and activates automatically when you connect a Firebase project.

### How it works

| Layer | File | Role |
|-------|------|------|
| Contract | `data/ListRepository.kt` | Interface both backends implement |
| Local | `data/KinboRepository.kt` | In-memory store (always works, offline) |
| Cloud | `data/FirestoreSyncRepository.kt` | Real-time Firestore sync + Firebase Auth |
| Selector | `data/SyncManager.kt` | Picks cloud if Firebase is configured, else local |

`SyncManager.get()` checks at startup whether Firebase was initialized with a real project
config. If yes → `FirestoreSyncRepository` (live sync). If no → `KinboRepository` (local).
The ViewModel only knows the interface, so no UI changes are needed to switch.

### Activation steps

1. Create a project at <https://console.firebase.google.com>
2. Add an Android app with package name **`com.kinbo.app`**; download `google-services.json`
3. Put `google-services.json` in the `app/` module directory
4. In `build.gradle.kts` (root), add the plugin at the top:
   ```kotlin
   plugins {
       id("com.google.gms.google-services") version "4.4.2" apply false
   }
   ```
5. In `app/build.gradle.kts`, apply it:
   ```kotlin
   plugins {
       id("com.google.gms.google-services")
       // ...existing plugins
   }
   ```
6. In the Firebase console, enable **Cloud Firestore** and **Authentication → Email/Password**
7. Rebuild: `./gradlew assembleDebug`

The app will now sync lists, items, and collaborators across all signed-in devices in real time.

### Firestore data model

```
users/{uid}                     user profile (name, email, plan, premium)
lists/{listId}                  name, emoji, ownerId, shareCode, members[], memberUids[]
lists/{listId}/items/{itemId}   name, quantity, unit, price, category, priority, purchased
```

A snapshot listener on `lists where memberUids contains myUid` keeps every member's view live.

### Sharing & inviting

- Each list gets a 6-character **share code** (`generateShareCode`)
- The **Collaborators** screen (top-bar group icon) lets you: send the invite via Android share
  sheet, copy the code, join a list by code, and manage member roles (Owner / Editor / Viewer)
- `ListShare.kt` remains for quick text-only sharing of a list snapshot to anyone (no app needed)

