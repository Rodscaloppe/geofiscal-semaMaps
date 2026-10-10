# 💻 Especificação Tecnológica: Tech Stack & Arquitetura

> **Projeto**: GeoFiscal SEMA-MT (Sistema Integrado de Fiscalização Geoespacial e Remota)  
> **Versão da Arquitetura**: 2.4.0  
> **Paridade**: Multiplataforma Nativa (Android Jetpack Compose + iOS SwiftUI) com Shared KMP Core  
> **Idioma**: Português do Brasil (pt-BR)

---

## 1. Visão Geral da Arquitetura Multiplataforma

O projeto adota uma arquitetura em camadas orientada a domínio (**Clean Architecture + MVI/MVVM**), combinando as melhores práticas das plataformas móveis com uma rigorosa paridade funcional entre Android e iOS.

```
                          ┌──────────────────────────────────────────────┐
                          │         Camada de Apresentação (UI)          │
                          ├──────────────────────┬───────────────────────┤
                          │ Android: Compose M3  │   iOS: SwiftUI Native │
                          └──────────┬───────────┴───────────┬───────────┘
                                     │                       │
                          ┌──────────▼───────────────────────▼───────────┐
                          │   Camada de Estado e Lógica de Aplicação    │
                          ├──────────────────────┬───────────────────────┤
                          │ InspectionViewModel  │    InspectionStore    │
                          └──────────┬───────────┴───────────┬───────────┘
                                     │                       │
                          ┌──────────▼───────────────────────▼───────────┐
                          │    Camada de Domínio, Dados & Criptografia    │
                          ├──────────────────────────────────────────────┤
                          │   • Criptografia AES-256-CBC & Salt          │
                          │   • Fingerprint e Integridade SHA-256        │
                          │   • SemaIntegrationService (MT GeoIntelligence)│
                          │   • Room SQLite (Android) / JSON Core (iOS)   │
                          └──────────────────────────────────────────────┘
```

---

## 2. Stack Tecnológica Android

| Componente | Tecnologia / Biblioteca | Função no Projeto |
| :--- | :--- | :--- |
| **Linguagem** | Kotlin 2.0+ (JVM 17/21) | Linguagem principal do ecossistema Android. |
| **UI Toolkit** | Jetpack Compose + Material 3 | Interface moderna, declarativa, temas institucionais SEMA Verde Floresta e modo escuro. |
| **Gerenciamento de Estado** | StateFlow, SharedFlow, ViewModel | Arquitetura reativa desacoplada com ciclo de vida seguro (`collectAsStateWithLifecycle`). |
| **Câmera Pericial** | AndroidX CameraX (Preview, ImageCapture) | Viewfinder com renderização de telemetria HUD em tempo real e captura de fotos em alta resolução. |
| **Localização de Alta Precisão** | Google Play Services `FusedLocationProviderClient` | Obtenção de coordenadas GNSS (GPS, GLONASS, Galileo) com cálculo de acurácia em metros e velocidade. |
| **Sensores de Campo** | Android `SensorManager` | Leitura de vetor de rotação, acelerômetro e magnetômetro para bússola pericial (azimute, pitch, roll). |
| **Banco de Dados Local** | Room Database (SQLite) v3 | Persistência relacional offline-first das vistorias, denúncias, coordenadas e logs de auditoria. |
| **Criptografia** | `javax.crypto.Cipher` (AES/CBC/PKCS5Padding) | Criptografia local de imagens e dados sensíveis antes do salvamento em disco. |
| **Geração de QR Code** | ZXing (`com.google.zxing:core`) | Geração de bitmaps de QR Code para validação e auditoria pública imediata em campo. |
| **Exportação de Laudos** | Android `PdfDocument` Nativo | Geração de Laudo Técnico oficial padrão A4 com fotos periciais descriptografadas e selo digital. |
| **Processamento Assíncrono** | Kotlin Coroutines (`Dispatchers.IO`, `viewModelScope`) | Execução não bloqueante de IO criptográfico e acesso a banco. |

