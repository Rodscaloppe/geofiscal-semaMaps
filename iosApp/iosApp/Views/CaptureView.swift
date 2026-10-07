import SwiftUI
import AVFoundation

struct CaptureView: View {
    @ObservedObject var locationManager: LocationManager
    @StateObject private var cameraManager = CameraManager()
    @State private var showingCapturedImage = false

    var body: some View {
        NavigationStack {
            ZStack {
                // Camera Preview
                CameraPreviewView(session: cameraManager.session)
                    .ignoresSafeArea()

                VStack {
                    Spacer()

                    // GPS metadata overlay
                    VStack(alignment: .leading, spacing: 4) {
                        HStack {
                            Image(systemName: "location.fill")
                                .foregroundColor(.cyan)
                            Text(String(
                                format: "%.6f, %.6f (±%.1fm)",
                                locationManager.latitude,
                                locationManager.longitude,
                                locationManager.accuracy
                            ))
                            .font(.caption)
                            .monospacedDigit()
                        }
                        HStack {
                            Image(systemName: "mountain.2")
                                .foregroundColor(.green)
                            Text(String(format: "Alt: %.1f m", locationManager.altitude))
                                .font(.caption)
                                .monospacedDigit()
                        }
                    }
                    .foregroundColor(.white)
                    .padding()
                    .background(.black.opacity(0.6))
                    .cornerRadius(12)
                    .padding(.bottom, 20)

                    // Capture controls
                    HStack(spacing: 40) {
                        // Flash toggle
                        Button(action: { cameraManager.cycleFlash() }) {
                            Image(systemName: flashIcon)
                                .font(.title2)
                                .foregroundColor(.white)
                                .frame(width: 50, height: 50)
                        }

                        // Shutter button
                        Button(action: {
                            Task {
                                do {
                                    _ = try await cameraManager.capturePhoto()
                                    showingCapturedImage = true
                                } catch {
                                    print("Capture error: \(error)")
                                }
                            }
                        }) {
                            ZStack {
                                Circle()
                                    .fill(.white)
                                    .frame(width: 70, height: 70)
                                Circle()
                                    .stroke(.white, lineWidth: 4)
                                    .frame(width: 80, height: 80)
                            }
                        }

                        // Camera flip
                        Button(action: { cameraManager.toggleCamera() }) {
                            Image(systemName: "camera.rotate")
                                .font(.title2)
                                .foregroundColor(.white)
                                .frame(width: 50, height: 50)
                        }
                    }
                    .padding(.bottom, 30)
                }
            }
            .navigationTitle("Captura Georeferenciada")
            .navigationBarTitleDisplayMode(.inline)
            .onAppear { cameraManager.setupSession() }
            .onDisappear { cameraManager.stopSession() }
            .sheet(isPresented: $showingCapturedImage) {
                if let image = cameraManager.capturedImage {
                    CapturedImageView(
                        image: image,
                        latitude: locationManager.latitude,
                        longitude: locationManager.longitude,
                        altitude: locationManager.altitude,
                        accuracy: locationManager.accuracy
                    )
                }
            }
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

struct CapturedImageView: View {
    let image: UIImage
    let latitude: Double
    let longitude: Double
    let altitude: Double
    let accuracy: Double

    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            VStack {
                Image(uiImage: image)
                    .resizable()
                    .aspectRatio(contentMode: .fit)

                GroupBox("Metadados GPS da Captura") {
                    VStack(alignment: .leading, spacing: 6) {
                        LabeledContent("Latitude") { Text(String(format: "%.6f", latitude)).monospacedDigit() }
                        LabeledContent("Longitude") { Text(String(format: "%.6f", longitude)).monospacedDigit() }
                        LabeledContent("Altitude") { Text(String(format: "%.1f m", altitude)).monospacedDigit() }
                        LabeledContent("Precisão") { Text(String(format: "±%.1f m", accuracy)).monospacedDigit() }
                    }
                }
                .padding()
            }
            .navigationTitle("Foto Capturada")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Fechar") { dismiss() }
                }
            }
        }
    }
}
