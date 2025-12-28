import SwiftUI

struct ProfileView: View {
    @State private var viewModel = ProfileViewModel()
    @State private var showErrorAlert = false

    var body: some View {
        NavigationStack {
            Group {
                if viewModel.isLoading {
                    loadingView
                } else if let profile = viewModel.profile {
                    profileContent(profile)
                } else {
                    setupProfileView
                }
            }
            .navigationTitle("Profile")
            .toolbar {
                if viewModel.profile != nil {
                    ToolbarItem(placement: .primaryAction) {
                        if viewModel.isEditing {
                            Button("Done") {
                                Task { await viewModel.saveProfile() }
                            }
                            .disabled(viewModel.isSaving || viewModel.editingDisplayName.isEmpty)
                            .fontWeight(.semibold)
                        } else {
                            Button("Edit") {
                                withAnimation(.easeInOut(duration: 0.3)) {
                                    viewModel.startEditing()
                                }
                            }
                        }
                    }
                }

                if viewModel.isEditing {
                    ToolbarItem(placement: .cancellationAction) {
                        Button("Cancel") {
                            withAnimation(.easeInOut(duration: 0.3)) {
                                viewModel.cancelEditing()
                            }
                        }
                    }
                }
            }
            .task { await viewModel.loadProfile() }
            .onChange(of: viewModel.errorMessage) { _, newValue in
                showErrorAlert = newValue != nil
            }
            .alert("Error", isPresented: $showErrorAlert) {
                Button("OK") { viewModel.clearError() }
            } message: {
                Text(viewModel.errorMessage ?? "Unknown error")
            }
        }
    }

    // MARK: - Loading View

    private var loadingView: some View {
        VStack(spacing: 16) {
            ProgressView()
                .scaleEffect(1.5)
            Text("Loading profile...")
                .font(.subheadline)
                .foregroundStyle(.secondary)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }

    // MARK: - Profile Content

    private func profileContent(_ profile: Profile) -> some View {
        ScrollView {
            VStack(spacing: 20) {
                // Avatar and Basic Info Card
                ProfileHeaderCard(
                    profile: profile,
                    isEditing: viewModel.isEditing,
                    displayName: $viewModel.editingDisplayName,
                    schoolName: $viewModel.editingSchoolName,
                    gradeLevel: $viewModel.editingGradeLevel
                )

                // Services Card
                ServicesCard(
                    services: profile.services,
                    isEditing: viewModel.isEditing,
                    editingServices: $viewModel.editingServices,
                    onToggleService: viewModel.toggleService
                )

                // About Me Card
                AboutMeCard(
                    bio: profile.bio,
                    isEditing: viewModel.isEditing,
                    editingBio: $viewModel.editingBio
                )

                // Location Card
                LocationCard(
                    neighborhood: profile.neighborhood,
                    isEditing: viewModel.isEditing,
                    editingNeighborhood: $viewModel.editingNeighborhood
                )

                // Account Info Card
                AccountInfoCard(profile: profile)
            }
            .padding()
        }
        .refreshable {
            await viewModel.refreshProfile()
        }
    }

    // MARK: - Setup Profile View

    private var setupProfileView: some View {
        ScrollView {
            VStack(spacing: 24) {
                // Welcome Section
                VStack(spacing: 16) {
                    Circle()
                        .fill(
                            LinearGradient(
                                colors: [Color(hex: "4285F4"), Color(hex: "34A853")],
                                startPoint: .topLeading,
                                endPoint: .bottomTrailing
                            )
                        )
                        .frame(width: 100, height: 100)
                        .overlay {
                            Image(systemName: "person.crop.circle.badge.plus")
                                .font(.system(size: 50))
                                .foregroundStyle(.white)
                        }
                        .shadow(color: Color(hex: "4285F4").opacity(0.3), radius: 20, x: 0, y: 10)

                    Text("Create Your Profile")
                        .font(.title.bold())

                    Text("Set up your profile to start offering services and connecting with your neighborhood")
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal)
                }
                .padding(.top, 40)

                // Setup Form
                VStack(spacing: 16) {
                    ProfileSetupCard(viewModel: viewModel)

                    Button {
                        Task { await viewModel.saveProfile() }
                    } label: {
                        if viewModel.isSaving {
                            ProgressView()
                                .tint(.white)
                        } else {
                            Text("Create Profile")
                                .fontWeight(.semibold)
                        }
                    }
                    .frame(maxWidth: .infinity)
                    .frame(height: 50)
                    .background(Color(hex: "4285F4"))
                    .foregroundStyle(.white)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                    .disabled(viewModel.isSaving || viewModel.editingDisplayName.isEmpty)
                    .opacity(viewModel.editingDisplayName.isEmpty ? 0.5 : 1.0)
                    .shadow(color: Color(hex: "4285F4").opacity(0.3), radius: 10, x: 0, y: 5)
                }
                .padding()
            }
        }
    }
}

