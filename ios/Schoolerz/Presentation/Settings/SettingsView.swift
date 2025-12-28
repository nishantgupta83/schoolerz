import SwiftUI

struct SettingsView: View {
    @AppStorage("themeMode") private var themeMode = "system"
    @State private var biometricEnabled = KeychainManager.shared.isBiometricEnabled

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
