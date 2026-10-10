---
name: mobile-dev
description: >-
  Provides comprehensive guidance, architectural patterns, and runbooks for modern
  mobile development, cross-platform architecture (KMP/Compose), offline-first synchronization,
  hardware sensor management (GPS, Camera, Motion), and mobile security best practices.
---

# Mobile Development Specialist Skill

This skill guides the design, implementation, debugging, and deployment of mission-critical mobile applications across iOS and Android.

---

## 1. Core Architectural Principles

### 1.1 Offline-First Architecture
Mobile devices frequently operate in low-signal or disconnected environments (field inspections, rural areas, moving vehicles):
* **Single Source of Truth**: The local database (Room on Android, SQLite/SwiftData/CoreData on iOS) is the primary source of truth for the UI.
* **Optimistic Local Writes**: Write records immediately to local disk, mark them with `syncStatus = PENDING`, and trigger background synchronization asynchronously.
* **Idempotent Synchronization**: Every record must carry an immutable UUID (`id`) and timestamp to prevent duplicates during retries.
* **Network Monitoring**: Continuously observe network reachability to trigger automatic synchronization when connectivity is restored.

### 1.2 Hardware & Sensor Management
* **Location & Geodetic Precision**:
  * Always request runtime location authorization gracefully.
  * Use platform-native sensor fusion (Google Play FusedLocationProviderClient on Android, CoreLocation on iOS).
  * Filter out GPS noise (e.g., ignore jumps < 1.5m or spikes > 500m/s).
  * Check for software-simulated GPS (Mock Location checks) for pericial and anti-fraud integrity.
* **Camera Integration**:
  * Free camera hardware sessions (`CameraX.unbindAll()`, `AVCaptureSession.stopRunning()`) immediately when leaving viewfinder screens to prevent battery drain and thermal throttling.
  * Stamp EXIF metadata and on-canvas watermarks (Coordinates, Altitude, Heading, Date/Time, Municipality) onto captured frames for public faith and legal evidence.
* **Motion & Heading (Orientation)**:
  * Combine accelerometer, magnetometer, and gyroscope to calculate magnetic/true heading (Azimuth) and inclination (Pitch & Roll).

---

## 2. Mobile Security & Chain of Custody

1. **At-Rest Encryption**:
   * Encrypt sensitive field media using AES-256-CBC or AES-GCM before saving to disk.
   * Store encryption keys in hardware-backed storage (Android Keystore / iOS Keychain & Secure Enclave).
2. **Cryptographic Fingerprinting (Public Faith)**:
   * Generate SHA-256 hexadecimal digests of both the original image bytes and the complete telemetry payload.
   * Embed the digest in pericial QR codes (`https://domain/valida/<id>`) to allow offline verification of document authenticity.
3. **Anti-Tampering**:
   * Reject or flag Mock GPS locations.
   * Strip or redact sensitive personal data in logs.

---

## 3. UI/UX Consistency Standards

When maintaining dual iOS & Android applications:
* **Feature Parity**: Ensure data models, validation rules (e.g., CAR format, environmental criteria), and business states match 1:1.
* **Platform Conventions**:
  * **Android**: Material 3 guidelines, Scaffold, TopAppBar, NavigationBar, FloatingActionButton, snackbars.
  * **iOS**: Human Interface Guidelines (HIG), SF Symbols, NavigationStack, TabView with native tinting, sheet modals.
* **Design Tokens**: Standardize brand colors across platforms (e.g., Forest Green `#1B5E20`, Alert Red `#D32F2F`, Accent `#2EE59D`).

---

## 4. Mobile Troubleshooting & Performance Runbook

| Issue | Root Cause | Solution |
| :--- | :--- | :--- |
| **High Battery Drain** | Continuous high-frequency GPS or camera running in background. | Reduce location update frequency when stationary (`distanceFilter`), stop camera session in `onDisappear`/`onPause`. |
| **Out of Memory (OOM)** | Large uncompressed camera Bitmaps loaded into memory. | Downsample images on capture (`inSampleSize` in Android, JPEG compression/scaling in iOS). |
| **Sync Deadlocks** | Synchronous network calls on Main thread. | Always dispatch network calls and database queries to background dispatchers (`Dispatchers.IO` in Kotlin, `Task.detached` / `Dispatchers` in Swift). |
| **Location Null/Stale** | Requesting location before GPS hardware fix. | Handle timeout gracefully and display "Acquiring GPS fix..." in the UI. |
