
Ran command: Check Swift files in project.pbxproj (`grep -n "Swift" iosApp/iosApp.xcodeproj/project.pbxproj`)
Viewed project.pbxproj:1-150
Viewed project.pbxproj:151-200
Ran command: Check xcodebuild version (`xcodebuild -version`)
Ran command: Check Xcode schemes (`xcodebuild -list -project iosApp/iosApp.xcodeproj`)
Viewed codemagic.yaml:1-48
Ran command: Check shared.framework existence (`ls -la shared/build/bin/iosArm64/releaseFramework/ 2>/dev/null || echo "Not found"`)
Ran command: Test iOS build (`xcodebuild build -project iosApp/iosApp.xcodeproj -scheme iosApp -destination "generic/platform=iOS" -configuration Debug CODE_SIGN_IDENTITY="" CODE_SIGNING_REQUIRED=NO CODE_SIGNING_ALLOWED=NO -derivedDataPath build/DerivedData`)
Viewed InspectionRecord.kt:1-51
Viewed DenunciaEntity.kt:1-61
Viewed AuditLog.kt:1-17
Viewed UserPermission.kt:1-61
Viewed SemaIntegrationService.kt:1-139
Viewed CryptoManager.kt:1-92
Ran command: Inspect shared.h (`grep -n -C 3 "GeoCalculations" shared/build/bin/iosArm64/releaseFramework/shared.framework/Headers/shared.h 2>/dev/null || true`)
Ran command: Find sample denúncias in Android (`grep -rn "DEN-" app/src/`)
Viewed InspectionRepository.kt:1-100
Viewed InspectionRepository.kt:101-260
Viewed InspectionViewModel.kt:1-100
Viewed InspectionViewModel.kt:101-200
Viewed InspectionViewModel.kt:201-300
Viewed InspectionViewModel.kt:301-350
Created InspectionModels.swift
Created SemaIntegrationService.swift
Created InspectionStore.swift
Created ExportService.swift
Viewed LocationManager.swift:1-40
Created LocationManager.swift
Created TopBarView.swift
Created CaptureView.swift
Created HistoryView.swift
Created MapInspectionView.swift
Created DashboardView.swift
Created AdminPanelView.swift
Created RecordDetailSheet.swift
Created QrCodeSheet.swift
Created ContentView.swift
Viewed project.pbxproj:1-120
Viewed project.pbxproj:9-25
Edited project.pbxproj
Viewed project.pbxproj:30-50
Edited project.pbxproj
Viewed project.pbxproj:60-110
Edited project.pbxproj
Viewed project.pbxproj:180-220
Viewed project.pbxproj:215-235
Edited project.pbxproj
Ran command: Check git status (`git status`)
Ran command: Check Swift files syntax (`swiftc -parse \
  iosApp/iosApp/Models/InspectionModels.swift \
  iosApp/iosApp/Services/SemaIntegrationService.swift \
  iosApp/iosApp/Services/InspectionStore.swift \
  iosApp/iosApp/Services/ExportService.swift \
  iosApp/iosApp/Services/LocationManager.swift \
  iosApp/iosApp/Services/CameraManager.swift \
  iosApp/iosApp/Services/SensorManager.swift \
  iosApp/iosApp/Views/Components/TopBarView.swift \
  iosApp/iosApp/Views/RecordDetailSheet.swift \
  iosApp/iosApp/Views/QrCodeSheet.swift \
  iosApp/iosApp/Views/HistoryView.swift \
  iosApp/iosApp/Views/DashboardView.swift \
  iosApp/iosApp/Views/AdminPanelView.swift \
  iosApp/iosApp/Views/CaptureView.swift \
  iosApp/iosApp/Views/MapInspectionView.swift \
  iosApp/iosApp/ContentView.swift \
  iosApp/iosApp/GeoFiscalSemaApp.swift`)
