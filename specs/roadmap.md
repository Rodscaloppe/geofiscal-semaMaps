# 🗺️ Roteiro Estratégico e Evolutivo: Roadmap

> **Projeto**: GeoFiscal SEMA-MT (Sistema Integrado de Fiscalização Geoespacial e Remota)  
> **Horizonte de Planejamento**: 2026 – 2028  
> **Idioma**: Português do Brasil (pt-BR)

---

## 1. Visão Geral das Fases

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│     FASE 1      │ ──> │     FASE 2      │ ──> │     FASE 3      │ ──> │     FASE 4      │
│ Fundação & Prova│     │Garimpo & Minério│     │ Satélite & APIs │     │  Edge AI & Visão│
│   [CONCLUÍDA]   │     │   [CONCLUÍDA]   │     │  [EM ANDAMENTO] │     │    [PLANEJADA]  │
└─────────────────┘     └─────────────────┘     └─────────────────┘     └─────────────────┘
```

---

## 2. Detalhamento dos Ciclos de Entrega

### ✅ Fase 1: Fundação Pericial e Captura Geoespacial (Concluída)
* [x] **Paridade Nativa Android & iOS**: Telas equivalentes em Jetpack Compose (Android) e SwiftUI (iOS).
* [x] **Câmera com Telemetria HUD**: Captura com carimbo d'água pericial de latitude, longitude, altitude, azimute e município em tempo real.
* [x] **Segurança e Anti-Fraude**: Criptografia de fotos em AES-256, hash da imagem e registro em SHA-256, detecção e alerta de GPS simulado (*Mock GPS*).
* [x] **Offline-First & Sincronização**: Armazenamento local (Room no Android, JSON no iOS) e fila de sincronização por status (`PENDING`, `SYNCING`, `SYNCED`, `FAILED`).
* [x] **Emissão de Laudo PDF e Selo QR Code**: Geração direta no aparelho de relatórios técnicos no padrão oficial do Governo de Mato Grosso / SEMA-MT com QR Code para consulta pública.

---

### ✅ Fase 2: Módulo Especializado de Garimpo e Justiça Ambiental (Concluída)
* [x] **Enquadramento de Regimes Minerários**: Suporte a PLG, Concessão de Lavra, Guia de Utilização, Autorização de Pesquisa e Lavra Clandestina.
* [x] **Detecção de Terras Indígenas e UCs**: Alerta de nulidade absoluta e crime constitucional (Art. 231 CF) para polígonos sobrepostos à TI Sararé, TI Menkragnoti, TI Enawenê-Nawê, Parque Estadual Cristalino e Estação Ecológica Serra das Araras.
* [x] **Cinturões Minerais de Mato Grosso**: Telemetria pericial contextual para Poconé, Peixoto de Azevedo/Matupá, Alta Floresta e Juína.
* [x] **Registro de Apreensões**: Campo dedicado para registro pericial e tombamento de maquinários pesados (escavadeiras, motores, balsas, dragas).
* [x] **Laudo Técnico Pericial Minerário**: Seção 4 dedicada no PDF de laudo com parecer para a Justiça Federal (Art. 2º da Lei 8.176/91 e Art. 55 da Lei 9.605/98).

---

### 🔄 Fase 3: Conectividade Orbital, SIG e Integração com Órgãos Oficiais (Em Andamento)
* [ ] **Integração com API Oficial SIMLAM / SEMA-MT**:
  * Autenticação OAuth2 / JWT com a infraestrutura central da SEMA-MT.
  * Download prévio de pacotes de mapas vetoriais do CAR de municípios inteiros para operação 100% offline.
* [ ] **Integração com WebServices da ANM (SIGMINE)**:
  * Consulta online do status atualizado de títulos e processos minerários diretamente na base de dados da ANM.
* [ ] **Camadas Cartográficas WFS/WMS da SEMA**:
  * Renderização de camadas vetoriais de Terras Indígenas (FUNAI), Unidades de Conservação (ICMBio/SEMA) e Áreas Embargadas diretamente no mapa da aplicação.
* [ ] **Alertas em Tempo Real DETER / INPE**:
  * Notificações *Push* para fiscais de campo quando novos polígonos de alertas de desmatamento ou focos de calor forem gerados na sua proximidade geográfica.

---

### 🚀 Fase 4: Inteligência Artificial Embarcada & Visão Computacional (Planejada - Q1/Q2 2027)
* [ ] **Edge ML com TensorFlow Lite / CoreML**:
  * Execução de modelos de rede neural convolucional (CNN) diretamente no aparelho sem necessidade de internet.
  * Reconhecimento automático em tempo real de maquinários de garimpo (escavadeiras, dragas e motores) via câmera do aparelho.
* [ ] **Análise Fotográfica de Turbidez e Mercúrio**:
  * Classificação espectral por foto da coloração de leitos de rios para identificação de rejeito garimpeiro e contaminação hídrica.
* [ ] **Cálculo Topográfico de Cavas por Fotogrametria**:
  * Estimativa volumétrica de cavas de lavra a partir de fotografias sequenciais georreferenciadas.

---

### 🛡️ Fase 5: Integração Federativa e Prova Digital Avançada (Planejada - Q3/Q4 2027)
* [ ] **Assinatura Digital ICP-Brasil / GOV.BR**:
  * Assinatura digital qualificada do Laudo Técnico com certificado digital em nuvem ou token A1/A3 de fiscais credenciados.
* [ ] **Comunicação Tática Via Satélite (Starlink / Iridium)**:
  * Suporte a envio de boletins emergenciais de campo em áreas remotas através de conectores satelitais de baixo consumo.
* [ ] **Canal Seguro de Intercâmbio PF / MPF**:
  * Exportação automatizada de pacotes de evidências digitais diretamente para o Sistema Eletrônico de Informações (SEI) e sistemas de persecução penal do Ministério Público Federal e Polícia Federal.

---

## 3. Matriz de Responsabilidade e Indicadores de Sucesso

| Indicador | Meta do Projeto | Método de Aferição |
| :--- | :--- | :--- |
| **Disponibilidade Offline** | 100% das funções de campo operacionais sem rede | Testes em modo avião e expedições remotas. |
| **Tempo de Emissão de Laudo** | Menos de 60 segundos após captura em campo | Medição do tempo de geração do PDF com QR Code. |
| **Integridade da Prova** | 0% de quebra de custódia contestada judicialmente | Validação dos hashes SHA-256 e selo QR Code no portal SEMA. |
| **Acurácia Geográfica** | Incerteza inferior a 5 metros em céu aberto | Controle via `FusedLocationProviderClient` e `CoreLocation`. |
