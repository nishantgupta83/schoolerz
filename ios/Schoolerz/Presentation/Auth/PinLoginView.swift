import SwiftUI

struct PinLoginView: View {
    @EnvironmentObject var appState: AppState
    @State private var pin = ""
    @State private var error: String?
    @State private var isLockedOut = KeychainManager.shared.isLockedOut
    @State private var remainingSeconds = KeychainManager.shared.remainingLockoutSeconds
    @FocusState private var focused: Bool

    private let timer = Timer.publish(every: 1, on: .main, in: .common).autoconnect()

    var body: some View {
        VStack(spacing: Tokens.Spacing.xl) {
            Spacer()
            Image(systemName: "lock.shield.fill")
                .font(.system(size: 60))
                .foregroundStyle(Colors.seed)

            Text("Enter PIN")
                .font(Typography.headline)

            if isLockedOut {
                Text("Locked out for \(remainingSeconds)s")
                    .foregroundStyle(.red)
                    .font(Typography.body)
            }

            SecureField("PIN", text: $pin)
                .keyboardType(.numberPad)
                .textContentType(.oneTimeCode)
                .multilineTextAlignment(.center)
                .font(.title)
                .frame(width: 150)
                .focused($focused)
                .disabled(isLockedOut)
                .onChange(of: pin) { _, newValue in
                    if newValue.count == 4 && !isLockedOut { verifyPin() }
                }

            if let error {
                Text(error)
                    .foregroundStyle(.red)
                    .font(Typography.caption)
            }

            if BiometricAuth.shared.canUseBiometrics && KeychainManager.shared.isBiometricEnabled && !isLockedOut {
                Button { Task { await authenticateWithBiometrics() } } label: {
                    Label("Use Face ID", systemImage: "faceid")
                }
                .buttonStyle(Theme.secondaryButton())
            }
            Spacer()
        }
        .padding(Tokens.Spacing.l)
        .onAppear {
            focused = true
            isLockedOut = KeychainManager.shared.isLockedOut
            if !isLockedOut && KeychainManager.shared.isBiometricEnabled {
                Task { await authenticateWithBiometrics() }
            }
        }
        .onReceive(timer) { _ in
            if isLockedOut {
                remainingSeconds = KeychainManager.shared.remainingLockoutSeconds
                if remainingSeconds <= 0 {
                    isLockedOut = false
                    error = nil
                }
            }
        }
    }

    private func verifyPin() {
        if KeychainManager.shared.verifyPIN(pin) {
            KeychainManager.shared.resetFailedAttempts()
            appState.isUnlocked = true
        } else {
            KeychainManager.shared.recordFailedAttempt()
            isLockedOut = KeychainManager.shared.isLockedOut

            if isLockedOut {
                error = "Too many attempts. Try again in 5 minutes."
            } else {
                let remaining = 5 - KeychainManager.shared.failedAttempts
                error = "Incorrect PIN. \(remaining) attempts remaining."
            }
            pin = ""
        }
    }

    private func authenticateWithBiometrics() async {
        if await BiometricAuth.shared.authenticate(reason: "Unlock Schoolerz") {
            await MainActor.run {
                KeychainManager.shared.resetFailedAttempts()
                appState.isUnlocked = true
            }
        }
    }
}
