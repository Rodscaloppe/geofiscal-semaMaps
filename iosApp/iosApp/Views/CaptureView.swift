import SwiftUI
import PhotosUI
import AVFoundation

struct CaptureView: View {
    @ObservedObject var locationManager: LocationManager
    @ObservedObject var store: InspectionStore
    @StateObject private var cameraManager = CameraManager()
    @StateObject private var sensorManager = SensorTelemetryManager()

    @State private var title: String = ""
    @State private var notes: String = ""
    @State private var inspectorName: String = "Fiscal Técnico SEMA-MT"
    @State private var customCarNumber: String = ""
    @State private var carValidationMessage: String? = nil
    @State private var isCarValid: Bool? = nil

    @State private var selectedPhotoItem: PhotosPickerItem? = nil
    @State private var showingSavedAlert = false
    @State private var savedRecordId: String = ""

    @State private var customAnmProcess: String = ""
    @State private var selectedRegime: RegimeMinerario? = nil
    @State private var selectedSubstance: SubstanciaMineral? = nil
    @State private var seizedMachinery: String = ""

    private var miningResult: MineracaoTelemetryResult {
        return SemaIntegrationService.shared.evaluateMiningActivity(
            lat: locationManager.latitude,
            lng: locationManager.longitude,
            customAnm: customAnmProcess
        )
    }

    private var telemetrySubtext: String {
        let alt = String(format: "%.1f", locationManager.altitude)
        let az = String(format: "%.0f", sensorManager.azimuth)
        return "ALT: \(alt)m | AZ: \(az)° | \(semaResult.municipality) - MT"
    }

