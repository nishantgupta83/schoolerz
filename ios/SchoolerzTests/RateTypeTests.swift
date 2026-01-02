import XCTest
@testable import Schoolerz

final class RateTypeTests: XCTestCase {

    // MARK: - RateType Format Price Tests

    func testHourlyRateFormatsSingleAmount() {
        let formatted = RateType.hourly.formatPrice(amount: 15.0)
        XCTAssertEqual(formatted, "$15/hour")
    }

    func testHourlyRateFormatsRange() {
        let formatted = RateType.hourly.formatPrice(amount: 15.0, max: 25.0)
        XCTAssertEqual(formatted, "$15-25/hour")
    }

    func testPerTaskRateFormatsSingleAmount() {
        let formatted = RateType.perTask.formatPrice(amount: 30.0)
        XCTAssertEqual(formatted, "$30/task")
    }

    func testPerTaskRateFormatsRange() {
        let formatted = RateType.perTask.formatPrice(amount: 30.0, max: 50.0)
        XCTAssertEqual(formatted, "$30-50/task")
    }

    func testNegotiableReturnsNegotiable() {
        let formatted = RateType.negotiable.formatPrice(amount: nil)
        XCTAssertEqual(formatted, "Negotiable")
    }

    func testNegotiableIgnoresAmount() {
        let formatted = RateType.negotiable.formatPrice(amount: 20.0)
        XCTAssertEqual(formatted, "Negotiable")
    }

    func testNilAmountReturnsNegotiable() {
        let formatted = RateType.hourly.formatPrice(amount: nil)
        XCTAssertEqual(formatted, "Negotiable")
    }

    func testZeroAmountFormatsCorrectly() {
        let formatted = RateType.hourly.formatPrice(amount: 0.0)
        XCTAssertEqual(formatted, "Free")
    }

    // MARK: - RateType Raw Values

    func testHourlyRawValue() {
        XCTAssertEqual(RateType.hourly.rawValue, "hourly")
    }

    func testPerTaskRawValue() {
        XCTAssertEqual(RateType.perTask.rawValue, "per_task")
    }

    func testNegotiableRawValue() {
        XCTAssertEqual(RateType.negotiable.rawValue, "negotiable")
    }

    // MARK: - RateType Decoding

    func testRateTypeDecodingFromRawValue() {
        XCTAssertEqual(RateType(rawValue: "hourly"), .hourly)
        XCTAssertEqual(RateType(rawValue: "per_task"), .perTask)
        XCTAssertEqual(RateType(rawValue: "negotiable"), .negotiable)
    }

    func testRateTypeInvalidRawValueReturnsNil() {
        XCTAssertNil(RateType(rawValue: "invalid"))
    }

    // MARK: - ExperienceLevel Tests

    func testExperienceLevelDisplayNames() {
        XCTAssertEqual(ExperienceLevel.beginner.displayName, "Beginner")
        XCTAssertEqual(ExperienceLevel.intermediate.displayName, "Intermediate")
        XCTAssertEqual(ExperienceLevel.experienced.displayName, "Experienced")
    }

    func testExperienceLevelBadgeColors() {
        XCTAssertFalse(ExperienceLevel.beginner.badgeColor.isEmpty)
        XCTAssertFalse(ExperienceLevel.intermediate.badgeColor.isEmpty)
        XCTAssertFalse(ExperienceLevel.experienced.badgeColor.isEmpty)
    }

    func testExperienceLevelRawValues() {
        XCTAssertEqual(ExperienceLevel.beginner.rawValue, "beginner")
        XCTAssertEqual(ExperienceLevel.intermediate.rawValue, "intermediate")
        XCTAssertEqual(ExperienceLevel.experienced.rawValue, "experienced")
    }
}
