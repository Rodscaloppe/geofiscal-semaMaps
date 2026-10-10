# 🛡️ Critérios de Validação e Homologação para Merge: Validation

> **Projeto**: GeoFiscal SEMA-MT (Sistema Integrado de Fiscalização Geoespacial e Remota)  
> **Propósito**: Guia definitivo de aceitação pericial e técnica para autorização de Merge em `main`  
> **Idioma**: Português do Brasil (pt-BR)

---

## 1. Critérios Fundamentais de Aceitação (*Definition of Done*)

Para que uma alteração, nova funcionalidade ou refatoração seja considerada concluída e apta para merge na branch principal (`main`), ela deve satisfazer **100% dos seguintes requisitos**:

1. **Compilação Limpa Multiplataforma**: Zero erros de compilação em Android e iOS.
2. **Paridade Funcional Rígida**: Nenhuma funcionalidade de campo ou dado pericial presente no Android pode estar ausente no iOS (e vice-versa).
3. **Inviolabilidade da Cadeia de Custódia**: As rotinas de hash SHA-256 e criptografia AES-256 não podem sofrer regressão.
4. **Resiliência Offline Total**: O aplicativo não pode apresentar travamentos (*crashes*) ou bloqueios ao ser executado em modo avião / sem conectividade.
5. **Legitimidade Jurídica dos Laudos**: Os enquadramentos legais citados nos relatórios PDF devem corresponder estritamente à legislação ambiental e mineral vigente.

---

## 2. Checklist Técnico de Compilação e Build

### 2.1. Plataforma Android
* [ ] **Compilação Kotlin**:
  ```bash
  kotlinc app/src/main/java/com/example/data/model/MineracaoModel.kt -d /tmp/test.jar
  ```
  *Resultado Esperado*: Código de saída `0` sem avisos impeditivos de tipos.
* [ ] **Compilação Gradle**:
  ```bash
  ./gradlew assembleDebug --no-daemon
  ```
  *Resultado Esperado*: `BUILD SUCCESSFUL` com geração do APK de depuração em `app/build/outputs/apk/debug/app-debug.apk`.
* [ ] **Verificação de Banco de Dados Room**:
  * O schema do Room deve estar na versão atualizada (`version = 3`) com destructive migration ou migração explícita para evitar crash na inicialização do app.

### 2.2. Plataforma iOS
* [ ] **Compilação Xcode (Simulador ou Device)**:
  ```bash
  xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -destination 'generic/platform=iOS Simulator' build CODE_SIGNING_ALLOWED=NO CODE_SIGNING_REQUIRED=NO
  ```
  *Resultado Esperado*: Saída final contendo:
  ```text
  ** BUILD SUCCEEDED **
  ```
* [ ] **Compatibilidade Swift / Codable**:
  * Decodificação de arquivos JSON legados em `InspectionStore.swift` não deve lançar exceção ao encontrar registros sem os novos campos de mineração (valores padrão `"SEM_TITULO"`, `"OURO"`, `"REGULAR"` aplicados com sucesso).

---

## 3. Matriz de Verificação de Paridade Funcional

| Funcionalidade / Tela | Android (Jetpack Compose) | iOS (SwiftUI) | Status de Paridade |
| :--- | :---: | :---: | :---: |
| **Viewfinder da Câmera com HUD** | `CameraCapturePreview.kt` | `CameraPreviewView` em `CaptureView.swift` | ✅ 100% Paritário |
| **Telemetria GNSS e Bússola** | FusedLocation + SensorManager | CoreLocation + CoreMotion | ✅ 100% Paritário |
| **Card Sensoriamento SEMA-MT** | Card com bioma, município e CAR | Card correspondente em `CaptureView.swift` | ✅ 100% Paritário |
| **Card Perícia de Mineração & ANM** | Card com status penal, ANM e cavas | Card com status penal, ANM e cavas | ✅ 100% Paritário |
| **Consulta de Processo ANM** | Botão "Checar" com recálculo | Botão "Consultar ANM" com recálculo | ✅ 100% Paritário |
| **Apreensão de Maquinários** | Campo de texto `seized_machinery` | Campo de texto `seizedMachinery` | ✅ 100% Paritário |
| **Detalhes da Vistoria** | `RecordDetailDialog.kt` | `RecordDetailSheet.swift` | ✅ 100% Paritário |
| **Emissão de Laudo PDF A4** | `PdfReportGenerator.kt` | `ExportService.swift` | ✅ 100% Paritário |
| **Exportação CSV com Mineração** | `ExcelReportGenerator.kt` | `ExportService.swift` | ✅ 100% Paritário |
| **QR Code com Selo Pericial** | ZXing nativo | CIFilter nativo | ✅ 100% Paritário |

