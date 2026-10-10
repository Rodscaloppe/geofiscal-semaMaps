# 📝 Plano de Implementação e Execução: Plan

> **Projeto**: GeoFiscal SEMA-MT (Sistema Integrado de Fiscalização Geoespacial e Remota)  
> **Formato**: Grupos de Tarefas Numerados (Numbered Task Groups)  
> **Idioma**: Português do Brasil (pt-BR)

---

## 1. Estrutura dos Grupos de Tarefas

O plano de implementação é organizado em 8 Grupos de Tarefas sequenciais e rastreáveis, projetados para garantir paridade total entre Android e iOS, robustez pericial e integridade criptográfica.

---

### Grupo de Tarefas 1: Configuração do Core e Modelagem de Dados
* [x] **1.1. Modelos Fundamentais de Inspeção**:
  * Implementar entidade `InspectionRecord` com suporte a metadados periciais, coordenadas geodésicas, bioma, CAR e hashes de integridade.
  * Definir enums `SemaStatus` e `SyncStatus` com representação cromática e textual institucional.
* [x] **1.2. Banco de Dados Local Room (Android)**:
  * Configurar `AppDatabase` (versão 3 com destructive migration para novas colunas periciais).
  * Criar `InspectionDao`, `DenunciaDao`, `AuditDao` e `FotoMetadataDao` com consultas reativas (`Flow`).
* [x] **1.3. Armazenamento e Modelos Codable (iOS)**:
  * Implementar `InspectionModels.swift` com codificadores/decodificadores compatíveis com schemas legados.
  * Criar `InspectionStore` no `@MainActor` gerenciando o ciclo de vida dos arquivos JSON em diretório sandbox protegido.
* [x] **1.4. Modelo de Denúncias e Auditoria**:
  * Estruturar `DenunciaEntity` / `DenunciaModel` com enums de gravidade, tipo de infração e fluxo de protocolo.
  * Implementar entidade `AuditLog` para registro indelével de ações dos operadores.

---

### Grupo de Tarefas 2: Serviços de Telemetria e Inteligência Geoespacial
* [x] **2.1. Serviço de Geolocalização GNSS**:
  * Integrar `FusedLocationProviderClient` com `Priority.PRIORITY_HIGH_ACCURACY` no Android.
  * Configurar `CLLocationManager` com `kCLLocationAccuracyBestForNavigation` no iOS.
  * Expor latitude, longitude, altitude ortométrica e margem de acurácia em metros.
* [x] **2.2. Fusão de Sensores e Bússola Digital**:
  * Conectar `SensorManager` com leitura de vetor de rotação para extrair azimute absoluto (0–360°), pitch e roll.
  * Conectar `CMMotionManager` e magnetômetro no iOS para paridade de bússola.
* [x] **2.3. Inteligência e Sensoriamento SEMA-MT**:
  * Desenvolver `SemaIntegrationService` determinístico baseado em hash das coordenadas.
  * Mapear 10 municípios polos de MT, biomas correspondentes e regras de geração de CAR sintético válido.
  * Simular anomalias de sensoriamento orbital: Alertas DETER/PRODES, focos de calor MODIS/VIIRS e termos de embargo.
* [x] **2.4. Mecanismo Anti-Spoofing (Mock GPS)**:
  * Implementar detecção de provedores simulados e flags de mock em tempo real com alertas vermelhos na UI.

---

### Grupo de Tarefas 3: Interface de Captura e Visores Periciais em Campo
* [x] **3.1. Câmera com Telemetria HUD (Android / CameraX)**:
  * Criar `CameraCapturePreview` com viewfinder CameraX em alta taxa de quadros.
  * Desenhar overlay d'água pericial renderizando coordenadas, altitude e azimute gravados sobre o frame.
* [x] **3.2. Câmera com Telemetria HUD (iOS / AVFoundation)**:
  * Criar `CameraPreviewView` com `AVCaptureSession` e controle de flash (auto, ligado, desligado).
  * Adicionar overlay de água dinâmico correspondente ao padrão da SEMA-MT.
* [x] **3.3. Card de Telemetria FusedLocation**:
  * Exibir HUD com luz de status do GPS, indicador de bússola com agulha giratória, velocidade e odômetro da missão.
  * Adicionar botão de "Fix de Precisão" e alternador de rastreamento em segundo plano.
* [x] **3.4. Card de Sensoriamento Remoto SEMA-MT**:
  * Exibir status do imóvel, bioma monitorado, número do CAR e parecer técnico de conformidade ou embargo.
  * Disponibilizar ferramenta de validação manual de CAR via expressão regular oficial.

---

### Grupo de Tarefas 4: Módulo Pericial de Garimpo, Mineração e Justiça Ambiental
* [x] **4.1. Modelagem de Mineração e Regimes ANM**:
  * Criar enums `RegimeMinerario` (PLG, Concessão, Pesquisa, Clandestino) e `SubstanciaMineral` (Ouro, Cassiterita, Diamante, etc.).
  * Criar enum `StatusJusticaAmbiental` (Regular, Crime Federal Art. 2º Lei 8.176/91, Art. 55 Lei 9.605/98, Invasão TI Art. 231 CF).
