```mermaid
graph TD
    A[Android Module] --> B[Shared Kotlin Multiplatform Code]
    A --> C[Build Configuration (build.gradle)]
    B --> D[shared/src/commonMain/kotlin/com/example/]
    B --> E[shared/src/iosMain/kotlin/com/example/]
    A --> F[app/src/main/java/com/example/]
    F --> G[data/ (DAOs, Repositories)]
    F --> H[security/ (Encryption, Authentication)]
    F --> I[service/ (Background Services)]
    F --> J[ui/ (UI Components)]
    F --> K[MainActivity.kt (Entry Point)]
    C --> L[AndroidManifest.xml (App Configuration)]
    C --> M[gradle.properties (Build Properties)]
```