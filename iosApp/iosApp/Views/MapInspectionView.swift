import SwiftUI
import MapKit

enum MapFilterLayer: String, CaseIterable {
    case all = "Todas Camadas"
    case inspections = "Vistorias"
    case denuncias = "Denúncias"
    case irregularities = "Irregularidades"
}

enum MapItemSelection: Identifiable {
    case inspection(InspectionRecord)
    case denuncia(DenunciaModel)

    var id: String {
        switch self {
        case .inspection(let r): return "insp_\(r.id)"
        case .denuncia(let d): return "den_\(d.id)"
        }
    }
}

struct MapInspectionView: View {
    @ObservedObject var locationManager: LocationManager
    @ObservedObject var store: InspectionStore

    @State private var mapType: MKMapType = .standard
    @State private var selectedLayer: MapFilterLayer = .all
    @State private var selectedItem: MapItemSelection? = nil
    @State private var detailInspection: InspectionRecord? = nil

    @State private var region = MKCoordinateRegion(
        center: CLLocationCoordinate2D(latitude: -12.8, longitude: -55.8),
        span: MKCoordinateSpan(latitudeDelta: 8.5, longitudeDelta: 8.0)
    )

    var body: some View {
        ZStack {
            // MapKit View with Trajectory & Custom Annotations
            MapContainerView(
                region: $region,
                mapType: mapType,
                userLocation: CLLocationCoordinate2D(latitude: locationManager.latitude, longitude: locationManager.longitude),
                trajectory: locationManager.trajectory,
                records: filteredRecords,
                denuncias: filteredDenuncias,
                onSelectRecord: { r in selectedItem = .inspection(r) },
                onSelectDenuncia: { d in selectedItem = .denuncia(d) }
            )
            .ignoresSafeArea(edges: .bottom)

            // Overlays & Floating HUD Controls
            VStack(spacing: 8) {
                // Top Telemetry Card
                HStack {
                    VStack(alignment: .leading, spacing: 2) {
                        HStack(spacing: 6) {
                            Image(systemName: "map.fill")
                                .foregroundColor(Color("ForestGreen"))
                            Text("Mapa Geoespacial SEMA-MT")
                                .font(.system(size: 13, weight: .bold))
                        }
                        Text(String(
                            format: "Lat: %.5f | Lng: %.5f (±%.1fm) • Dist: %.2f km",
                            locationManager.latitude,
                            locationManager.longitude,
                            locationManager.accuracy,
                            locationManager.distanceTraveledKm
                        ))
                        .font(.system(size: 10, design: .monospaced))
                        .foregroundColor(.secondary)
                    }
                    Spacer()
                    Text("\(filteredRecords.count + filteredDenuncias.count) Pontos")
                        .font(.system(size: 11, weight: .bold))
                        .padding(.horizontal, 8)
                        .padding(.vertical, 4)
                        .background(Color("ForestGreen").opacity(0.15))
                        .cornerRadius(8)
                }
                .padding(10)
                .background(.ultraThinMaterial)
                .cornerRadius(12)
                .padding(.horizontal)

                // Layer Filter Chips
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 6) {
                        ForEach(MapFilterLayer.allCases, id: \.self) { layer in
                            Button(action: { selectedLayer = layer }) {
                                Text(layer.rawValue)
                                    .font(.system(size: 11, weight: .semibold))
                                    .padding(.horizontal, 10)
                                    .padding(.vertical, 5)
                                    .background(selectedLayer == layer ? Color("ForestGreen") : Color.black.opacity(0.55))
                                    .foregroundColor(.white)
                                    .cornerRadius(14)
                            }
                        }
                    }
                    .padding(.horizontal)
                }

                Spacer()

                // Floating Action Buttons on Right
                HStack {
                    Spacer()
                    VStack(spacing: 8) {
                        MapActionButton(icon: "location.fill", color: Color("ForestGreen")) {
                            withAnimation {
                                region.center = CLLocationCoordinate2D(
                                    latitude: locationManager.latitude,
                                    longitude: locationManager.longitude
                                )
                                region.span = MKCoordinateSpan(latitudeDelta: 0.05, longitudeDelta: 0.05)
                            }
                        }

                        MapActionButton(icon: mapTypeIcon, color: .primary) {
                            cycleMapType()
                        }

                        MapActionButton(icon: "plus", color: .primary) {
                            withAnimation {
                                region.span.latitudeDelta /= 2
                                region.span.longitudeDelta /= 2
                            }
                        }

                        MapActionButton(icon: "minus", color: .primary) {
                            withAnimation {
                                region.span.latitudeDelta *= 2
                                region.span.longitudeDelta *= 2
                            }
                        }
                    }
                    .padding(.trailing, 14)
                }

                // Selected Marker Bottom Info Card
                if let item = selectedItem {
                    VStack(alignment: .leading, spacing: 8) {
                        HStack {
                            switch item {
                            case .inspection(let r):
                                Image(systemName: "doc.text.fill")
                                    .foregroundColor(r.hasIrregularity ? .red : .green)
                                Text(r.title)
                                    .font(.system(size: 13, weight: .bold))
                                    .lineLimit(1)
                            case .denuncia(let d):
                                Image(systemName: "exclamationmark.triangle.fill")
                                    .foregroundColor(d.gravidade.color)
                                Text(d.protocolo)
                                    .font(.system(size: 13, weight: .bold))
                                    .lineLimit(1)
                            }

                            Spacer()

                            Button(action: { selectedItem = nil }) {
                                Image(systemName: "xmark.circle.fill")
                                    .foregroundColor(.secondary)
                            }
                        }

                        switch item {
                        case .inspection(let r):
                            Text("\(r.municipality) • \(r.biome) | CAR: \(r.carNumber)")
                                .font(.system(size: 11))
                                .foregroundColor(.secondary)
                            Text(r.semaStatus.label)
                                .font(.system(size: 11, weight: .semibold))
                                .foregroundColor(r.semaStatus.color)

                            Button(action: { detailInspection = r }) {
                                Text("Abrir Laudo Completo")
                                    .font(.system(size: 12, weight: .bold))
                                    .frame(maxWidth: .infinity)
                            }
                            .buttonStyle(.borderedProminent)
                            .tint(Color("ForestGreen"))

                        case .denuncia(let d):
                            Text(d.titulo)
                                .font(.system(size: 12, weight: .medium))
                            Text("\(d.municipio) • Gravidade: \(d.gravidade.label)")
                                .font(.system(size: 11))
                                .foregroundColor(d.gravidade.color)
                        }
                    }
                    .padding(12)
                    .background(Color(UIColor.systemBackground))
                    .cornerRadius(14)
                    .shadow(radius: 6)
                    .padding(.horizontal)
                    .padding(.bottom, 8)
                }
            }
            .padding(.top, 8)
        }
        .navigationTitle("Mapa")
        .navigationBarTitleDisplayMode(.inline)
        .sheet(item: $detailInspection) { rec in
            RecordDetailSheet(record: rec, store: store)
        }
    }

    private var filteredRecords: [InspectionRecord] {
        switch selectedLayer {
        case .all: return store.records
        case .inspections: return store.records
        case .denuncias: return []
        case .irregularities: return store.records.filter { $0.hasIrregularity }
        }
    }

    private var filteredDenuncias: [DenunciaModel] {
        switch selectedLayer {
        case .all: return store.denuncias
        case .inspections: return []
        case .denuncias: return store.denuncias
        case .irregularities: return store.denuncias.filter { $0.gravidade == .critica || $0.gravidade == .alta }
        }
    }

    private var mapTypeIcon: String {
        switch mapType {
        case .standard: return "globe.americas.fill"
        case .satellite, .satelliteFlyover: return "square.2.layers.3d"
        default: return "map"
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

// MARK: - MapActionButton
struct MapActionButton: View {
    let icon: String
    let color: Color
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Image(systemName: icon)
                .font(.system(size: 16, weight: .bold))
                .foregroundColor(color)
                .frame(width: 42, height: 42)
                .background(.ultraThinMaterial)
                .clipShape(Circle())
                .shadow(radius: 3)
        }
    }
}

