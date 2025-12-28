import SwiftUI

@Observable
@MainActor
final class ProfileViewModel {
    var profile: Profile?
    var isLoading = false
    var isSaving = false
    var isRefreshing = false
    var errorMessage: String?

    // Edit state
    var editingDisplayName = ""
    var editingSchoolName = ""
    var editingGradeLevel = ""
    var editingBio = ""
    var editingNeighborhood = ""
    var editingServices: Set<ServiceType> = []
    var isEditing = false

    private let repository: ProfileRepository

    init(repository: ProfileRepository? = nil) {
        self.repository = repository ?? Container.shared.profileRepository
    }

    func loadProfile() async {
        isLoading = true
        errorMessage = nil
        do {
            profile = try await repository.fetchProfile()
            syncEditingState()
        } catch {
            errorMessage = error.localizedDescription
        }
        isLoading = false
    }

    func refreshProfile() async {
        isRefreshing = true
        errorMessage = nil
        do {
            profile = try await repository.fetchProfile()
            syncEditingState()
        } catch {
            errorMessage = error.localizedDescription
        }
        isRefreshing = false
    }

    func saveProfile() async {
        guard !editingDisplayName.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else {
            errorMessage = "Display name is required"
            return
        }

        isSaving = true
        errorMessage = nil

        let updatedProfile = Profile(
            id: profile?.id ?? UUID().uuidString,
            displayName: editingDisplayName.trimmingCharacters(in: .whitespacesAndNewlines),
            schoolName: editingSchoolName.isEmpty ? nil : editingSchoolName.trimmingCharacters(in: .whitespacesAndNewlines),
            gradeLevel: editingGradeLevel.isEmpty ? nil : editingGradeLevel.trimmingCharacters(in: .whitespacesAndNewlines),
            avatarPath: profile?.avatarPath,
            verificationStatus: profile?.verificationStatus ?? .unverified,
            services: Array(editingServices).sorted(by: { $0.displayName < $1.displayName }),
            bio: editingBio.isEmpty ? nil : editingBio.trimmingCharacters(in: .whitespacesAndNewlines),
            neighborhood: editingNeighborhood.isEmpty ? nil : editingNeighborhood.trimmingCharacters(in: .whitespacesAndNewlines),
            createdAt: profile?.createdAt ?? Date()
        )

        do {
            try await repository.saveProfile(updatedProfile)
            profile = updatedProfile
            isEditing = false
        } catch {
            errorMessage = error.localizedDescription
        }
        isSaving = false
    }

    func startEditing() {
        syncEditingState()
        isEditing = true
    }

    func cancelEditing() {
        syncEditingState()
        isEditing = false
    }

    func toggleService(_ service: ServiceType) {
        if editingServices.contains(service) {
            editingServices.remove(service)
        } else {
            editingServices.insert(service)
        }
    }

    func clearError() {
        errorMessage = nil
    }

    private func syncEditingState() {
        if let profile {
            editingDisplayName = profile.displayName
            editingSchoolName = profile.schoolName ?? ""
            editingGradeLevel = profile.gradeLevel ?? ""
            editingBio = profile.bio ?? ""
            editingNeighborhood = profile.neighborhood ?? ""
            editingServices = Set(profile.services)
        }
    }
}