Ran command: Run swiftc typecheck (`xcrun -sdk iphonesimulator swiftc -typecheck -target arm64-apple-ios17.0-simulator \
  -F shared/build/bin/iosArm64/releaseFramework \
  iosApp/iosApp/Models/InspectionModels.swift \
  iosApp/iosApp/Services/SemaIntegrationService.swift \
  iosApp/iosApp/Services/InspectionStore.swift \
  iosApp/iosApp/Services/ExportService.swift \
  iosApp/iosApp/Services/LocationManager.swift \
  iosApp/iosApp/Services/CameraManager.swift \
  iosApp/iosApp/Services/SensorManager.swift \
  iosApp/iosApp/Views/Components/TopBarView.swift \
  iosApp/iosApp/Views/RecordDetailSheet.swift \
  iosApp/iosApp/Views/QrCodeSheet.swift \
  iosApp/iosApp/Views/HistoryView.swift \
  iosApp/iosApp/Views/DashboardView.swift \
  iosApp/iosApp/Views/AdminPanelView.swift \
  iosApp/iosApp/Views/CaptureView.swift \
  iosApp/iosApp/Views/MapInspectionView.swift \
  iosApp/iosApp/ContentView.swift \
  iosApp/iosApp/GeoFiscalSemaApp.swift`)
Ran command: Typecheck iOS Swift files (`mkdir -p build/ModuleCache && xcrun -sdk iphonesimulator swiftc -typecheck -target arm64-apple-ios17.0-simulator \
  -module-cache-path build/ModuleCache \
  -F shared/build/bin/iosArm64/releaseFramework \
  iosApp/iosApp/Models/InspectionModels.swift \
  iosApp/iosApp/Services/SemaIntegrationService.swift \
  iosApp/iosApp/Services/InspectionStore.swift \
  iosApp/iosApp/Services/ExportService.swift \
  iosApp/iosApp/Services/LocationManager.swift \
  iosApp/iosApp/Services/CameraManager.swift \
  iosApp/iosApp/Services/SensorManager.swift \
  iosApp/iosApp/Views/Components/TopBarView.swift \
  iosApp/iosApp/Views/RecordDetailSheet.swift \
  iosApp/iosApp/Views/QrCodeSheet.swift \
  iosApp/iosApp/Views/HistoryView.swift \
  iosApp/iosApp/Views/DashboardView.swift \
  iosApp/iosApp/Views/AdminPanelView.swift \
  iosApp/iosApp/Views/CaptureView.swift \
  iosApp/iosApp/Views/MapInspectionView.swift \
  iosApp/iosApp/ContentView.swift \
  iosApp/iosApp/GeoFiscalSemaApp.swift`)
Viewed InspectionStore.swift:115-145
Edited InspectionStore.swift
Ran command: Typecheck Swift code (`xcrun -sdk iphonesimulator swiftc -typecheck -target arm64-apple-ios17.0-simulator \
  -module-cache-path build/ModuleCache \
  -F shared/build/bin/iosArm64/releaseFramework \
  iosApp/iosApp/Models/InspectionModels.swift \
  iosApp/iosApp/Services/SemaIntegrationService.swift \
  iosApp/iosApp/Services/InspectionStore.swift \
  iosApp/iosApp/Services/ExportService.swift \
  iosApp/iosApp/Services/LocationManager.swift \
  iosApp/iosApp/Services/CameraManager.swift \
  iosApp/iosApp/Services/SensorManager.swift \
  iosApp/iosApp/Views/Components/TopBarView.swift \
  iosApp/iosApp/Views/RecordDetailSheet.swift \
  iosApp/iosApp/Views/QrCodeSheet.swift \
  iosApp/iosApp/Views/HistoryView.swift \
  iosApp/iosApp/Views/DashboardView.swift \
  iosApp/iosApp/Views/AdminPanelView.swift \
  iosApp/iosApp/Views/CaptureView.swift \
  iosApp/iosApp/Views/MapInspectionView.swift \
  iosApp/iosApp/ContentView.swift \
  iosApp/iosApp/GeoFiscalSemaApp.swift`)
