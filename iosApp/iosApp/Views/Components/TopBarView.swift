import SwiftUI

struct TopBarView: View {
    @ObservedObject var store: InspectionStore

    var body: some View {
        HStack(spacing: 12) {
            // Shield icon
            Image(systemName: "shield.fill")
                .font(.system(size: 22))
                .foregroundColor(Color(red: 0x2E/255.0, green: 0xE5/255.0, blue: 0x9D/255.0))

            // App Title & Role
            VStack(alignment: .leading, spacing: 2) {
                Text("GeoFiscal SEMA-MT")
                    .font(.system(size: 16, weight: .black))
                    .foregroundColor(.white)
                Text(store.currentRole.title)
                    .font(.system(size: 11, weight: .medium))
                    .foregroundColor(Color(red: 0xC7/255.0, green: 0xF9/255.0, blue: 0xDC/255.0))
                    .lineLimit(1)
            }

            Spacer()

            // Online / Offline Status Badge
            HStack(spacing: 5) {
                Circle()
                    .fill(store.isOnline ? Color.green : Color.red)
                    .frame(width: 8, height: 8)
                Text(store.isOnline ? "Online" : "Offline")
                    .font(.system(size: 11, weight: .bold))
                    .foregroundColor(.white)
            }
            .padding(.horizontal, 8)
            .padding(.vertical, 4)
            .background(store.isOnline ? Color(red: 0x0D/255.0, green: 0x3B/255.0, blue: 0x13/255.0) : Color(red: 0x4A/255.0, green: 0x1E/255.0, blue: 0x1E/255.0))
            .cornerRadius(12)

            // Sync Button
            Button(action: {
                store.triggerSync()
            }) {
                Image(systemName: "arrow.triangle.2.circlepath")
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(.white)
                    .rotationEffect(.degrees(store.isSyncing ? 360 : 0))
                    .animation(store.isSyncing ? Animation.linear(duration: 1).repeatForever(autoreverses: false) : .default, value: store.isSyncing)
            }
            .disabled(store.isSyncing)
            .padding(.trailing, 4)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 10)
        .background(Color("ForestGreen"))
    }
}