    private var semaResult: SemaTelemetryResult {
        let res = SemaIntegrationService.shared.evaluateLocation(
            lat: locationManager.latitude,
            lng: locationManager.longitude
        )
        if !customCarNumber.isEmpty {
            return SemaTelemetryResult(
                municipality: res.municipality,
                biome: res.biome,
                carNumber: customCarNumber,
                carStatus: isCarValid == true ? "CAR Validado Manualmente" : res.carStatus,
                semaStatus: res.semaStatus,
                hasIrregularity: res.hasIrregularity,
                irregularityDetails: res.irregularityDetails,
                remoteSensingSatellite: res.remoteSensingSatellite,
                deforestationAreaHectares: res.deforestationAreaHectares,
                inpeFireConfidencePercent: res.inpeFireConfidencePercent,
                embargoTermNumber: res.embargoTermNumber
            )
        }
        return res
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                // Header Banner
                HStack {
                    VStack(alignment: .leading, spacing: 2) {
                        Text("VISTORIA GEOESPACIAL SEMA-MT")
                            .font(.system(size: 11, weight: .bold))
                            .foregroundColor(Color(red: 0xC7/255.0, green: 0xF9/255.0, blue: 0xDC/255.0))
                        Text("Câmera com Telemetria e Criptografia")
                            .font(.system(size: 14, weight: .heavy))
                            .foregroundColor(.white)
                    }
                    Spacer()
                    Image(systemName: "lock.shield.fill")
                        .font(.title2)
                        .foregroundColor(Color(red: 0x2E/255.0, green: 0xE5/255.0, blue: 0x9D/255.0))
                }
                .padding()
                .background(Color("ForestGreen"))
                .cornerRadius(12)

                // Camera Viewfinder or Captured Photo Card
                if let capturedImage = cameraManager.capturedImage {
                    VStack(spacing: 10) {
                        ZStack(alignment: .bottomLeading) {
                            Image(uiImage: capturedImage)
                                .resizable()
                                .scaledToFill()
                                .frame(height: 250)
                                .clipped()
                                .cornerRadius(14)

                            // Watermark Overlay
                            VStack(alignment: .leading, spacing: 2) {
                                Text(String(format: "LAT: %.6f°  LNG: %.6f°", locationManager.latitude, locationManager.longitude))
                                    .font(.system(size: 11, weight: .bold))
                                    .foregroundColor(.white)
                                Text(telemetrySubtext)
                                    .font(.system(size: 10, weight: .medium))
                                    .foregroundColor(Color(red: 0x2E/255.0, green: 0xE5/255.0, blue: 0x9D/255.0))
                            }
                            .padding(8)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .background(Color.black.opacity(0.75))
                            .cornerRadius(8)
                            .padding(8)
                        }

                        // Retake / Gallery Row
                        HStack(spacing: 12) {
                            Button(action: {
                                cameraManager.capturedImage = nil
                            }) {
                                Label("Tirar Outra Foto", systemImage: "camera.fill")
                                    .frame(maxWidth: .infinity)
                            }
                            .buttonStyle(.borderedProminent)
                            .tint(Color("ForestGreen"))

                            PhotosPicker(selection: $selectedPhotoItem, matching: .images) {
                                Label("Galeria", systemImage: "photo.on.rectangle")
                                    .frame(maxWidth: .infinity)
                            }
                            .buttonStyle(.bordered)
                        }
                    }
                } else {
                    // Live Camera Viewfinder Box
                    ZStack {
                        CameraPreviewView(session: cameraManager.session)
                            .frame(height: 250)
                            .cornerRadius(14)
                            .clipped()

                        // Camera Controls Overlay
                        VStack {
                            HStack {
                                Button(action: { cameraManager.cycleFlash() }) {
                                    Image(systemName: flashIcon)
                                        .font(.title3)
                                        .foregroundColor(.white)
                                        .padding(8)
                                        .background(Color.black.opacity(0.5))
                                        .clipShape(Circle())
                                }
                                Spacer()
                                Button(action: { cameraManager.toggleCamera() }) {
                                    Image(systemName: "camera.rotate.fill")
                                        .font(.title3)
                                        .foregroundColor(.white)
                                        .padding(8)
                                        .background(Color.black.opacity(0.5))
                                        .clipShape(Circle())
                                }
                            }
                            .padding(12)

                            Spacer()

                            // Shutter Button
                            HStack {
                                Spacer()
                                Button(action: {
                                    Task {
                                        _ = try? await cameraManager.capturePhoto()
                                    }
                                }) {
                                    Circle()
                                        .strokeBorder(Color.white, lineWidth: 4)
                                        .background(Circle().fill(Color.white.opacity(0.85)))
                                        .frame(width: 60, height: 60)
                                }
                                Spacer()
                            }
                            .padding(.bottom, 12)
                        }
                    }

                    // Gallery Import Option
                    PhotosPicker(selection: $selectedPhotoItem, matching: .images) {
                        Label("Ou Importar Imagem da Galeria", systemImage: "photo.on.rectangle")
                            .font(.system(size: 13, weight: .semibold))
                            .frame(maxWidth: .infinity)
                    }
                    .buttonStyle(.bordered)
                }

                // Telemetry & Sensor HUD Card
                VStack(alignment: .leading, spacing: 10) {
                    HStack {
                        HStack(spacing: 6) {
                            Circle()
                                .fill(locationManager.isGpsFixed ? Color.green : Color.orange)
                                .frame(width: 10, height: 10)
                            VStack(alignment: .leading, spacing: 1) {
                                Text("CoreLocation & Sensores")
                                    .font(.system(size: 13, weight: .bold))
                                Text(locationManager.isGpsFixed ? "GPS de Alta Precisão Ativo" : "Buscando satélites...")
                                    .font(.system(size: 11))
                                    .foregroundColor(.secondary)
                            }
                        }
                        Spacer()
                        // Compass needle
                        HStack(spacing: 4) {
                            Image(systemName: "location.north.line.fill")
                                .font(.system(size: 16))
                                .foregroundColor(Color("ForestGreen"))
                                .rotationEffect(.degrees(sensorManager.azimuth))
                            Text("\(String(format: "%.0f", sensorManager.azimuth))°")
                                .font(.system(size: 12, weight: .bold))
                        }
                        .padding(.horizontal, 8)
                        .padding(.vertical, 4)
                        .background(Color("ForestGreen").opacity(0.12))
                        .cornerRadius(8)
                    }

                    Divider()

                    // Sensor Values Grid
                    HStack {
                        telemetryItem(label: "Latitude", value: String(format: "%.5f°", locationManager.latitude))
                        Spacer()
                        telemetryItem(label: "Longitude", value: String(format: "%.5f°", locationManager.longitude))
                        Spacer()
                        telemetryItem(label: "Altitude", value: String(format: "%.1f m", locationManager.altitude))
                        Spacer()
                        telemetryItem(label: "Acurácia", value: String(format: "±%.1f m", locationManager.accuracy))
                    }

                    // Speed & Anti-Fraud Row
                    HStack {
                        HStack(spacing: 4) {
                            Image(systemName: "speedometer")
                                .font(.system(size: 12))
                                .foregroundColor(Color("ForestGreen"))
                            Text("\(String(format: "%.1f", locationManager.speedKmh)) km/h • \(String(format: "%.2f", locationManager.distanceTraveledKm)) km")
                                .font(.system(size: 11, weight: .medium))
                                .foregroundColor(.secondary)
                        }
                        Spacer()
                        HStack(spacing: 4) {
                            Image(systemName: locationManager.isMockLocation ? "exclamationmark.triangle.fill" : "checkmark.seal.fill")
                                .font(.system(size: 12))
                                .foregroundColor(locationManager.isMockLocation ? .red : .green)
                            Text(locationManager.isMockLocation ? "ALERTA: Mock GPS" : "Hardware Autêntico")
                                .font(.system(size: 11, weight: .bold))
                                .foregroundColor(locationManager.isMockLocation ? .red : .green)
                        }
                    }

                    // Location Actions
                    HStack(spacing: 10) {
                        Button(action: {
                            locationManager.requestPrecisionFix()
                        }) {
                            Label("Fix de Precisão", systemImage: "arrow.clockwise")
                                .font(.system(size: 12))
                                .frame(maxWidth: .infinity)
                        }
                        .buttonStyle(.bordered)

                        Button(action: {
                            if locationManager.isTracking {
                                locationManager.stopTracking()
                            } else {
                                locationManager.startTracking()
                            }
                        }) {
                            Label(
                                locationManager.isTracking ? "Pausar GPS" : "Rastrear em 2º Plano",
                                systemImage: locationManager.isTracking ? "pause.fill" : "location.fill"
                            )
                            .font(.system(size: 12))
                            .frame(maxWidth: .infinity)
                        }
                        .buttonStyle(.borderedProminent)
                        .tint(locationManager.isTracking ? .red : Color("ForestGreen"))
                    }
                }
                .padding()
                .background(Color(UIColor.secondarySystemBackground))
                .cornerRadius(14)

                // SEMA-MT Remote Sensing & Environmental Evaluation Card
                VStack(alignment: .leading, spacing: 10) {
                    HStack {
                        Text(semaResult.hasIrregularity ? "ALERTA AMBIENTAL SEMA-MT" : "EM CONFORMIDADE AMBIENTAL")
                            .font(.system(size: 12, weight: .black))
                            .foregroundColor(semaResult.hasIrregularity ? .red : Color(red: 0x1B/255.0, green: 0x5E/255.0, blue: 0x20/255.0))
                        Spacer()
                        Text(semaResult.biome.uppercased())
                            .font(.system(size: 10, weight: .bold))
                            .padding(.horizontal, 6)
                            .padding(.vertical, 2)
                            .background(semaResult.hasIrregularity ? Color.red.opacity(0.15) : Color.green.opacity(0.15))
                            .cornerRadius(6)
                    }

                    Text(semaResult.irregularityDetails)
                        .font(.system(size: 12))
                        .foregroundColor(.primary)

                    Divider()

                    // CAR Field & Validation
                    VStack(alignment: .leading, spacing: 6) {
                        Text("Cadastro Ambiental Rural (CAR)")
                            .font(.system(size: 11, weight: .bold))
                            .foregroundColor(.secondary)

                        HStack {
                            TextField(semaResult.carNumber, text: $customCarNumber)
                                .font(.system(size: 13, design: .monospaced))
                                .textFieldStyle(.roundedBorder)

                            Button("Validar CAR") {
                                let carToTest = customCarNumber.isEmpty ? semaResult.carNumber : customCarNumber
                                let valid = SemaIntegrationService.shared.isValidCarNumber(carToTest)
                                isCarValid = valid
                                carValidationMessage = valid ? "CAR Regular e Validado na base SEMA." : "Formato CAR inválido (esperado MT-XXXXXXX-XXXX)."
                            }
                            .buttonStyle(.bordered)
                            .tint(Color("ForestGreen"))
                        }

                        if let msg = carValidationMessage {
                            Text(msg)
                                .font(.system(size: 11, weight: .semibold))
                                .foregroundColor(isCarValid == true ? .green : .red)
                        }
                    }
                }
                .padding()
                .background(semaResult.hasIrregularity ? Color.red.opacity(0.08) : Color.green.opacity(0.08))
                .overlay(
                    RoundedRectangle(cornerRadius: 14)
                        .stroke(semaResult.hasIrregularity ? Color.red.opacity(0.3) : Color.green.opacity(0.3), lineWidth: 1)
                )
                .cornerRadius(14)

                // MARK: - Mining & ANM Pericial Card
                let mining = miningResult
                let isCrime = mining.statusJustica.isCrime
                VStack(alignment: .leading, spacing: 12) {
                    HStack {
                        Image(systemName: isCrime ? "exclamationmark.triangle.fill" : "shield.lefthalf.filled")
                            .font(.title3)
                            .foregroundColor(isCrime ? .red : Color("ForestGreen"))

                        VStack(alignment: .leading, spacing: 2) {
                            Text("PERÍCIA DE MINERAÇÃO & ANM")
                                .font(.system(size: 11, weight: .bold))
                                .foregroundColor(.secondary)
                            Text(mining.statusJustica.label)
                                .font(.system(size: 13, weight: .bold))
                                .foregroundColor(isCrime ? .red : Color("ForestGreen"))
                        }

                        Spacer()

                        Text(isCrime ? "CRIME / IRREGULAR" : "REGULAR")
                            .font(.system(size: 10, weight: .heavy))
                            .foregroundColor(.white)
                            .padding(.horizontal, 8)
                            .padding(.vertical, 4)
                            .background(isCrime ? Color.red : Color.green)
                            .cornerRadius(6)
                    }

                    if mining.isNearProtectedArea, let area = mining.protectedAreaName {
                        Text("ALERTA CRÍTICO: Sobreposição à \(area) (Vedação Art. 231 CF)")
                            .font(.system(size: 11, weight: .bold))
                            .foregroundColor(.red)
                            .padding(6)
                            .background(Color.red.opacity(0.15))
                            .cornerRadius(6)
                    }

                    Text("Enquadramento: \(mining.enquadramentoLegal)")
                        .font(.system(size: 11))
                        .foregroundColor(.secondary)

                    Divider()

                    // ANM Process Consultation
                    VStack(alignment: .leading, spacing: 4) {
                        Text("Processo ANM / Titularidade")
                            .font(.system(size: 11, weight: .bold))
                            .foregroundColor(.secondary)

                        HStack {
                            TextField(mining.anmProcess, text: $customAnmProcess)
                                .font(.system(size: 13, design: .monospaced))
                                .textFieldStyle(.roundedBorder)

                            Button("Consultar ANM") {
                                // Triggers recalculation via computed property
                            }
                            .buttonStyle(.bordered)
                            .tint(Color("ForestGreen"))
                        }

                        HStack {
                            Text("Titular: \(mining.titularProcesso)")
                                .font(.system(size: 11))
                                .foregroundColor(.secondary)
                            Spacer()
                            Text("Regime: \((selectedRegime ?? mining.regimeMinerario).label)")
                                .font(.system(size: 11, weight: .semibold))
                        }
                    }

                    // Mineral Substance and Cavas Info
                    HStack {
                        VStack(alignment: .leading, spacing: 2) {
                            Text("Substância Mineral")
                                .font(.system(size: 10))
                                .foregroundColor(.secondary)
                            Text("\((selectedSubstance ?? mining.substancia).label) (\((selectedSubstance ?? mining.substancia).symbol))")
                                .font(.system(size: 12, weight: .bold))
                                .foregroundColor(.orange)
                        }
                        Spacer()
                        VStack(alignment: .trailing, spacing: 2) {
                            Text("Área de Cavas")
                                .font(.system(size: 10))
                                .foregroundColor(.secondary)
                            Text("\(String(format: "%.1f", mining.cavasAreaHectares)) ha")
                                .font(.system(size: 12, weight: .bold))
                        }
                    }

                    if mining.alertaTurbidezHidrica || mining.presencaMercurio {
                        HStack(spacing: 8) {
                            if mining.alertaTurbidezHidrica {
                                Text("Turbidez Hídrica Severa")
                                    .font(.system(size: 10, weight: .bold))
                                    .foregroundColor(.brown)
                                    .padding(.horizontal, 6)
                                    .padding(.vertical, 3)
                                    .background(Color.yellow.opacity(0.3))
                                    .cornerRadius(6)
                            }
                            if mining.presencaMercurio {
                                Text("Risco Mercúrio (Hg)")
                                    .font(.system(size: 10, weight: .bold))
                                    .foregroundColor(.red)
                                    .padding(.horizontal, 6)
                                    .padding(.vertical, 3)
                                    .background(Color.red.opacity(0.2))
                                    .cornerRadius(6)
                            }
                        }
                    }

                    // Seized Machinery
                    VStack(alignment: .leading, spacing: 4) {
                        Text("Apreensão de Maquinários e Equipamentos")
                            .font(.system(size: 11, weight: .bold))
                            .foregroundColor(.secondary)
                        TextField("Ex: 2 Escavadeiras CAT 320, 1 Draga, 1 Motor MWM", text: $seizedMachinery)
                            .font(.system(size: 12))
                            .textFieldStyle(.roundedBorder)
                    }
                }
                .padding()
                .background(isCrime ? Color.red.opacity(0.08) : Color.green.opacity(0.08))
                .overlay(
                    RoundedRectangle(cornerRadius: 14)
                        .stroke(isCrime ? Color.red.opacity(0.3) : Color.green.opacity(0.3), lineWidth: 1)
                )
                .cornerRadius(14)

                // Inspection Form Fields
                VStack(spacing: 12) {
                    VStack(alignment: .leading, spacing: 4) {
                        Text("Título da Vistoria")
                            .font(.system(size: 12, weight: .semibold))
                        TextField("Ex: Vistoria Preventiva Fazenda Santa Fé", text: $title)
                            .textFieldStyle(.roundedBorder)
                    }

                    VStack(alignment: .leading, spacing: 4) {
                        Text("Parecer do Fiscal / Observações Técnicas")
                            .font(.system(size: 12, weight: .semibold))
                        TextEditor(text: $notes)
                            .frame(height: 70)
                            .overlay(RoundedRectangle(cornerRadius: 6).stroke(Color.secondary.opacity(0.2), lineWidth: 1))
                    }

                    VStack(alignment: .leading, spacing: 4) {
                        Text("Nome do Fiscal Responsável")
                            .font(.system(size: 12, weight: .semibold))
                        TextField("Fiscal Técnico SEMA-MT", text: $inspectorName)
                            .textFieldStyle(.roundedBorder)
                    }
                }
                .padding()
                .background(Color(UIColor.secondarySystemBackground))
                .cornerRadius(14)

                // Save Inspection Action Button
                Button(action: {
                    let mining = miningResult
                    let record = store.saveInspection(
                        title: title,
                        notes: notes,
                        inspectorName: inspectorName,
                        photo: cameraManager.capturedImage,
                        latitude: locationManager.latitude,
                        longitude: locationManager.longitude,
                        altitude: locationManager.altitude,
                        accuracy: locationManager.accuracy,
                        azimuthBearing: sensorManager.azimuth,
                        pitch: sensorManager.pitch,
                        roll: sensorManager.roll,
                        semaResult: semaResult,
                        anmProcessNumber: customAnmProcess.isEmpty ? mining.anmProcess : customAnmProcess,
                        miningRegime: (selectedRegime ?? mining.regimeMinerario).rawValue,
                        mineralSubstance: (selectedSubstance ?? mining.substancia).rawValue,
                        justiceStatus: mining.statusJustica.rawValue,
                        seizedMachinery: seizedMachinery
                    )
                    savedRecordId = record.id
                    showingSavedAlert = true

                    // Reset form
                    title = ""
                    notes = ""
                    customCarNumber = ""
                    customAnmProcess = ""
                    seizedMachinery = ""
                    cameraManager.capturedImage = nil
                }) {
                    HStack(spacing: 8) {
                        Image(systemName: "checkmark.shield.fill")
                            .font(.headline)
                        Text("Salvar Vistoria com Fé Pública (SHA-256)")
                            .font(.system(size: 14, weight: .bold))
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 12)
                }
                .buttonStyle(.borderedProminent)
                .tint(Color("ForestGreen"))
                .padding(.bottom, 24)
            }
            .padding()
        }
        .navigationTitle("Captura Georreferenciada")
        .navigationBarTitleDisplayMode(.inline)
        .onAppear {
            cameraManager.setupSession()
            sensorManager.startListening()
            if !locationManager.isTracking {
                locationManager.startTracking()
            }
        }
        .onDisappear {
            cameraManager.stopSession()
            sensorManager.stopListening()
        }
        .onChange(of: selectedPhotoItem) { _, newItem in
            Task {
                if let data = try? await newItem?.loadTransferable(type: Data.self),
                   let uiImage = UIImage(data: data) {
                    await MainActor.run {
                        cameraManager.capturedImage = uiImage
                    }
                }
            }
        }
        .alert("Vistoria Registrada!", isPresented: $showingSavedAlert) {
            Button("OK", role: .cancel) { }
        } message: {
            Text("O laudo foi assinado criptograficamente e salvo na base de dados com fé pública.")
        }
    }

    private func telemetryItem(label: String, value: String) -> some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(label)
                .font(.system(size: 10))
                .foregroundColor(.secondary)
            Text(value)
                .font(.system(size: 12, weight: .bold, design: .monospaced))
        }
    }

    private var flashIcon: String {
        switch cameraManager.flashMode {
        case .on: return "bolt.fill"
        case .auto: return "bolt.badge.automatic"
        default: return "bolt.slash"
        }
    }
}

struct CameraPreviewView: UIViewRepresentable {
    let session: AVCaptureSession

    func makeUIView(context: Context) -> UIView {
        let view = UIView(frame: .zero)
        let previewLayer = AVCaptureVideoPreviewLayer(session: session)
        previewLayer.videoGravity = .resizeAspectFill
        view.layer.addSublayer(previewLayer)
        context.coordinator.previewLayer = previewLayer
        return view
    }

    func updateUIView(_ uiView: UIView, context: Context) {
        context.coordinator.previewLayer?.frame = uiView.bounds
    }

    func makeCoordinator() -> Coordinator { Coordinator() }

    class Coordinator {
        var previewLayer: AVCaptureVideoPreviewLayer?
    }
}