// MARK: - Profile Header Card

struct ProfileHeaderCard: View {
    let profile: Profile
    let isEditing: Bool
    @Binding var displayName: String
    @Binding var schoolName: String
    @Binding var gradeLevel: String

    var body: some View {
        VStack(spacing: 20) {
            // Avatar
            Circle()
                .fill(
                    LinearGradient(
                        colors: [Color(hex: "4285F4"), Color(hex: "34A853")],
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    )
                )
                .frame(width: 100, height: 100)
                .overlay {
                    Text(profile.initials)
                        .font(.system(size: 40, weight: .bold))
                        .foregroundStyle(.white)
                }
                .shadow(color: Color(hex: "4285F4").opacity(0.3), radius: 15, x: 0, y: 8)

            // Name and Verification
            VStack(spacing: 8) {
                if isEditing {
                    TextField("Display Name", text: $displayName)
                        .font(.title2.bold())
                        .multilineTextAlignment(.center)
                        .textFieldStyle(.roundedBorder)
                } else {
                    Text(profile.displayName)
                        .font(.title2.bold())
                }

                VerificationBadge(status: profile.verificationStatus)
            }

            Divider()
                .padding(.horizontal)

            // School and Grade
            VStack(spacing: 12) {
                HStack(spacing: 12) {
                    Image(systemName: "building.2.fill")
                        .foregroundStyle(Color(hex: "4285F4"))
                        .frame(width: 24)

                    if isEditing {
                        TextField("School Name", text: $schoolName)
                            .textFieldStyle(.roundedBorder)
                    } else {
                        Text(profile.schoolName ?? "No school set")
                            .foregroundStyle(profile.schoolName == nil ? .secondary : .primary)
                        Spacer()
                    }
                }

                HStack(spacing: 12) {
                    Image(systemName: "graduationcap.fill")
                        .foregroundStyle(Color(hex: "34A853"))
                        .frame(width: 24)

                    if isEditing {
                        TextField("Grade Level (e.g., 11th Grade)", text: $gradeLevel)
                            .textFieldStyle(.roundedBorder)
                    } else {
                        Text(profile.gradeLevel ?? "No grade set")
                            .foregroundStyle(profile.gradeLevel == nil ? .secondary : .primary)
                        Spacer()
                    }
                }
            }
            .padding(.horizontal)
        }
        .padding()
        .background(Color(.systemBackground))
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .shadow(color: .black.opacity(0.05), radius: 10, x: 0, y: 4)
    }
}

// MARK: - Services Card

struct ServicesCard: View {
    let services: [ServiceType]
    let isEditing: Bool
    @Binding var editingServices: Set<ServiceType>
    let onToggleService: (ServiceType) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            HStack {
                Image(systemName: "briefcase.fill")
                    .foregroundStyle(Color(hex: "4285F4"))
                Text("Services Offered")
                    .font(.headline)
            }

            if isEditing {
                Text("Tap to select services you offer")
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }

            FlowLayout(spacing: 8) {
                ForEach(ServiceType.allCases, id: \.self) { service in
                    ServiceChip(
                        service: service,
                        isSelected: isEditing ? editingServices.contains(service) : services.contains(service),
                        isEditing: isEditing
                    ) {
                        if isEditing {
                            withAnimation(.spring(response: 0.3, dampingFraction: 0.7)) {
                                onToggleService(service)
                            }
                        }
                    }
                }
            }

            if !isEditing && services.isEmpty {
                Text("No services selected yet")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
                    .italic()
            }
        }
        .padding()
        .background(Color(.systemBackground))
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .shadow(color: .black.opacity(0.05), radius: 10, x: 0, y: 4)
    }
}

// MARK: - Service Chip

struct ServiceChip: View {
    let service: ServiceType
    let isSelected: Bool
    let isEditing: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: 6) {
                Image(systemName: service.icon)
                    .font(.caption)
                Text(service.displayName)
                    .font(.subheadline)
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 8)
            .background(
                isSelected ?
                Color(hex: "4285F4") :
                Color(.systemGray6)
            )
            .foregroundStyle(isSelected ? .white : .primary)
            .clipShape(Capsule())
            .overlay(
                Capsule()
                    .stroke(Color(hex: "4285F4"), lineWidth: isSelected ? 0 : 1)
            )
        }
        .buttonStyle(.plain)
        .disabled(!isEditing)
        .scaleEffect(isSelected && isEditing ? 1.05 : 1.0)
        .animation(.spring(response: 0.3, dampingFraction: 0.7), value: isSelected)
    }
}

