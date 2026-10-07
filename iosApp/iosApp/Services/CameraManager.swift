import AVFoundation
import UIKit
import SwiftUI

class CameraManager: NSObject, ObservableObject {
    @Published var capturedImage: UIImage?
    @Published var isSessionRunning = false
    @Published var isFrontCamera = false
    @Published var flashMode: AVCaptureDevice.FlashMode = .off
    @Published var errorMessage: String?

    let session = AVCaptureSession()
    private var photoOutput = AVCapturePhotoOutput()
    private var currentDevice: AVCaptureDevice?
    private var photoContinuation: CheckedContinuation<UIImage, Error>?

    func setupSession() {
        session.sessionPreset = .photo

        let position: AVCaptureDevice.Position = isFrontCamera ? .front : .back
        guard let device = AVCaptureDevice.default(.builtInWideAngleCamera, for: .video, position: position) else {
            errorMessage = "Câmera não disponível"
            return
        }
        currentDevice = device

        do {
            let input = try AVCaptureDeviceInput(device: device)
            if session.canAddInput(input) {
                session.addInput(input)
            }
            if session.canAddOutput(photoOutput) {
                session.addOutput(photoOutput)
            }

            DispatchQueue.global(qos: .userInitiated).async { [weak self] in
                self?.session.startRunning()
                DispatchQueue.main.async {
                    self?.isSessionRunning = true
                }
            }
        } catch {
            errorMessage = "Falha ao inicializar câmera: \(error.localizedDescription)"
        }
    }

    func capturePhoto() async throws -> UIImage {
        return try await withCheckedThrowingContinuation { continuation in
            self.photoContinuation = continuation

            let settings = AVCapturePhotoSettings()
            if let device = currentDevice, device.hasFlash {
                settings.flashMode = flashMode
            }

            photoOutput.capturePhoto(with: settings, delegate: self)
        }
    }

    func toggleCamera() {
        session.stopRunning()
        session.inputs.forEach { session.removeInput($0) }
        isFrontCamera.toggle()
        setupSession()
    }

    func cycleFlash() {
        switch flashMode {
        case .off: flashMode = .auto
        case .auto: flashMode = .on
        case .on: flashMode = .off
        @unknown default: flashMode = .off
        }
    }

    func stopSession() {
        session.stopRunning()
        isSessionRunning = false
    }
}

extension CameraManager: AVCapturePhotoCaptureDelegate {
    func photoOutput(_ output: AVCapturePhotoOutput,
                     didFinishProcessingPhoto photo: AVCapturePhoto,
                     error: Error?) {
        if let error = error {
            photoContinuation?.resume(throwing: error)
            photoContinuation = nil
            return
        }

        guard let data = photo.fileDataRepresentation(),
              let image = UIImage(data: data) else {
            photoContinuation?.resume(throwing: NSError(
                domain: "CameraManager",
                code: -1,
                userInfo: [NSLocalizedDescriptionKey: "Falha ao processar imagem capturada"]
            ))
            photoContinuation = nil
            return
        }

        DispatchQueue.main.async {
            self.capturedImage = image
        }
        photoContinuation?.resume(returning: image)
        photoContinuation = nil
    }
}