Ran command: Typecheck with bypass sandbox (`xcrun -sdk iphonesimulator swiftc -typecheck -target arm64-apple-ios17.0-simulator \
  -module-cache-path build/ModuleCache \
  -F shared/build/bin/iosArm64/releaseFramework \
  iosApp/iosApp/Models/InspectionModels.swift \
  iosApp/iosApp/Services/SemaIntegrationService.swift \
  iosApp/iosApp/Services/InspectionStore.swift \
  iosApp/iosApp/Services/ExportService.swift \
  iosApp/iosApp/Services/LocationManager.swift \
  iosApp/iosApp/Services/CameraManager.swift \
  iosApp/iosApp/Services/SensorManager.swift \
  iosApp/iosApp/Views/Components/TopBarView.swift \
  iosApp/iosApp/Views/RecordDetailSheet.swift \
  iosApp/iosApp/Views/QrCodeSheet.swift \
  iosApp/iosApp/Views/HistoryView.swift \
  iosApp/iosApp/Views/DashboardView.swift \
  iosApp/iosApp/Views/AdminPanelView.swift \
  iosApp/iosApp/Views/CaptureView.swift \
  iosApp/iosApp/Views/MapInspectionView.swift \
  iosApp/iosApp/ContentView.swift \
  iosApp/iosApp/GeoFiscalSemaApp.swift`)
Edited InspectionModels.swift
Ran command: Re-run typecheck (`xcrun -sdk iphonesimulator swiftc -typecheck -target arm64-apple-ios17.0-simulator \
  -module-cache-path build/ModuleCache \
  -F shared/build/bin/iosArm64/releaseFramework \
  iosApp/iosApp/Models/InspectionModels.swift \
  iosApp/iosApp/Services/SemaIntegrationService.swift \
  iosApp/iosApp/Services/InspectionStore.swift \
  iosApp/iosApp/Services/ExportService.swift \
  iosApp/iosApp/Services/LocationManager.swift \
  iosApp/iosApp/Services/CameraManager.swift \
  iosApp/iosApp/Services/SensorManager.swift \
  iosApp/iosApp/Views/Components/TopBarView.swift \
  iosApp/iosApp/Views/RecordDetailSheet.swift \
  iosApp/iosApp/Views/QrCodeSheet.swift \
  iosApp/iosApp/Views/HistoryView.swift \
  iosApp/iosApp/Views/DashboardView.swift \
  iosApp/iosApp/Views/AdminPanelView.swift \
  iosApp/iosApp/Views/CaptureView.swift \
  iosApp/iosApp/Views/MapInspectionView.swift \
  iosApp/iosApp/ContentView.swift \
  iosApp/iosApp/GeoFiscalSemaApp.swift`)

I launched the Swift typecheck compilation to verify all screens, models, and services in the iOS app. I will check the result as soon as it completes.

Checked task Re-run typecheck
Viewed CaptureView.swift:70-95
Viewed CaptureView.swift:425-443
Edited CaptureView.swift
Viewed CaptureView.swift:80-95
Edited CaptureView.swift
Viewed CaptureView.swift:435-450
Edited CaptureView.swift
Ran command: Run swiftc typecheck (`xcrun -sdk iphonesimulator swiftc -typecheck -target arm64-apple-ios17.0-simulator \
  -module-cache-path build/ModuleCache \
  -F shared/build/bin/iosArm64/releaseFramework \
  iosApp/iosApp/Models/InspectionModels.swift \
  iosApp/iosApp/Services/SemaIntegrationService.swift \
  iosApp/iosApp/Services/InspectionStore.swift \
  iosApp/iosApp/Services/ExportService.swift \
  iosApp/iosApp/Services/LocationManager.swift \
  iosApp/iosApp/Services/CameraManager.swift \
  iosApp/iosApp/Services/SensorManager.swift \
  iosApp/iosApp/Views/Components/TopBarView.swift \
  iosApp/iosApp/Views/RecordDetailSheet.swift \
  iosApp/iosApp/Views/QrCodeSheet.swift \
  iosApp/iosApp/Views/HistoryView.swift \
  iosApp/iosApp/Views/DashboardView.swift \
  iosApp/iosApp/Views/AdminPanelView.swift \
  iosApp/iosApp/Views/CaptureView.swift \
  iosApp/iosApp/Views/MapInspectionView.swift \
  iosApp/iosApp/ContentView.swift \
  iosApp/iosApp/GeoFiscalSemaApp.swift`)
