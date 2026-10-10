# 📜 Constituição do Projeto: Missão e Princípios Fundamentais

> **Projeto**: GeoFiscal SEMA-MT (Sistema Integrado de Fiscalização Geoespacial e Remota)  
> **Jurisdição**: Estado de Mato Grosso — Brasil  
> **Órgão Correlato**: Secretaria de Estado de Meio Ambiente (SEMA-MT)  
> **Versão da Constituição**: 1.0.0  
> **Idioma**: Português do Brasil (pt-BR)

---

## 1. Declaração de Missão

A missão inegociável do **GeoFiscal SEMA-MT** é prover aos fiscais ambientais, peritos e autoridades de segurança pública uma plataforma móvel e distribuída de **alta precisão pericial, integridade criptográfica e operação em campo 100% offline**, capaz de documentar, combater e subsidiar a persecução judicial e administrativa de ilícitos ambientais e minerários em todo o território de Mato Grosso.

O aplicativo existe para salvaguardar os três biomas fundamentais do Estado de Mato Grosso — **Amazônia, Cerrado e Pantanal** —, atuando como instrumento de Estado para a defesa dos direitos difusos, soberania dos recursos minerais da União e preservação dos povos tradicionais e áreas protegidas.

---

## 2. Visão de Futuro

Consolidar-se como o padrão-ouro pericial e operacional em tecnologia pública ambiental no Brasil, integrando sensoriamento remoto orbital, telemetria de sensores de hardware embarcados, inteligência artificial na borda (*Edge ML*) e prova material pericial incontestável com cadeia de custódia auditável por tribunais estaduais e federais.

---

## 3. Pilares e Princípios Constitucionais Invioláveis

### I. Fé Pública e Inviolabilidade da Prova Material (*Cadeia de Custódia*)
* Toda captura fotográfica, coordenada geográfica e anotação técnica colhida em campo possui presunção de fé pública quando chancelada pelo fiscal técnico.
* As evidências coletadas devem ser blindadas por criptografia simétrica forte (**AES-256**) e assinadas por algoritmos de resumo criptográfico (**SHA-256**).
* Qualquer adulteração em arquivo, foto ou metadado invalidará a cadeia de custódia e será acusada imediatamente pelo sistema de auditoria.

### II. Soberania do Offline-First (Resiliência na Selva e Áreas Remotas)
* A atuação fiscalizatória ocorre predominantemente em regiões sem conectividade celular (calhas de rios amazônicos, terras indígenas, pantanais e serras remotas).
* Nenhuma funcionalidade essencial de vistoria, checagem geográfica, enquadramento penal, geração de QR Code ou emissão de laudo técnico preliminar pode depender de conexão ativa com a internet.
* A persistência local em banco de dados isolado e sincronização assíncrona bidirecional com filas transacionais é a regra máxima de arquitetura.

### III. Tolerância Zero a Fraudes e Localização Fictícia (*Anti-Spoofing*)
* O sistema deve inspecionar proativamente o hardware e as APIs do sistema operacional para detectar ferramentas de *Mock GPS* / localização simulada.
* Registros sob suspeita de mock devem ser flagrados ostensivamente no laudo pericial, impedindo a lavratura de autos fraudulentos.

### IV. Proteção Incondicional de Terras Indígenas e Unidades de Conservação
* O sistema reconhece como nulidade absoluta e crime inafiançável qualquer extração mineral ou desmatamento sobreposto a Terras Indígenas demarcadas (**Art. 231 da Constituição Federal**) e Unidades de Conservação de Proteção Integral.
* A detecção espacial nessas zonas dispara automaticamente o mais alto nível de enquadramento criminal federal (Art. 2º da Lei 8.176/91 e Art. 55 da Lei 9.605/98).

### V. Transparência Pública com QR Code e Rastreabilidade Judicial
* Todo laudo técnico emitido deve conter um selo de verificação pública por QR Code contendo o identificador unívoco do processo, coordenadas truncadas com precisão pericial, status do CAR/SEMA e o hash SHA-256 da prova.
* Cidadãos, Ministério Público e Poder Judiciário devem poder verificar a autenticidade documental diretamente pelo portal de validação oficial da SEMA-MT.

---

## 4. Escopo Operacional e Teatros de Atuação

O GeoFiscal SEMA-MT atua de maneira direta e integrada nos seguintes eixos operacionais:

1. **Combate ao Desmatamento Ilegal**: Cruzamento in-loco de polígonos de alertas orbitais (**PRODES / DETER / SEMA-MT**) e validação do Cadastro Ambiental Rural (**CAR**).
2. **Monitoramento e Combate a Queimadas**: Apuração de focos de calor ativos identificados por sensores térmicos orbitais (**INPE / MODIS / VIIRS**) em períodos proibitivos.
3. **Perícia de Mineração e Garimpo Clandestino**: Checagem de títulos perante a **ANM** (Permissões de Lavra Garimpeira - PLG, Concessões, Guias de Pesquisa), detecção de cavas de rejeitos, contaminação de bacias hidrográficas e uso ilícito de mercúrio ($Hg$).
4. **Apreensão e Custódia de Maquinários**: Registro detalhado e tombamento pericial de maquinários pesados (escavadeiras hidráulicas, dragas, motores estacionários, tratores de esteira) para fins de desintrusão e perdimento legal.
5. **Canal de Denúncias e Participação Cidadã**: Triagem, investigação, despacho técnico e formalização de Autos de Infração a partir de manifestações da sociedade.
