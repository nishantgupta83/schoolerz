import SwiftUI

struct PostComposerView: View {
    @Environment(\.dismiss) private var dismiss
    @State private var type: PostType = .offer
    @State private var postBody = ""
    @State private var neighborhood = "Downtown"
    @State private var showDiscardAlert = false

    let onSubmit: (PostType, String, String) async -> Void

    private var hasContent: Bool { !postBody.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty }

    var body: some View {
        NavigationStack {
            Form {
                Section("Type") {
                    Picker("Post Type", selection: $type) {
                        ForEach(PostType.allCases, id: \.self) { t in
                            Text(t.displayName).tag(t)
                        }
                    }
                    .pickerStyle(.segmented)
                }

                Section("Neighborhood") {
                    Picker("Neighborhood", selection: $neighborhood) {
                        ForEach(["Downtown", "Westside", "Eastside", "Northgate"], id: \.self) {
                            Text($0).tag($0)
                        }
                    }
                }

                Section("Description") {
                    TextEditor(text: $postBody)
                        .frame(minHeight: 120)
                }
            }
            .navigationTitle("New Post")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") {
                        if hasContent { showDiscardAlert = true } else { dismiss() }
                    }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Post") {
                        Task {
                            await onSubmit(type, postBody, neighborhood)
                            dismiss()
                        }
                    }
                    .disabled(!hasContent)
                }
            }
            .interactiveDismissDisabled(hasContent)
            .alert("Discard Draft?", isPresented: $showDiscardAlert) {
                Button("Discard", role: .destructive) { dismiss() }
                Button("Keep Editing", role: .cancel) { }
            }
        }
    }
}
