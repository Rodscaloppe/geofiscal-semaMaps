---
name: ios-dev
description: >-
  Use this skill for native iOS development with SwiftUI, MapKit, AVFoundation,
  CoreLocation, CoreMotion, Xcode project management (.xcodeproj / project.pbxproj),
  xcodebuild CLI, IPA packaging, and Apple app lifecycle.
---

# iOS Native Development Specialist Skill

This skill provides comprehensive instructions, patterns, and runbooks for native iOS development using Swift, SwiftUI, and Apple SDKs.

---

## 1. SwiftUI Architectural Patterns

### State Management
* **`@StateObject`**: Use for creating and owning reference-type models (`ObservableObject`).
* **`@ObservedObject`**: Use for passing already-instantiated observable models into child views.
* **`@Published`**: Annotate properties inside `ObservableObject` classes that trigger view updates.
* **`@MainActor`**: Annotate observable stores and ViewModels with `@MainActor` to guarantee UI updates happen on the main dispatch queue.

### Navigation & Modals
* Always wrap tab view contents in `NavigationStack` for navigation bars and title display modes.
* Present detail views and alerts using `.sheet(item: $selectedItem)` with identifiable models.

---

## 2. Hardware & Sensor Integration

### CoreLocation & Background Tracking
* Configure `CLLocationManager`:
  ```swift
  clManager.desiredAccuracy = kCLLocationAccuracyBest
  clManager.distanceFilter = 1.0
  clManager.allowsBackgroundLocationUpdates = true
  clManager.pausesLocationUpdatesAutomatically = false
  ```
* Required entries in `Info.plist`:
  * `NSLocationWhenInUseUsageDescription`
  * `NSLocationAlwaysAndWhenInUseUsageDescription`
  * `UIBackgroundModes` containing `location`

### AVFoundation Camera
* Manage `AVCaptureSession` lifecycle:
  * Configure session preset: `sessionPreset = .photo`.
  * Start and stop on background queues (`DispatchQueue.global(qos: .userInitiated)`).
  * Use `AVCapturePhotoOutput` with `AVCapturePhotoSettings` for high-resolution stills.
  * Integrate preview into SwiftUI via `UIViewRepresentable` wrapping `AVCaptureVideoPreviewLayer`.

### CoreMotion & Heading
* Use `CMMotionManager` with `xMagneticNorthZVertical` reference frame to calculate true azimuth heading regardless of device pitch and roll.

---

## 3. MapKit: Polylines & Custom Annotations

When requiring dynamic breadcrumb trails (trajectories) alongside custom pins:
* Wrap `MKMapView` in `UIViewRepresentable`.
* Implement `MKMapViewDelegate` in a `Coordinator` class:
  * `mapView(_:rendererFor:)`: Render `MKPolyline` with `MKPolylineRenderer` (strokeColor, lineWidth).
  * `mapView(_:viewFor:)`: Dequeue and configure `MKMarkerAnnotationView`.
  * `mapView(_:didSelect:)`: Handle pin selection callbacks to trigger SwiftUI sheets.

---

## 4. Xcode Project Structure (`project.pbxproj`)

When adding new `.swift` files programmatically without the Xcode GUI, register the file in `project.pbxproj`:
1. **`PBXBuildFile` section**: Add fileRef with unique 24-character hex ID.
2. **`PBXFileReference` section**: Declare path, file type (`sourcecode.swift`).
3. **`PBXGroup` section**: Add the file reference ID into the appropriate group (`Views`, `Services`, `Models`).
4. **`PBXSourcesBuildPhase` section**: Add the build file ID to compile during the build step.

---

## 5. Build & Packaging Runbook

### 5.1 Typecheck Swift Files
Fast syntax and typecheck validation without full IDE build:
```bash
xcrun -sdk iphonesimulator swiftc -typecheck -target arm64-apple-ios17.0-simulator \
  -module-cache-path build/ModuleCache \
  -F shared/build/bin/iosArm64/releaseFramework \
  iosApp/iosApp/**/*.swift
```

### 5.2 Build with xcodebuild
* **Simulator Debug Build**:
  ```bash
  xcodebuild build \
    -project iosApp/iosApp.xcodeproj \
    -scheme iosApp \
    -destination "generic/platform=iOS Simulator" \
    -configuration Debug \
    CODE_SIGN_IDENTITY="" CODE_SIGNING_REQUIRED=NO CODE_SIGNING_ALLOWED=NO
  ```

* **Device Release Build**:
  ```bash
  xcodebuild build \
    -project iosApp/iosApp.xcodeproj \
    -scheme iosApp \
    -destination "generic/platform=iOS" \
    -configuration Release \
    CODE_SIGN_IDENTITY="" CODE_SIGNING_REQUIRED=NO CODE_SIGNING_ALLOWED=NO \
    -derivedDataPath build/DerivedData
  ```

### 5.3 Generate `.ipa` Archive
```bash
rm -rf build/ios/ipa && mkdir -p build/ios/ipa/Payload
cp -R build/derivedData/Build/Products/Release-iphoneos/iosApp.app build/ios/ipa/Payload/
(cd build/ios/ipa && zip -r -y GeoFiscalSEMA.ipa Payload)
```

---

## 6. Common Gotchas & Solutions

* **iOS 17 `onChange` Deprecation**: Use `.onChange(of: value) { oldValue, newValue in ... }` instead of single-parameter closure.
* **Macro Server Issues in Sandbox**: In restricted sandbox environments, pass `-module-cache-path` or run via `xcodebuild`.
* **Missing Encryption Declaration**: Add `<key>ITSAppUsesNonExemptEncryption</key><false/>` in `Info.plist` to prevent TestFlight compliance prompts.