// MARK: - MapContainerView (MKMapView wrapper for full polyline + annotations support)
struct MapContainerView: UIViewRepresentable {
    @Binding var region: MKCoordinateRegion
    var mapType: MKMapType
    var userLocation: CLLocationCoordinate2D
    var trajectory: [CLLocationCoordinate2D]
    var records: [InspectionRecord]
    var denuncias: [DenunciaModel]
    var onSelectRecord: (InspectionRecord) -> Void
    var onSelectDenuncia: (DenunciaModel) -> Void

    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }

    func makeUIView(context: Context) -> MKMapView {
        let mapView = MKMapView()
        mapView.delegate = context.coordinator
        mapView.showsUserLocation = true
        mapView.region = region
        mapView.mapType = mapType
        return mapView
    }

    func updateUIView(_ mapView: MKMapView, context: Context) {
        if mapView.mapType != mapType {
            mapView.mapType = mapType
        }

        context.coordinator.parent = self

        // Update Overlays (Trajectory Polyline)
        mapView.removeOverlays(mapView.overlays)
        if trajectory.count > 1 {
            let polyline = MKPolyline(coordinates: trajectory, count: trajectory.count)
            mapView.addOverlay(polyline)
        }

        // Update Annotations
        mapView.removeAnnotations(mapView.annotations.filter { !($0 is MKUserLocation) })

        for r in records {
            let ann = CustomMapAnnotation(
                title: r.title,
                subtitle: "\(r.municipality) • \(r.semaStatus.label)",
                coordinate: CLLocationCoordinate2D(latitude: r.latitude, longitude: r.longitude),
                isIrregular: r.hasIrregularity,
                isDenuncia: false,
                recordRef: r,
                denunciaRef: nil
            )
            mapView.addAnnotation(ann)
        }

        for d in denuncias {
            let ann = CustomMapAnnotation(
                title: d.protocolo,
                subtitle: "\(d.titulo) (\(d.gravidade.label))",
                coordinate: CLLocationCoordinate2D(latitude: d.latitude, longitude: d.longitude),
                isIrregular: d.gravidade == .critica || d.gravidade == .alta,
                isDenuncia: true,
                recordRef: nil,
                denunciaRef: d
            )
            mapView.addAnnotation(ann)
        }
    }

    class Coordinator: NSObject, MKMapViewDelegate {
        var parent: MapContainerView

        init(_ parent: MapContainerView) {
            self.parent = parent
        }

        func mapView(_ mapView: MKMapView, regionDidChangeAnimated animated: Bool) {
            DispatchQueue.main.async {
                self.parent.region = mapView.region
            }
        }

        func mapView(_ mapView: MKMapView, rendererFor overlay: MKOverlay) -> MKOverlayRenderer {
            if let polyline = overlay as? MKPolyline {
                let renderer = MKPolylineRenderer(polyline: polyline)
                renderer.strokeColor = UIColor(red: 46/255, green: 229/255, blue: 157/255, alpha: 0.9)
                renderer.lineWidth = 4.0
                return renderer
            }
            return MKOverlayRenderer(overlay: overlay)
        }

        func mapView(_ mapView: MKMapView, viewFor annotation: MKAnnotation) -> MKAnnotationView? {
            guard let customAnn = annotation as? CustomMapAnnotation else { return nil }

            let identifier = "GeoFiscalPin"
            var view = mapView.dequeueReusableAnnotationView(withIdentifier: identifier) as? MKMarkerAnnotationView
            if view == nil {
                view = MKMarkerAnnotationView(annotation: annotation, reuseIdentifier: identifier)
                view?.canShowCallout = true
            } else {
                view?.annotation = annotation
            }

            if customAnn.isDenuncia {
                view?.glyphImage = UIImage(systemName: "exclamationmark.triangle.fill")
                view?.markerTintColor = customAnn.isIrregular ? .systemRed : .systemOrange
            } else {
                view?.glyphImage = UIImage(systemName: "shield.fill")
                view?.markerTintColor = customAnn.isIrregular ? .systemRed : UIColor(red: 27/255, green: 94/255, blue: 32/255, alpha: 1.0)
            }

            return view
        }

        func mapView(_ mapView: MKMapView, didSelect view: MKAnnotationView) {
            guard let customAnn = view.annotation as? CustomMapAnnotation else { return }
            if let r = customAnn.recordRef {
                parent.onSelectRecord(r)
            } else if let d = customAnn.denunciaRef {
                parent.onSelectDenuncia(d)
            }
        }
    }
}

// MARK: - Custom Annotation Object
class CustomMapAnnotation: NSObject, MKAnnotation {
    let title: String?
    let subtitle: String?
    let coordinate: CLLocationCoordinate2D
    let isIrregular: Bool
    let isDenuncia: Bool
    let recordRef: InspectionRecord?
    let denunciaRef: DenunciaModel?

    init(title: String?, subtitle: String?, coordinate: CLLocationCoordinate2D, isIrregular: Bool, isDenuncia: Bool, recordRef: InspectionRecord?, denunciaRef: DenunciaModel?) {
        self.title = title
        self.subtitle = subtitle
        self.coordinate = coordinate
        self.isIrregular = isIrregular
        self.isDenuncia = isDenuncia
        self.recordRef = recordRef
        self.denunciaRef = denunciaRef
    }
}