---

## 3. Stack Tecnológica iOS

| Componente | Tecnologia / Framework | Função no Projeto |
| :--- | :--- | :--- |
| **Linguagem** | Swift 5.10 / Swift 6 | Linguagem nativa e tipada para desenvolvimento Apple. |
| **UI Toolkit** | SwiftUI Nativo (iOS 17+) | Telas responsivas, cards periciais dinâmicos e folhas modais (`Sheet`). |
| **Gerenciamento de Estado** | `@ObservableObject`, `@Published`, Combine | Fluxo de dados unidirecional através de `InspectionStore` no `@MainActor`. |
| **Câmera Pericial** | AVFoundation (`AVCaptureSession`, `AVCapturePhotoOutput`) | Controle fino de sensor óptico, flash e preview com overlay d'água pericial. |
| **Geolocalização** | `CoreLocation` (`CLLocationManager`) | Monitoramento contínuo em primeiro e segundo plano com melhor acurácia disponível. |
| **Sensores de Hardware** | `CoreMotion` (`CMMotionManager`) | Leitura de magnetômetro e atitude do dispositivo para cálculo de azimute e inclinação. |
| **Persistência Local** | Swift Codable + Sandbox Documents | Armazenamento de registros em JSON com suporte retrocompatível para schemas legados. |
| **Criptografia & Hashes** | `CommonCrypto` (`CC_SHA256`) | Resumo criptográfico de fotos e cadeias de custódia do laudo. |
| **Geração de QR Code** | `CoreImage` (`CIFilter.qrCodeGenerator()`) | Renderização de QR Code em alta fidelidade com correção de erro nível 'Q'. |
| **Exportação de Laudos** | `UIGraphicsPDFRenderer` Nativo | Emissão de Laudos Técnicos A4 vetoriais prontos para impressão ou compartilhamento via `UIActivityViewController`. |
| **Mapeamento** | MapKit (`Map`, `Annotation`, `Polyline`) | Visualização cartográfica dos polígonos de vistoria e denúncias ambientais em Mato Grosso. |

---

## 4. Núcleo Compartilhado (Kotlin Multiplatform - KMP)

Localizado no diretório `shared/`:
* Configuração Gradle para compilação multiplataforma (`androidTarget` e `iosTarget`/`iosArm64`/`iosSimulatorArm64`).
* Compartilhamento de contratos, regras de validação cadastral (CAR), estruturas de cálculo geodésico e enquadramentos legais.
* Exportação de framework nativo Objective-C / Swift para incorporação no Xcode workspace.

---

## 5. Parâmetros de Segurança e Anti-Fraude

1. **Hardware Salt**: A derivação das chaves criptográficas utiliza identificadores únicos de hardware do aparelho, garantindo que arquivos criptografados não possam ser enxertados a partir de outros dispositivos.
2. **Anti-Mocking**: Verificação de `location.isFromMockProvider()` no Android e flags de simulação em iOS para barrar falsificação pericial de GPS.
3. **Imutabilidade da Cadeia de Custódia**: Cada vistoria possui um `recordHashSha256` calculado sobre o ID, carimbo de tempo, coordenadas de latitude/longitude, hash da foto e número do CAR. Qualquer alteração em banco invalida a correspondência do hash.
4. **Log de Auditoria AuditLog**: Registro de cada evento do sistema (`CRIACAO_REGISTRO`, `ALTERACAO_PERMISSAO`, `SINCRONIZACAO_REMOTA`) acompanhado de identificador do usuário e timestamp criptografado.

---

## 6. Automação de Build e CI/CD

* **Build System Android**: Gradle 8.x/9.x com Kotlin DSL (`build.gradle.kts`).
* **Build System iOS**: Xcode 15/16 com `xcodebuild`, suporte a CocoaPods e SPM.
* **Pipelines Prontas**: `bitrise.yml` e `codemagic.yaml` configurados na raiz do repositório para automação de testes unitários e geração de binários de produção (.apk / .aab / .ipa).
