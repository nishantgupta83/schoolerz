import XCTest
@testable import Schoolerz

final class ServiceTypeTests: XCTestCase {

    // MARK: - Display Names

    func testServiceTypeDisplayNames() {
        XCTAssertEqual(ServiceType.dogWalking.displayName, "Dog Walking")
        XCTAssertEqual(ServiceType.tutoring.displayName, "Tutoring")
        XCTAssertEqual(ServiceType.babysitting.displayName, "Babysitting")
        XCTAssertEqual(ServiceType.lawnCare.displayName, "Lawn Care")
        XCTAssertEqual(ServiceType.techHelp.displayName, "Tech Help")
        XCTAssertEqual(ServiceType.musicLessons.displayName, "Music Lessons")
        XCTAssertEqual(ServiceType.essayReview.displayName, "Essay Review")
        XCTAssertEqual(ServiceType.carWash.displayName, "Car Wash")
    }

    // MARK: - Icon Names

    func testServiceTypeIconNames() {
        XCTAssertEqual(ServiceType.dogWalking.iconName, "pawprint.fill")
        XCTAssertEqual(ServiceType.tutoring.iconName, "book.fill")
        XCTAssertEqual(ServiceType.babysitting.iconName, "figure.and.child.holdinghands")
        XCTAssertEqual(ServiceType.lawnCare.iconName, "leaf.fill")
        XCTAssertEqual(ServiceType.techHelp.iconName, "desktopcomputer")
        XCTAssertEqual(ServiceType.musicLessons.iconName, "music.note")
        XCTAssertEqual(ServiceType.essayReview.iconName, "doc.text.fill")
        XCTAssertEqual(ServiceType.carWash.iconName, "car.fill")
    }

    // MARK: - All Cases

    func testServiceTypeAllCasesCount() {
        XCTAssertEqual(ServiceType.allCases.count, 8)
    }

    func testServiceTypeAllCasesContainsExpected() {
        let allCases = ServiceType.allCases

        XCTAssertTrue(allCases.contains(.dogWalking))
        XCTAssertTrue(allCases.contains(.tutoring))
        XCTAssertTrue(allCases.contains(.babysitting))
        XCTAssertTrue(allCases.contains(.lawnCare))
        XCTAssertTrue(allCases.contains(.techHelp))
        XCTAssertTrue(allCases.contains(.musicLessons))
        XCTAssertTrue(allCases.contains(.essayReview))
        XCTAssertTrue(allCases.contains(.carWash))
    }

    // MARK: - Raw Values

    func testServiceTypeRawValues() {
        XCTAssertEqual(ServiceType.dogWalking.rawValue, "dog_walking")
        XCTAssertEqual(ServiceType.tutoring.rawValue, "tutoring")
        XCTAssertEqual(ServiceType.babysitting.rawValue, "babysitting")
        XCTAssertEqual(ServiceType.lawnCare.rawValue, "lawn_care")
        XCTAssertEqual(ServiceType.techHelp.rawValue, "tech_help")
        XCTAssertEqual(ServiceType.musicLessons.rawValue, "music_lessons")
        XCTAssertEqual(ServiceType.essayReview.rawValue, "essay_review")
        XCTAssertEqual(ServiceType.carWash.rawValue, "car_wash")
    }

    // MARK: - Decoding

    func testServiceTypeDecodingFromRawValue() {
        XCTAssertEqual(ServiceType(rawValue: "dog_walking"), .dogWalking)
        XCTAssertEqual(ServiceType(rawValue: "tutoring"), .tutoring)
        XCTAssertEqual(ServiceType(rawValue: "babysitting"), .babysitting)
        XCTAssertEqual(ServiceType(rawValue: "lawn_care"), .lawnCare)
        XCTAssertEqual(ServiceType(rawValue: "tech_help"), .techHelp)
        XCTAssertEqual(ServiceType(rawValue: "music_lessons"), .musicLessons)
        XCTAssertEqual(ServiceType(rawValue: "essay_review"), .essayReview)
        XCTAssertEqual(ServiceType(rawValue: "car_wash"), .carWash)
    }

    func testServiceTypeInvalidRawValueReturnsNil() {
        XCTAssertNil(ServiceType(rawValue: "invalid"))
        XCTAssertNil(ServiceType(rawValue: ""))
        XCTAssertNil(ServiceType(rawValue: "Dog Walking")) // Case sensitive
    }

    // MARK: - Equatable

    func testServiceTypeEquality() {
        XCTAssertEqual(ServiceType.dogWalking, ServiceType.dogWalking)
        XCTAssertNotEqual(ServiceType.dogWalking, ServiceType.tutoring)
    }

    // MARK: - Hashable

    func testServiceTypeHashable() {
        var set = Set<ServiceType>()
        set.insert(.dogWalking)
        set.insert(.tutoring)
        set.insert(.dogWalking) // Duplicate

        XCTAssertEqual(set.count, 2)
    }
}
