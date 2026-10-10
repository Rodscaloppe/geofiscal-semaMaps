---
name: kotlin-kmp
description: >-
  Use this skill when developing with Kotlin and Kotlin Multiplatform (KMP),
  writing shared commonMain code, handling expect/actual platform declarations,
  coroutines, Kotlin/Native Objective-C framework export, and Gradle KMP configurations.
---

# Kotlin & Kotlin Multiplatform (KMP) Specialist Skill

This skill provides patterns, architecture guidelines, and build runbooks for developing shared multiplatform code in Kotlin for Android and iOS.

---

## 1. Kotlin Language Standards & Idioms

* **Immutability First**: Default to `val` and immutable data classes.
* **Sealed Interfaces & Hierarchies**: Model domain states, results, and network responses using `sealed interface` or `sealed class` for exhaustive `when` matching.
* **Null Safety**: Avoid force unwrapping (`!!`). Use safe calls (`?.`), Elvis operator (`?:`), and `requireNotNull()`.
* **Coroutines**: Structured concurrency with `CoroutineScope`, `SupervisorJob`, and context switching via `withContext(Dispatchers.IO)`.

---

## 2. KMP SourceSet Architecture

```text
shared/src/
├── commonMain/kotlin/     # 100% platform-agnostic business logic, domain models, geo math
├── androidMain/kotlin/    # Android-specific implementations (Android Context, Room, CameraX)
└── iosMain/kotlin/        # iOS-specific implementations (Apple SDKs, CoreLocation, AVFoundation)
```

### What Belongs in `commonMain`:
* Mathematical and geodetic algorithms (e.g., Haversine distance, polygon area calculation).
* Formatting and validation rules (e.g., CAR format regex, environmental status rules).
* Domain entities and business models (`LocationData`, `SensorData`, `MapMarker`).
* Repository interfaces and business ViewModels.

### What Belongs in Platform SourceSets:
* Hardware sensor listeners (`android.hardware.SensorManager` vs `CoreMotion`).
* Camera APIs (`androidx.camera` vs `AVFoundation`).
* Local databases (Android Room SQLite vs iOS SQLite/CoreData).

---

## 3. The `expect` / `actual` Pattern

Use `expect` declarations in `commonMain` to specify contracts that must be implemented by platform sourceSets:

```kotlin
// commonMain
expect class PlatformLocationService {
    fun startListening()
    fun stopListening()
}

// androidMain
actual class PlatformLocationService(private val context: Context) {
    actual fun startListening() { /* FusedLocationProviderClient */ }
    actual fun stopListening() { /* ... */ }
}

// iosMain
actual class PlatformLocationService {
    actual fun startListening() { /* CLLocationManager */ }
    actual fun stopListening() { /* ... */ }
}
```

---

## 4. Kotlin/Native & Swift Interoperability

### Objective-C Export
Kotlin/Native compiles `iosMain` into an Objective-C framework (`shared.framework`):
* Kotlin objects are accessed in Swift via `.shared`:
  ```swift
  let distance = GeoCalculations.shared.haversineDistanceKm(...)
  ```
* Kotlin `List` maps to Swift `NSArray` / `[Any]`.
* Kotlin `Double` and `Int` map directly to Swift primitives.
* Custom Swift names can be designated in Kotlin using `@ObjCName(name = "SwiftName")`.

### Flow to Swift Bridging
Kotlin Coroutines `Flow` cannot be directly observed by SwiftUI without an adapter. Either:
1. Wrap flows in a callback-based listener (`watch { value in ... }`).
2. Use SKIE (Swift Kotlin Interface Enhancer) to generate native Swift `AsyncSequence`.

---

## 5. Gradle KMP Build Commands

* **Compile Shared Framework for iOS Device (ARM64)**:
  ```bash
  ./gradlew :shared:linkReleaseFrameworkIosArm64
  ```
  Artifact location: `shared/build/bin/iosArm64/releaseFramework/shared.framework`

* **Compile Shared Framework for iOS Simulator**:
  ```bash
  ./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
  ```

* **Build Android Library**:
  ```bash
  ./gradlew :shared:assembleDebug
  ```

* **Run Shared Unit Tests**:
  ```bash
  ./gradlew :shared:allTests
  ```
