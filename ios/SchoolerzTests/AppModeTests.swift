import XCTest
@testable import Schoolerz

final class AppModeTests: XCTestCase {

    // MARK: - Enum Values

    func testAppModeHasMockCase() {
        let mode = AppMode.mock
        XCTAssertNotNil(mode)
    }

    func testAppModeHasFirebaseCase() {
        let mode = AppMode.firebase
        XCTAssertNotNil(mode)
    }

    // MARK: - shouldInitializeFirebase

    func testMockModeShouldNotInitializeFirebase() {
        let mode = AppMode.mock
        XCTAssertFalse(mode.shouldInitializeFirebase)
    }

    func testFirebaseModeShouldInitializeFirebase() {
        let mode = AppMode.firebase
        XCTAssertTrue(mode.shouldInitializeFirebase)
    }

    // MARK: - Description

    func testMockModeDescription() {
        let mode = AppMode.mock
        XCTAssertEqual(mode.description, "Mock Mode")
    }

    func testFirebaseModeDescription() {
        let mode = AppMode.firebase
        XCTAssertEqual(mode.description, "Firebase Mode")
    }

    func testDescriptionIsNotEmpty() {
        XCTAssertFalse(AppMode.mock.description.isEmpty)
        XCTAssertFalse(AppMode.firebase.description.isEmpty)
    }

    // MARK: - Equality

    func testAppModeEquality() {
        XCTAssertEqual(AppMode.mock, AppMode.mock)
        XCTAssertEqual(AppMode.firebase, AppMode.firebase)
        XCTAssertNotEqual(AppMode.mock, AppMode.firebase)
    }

    // MARK: - Switch Expression

    func testAppModeCanBeUsedInSwitch() {
        let mode = AppMode.mock
        var result: String

        switch mode {
        case .mock:
            result = "mock"
        case .firebase:
            result = "firebase"
        }

        XCTAssertEqual(result, "mock")
    }

    func testAppModeFirebaseInSwitch() {
        let mode = AppMode.firebase
        var result: String

        switch mode {
        case .mock:
            result = "mock"
        case .firebase:
            result = "firebase"
        }

        XCTAssertEqual(result, "firebase")
    }
}
