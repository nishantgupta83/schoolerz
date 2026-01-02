import XCTest
@testable import Schoolerz

final class ProfileViewModelTests: XCTestCase {

    // MARK: - Initial State Tests

    func testInitialProfileIsNil() {
        let viewModel = ProfileViewModel(repository: MockProfileRepository())

        XCTAssertNil(viewModel.profile)
    }

    func testInitialLoadingStateIsFalse() {
        let viewModel = ProfileViewModel(repository: MockProfileRepository())

        XCTAssertFalse(viewModel.isLoading)
    }

    func testInitialSavingStateIsFalse() {
        let viewModel = ProfileViewModel(repository: MockProfileRepository())

        XCTAssertFalse(viewModel.isSaving)
    }

    func testInitialRefreshingStateIsFalse() {
        let viewModel = ProfileViewModel(repository: MockProfileRepository())

        XCTAssertFalse(viewModel.isRefreshing)
    }

    func testInitialErrorMessageIsNil() {
        let viewModel = ProfileViewModel(repository: MockProfileRepository())

        XCTAssertNil(viewModel.errorMessage)
    }

    func testInitialEditingStateIsFalse() {
        let viewModel = ProfileViewModel(repository: MockProfileRepository())

        XCTAssertFalse(viewModel.isEditing)
    }

    func testInitialEditingFieldsAreEmpty() {
        let viewModel = ProfileViewModel(repository: MockProfileRepository())

        XCTAssertEqual(viewModel.editingDisplayName, "")
        XCTAssertEqual(viewModel.editingSchoolName, "")
        XCTAssertEqual(viewModel.editingGradeLevel, "")
        XCTAssertEqual(viewModel.editingBio, "")
        XCTAssertEqual(viewModel.editingNeighborhood, "")
        XCTAssertTrue(viewModel.editingServices.isEmpty)
    }

    // MARK: - Edit State Management Tests

    func testStartEditingSetsIsEditingTrue() {
        let viewModel = ProfileViewModel(repository: MockProfileRepository())
        viewModel.startEditing()

        XCTAssertTrue(viewModel.isEditing)
    }

    func testCancelEditingSetsIsEditingFalse() {
        let viewModel = ProfileViewModel(repository: MockProfileRepository())
        viewModel.startEditing()
        viewModel.cancelEditing()

        XCTAssertFalse(viewModel.isEditing)
    }

    // MARK: - Service Toggle Tests

    func testToggleServiceAddsService() {
        let viewModel = ProfileViewModel(repository: MockProfileRepository())
        viewModel.toggleService(.tutoring)

        XCTAssertTrue(viewModel.editingServices.contains(.tutoring))
    }

    func testToggleServiceRemovesExistingService() {
        let viewModel = ProfileViewModel(repository: MockProfileRepository())
        viewModel.toggleService(.tutoring)
        viewModel.toggleService(.tutoring)

        XCTAssertFalse(viewModel.editingServices.contains(.tutoring))
    }

    func testToggleMultipleServices() {
        let viewModel = ProfileViewModel(repository: MockProfileRepository())
        viewModel.toggleService(.tutoring)
        viewModel.toggleService(.babysitting)
        viewModel.toggleService(.dogWalking)

        XCTAssertEqual(viewModel.editingServices.count, 3)
        XCTAssertTrue(viewModel.editingServices.contains(.tutoring))
        XCTAssertTrue(viewModel.editingServices.contains(.babysitting))
        XCTAssertTrue(viewModel.editingServices.contains(.dogWalking))
    }

    // MARK: - Clear Error Tests

    func testClearErrorSetsErrorToNil() {
        let viewModel = ProfileViewModel(repository: MockProfileRepository())
        viewModel.errorMessage = "Test error"
        viewModel.clearError()

        XCTAssertNil(viewModel.errorMessage)
    }

    // MARK: - Editing Fields Tests

    func testEditingDisplayNameCanBeSet() {
        let viewModel = ProfileViewModel(repository: MockProfileRepository())
        viewModel.editingDisplayName = "New Name"

        XCTAssertEqual(viewModel.editingDisplayName, "New Name")
    }

    func testEditingSchoolNameCanBeSet() {
        let viewModel = ProfileViewModel(repository: MockProfileRepository())
        viewModel.editingSchoolName = "New School"

        XCTAssertEqual(viewModel.editingSchoolName, "New School")
    }

    func testEditingGradeLevelCanBeSet() {
        let viewModel = ProfileViewModel(repository: MockProfileRepository())
        viewModel.editingGradeLevel = "12th Grade"

        XCTAssertEqual(viewModel.editingGradeLevel, "12th Grade")
    }

    func testEditingBioCanBeSet() {
        let viewModel = ProfileViewModel(repository: MockProfileRepository())
        viewModel.editingBio = "My bio"

        XCTAssertEqual(viewModel.editingBio, "My bio")
    }

    func testEditingNeighborhoodCanBeSet() {
        let viewModel = ProfileViewModel(repository: MockProfileRepository())
        viewModel.editingNeighborhood = "Downtown"

        XCTAssertEqual(viewModel.editingNeighborhood, "Downtown")
    }

    // MARK: - Profile Data Tests

    func testProfileCanBeSet() {
        let viewModel = ProfileViewModel(repository: MockProfileRepository())
        let profile = Profile(id: "test", displayName: "Test User")
        viewModel.profile = profile

        XCTAssertNotNil(viewModel.profile)
        XCTAssertEqual(viewModel.profile?.displayName, "Test User")
    }

    // MARK: - State Flags Tests

    func testLoadingStateCanBeSet() {
        let viewModel = ProfileViewModel(repository: MockProfileRepository())
        viewModel.isLoading = true

        XCTAssertTrue(viewModel.isLoading)
    }

    func testSavingStateCanBeSet() {
        let viewModel = ProfileViewModel(repository: MockProfileRepository())
        viewModel.isSaving = true

        XCTAssertTrue(viewModel.isSaving)
    }

    func testRefreshingStateCanBeSet() {
        let viewModel = ProfileViewModel(repository: MockProfileRepository())
        viewModel.isRefreshing = true

        XCTAssertTrue(viewModel.isRefreshing)
    }

    func testErrorMessageCanBeSet() {
        let viewModel = ProfileViewModel(repository: MockProfileRepository())
        viewModel.errorMessage = "An error occurred"

        XCTAssertEqual(viewModel.errorMessage, "An error occurred")
    }

    // MARK: - Services Set Operations

    func testEditingServicesIsSet() {
        let viewModel = ProfileViewModel(repository: MockProfileRepository())
        viewModel.editingServices = [.tutoring, .babysitting]

        XCTAssertEqual(viewModel.editingServices.count, 2)
    }

    func testEditingServicesNoDuplicates() {
        let viewModel = ProfileViewModel(repository: MockProfileRepository())
        viewModel.editingServices = [.tutoring, .tutoring, .babysitting]

        XCTAssertEqual(viewModel.editingServices.count, 2)
    }
}