Edited CaptureView.swift
Edited ContentView.swift
Ran command: Verify typecheck clean (`xcrun -sdk iphonesimulator swiftc -typecheck -target arm64-apple-ios17.0-simulator \
  -module-cache-path build/ModuleCache \
  -F shared/build/bin/iosArm64/releaseFramework \
  iosApp/iosApp/Models/InspectionModels.swift \
  iosApp/iosApp/Services/SemaIntegrationService.swift \
  iosApp/iosApp/Services/InspectionStore.swift \
  iosApp/iosApp/Services/ExportService.swift \
  iosApp/iosApp/Services/LocationManager.swift \
  iosApp/iosApp/Services/CameraManager.swift \
  iosApp/iosApp/Services/SensorManager.swift \
  iosApp/iosApp/Views/Components/TopBarView.swift \
  iosApp/iosApp/Views/RecordDetailSheet.swift \
  iosApp/iosApp/Views/QrCodeSheet.swift \
  iosApp/iosApp/Views/HistoryView.swift \
  iosApp/iosApp/Views/DashboardView.swift \
  iosApp/iosApp/Views/AdminPanelView.swift \
  iosApp/iosApp/Views/CaptureView.swift \
  iosApp/iosApp/Views/MapInspectionView.swift \
  iosApp/iosApp/ContentView.swift \
  iosApp/iosApp/GeoFiscalSemaApp.swift`)
Ran command: Build iOS App (`xcodebuild build \
  -project iosApp/iosApp.xcodeproj \
  -scheme iosApp \
  -destination "generic/platform=iOS Simulator" \
  -configuration Debug \
  CODE_SIGN_IDENTITY="" \
  CODE_SIGNING_REQUIRED=NO \
  CODE_SIGNING_ALLOWED=NO`)

I have initiated the Xcode build for the iOS app to verify that all Xcode configurations and source files compile together. I will review the build log as soon as it completes.

Checked task Build iOS App
Checked task Build iOS App
Ran command: Git status summary (`git status -s`)

As telas e funcionalidades equivalentes do Android foram **completamente desenvolvidas em SwiftUI nativo no `iosApp`**, equalizando a experiência, regras de negócio e interface visual entre ambas as plataformas.

O build completo do Xcode foi validado com **100% de sucesso (`** BUILD SUCCEEDED **`)**.

---

### Resumo das Telas e Recursos Implementados no iOS

#### 1. Navegação Principal e Barra Superior (TopBar)
* **Estrutura de 5 Abas Idêntica ao Android**:
  1. **Captura** (`camera.fill`): Aba inicial de vistoria e sensoriamento.
  2. **Histórico** (`clock.arrow.circlepath`): Listagem de vistorias e denúncias.
  3. **Mapa** (`map.fill`): Visualização geoespacial com traçados e marcadores.
  4. **Painel** (`chart.bar.fill`): Dashboard analítico e gerencial de KPIs.
  5. **Admin** (`person.badge.shield.checkmark.fill`): Controle de perfis, auditoria e segurança.