* [x] **4.2. Algoritmo Espacial de Detecção de Zonas Vedadas**:
  * Integrar coordenadas e raios de proteção para Terras Indígenas (TI Sararé, TI Menkragnoti, TI Enawenê-Nawê) e UCs (PE Cristalino, EE Serra das Araras).
  * Disparar enquadramento gravíssimo constitucional automático na proximidade dessas zonas.
* [x] **4.3. Interface de Perícia de Garimpo (Android / Compose)**:
  * Adicionar Card "Perícia de Mineração & ANM" em `CaptureScreen.kt` com badges de alerta criminal, busca de processo ANM e campos para maquinários apreendidos.
* [x] **4.4. Interface de Perícia de Garimpo (iOS / SwiftUI)**:
  * Adicionar Card equivalente em `CaptureView.swift` com recálculo em tempo real e campos de maquinários.
* [x] **4.5. Atualização de Detalhes da Vistoria**:
  * Atualizar `RecordDetailDialog.kt` (Android) e `RecordDetailSheet.swift` (iOS) para exibir o bloco completo de mineração e custódia.

---

### Grupo de Tarefas 5: Motor de Criptografia, Cadeia de Custódia e QR Code
* [x] **5.1. Criptografia AES-256 de Imagens**:
  * Implementar `CryptoManager.kt` e rotinas criptográficas em iOS para cifrar imagens em repouso com chave simétrica e salt de hardware.
* [x] **5.2. Cálculo de Fingerprint SHA-256**:
  * Gerar resumo SHA-256 da imagem capturada (`imageHashSha256`).
  * Gerar resumo SHA-256 concatenado de todos os metadados (`recordHashSha256`).
* [x] **5.3. Gerador de QR Code Pericial**:
  * Estruturar payload normatizado: `SEMA-MT|ID:{id}|GEO:{lat},{lng}|STATUS:{status}|MIN:{regime}|{crime}|CAR:{car}|HASH:{hash}|VAL:{url}`.
  * Gerar bitmap gráfico de QR Code com biblioteca nativa ZXing (Android) e CIFilter (iOS).
* [x] **5.4. Log de Auditoria Imutável**:
  * Gravação automática de logs a cada evento de criação ou modificação com assinatura parcial de integridade.

---

### Grupo de Tarefas 6: Geração de Laudos Técnicos Oficiais em PDF e Exportações
* [x] **6.1. Motor de Renderização de Laudo PDF Android**:
  * Implementar `PdfReportGenerator.kt` com desenho vetorial A4, brasão estilizado, cabeçalho do Governo de MT, foto descriptografada em alta definição, tabela de telemetria, parecer técnico, caixa de mineração ANM, selo de QR Code e assinatura pericial.
* [x] **6.2. Motor de Renderização de Laudo PDF iOS**:
  * Implementar `ExportService.swift` com `UIGraphicsPDFRenderer` gerando documento espelhado com Seção 4 pericial de mineração e QR Code.
* [x] **6.3. Exportação de Relatórios Tabulares (CSV/Excel)**:
  * Adicionar geração de CSV pericial completo contendo colunas de processo ANM, regime minerário, substância, status de justiça e maquinários.
* [x] **6.4. Compartilhamento Nativo no SO**:
  * Integrar Android `FileProvider` e iOS `UIActivityViewController` para compartilhamento instantâneo via WhatsApp, E-mail ou Bluetooth.

---

### Grupo de Tarefas 7: Painel Administrativo, Controle de Acesso (RBAC) e Denúncias
* [x] **7.1. Sistema de Papéis e Permissões**:
  * Implementar `UserRolePermissions` com papéis de Administrador, Fiscal Técnico, Inspetor de Campo e Auditor Externo.
* [x] **7.2. Telas de Histórico e Visualização no Mapa**:
  * Telas de listagem histórica com busca e filtros por status SEMA e gravidade.
  * Mapa interativo com pins de geolocalização e polígonos de vistoria.
* [x] **7.3. Painel Administrativo com Sincronização em Lote**:
  * Tela de controle com alternância de perfil, sincronização manual de filas pendentes e métricas de armazenamento.

---

### Grupo de Tarefas 8: Validação de Qualidade, Builds e Preparação para Merge
* [x] **8.1. Verificação de Compilação do Módulo Android**:
  * Executar validação com compilador Kotlin (`kotlinc`) para garantir integridade dos novos modelos periciais de mineração.
* [x] **8.2. Compilação Completa do Target iOS**:
  * Executar `xcodebuild` com o simulador iOS garantindo status `** BUILD SUCCEEDED **`.
* [x] **8.3. Auditoria de Paridade e Documentação**:
  * Redigir e consolidar as especificações na pasta `specs/` (`mission.md`, `tech-stack.md`, `roadmap.md`, `requirements.md`, `plan.md`, `validation.md`).
