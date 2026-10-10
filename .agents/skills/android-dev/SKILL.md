---
name: android-dev
description: >-
  Use this skill for native Android development with Kotlin, Jetpack Compose, Material 3,
  Android Architecture Components (ViewModel, StateFlow), Room Database, CameraX,
  FusedLocationProviderClient, Gradle builds, APK packaging, and debugging.
---

# Android Native Development Specialist Skill

This skill provides comprehensive instructions, patterns, and runbooks for developing native Android applications using modern Jetpack libraries, Compose, and Kotlin.

---

## 1. UI Layer: Jetpack Compose & Material 3

### Core Pattern
Use declarative Composable functions with single unidirectional data flow (UDF):
* **State Hoisting**: Pass state down as parameters, bubble events up as lambda callbacks (`onAction: () -> Unit`).
* **Lifecycle-Aware State Collection**: Always collect StateFlows using `collectAsStateWithLifecycle()`:
  ```kotlin
  val records by viewModel.allRecords.collectAsStateWithLifecycle()
  ```
* **Previews and Tags**: Add semantic `Modifier.testTag("tag_name")` to key interactive elements for automated UI testing.

### Navigation & Top Bars
* Use `Scaffold` with `CenterAlignedTopAppBar` and `NavigationBar` / `NavigationBarItem`.
* Handle hardware back button navigation on secondary tabs using `BackHandler`:
  ```kotlin
  if (currentTab != AppNavTab.HOME) {
      BackHandler { currentTab = AppNavTab.HOME }
  }
  ```

---

## 2. Business Logic & Concurrency

### ViewModel & Coroutines
* Never expose `MutableStateFlow` outside the ViewModel. Expose immutable `StateFlow`:
  ```kotlin
  private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
  val uiState: StateFlow<UiState> = _uiState.asStateFlow()
  ```
* Execute background I/O operations strictly on `Dispatchers.IO`:
  ```kotlin
  viewModelScope.launch {
      val result = withContext(Dispatchers.IO) {
          repository.saveRecord(...)
      }
  }
  ```

---

## 3. Data Layer: Room Persistence

### Best Practices
1. **Entities**: Define immutable data classes annotated with `@Entity(tableName = "...")` and `@PrimaryKey`.
2. **DAOs**: Use Coroutine `Flow` for reactive queries and `suspend` functions for writes:
   ```kotlin
   @Dao
   interface InspectionDao {
       @Query("SELECT * FROM inspection_records ORDER BY timestamp DESC")
       fun getAllRecords(): Flow<List<InspectionRecord>>

       @Insert(onConflict = OnConflictStrategy.REPLACE)
       suspend fun insertRecord(record: InspectionRecord)
   }
   ```
3. **Database Singleton**: Provide synchronized thread-safe singleton initialization with Room database builder.

---

## 4. Hardware Services: CameraX & Location

### CameraX (Jetpack Camera)
* Bind lifecycle cleanly to avoid camera lock:
  ```kotlin
  val cameraProvider = ProcessCameraProvider.getInstance(context).get()
  cameraProvider.unbindAll()
  cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture)
  ```

### FusedLocationProviderClient
* Request high-accuracy updates for field telemetry:
  ```kotlin
  val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L)
      .setMinUpdateDistanceMeters(1.0f)
      .build()
  ```
* Detect Mock Locations to ensure anti-fraud integrity:
  ```kotlin
  val isMock = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      location.isMock
  } else {
      @Suppress("DEPRECATION")
      location.isFromMockProvider
  }
  ```

---

## 5. Gradle Build & CLI Runbook

### Build Commands
* **Build Debug APK**:
  ```bash
  ./gradlew :app:assembleDebug
  ```
  Artifact location: `app/build/outputs/apk/debug/app-debug.apk`

* **Run Unit Tests**:
  ```bash
  ./gradlew test
  ```

* **Clean Build Cache**:
  ```bash
  ./gradlew clean
  ```

---

## 6. Common Pitfalls & Solutions

* **Foreground Service Crashing on Android 14+**: Requires foreground service types declared in `AndroidManifest.xml` (e.g., `android:foregroundServiceType="location"`).
* **Missing Runtime Permissions**: Android 13+ (API 33) requires explicit `POST_NOTIFICATIONS` runtime permission.
* **Canvas Coordinate Scaling**: When drawing vector graphics on `Canvas`, calculate scales using `size.width` and `size.height` proportionally.