* **TopBar Institucional ([`TopBarView.swift`](file:///Volumes/Macintosh_HD/Users/user301275/geofiscal-semaMaps/iosApp/iosApp/Views/Components/TopBarView.swift))**:
  * Emblema com escudo SEMA e título *GeoFiscal SEMA-MT*.
  * Subtítulo dinâmico exibindo o cargo do fiscal ativo (ex: *Fiscal Técnico Ambiental (SEMA)*).
  * Badge de conectividade **Online/Offline** com indicador luminoso verde/vermelho.
  * Botão de **Sincronização manual** com animação contínua de rotação durante o envio.

---

#### 2. Tela de Captura Georreferenciada ([`CaptureView.swift`](file:///Volumes/Macintosh_HD/Users/user301275/geofiscal-semaMaps/iosApp/iosApp/Views/CaptureView.swift))
* **Câmera & Watermark Pericial**:
  * Visualizador de câmera ao vivo com controle de flash, troca de lente e disparador, além de opção de importar foto da galeria.
  * Marca d'água pericial em overlay na foto capturada com Latitude, Longitude, Altitude, Azimute da bússola e Município-MT.
* **HUD de Telemetria e Sensores**:
  * Status de fixação de satélites GPS de alta precisão.
  * Indicador de agulha magnética de azimute/bússola em tempo real.
  * Grid com Latitude, Longitude, Altitude e Acurácia.
  * Velocidade (km/h) e odômetro de distância percorrida (km).
  * **Selo Anti-Fraude**: Validação pericial de Hardware GPS autêntico versus Mock GPS.
  * Ações de *Fix de Precisão* e alternância de rastreamento em 2º plano.
* **Sensoriamento Remoto SEMA-MT**:
  * Identificação automática do município de MT mais próximo e bioma correspondente (**Amazônia**, **Cerrado**, **Pantanal**).
  * Alertas de irregularidade (Termos de Embargo ativos, alertas de desmatamento DETER/PRODES, focos de calor INPE/MODIS).
  * Campo e botão de **Validação de CAR** com checagem de formato e integridade.
* **Assinatura e Fé Pública**:
  * Campos de Título da Vistoria, Parecer Técnico e Nome do Fiscal.
  * Botão *"Salvar Vistoria com Fé Pública (SHA-256)"*, gerando hash criptográfico de integridade e salvando os dados no dispositivo.

---

#### 3. Tela de Histórico ([`HistoryView.swift`](file:///Volumes/Macintosh_HD/Users/user301275/geofiscal-semaMaps/iosApp/iosApp/Views/HistoryView.swift))
* **Seletor de Seções**:
  * Alternância entre **Vistorias** e **Denúncias**.
* **Busca & Filtros**:
  * Campo de busca textual por título, município ou número de CAR.
  * Filtros rápidos: `TODOS`, `IRREGULAR`, `REGULAR` e `PENDENTE SYNC`.
* **Cards de Vistoria**:
  * Indicadores de bioma, município, CAR, enquadramento ambiental e status de sincronização (Pendente/Sincronizado).
  * Botões de ação rápida: abrir detalhes, exportar laudo em PDF oficial, visualizar QR Code e exclusão.
* **Cards de Denúncias**:
  * Protocolo oficial (`DEN-2026-SEMA-XXXX`), gravidade (Crítica, Alta, Média, Baixa), tipificação da infração e status de apuração.

---

#### 4. Mapa Geoespacial ([`MapInspectionView.swift`](file:///Volumes/Macintosh_HD/Users/user301275/geofiscal-semaMaps/iosApp/iosApp/Views/MapInspectionView.swift))
* **MapKit Integrado**:
  * Traçado da **trajetória percorrida pelo fiscal** via polyline em verde fluorescente.
  * Marcadores diferenciados de vistorias (verdes para regulares e vermelhos para alertas/embargos).
  * Marcadores de denúncias SEMA-MT coloridos por gravidade.
  * Filtros flutuantes de camada: *Todas Camadas*, *Vistorias*, *Denúncias*, *Irregularidades*.
  * Alternância de tipo de mapa (Padrão, Satélite, Híbrido).
  * Card flutuante inferior ao tocar em qualquer marcador, com resumo e botão para abrir o laudo pericial.

---

#### 5. Painel Gerencial / Dashboard ([`DashboardView.swift`](file:///Volumes/Macintosh_HD/Users/user301275/geofiscal-semaMaps/iosApp/iosApp/Views/DashboardView.swift))
* **Indicadores e KPIs**:
  * Total de Vistorias e Denúncias registradas.
  * **Taxa de Conformidade Ambiental** com gráfico circular (*gauge* percentual).
  * Total de irregularidades críticas identificadas.
  * Pendências de sincronização com o servidor da SEMA.
* **Distribuição Territorial por Bioma**:
  * Barras de progresso proporcionais para os biomas Amazônia, Cerrado e Pantanal.
* **Exportação e Nuvem**:
  * Botão de exportar **Laudo PDF Consolidado**.
  * Botão de exportar **Planilha Gerencial (CSV/Excel)**.
  * Botão de sincronização em lote com a base central da SEMA-MT.
  * Lista de alertas de irregularidades recentes.

---

#### 6. Painel de Administração & Auditoria ([`AdminPanelView.swift`](file:///Volumes/Macintosh_HD/Users/user301275/geofiscal-semaMaps/iosApp/iosApp/Views/AdminPanelView.swift))
* **Troca de Perfil de Acesso**:
  * Suporte aos papéis: *Administrador Geral*, *Fiscal Técnico Ambiental*, *Inspetor Operacional* e *Auditor Externo / Ministério Público*.
  * Matriz dinâmica de permissões de acordo com o papel selecionado.
* **Segurança e Criptografia**:
  * Status dos módulos AES-256-CBC, HMAC-SHA256 e cadeia de custódia.
* **Configurações**:
  * Modo offline forçado para testes de campo sem sinal.
  * Configuração da URL de endpoint da API REST SEMA-MT.
* **Trilha de Auditoria (`AuditLog`)**:
  * Tabela com registro cronológico de todas as ações executadas no app (criações, exclusões, sincronizações, exportações) com seus respectivos hashes SHA-256.

---

#### 7. Modais & Serviços de Dados
* **Detalhes da Vistoria ([`RecordDetailSheet.swift`](file:///Volumes/Macintosh_HD/Users/user301275/geofiscal-semaMaps/iosApp/iosApp/Views/RecordDetailSheet.swift))**: Exibe a foto pericial, parecer do fiscal, telemetria completa dos sensores e hash de custódia.
* **Visualizador de QR Code ([`QrCodeSheet.swift`](file:///Volumes/Macintosh_HD/Users/user301275/geofiscal-semaMaps/iosApp/iosApp/Views/QrCodeSheet.swift))**: Gera e exibe o QR Code em alta resolução com o payload de autenticação da SEMA-MT e botão para copiar payload.
* **Serviço de Exportação ([`ExportService.swift`](file:///Volumes/Macintosh_HD/Users/user301275/geofiscal-semaMaps/iosApp/iosApp/Services/ExportService.swift))**: Geração de PDFs em formato A4 institucional com cabeçalho oficial, dados periciais e QR Code embutido, além de exportação de dados em CSV.
* **Persistência Local ([`InspectionStore.swift`](file:///Volumes/Macintosh_HD/Users/user301275/geofiscal-semaMaps/iosApp/iosApp/Services/InspectionStore.swift))**: Persistência em disco via JSON, com carga automática e dados iniciais de demonstração em Mato Grosso (Cuiabá, Sinop, Alta Floresta, Pantanal e Sorriso), equivalentes aos dados pré-carregados no Android.
* **Sensoriamento Remoto ([`SemaIntegrationService.swift`](file:///Volumes/Macintosh_HD/Users/user301275/geofiscal-semaMaps/iosApp/iosApp/Services/SemaIntegrationService.swift))**: Avaliação de coordenadas e biomas de MT com regras ambientais.

---

### Verificação do Projeto Xcode
Todos os 10 novos arquivos foram devidamente cadastrados no [`project.pbxproj`](file:///Volumes/Macintosh_HD/Users/user301275/geofiscal-semaMaps/iosApp/iosApp.xcodeproj/project.pbxproj) dentro de seus grupos (`Models`, `Views`, `Services`, `Components`). O projeto compila sem nenhum erro ou aviso via Xcode (`xcodebuild`).