// MARK: - About Me Card

struct AboutMeCard: View {
    let bio: String?
    let isEditing: Bool
    @Binding var editingBio: String

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            HStack {
                Image(systemName: "text.quote")
                    .foregroundStyle(Color(hex: "34A853"))
                Text("About Me")
                    .font(.headline)
            }

            if isEditing {
                TextEditor(text: $editingBio)
                    .frame(minHeight: 100)
                    .padding(8)
                    .background(Color(.systemGray6))
                    .clipShape(RoundedRectangle(cornerRadius: 8))
                    .overlay(
                        RoundedRectangle(cornerRadius: 8)
                            .stroke(Color(.systemGray4), lineWidth: 1)
                    )
            } else {
                Text(bio ?? "No bio yet. Tap Edit to add one!")
                    .font(.subheadline)
                    .foregroundStyle(bio == nil ? .secondary : .primary)
                    .italic(bio == nil)
            }
        }
        .padding()
        .background(Color(.systemBackground))
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .shadow(color: .black.opacity(0.05), radius: 10, x: 0, y: 4)
    }
}

// MARK: - Location Card

struct LocationCard: View {
    let neighborhood: String?
    let isEditing: Bool
    @Binding var editingNeighborhood: String

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            HStack {
                Image(systemName: "map.fill")
                    .foregroundStyle(Color(hex: "EA4335"))
                Text("Neighborhood")
                    .font(.headline)
            }

            HStack(spacing: 12) {
                Image(systemName: "location.fill")
                    .foregroundStyle(Color(hex: "EA4335"))
                    .frame(width: 24)

                if isEditing {
                    TextField("Neighborhood (e.g., Westside)", text: $editingNeighborhood)
                        .textFieldStyle(.roundedBorder)
                } else {
                    Text(neighborhood ?? "No neighborhood set")
                        .foregroundStyle(neighborhood == nil ? .secondary : .primary)
                    Spacer()
                }
            }
        }
        .padding()
        .background(Color(.systemBackground))
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .shadow(color: .black.opacity(0.05), radius: 10, x: 0, y: 4)
    }
}

// MARK: - Account Info Card

struct AccountInfoCard: View {
    let profile: Profile

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            HStack {
                Image(systemName: "info.circle.fill")
                    .foregroundStyle(Color(.systemGray))
                Text("Account Info")
                    .font(.headline)
            }

            VStack(spacing: 12) {
                HStack {
                    Text("Member Since")
                        .foregroundStyle(.secondary)
                    Spacer()
                    Text(profile.createdAt, style: .date)
                        .fontWeight(.medium)
                }

                Divider()

                HStack {
                    Text("User ID")
                        .foregroundStyle(.secondary)
                    Spacer()
                    Text(String(profile.id.prefix(8)) + "...")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                        .fontDesign(.monospaced)
                }
            }
        }
        .padding()
        .background(Color(.systemBackground))
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .shadow(color: .black.opacity(0.05), radius: 10, x: 0, y: 4)
    }
}

// MARK: - Verification Badge

struct VerificationBadge: View {
    let status: VerificationStatus

    var body: some View {
        HStack(spacing: 4) {
            switch status {
            case .verified:
                Image(systemName: "checkmark.seal.fill")
                    .foregroundStyle(.green)
                Text("Verified")
                    .foregroundStyle(.green)
            case .emailPending:
                Image(systemName: "envelope.badge")
                    .foregroundStyle(.orange)
                Text("Email Pending")
                    .foregroundStyle(.orange)
            case .unverified:
                Image(systemName: "exclamationmark.circle")
                    .foregroundStyle(.secondary)
                Text("Unverified")
                    .foregroundStyle(.secondary)
            }
        }
        .font(.caption)
        .fontWeight(.medium)
    }
}

// MARK: - Profile Setup Card

struct ProfileSetupCard: View {
    @Bindable var viewModel: ProfileViewModel