---

## 4. Protocolo de Testes Operacionais em Campo

### Teste A: Simulação em Zona de Vedação Constitucional (Terra Indígena Sararé)
1. Inserir ou mockar coordenadas na Terra Indígena Sararé: `Lat: -15.020000`, `Lng: -59.580000`.
2. **Critério de Sucesso**:
   * O card de mineração exibe badge vermelho vivo: `CRIME CONSTITUCIONAL / INVASÃO TI`.
   * Banner de alerta crítico: `"ALERTA CRÍTICO: Sobreposição à Terra Indígena Sararé (Povo Nambikwara) (Vedação Art. 231 CF)"`.
   * Enquadramento legal cita textualmente: `Art. 231 CF`, `Art. 2º Lei 8.176/91` e `Art. 55 Lei 9.605/98`.
   * Alertas de risco de mercúrio e turbidez hídrica ativados automaticamente.

### Teste B: Simulação em Cinturão Mineral com Regularidade (Poconé)
1. Inserir ou mockar coordenadas em Poconé: `Lat: -16.256700`, `Lng: -56.622800`.
2. Inserir processo ANM regular: `866.123/2020`.
3. **Critério de Sucesso**:
   * Se o roll simular regularidade, o badge exibe verde: `REGULAR`.
   * Substância identificada como `"Ouro Aluvionar e Filão (Au)"`.
   * Citação de Licença de Operação (LO) válida da SEMA-MT.

### Teste C: Integridade Criptográfica e Validação por QR Code
1. Concluir a captura e clicar em `"Salvar Vistoria"`.
2. Abrir o detalhe do registro recém-criado e inspecionar o QR Code.
3. Decodificar o payload do QR Code usando leitor padrão.
4. **Critério de Sucesso**:
   * O payload decodificado deve conter a estrutura exata:  
     `SEMA-MT|ID:{UUID}|GEO:{LAT},{LNG}|STATUS:{STATUS}|MIN:{REGIME}|{CRIME}|CAR:{CAR}|HASH:{PREFIXO_HASH}|VAL:https://sema.mt.gov.br/valida/{UUID}`.
   * O hash da foto coincide com o cálculo SHA-256 do arquivo em disco.

### Teste D: Resiliência em Modo Avião (*Offline-First*)
1. Ativar o Modo Avião do dispositivo ou cortar conectividade Wi-Fi e Celular.
2. Criar uma nova vistoria completa com foto, anotações de fiscal e maquinários apreendidos.
3. Salvar o registro.
4. **Critério de Sucesso**:
   * O salvamento ocorre em menos de 1 segundo sem travar a interface.
   * O status de sincronização inicial é `PENDING` / `Pendente`.
   * O registro aparece no topo do Histórico e do Mapa.
   * O laudo PDF pode ser gerado e visualizado offline com a foto capturada.

---

## 5. Procedimento de Aprovação e Merge no Repositório

Quando todos os testes forem validados:
1. **Inspeção de Git Status**:
   ```bash
   git status
   ```
   Certificar-se de que não existem arquivos temporários, diretórios `.DS_Store` ou builds locais não rastreados.
2. **Criação de Commit Semântico**:
   ```bash
   git add specs/ app/ iosApp/
   git commit -m "feat(mining): implement illegal mining and ANM judicial compliance module with full specs"
   ```
3. **Pull Request & Merge**:
   * Vincular as especificações contidas em `specs/plan.md`, `specs/requirements.md` e `specs/validation.md` na descrição do Pull Request.
   * Merge liberado para a branch `main`.
