import SwiftUI
import MapKit

enum IosNavTab: Int, CaseIterable {
    case capture = 0
    case history = 1
    case map = 2
    case dashboard = 3
    case admin = 4

    var label: String {
        switch self {
        case .capture: return "Captura"
        case .history: return "Histórico"
        case .map: return "Mapa"
        case .dashboard: return "Painel"
        case .admin: return "Admin"
        }
    }

    var icon: String {
        switch self {
        case .capture: return "camera.fill"
        case .history: return "clock.arrow.circlepath"
        case .map: return "map.fill"
        case .dashboard: return "chart.bar.fill"
        case .admin: return "person.badge.shield.checkmark.fill"
        }
    }
}

struct ContentView: View {
    @StateObject private var locationManager = LocationManager()
    @StateObject private var store = InspectionStore()
    @State private var selectedTab: IosNavTab = .capture
    @State private var showToast = false
    @State private var toastMessage = ""

    var body: some View {
        VStack(spacing: 0) {
            // SEMA-MT Top App Bar
            TopBarView(store: store)

            // Main Tab Navigation (Matching Android's 5 tabs)
            TabView(selection: $selectedTab) {
                NavigationStack {
                    CaptureView(locationManager: locationManager, store: store)
                }
                .tabItem {
                    Label(IosNavTab.capture.label, systemImage: IosNavTab.capture.icon)
                }
                .tag(IosNavTab.capture)

                NavigationStack {
                    HistoryView(store: store)
                }
                .tabItem {
                    Label(IosNavTab.history.label, systemImage: IosNavTab.history.icon)
                }
                .tag(IosNavTab.history)

                NavigationStack {
                    MapInspectionView(locationManager: locationManager, store: store)
                }
                .tabItem {
                    Label(IosNavTab.map.label, systemImage: IosNavTab.map.icon)
                }
                .tag(IosNavTab.map)

                NavigationStack {
                    DashboardView(store: store, locationManager: locationManager)
                }
                .tabItem {
                    Label(IosNavTab.dashboard.label, systemImage: IosNavTab.dashboard.icon)
                }
                .tag(IosNavTab.dashboard)

                NavigationStack {
                    AdminPanelView(store: store)
                }
                .tabItem {
                    Label(IosNavTab.admin.label, systemImage: IosNavTab.admin.icon)
                }
                .tag(IosNavTab.admin)
            }
            .tint(Color("ForestGreen"))
        }
        .overlay(alignment: .bottom) {
            if showToast {
                Text(toastMessage)
                    .font(.system(size: 12, weight: .semibold))
                    .foregroundColor(.white)
                    .padding(.horizontal, 16)
                    .padding(.vertical, 10)
                    .background(Color.black.opacity(0.85))
                    .cornerRadius(20)
                    .padding(.bottom, 65)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
            }
        }
        .onChange(of: store.uiMessage) { _, msg in
            if let msg = msg {
                toastMessage = msg
                withAnimation { showToast = true }
                DispatchQueue.main.asyncAfter(deadline: .now() + 3.0) {
                    withAnimation { showToast = false }
                    store.uiMessage = nil
                }
            }
        }
    }
}
