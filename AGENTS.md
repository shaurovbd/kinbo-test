# AGENTS.md — repository memory for OpenHands

## Kinbo Android app — build environment & lessons

The Kinbo app lives in `/workspace/project`. Build/test setup that worked here:

- **JDK**: OpenJDK 21 (`/usr/lib/jvm/java-21-openjdk-amd64`). Android Gradle Plugin 8.5.2 + Gradle 8.7 accept JDK 21; Kotlin/Java bytecode target stays 17.
- **Android SDK**: installed at `/opt/android-sdk` via commandline-tools. Packages needed: `platform-tools`, `platforms;android-34`, `build-tools;34.0.0`. Accept licenses with `yes | sdkmanager --licenses` (run with `sudo -E` preserving ANDROID_HOME since the dir is root-owned, then `chown -R openhands` it).
- `local.properties` pins `sdk.dir=/opt/android-sdk`.
- Env to source before building:
  ```
  export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
  export ANDROID_HOME=/opt/android-sdk
  export PATH=$JAVA_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH
  ```
- Build: `./gradlew assembleDebug lintDebug` → APK at `app/build/outputs/apk/debug/app-debug.apk`.

### Stack versions that compile together
- Kotlin 2.0.20 + `org.jetbrains.kotlin.plugin.compose` 2.0.20 (the compose plugin version MUST equal the Kotlin version; 1.9.24 has no compose plugin artifact).
- AGP 8.5.2, Compose BOM 2024.06.00, navigation-compose 2.7.7.
- With Kotlin 2.0 the Compose compiler plugin is applied via `org.jetbrains.kotlin.plugin.compose` — do NOT set `composeOptions.kotlinCompilerExtensionVersion`.

### Gotchas hit during first compile (and fixes)
1. **AndroidX not enabled** → `gradle.properties` needs `android.useAndroidX=true`.
2. **Experimental Compose APIs become errors in Kotlin 2.0** → add `-opt-in=...ExperimentalMaterial3Api/ExperimentalFoundationApi/ExperimentalLayoutApi/...` to `kotlinOptions.freeCompilerArgs` globally instead of annotating each call site.
3. **Data-class positional args** — `ShoppingItem("Milk", 2.0, "L", ...)` silently mapped to the wrong params because `id` is the first constructor param. Use named args: `ShoppingItem(name=..., quantity=..., unit=...)`.
4. **Parameter name shadowing in Canvas** — a `size: Int` composable param shadowed DrawScope `size`, so `Size(size.width, ...)` failed. Use `this.size.width` inside the DrawScope.
5. **Icon imports** — `Notifications` is `icons.rounded.Notifications` (not `automirrored`); `ArrowForwardIos` isn't auto-mirrored — use `Icons.Rounded.ChevronRight`.
6. **Lint**: `CAMERA`/`RECORD_AUDIO` permissions require `<uses-feature ... required="false">` tags or lint fails (ChromeOS hardware check).
