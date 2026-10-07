import SwiftUI
import MapKit
import shared

struct ContentView: View {
    @StateObject private var locationManager = LocationManager()
    @State private var selectedTab = 0

    var body: some View {
        TabView(selection: $selectedTab) {
            DashboardView(locationManager: locationManager)
                .tabItem {
                    Label("Dashboard", systemImage: "chart.bar.fill")
                }
                .tag(0)

            MapInspectionView(locationManager: locationManager)
                .tabItem {
                    Label("Mapa", systemImage: "map.fill")
                }
                .tag(1)

            CaptureView(locationManager: locationManager)
                .tabItem {
                    Label("Captura", systemImage: "camera.fill")
                }
                .tag(2)

            HistoryView()
                .tabItem {
                    Label("Histórico", systemImage: "clock.fill")
                }
                .tag(3)
        }
        .tint(Color("ForestGreen"))
    }
}

struct DashboardView: View {
    @ObservedObject var locationManager: LocationManager

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 16) {
                    // GPS Status Card
                    GroupBox("GPS em Tempo Real") {
                        VStack(alignment: .leading, spacing: 8) {
                            HStack {
                                Image(systemName: locationManager.isTracking ? "location.fill" : "location.slash")
                                    .foregroundColor(locationManager.isTracking ? .green : .red)
                                Text(locationManager.isTracking ? "GPS Ativo" : "GPS Inativo")
                                    .fontWeight(.semibold)
                            }

                            LabeledContent("Latitude") {
                                Text(String(format: "%.6f", locationManager.latitude))
                                    .monospacedDigit()
                            }
                            LabeledContent("Longitude") {
                                Text(String(format: "%.6f", locationManager.longitude))
                                    .monospacedDigit()
                            }
                            LabeledContent("Altitude") {
                                Text(String(format: "%.1f m", locationManager.altitude))
                                    .monospacedDigit()
                            }
                            LabeledContent("Precisão") {
                                Text(String(format: "±%.1f m", locationManager.accuracy))
                                    .monospacedDigit()
                            }
                            LabeledContent("Velocidade") {
                                Text(String(format: "%.1f km/h", locationManager.speedKmh))
                                    .monospacedDigit()
                            }
                        }
                    }

                    // Actions
                    HStack(spacing: 12) {
                        Button(action: {
                            if locationManager.isTracking {
                                locationManager.stopTracking()
                            } else {
                                locationManager.startTracking()
                            }
                        }) {
                            Label(
                                locationManager.isTracking ? "Parar GPS" : "Iniciar GPS",
                                systemImage: locationManager.isTracking ? "stop.fill" : "play.fill"
                            )
                            .frame(maxWidth: .infinity)
                        }
                        .buttonStyle(.borderedProminent)
                        .tint(locationManager.isTracking ? .red : Color("ForestGreen"))
                    }
                }
                .padding()
            }
            .navigationTitle("GeoFiscal SEMA")
        }
    }
}

struct HistoryView: View {
    var body: some View {
        NavigationStack {
            List {
                Text("Nenhum registro de vistoria encontrado")
                    .foregroundColor(.secondary)
            }
            .navigationTitle("Histórico")
        }
    }
}
