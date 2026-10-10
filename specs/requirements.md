# 📋 Requisitos, Escopo e Decisões de Arquitetura: Requirements

> **Projeto**: GeoFiscal SEMA-MT (Sistema Integrado de Fiscalização Geoespacial e Remota)  
> **Status**: Ativo / Em Produção Pericial  
> **Idioma**: Português do Brasil (pt-BR)

---

## 1. Contexto e Justificativa Institucional

O Estado de Mato Grosso abriga mais de 900 mil km² de extensão territorial, englobando três biomas de relevância planetária: **Amazônia, Cerrado e Pantanal**. A fiscalização ambiental e minerária enfrenta desafios monumentais:
* Vastidão geográfica com ausência quase total de conectividade celular em zonas de conflito e lavra.
* Necessidade de constituir prova material incontestável com **fé pública** para respaldar embargos imediatos, apreensões de maquinários e subsidiar ações civis públicas e penais pelo Ministério Público Federal (MPF), Ministério Público Estadual (MPE) e Polícia Federal (PF).
* Pressão antrópica crescente sobre Terras Indígenas demarcadas e Unidades de Conservação de Proteção Integral.

O **GeoFiscal SEMA-MT** foi concebido como a resposta tecnológica oficial do Estado para padronizar, proteger e agilizar a fiscalização de campo.

---

## 2. Escopo do Sistema

### 2.1. Módulos Funcionais Obrigatórios

#### Módulo 1: Vistoria Geoespacial & Telemetria HUD
* **Captura Fotográfica Pericial**: Câmera integrada com carimbo d'água gráfico inalterável contendo coordenadas lat/lng em graus decimais (6 casas), altitude ortométrica, azimute da bússola em graus (0° a 360°), município e UF.
* **Telemetria de Sensores GNSS**: Uso de `FusedLocationProviderClient` (Android) e `CoreLocation` (iOS) para fornecer acurácia em metros (tolerância pericial $\le 5\text{m}$), velocidade de deslocamento e odometria da missão.
* **Detecção de Anti-Spoofing / Mock GPS**: Bloqueio de adulteração ou aposição de alerta visual gravíssimo nos laudos caso o GPS esteja sendo emulado por software.
* **Validação Cadastral CAR**: Validação do formato e conformidade do Cadastro Ambiental Rural (`MT-XXXXXXX-XXXXXX`).

#### Módulo 2: Perícia de Garimpo e Mineração Ilegal (ANM & SEMA)
* **Regimes Minerários ANM**: Classificação estrita entre:
  * Permissão de Lavra Garimpeira (PLG);
  * Concessão de Lavra Definitiva;
  * Guia de Utilização Prévia;
  * Autorização de Pesquisa (*com alerta de extração proibida*);
  * Requerimento em Análise;
  * Clandestino / Sem Processo na ANM.
* **Enquadramento Jurídico Penal**:
  * Crime Federal de Usurpação de Bens da União (**Art. 2º da Lei 8.176/91** — pena de 1 a 5 anos de detenção).
  * Crime Ambiental de Extração Mineral sem Licença (**Art. 55 da Lei 9.605/98**).
  * Invasão de Terra Indígena com nulidade constitucional absoluta (**Art. 231 da CF/88**).
  * Descumprimento de Termo de Embargo SEMA/IBAMA.
* **Zonas de Vedação Absoluta em MT**: Detecção automática de sobreposição espacial a áreas críticas (TI Sararé, TI Menkragnoti, TI Enawenê-Nawê, Parque Estadual Cristalino e Estação Ecológica Serra das Araras).
* **Parâmetros Ambientais Críticos**: Mapeamento de área de cavas (em hectares), alerta de turbidez severa em corpos hídricos e risco químico de mercúrio ($Hg$).
* **Apreensão e Tombamento de Maquinários**: Registro descritivo de maquinários pesados confiscados em campo (escavadeiras hidráulicas, dragas, motores MWM, pás-carregadeiras).

#### Módulo 3: Triagem e Despacho de Denúncias Ambientais
* Registro padronizado de denúncias cidadãs com número de protocolo oficial (`DEN-2026-SEMA-XXXX`).
* Categorização por tipo de infração: Desmatamento Ilegal, Queimada não autorizada, Poluição Hídrica, Dano em APP, Garimpo Ilegal e Outros.
* Classificação de gravidade (Baixa, Média, Alta e Crítica) e fluxo de tramitação: Recebida $\rightarrow$ Em Apuração $\rightarrow$ Vistoriada $\rightarrow$ Autuada $\rightarrow$ Arquivada.

#### Módulo 4: Painel Administrativo e Controle de Acesso (RBAC)
* Controle estrito de papéis de usuários com diferentes privilégios operacionais:
  * **Administrador Geral SEMA-MT**: Acesso irrestrito a aprovações, exportações, parametrização e edição.
  * **Fiscal Técnico Ambiental**: Criação de vistorias, aprovação técnica, geração de laudos PDF e exportação.
  * **Inspetor Operacional de Campo**: Coleta de campo, fotos e visualização, sem autorização para alteração de parâmetros.
  * **Auditor Externo / Ministério Público**: Acesso de leitura pericial, auditoria de hashes e exportação de relatórios.

---

## 3. Decisões Arquiteturais Registradas (ADRs)

### ADR-01: Paridade Nativa Multiplataforma (Jetpack Compose + SwiftUI)
* **Decisão**: Desenvolver as interfaces de usuário nativamente em Kotlin/Jetpack Compose para Android e Swift/SwiftUI para iOS, utilizando contratos e modelos espelhados.
* **Justificativa**: Acesso direto sem pontes híbridas (*bridges*) às APIs de hardware de baixo nível (`CameraX`, `AVFoundation`, `SensorManager`, `CoreMotion`), garantindo máxima taxa de quadros e estabilidade no campo sob temperaturas extremas de Mato Grosso.

### ADR-02: Arquitetura Offline-First Rigorosa
* **Decisão**: Todos os registros são salvos imediatamente em banco de dados local com hashes pré-calculados. A sincronização com a nuvem é tratada como um processo assíncrono em segundo plano com fila transacional.
* **Justificativa**: O fiscal não pode ficar impedido de registrar vistorias quando estiver operando em calhas de rios ou floresta densa sem sinal celular.

### ADR-03: Criptografia AES-256 em Repouso & Salt de Hardware
* **Decisão**: Imagens capturadas nunca são salvas na galeria pública do sistema operacional. São criptografadas com chave derivada de hardware salt antes da gravação em disco.
* **Justificativa**: Prevenir vazamento de fotos confidenciais de operações, manipulação por aplicativos terceiros ou adulteração maliciosa antes do envio aos servidores do Estado.

### ADR-04: Selo de Autenticidade Digital por QR Code
* **Decisão**: Todo laudo emitido incorpora um QR Code contendo payload assinado com link oficial de validação pública (`https://sema.mt.gov.br/valida/{id}`).
* **Justificativa**: Permite que agentes da Polícia Militar Ambiental, delegados e magistrados verifiquem a autenticidade do laudo impresso instantaneamente com a câmera de qualquer celular.

### ADR-05: Emissão Local de Laudo Técnico PDF A4
* **Decisão**: Geração do documento PDF realizada no próprio dispositivo móvel via bibliotecas nativas de desenho vetorial (`android.graphics.pdf.PdfDocument` e `UIGraphicsPDFRenderer`).
* **Justificativa**: Autonomia completa para lavratura imediata do auto de infração e laudo pericial impresso via impressora portátil térmica ou compartilhamento Bluetooth/Satélite.
