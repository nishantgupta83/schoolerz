import SwiftUI

struct SettingsView: View {
    @AppStorage("themeMode") private var themeMode = "system"
    @State private var biometricEnabled = KeychainManager.shared.isBiometricEnabled
    @StateObject private var notificationManager = NotificationManager.shared

    var body: some View {
        NavigationStack {
            List {

                Section("Appearance") {
                    Picker("Theme", selection: $themeMode) {
                        Text("Light").tag("light")
                        Text("Dark").tag("dark")
                        Text("System").tag("system")
                    }
                }

                Section("Security") {
                    Toggle("Use Face ID", isOn: $biometricEnabled)
                        .disabled(!BiometricAuth.shared.canUseBiometrics)
                        .onChange(of: biometricEnabled) { _, newValue in
                            KeychainManager.shared.isBiometricEnabled = newValue
                        }

                    Button("Change PIN") { }
                }

                #if DEBUG
                Section("Debug") {
                    Button(action: {
                        Task {
                            if !notificationManager.isAuthorized {
                                await notificationManager.requestPermission()
                            }
                            notificationManager.sendTestNotification()
                        }
                    }) {
                        Label("Test Notification (5s)", systemImage: "bell.badge.fill")
                    }

                    HStack {
                        Text("Notification Permission")
                        Spacer()
                        Text(notificationManager.isAuthorized ? "Granted" : "Denied")
                            .foregroundStyle(.secondary)
                    }
                }
                #endif

                Section("About") {
                    HStack {
                        Text("Version")
                        Spacer()
                        Text("1.0.0").foregroundStyle(.secondary)
                    }
                }
            }
            .navigationTitle("Settings")
        }
    }
}
