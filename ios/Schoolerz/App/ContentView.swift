import SwiftUI

struct ContentView: View {
    @EnvironmentObject var appState: AppState

    var body: some View {
        Group {
            if appState.isUnlocked {
                MainTabView()
            } else if appState.hasPIN {
                PinLoginView()
            } else {
                SetPinView()
            }
        }
    }
}

#Preview {
    ContentView()
        .environmentObject(AppState())
}
