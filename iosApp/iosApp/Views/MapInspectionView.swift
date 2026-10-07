import SwiftUI
import MapKit

struct MapInspectionView: View {
    @ObservedObject var locationManager: LocationManager
    @State private var mapType: MKMapType = .standard
    @State private var region = MKCoordinateRegion(
        center: CLLocationCoordinate2D(latitude: -12.8, longitude: -55.8),
        span: MKCoordinateSpan(latitudeDelta: 8.5, longitudeDelta: 8.0)
    )

    var body: some View {
        NavigationStack {
            ZStack {
                Map(coordinateRegion: $region,
                    showsUserLocation: true,
                    annotationItems: liveAnnotations) { item in
                    MapAnnotation(coordinate: item.coordinate) {
                        VStack(spacing: 2) {
                            Image(systemName: "location.fill")
                                .font(.title2)
                                .foregroundColor(.cyan)
                            Text("GPS Fiscal")
                                .font(.caption2)
                                .fontWeight(.bold)
                                .foregroundColor(.white)
                                .padding(.horizontal, 6)
                                .padding(.vertical, 2)
                                .background(.black.opacity(0.7))
                                .cornerRadius(6)
                        }
                    }
                }
                .mapStyle(mapStyleForType)
                .ignoresSafeArea(edges: .bottom)

                // Floating controls
                VStack {
                    // Top status card
                    HStack {
                        VStack(alignment: .leading, spacing: 4) {
                            HStack {
                                Image(systemName: "map.fill")
                                    .foregroundColor(Color("ForestGreen"))
                                Text("Mapa MapKit (SEMA-MT)")
                                    .font(.headline)
                            }
                            Text(String(
                                format: "Lat: %.5f | Lng: %.5f (±%.1fm)",
                                locationManager.latitude,
                                locationManager.longitude,
                                locationManager.accuracy
                            ))
                            .font(.caption)
                            .monospacedDigit()
                        }
                        Spacer()
                    }
                    .padding()
                    .background(.ultraThinMaterial)
                    .cornerRadius(14)
                    .padding(.horizontal)

                    Spacer()

                    // Right-side map controls
                    HStack {
                        Spacer()
                        VStack(spacing: 10) {
                            MapControlButton(icon: "location.fill", color: Color("ForestGreen")) {
                                withAnimation {
                                    region.center = CLLocationCoordinate2D(
                                        latitude: locationManager.latitude,
                                        longitude: locationManager.longitude
                                    )
                                    region.span = MKCoordinateSpan(latitudeDelta: 0.02, longitudeDelta: 0.02)
                                }
                            }

                            MapControlButton(icon: mapTypeIcon, color: .primary) {
                                cycleMapType()
                            }

                            MapControlButton(icon: "plus", color: .primary) {
                                withAnimation {
                                    region.span.latitudeDelta /= 2
                                    region.span.longitudeDelta /= 2
                                }
                            }

                            MapControlButton(icon: "minus", color: .primary) {
                                withAnimation {
                                    region.span.latitudeDelta *= 2
                                    region.span.longitudeDelta *= 2
                                }
                            }
                        }
                        .padding(.trailing, 12)
                    }
                    .padding(.bottom, 40)
                }
            }
            .navigationTitle("Mapa de Fiscalização")
            .navigationBarTitleDisplayMode(.inline)
        }
    }

    private var liveAnnotations: [MapPin] {
        guard locationManager.isGpsFixed else { return [] }
        return [MapPin(
            id: "live-gps",
            coordinate: CLLocationCoordinate2D(
                latitude: locationManager.latitude,
                longitude: locationManager.longitude
            ),
            title: "Fiscal em Campo"
        )]
    }

    private var mapTypeIcon: String {
        switch mapType {
        case .satellite, .satelliteFlyover: return "globe.americas"
        case .hybrid, .hybridFlyover: return "square.3.layers.3d"
        default: return "map"
        }
    }

    @MapContentBuilder
    private var mapStyleForType: some MapStyle {
        switch mapType {
        case .satellite: .imagery
        case .hybrid: .hybrid
        default: .standard
        }
    }

    private func cycleMapType() {
        switch mapType {
        case .standard: mapType = .satellite
        case .satellite: mapType = .hybrid
        default: mapType = .standard
        }
    }
}

struct MapPin: Identifiable {
    let id: String
    let coordinate: CLLocationCoordinate2D
    let title: String
}

struct MapControlButton: View {
    let icon: String
    let color: Color
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Image(systemName: icon)
                .font(.system(size: 16, weight: .semibold))
                .foregroundColor(color)
                .frame(width: 44, height: 44)
                .background(.ultraThinMaterial)
                .clipShape(Circle())
                .shadow(radius: 3)
        }
    }
}