    var body: some View {
        VStack(spacing: 16) {
            VStack(alignment: .leading, spacing: 8) {
                Label("Display Name *", systemImage: "person.fill")
                    .font(.subheadline.bold())
                    .foregroundStyle(Color(hex: "4285F4"))
                TextField("Enter your name", text: $viewModel.editingDisplayName)
                    .textFieldStyle(.roundedBorder)
            }

            VStack(alignment: .leading, spacing: 8) {
                Label("School", systemImage: "building.2.fill")
                    .font(.subheadline.bold())
                    .foregroundStyle(Color(hex: "4285F4"))
                TextField("e.g., Lincoln High School", text: $viewModel.editingSchoolName)
                    .textFieldStyle(.roundedBorder)
            }

            VStack(alignment: .leading, spacing: 8) {
                Label("Grade Level", systemImage: "graduationcap.fill")
                    .font(.subheadline.bold())
                    .foregroundStyle(Color(hex: "34A853"))
                TextField("e.g., 11th Grade", text: $viewModel.editingGradeLevel)
                    .textFieldStyle(.roundedBorder)
            }

            VStack(alignment: .leading, spacing: 8) {
                Label("Neighborhood", systemImage: "map.fill")
                    .font(.subheadline.bold())
                    .foregroundStyle(Color(hex: "EA4335"))
                TextField("e.g., Westside", text: $viewModel.editingNeighborhood)
                    .textFieldStyle(.roundedBorder)
            }

            VStack(alignment: .leading, spacing: 8) {
                Label("About Me", systemImage: "text.quote")
                    .font(.subheadline.bold())
                    .foregroundStyle(Color(hex: "34A853"))
                TextEditor(text: $viewModel.editingBio)
                    .frame(height: 100)
                    .padding(8)
                    .background(Color(.systemGray6))
                    .clipShape(RoundedRectangle(cornerRadius: 8))
                    .overlay(
                        RoundedRectangle(cornerRadius: 8)
                            .stroke(Color(.systemGray4), lineWidth: 1)
                    )
            }

            VStack(alignment: .leading, spacing: 8) {
                Label("Services I Offer", systemImage: "briefcase.fill")
                    .font(.subheadline.bold())
                    .foregroundStyle(Color(hex: "4285F4"))
                Text("Tap to select")
                    .font(.caption)
                    .foregroundStyle(.secondary)

                FlowLayout(spacing: 8) {
                    ForEach(ServiceType.allCases, id: \.self) { service in
                        ServiceChip(
                            service: service,
                            isSelected: viewModel.editingServices.contains(service),
                            isEditing: true
                        ) {
                            withAnimation(.spring(response: 0.3, dampingFraction: 0.7)) {
                                viewModel.toggleService(service)
                            }
                        }
                    }
                }
            }
        }
        .padding()
        .background(Color(.systemBackground))
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .shadow(color: .black.opacity(0.05), radius: 10, x: 0, y: 4)
    }
}

// MARK: - Flow Layout for Service Chips

struct FlowLayout: Layout {
    var spacing: CGFloat = 8

    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        let result = FlowResult(
            in: proposal.replacingUnspecifiedDimensions().width,
            subviews: subviews,
            spacing: spacing
        )
        return result.size
    }

    func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) {
        let result = FlowResult(
            in: bounds.width,
            subviews: subviews,
            spacing: spacing
        )
        for (index, subview) in subviews.enumerated() {
            subview.place(at: CGPoint(x: bounds.minX + result.positions[index].x, y: bounds.minY + result.positions[index].y), proposal: .unspecified)
        }
    }

    struct FlowResult {
        var size: CGSize = .zero
        var positions: [CGPoint] = []

        init(in maxWidth: CGFloat, subviews: Subviews, spacing: CGFloat) {
            var currentX: CGFloat = 0
            var currentY: CGFloat = 0
            var lineHeight: CGFloat = 0

            for subview in subviews {
                let size = subview.sizeThatFits(.unspecified)

                if currentX + size.width > maxWidth && currentX > 0 {
                    currentX = 0
                    currentY += lineHeight + spacing
                    lineHeight = 0
                }

                positions.append(CGPoint(x: currentX, y: currentY))
                lineHeight = max(lineHeight, size.height)
                currentX += size.width + spacing
            }

            self.size = CGSize(width: maxWidth, height: currentY + lineHeight)
        }
    }
}

// MARK: - Color Extension

extension Color {
    init(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let a, r, g, b: UInt64
        switch hex.count {
        case 3: // RGB (12-bit)
            (a, r, g, b) = (255, (int >> 8) * 17, (int >> 4 & 0xF) * 17, (int & 0xF) * 17)
        case 6: // RGB (24-bit)
            (a, r, g, b) = (255, int >> 16, int >> 8 & 0xFF, int & 0xFF)
        case 8: // ARGB (32-bit)
            (a, r, g, b) = (int >> 24, int >> 16 & 0xFF, int >> 8 & 0xFF, int & 0xFF)
        default:
            (a, r, g, b) = (255, 0, 0, 0)
        }
        self.init(
            .sRGB,
            red: Double(r) / 255,
            green: Double(g) / 255,
            blue: Double(b) / 255,
            opacity: Double(a) / 255
        )
    }
}

// MARK: - Preview

#Preview {
    ProfileView()
}

#Preview("With Sample Data") {
    let viewModel = ProfileViewModel()
    // Simulate loaded profile
    Task {
        await viewModel.loadProfile()
    }
    return ProfileView()
}
