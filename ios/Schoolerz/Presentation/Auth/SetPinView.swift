import SwiftUI

struct SetPinView: View {
    @EnvironmentObject var appState: AppState
    @State private var pin = ""
    @State private var confirmPin = ""
    @State private var step: Step = .create
    @State private var error: String?
    @FocusState private var focused: Bool

    enum Step { case create, confirm }

    var body: some View {
        VStack(spacing: Tokens.Spacing.xl) {
            Spacer()
            Image(systemName: "lock.circle.fill")
                .font(.system(size: 60))
                .foregroundStyle(Colors.seed)

            Text(step == .create ? "Create a PIN" : "Confirm PIN")
                .font(Typography.headline)

            Text("Use a 4-digit PIN to secure your app")
                .font(Typography.body)
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)

            SecureField("PIN", text: step == .create ? $pin : $confirmPin)
                .keyboardType(.numberPad)
                .textContentType(.oneTimeCode)
                .multilineTextAlignment(.center)
                .font(.title)
                .frame(width: 150)
                .focused($focused)
                .onChange(of: step == .create ? pin : confirmPin) { _, newValue in
                    if newValue.count == 4 { handlePinEntry() }
                }

            if let error {
                Text(error)
                    .foregroundStyle(.red)
                    .font(Typography.caption)
            }
            Spacer()
        }
        .padding(Tokens.Spacing.l)
        .onAppear { focused = true }
    }

    private func handlePinEntry() {
        switch step {
        case .create:
            step = .confirm
            focused = true
        case .confirm:
            if pin == confirmPin {
                do {
                    try KeychainManager.shared.storePIN(pin)
                    appState.hasPIN = true
                    appState.isUnlocked = true
                } catch {
                    self.error = "Failed to save PIN"
                }
            } else {
                error = "PINs don't match"
                confirmPin = ""
                step = .create
                pin = ""
            }
        }
    }
}
