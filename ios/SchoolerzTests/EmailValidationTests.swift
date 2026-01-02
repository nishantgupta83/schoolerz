import XCTest
@testable import Schoolerz

final class EmailValidationTests: XCTestCase {

    func testValidSchoolEmailReturnsTrue() {
        XCTAssertTrue(SchoolDomainWhitelist.isApprovedDomain("john@student.edu"))
        XCTAssertTrue(SchoolDomainWhitelist.isApprovedDomain("jane.doe@k12.ca.us"))
        XCTAssertTrue(SchoolDomainWhitelist.isApprovedDomain("student123@lausd.net"))
    }

    func testInvalidDomainReturnsFalse() {
        XCTAssertFalse(SchoolDomainWhitelist.isApprovedDomain("john@gmail.com"))
        XCTAssertFalse(SchoolDomainWhitelist.isApprovedDomain("jane@yahoo.com"))
        XCTAssertFalse(SchoolDomainWhitelist.isApprovedDomain("student@hotmail.com"))
    }

    func testEmptyEmailReturnsFalse() {
        XCTAssertFalse(SchoolDomainWhitelist.isApprovedDomain(""))
        XCTAssertFalse(SchoolDomainWhitelist.isApprovedDomain("   "))
    }

    func testEmailWithoutAtSignReturnsFalse() {
        XCTAssertFalse(SchoolDomainWhitelist.isApprovedDomain("johngmail.com"))
        XCTAssertFalse(SchoolDomainWhitelist.isApprovedDomain("noemail"))
    }

    func testApprovedDomainsListExists() {
        XCTAssertFalse(SchoolDomainWhitelist.approvedDomains.isEmpty)
        XCTAssertTrue(SchoolDomainWhitelist.approvedDomains.count > 5)
    }

    func testDomainExtraction() {
        let email = "test@student.edu"
        let domain = email.split(separator: "@").last.map(String.init) ?? ""
        XCTAssertEqual(domain, "student.edu")
    }

    // MARK: - Case Insensitivity Tests

    func testCaseInsensitiveDomainMatching() {
        XCTAssertTrue(SchoolDomainWhitelist.isApprovedDomain("john@STUDENT.EDU"))
        XCTAssertTrue(SchoolDomainWhitelist.isApprovedDomain("john@Student.Edu"))
        XCTAssertTrue(SchoolDomainWhitelist.isApprovedDomain("john@K12.CA.US"))
    }

    // MARK: - Subdomain Tests

    func testSubdomainOfApprovedDomainIsValid() {
        XCTAssertTrue(SchoolDomainWhitelist.isApprovedDomain("john@mail.student.edu"))
        XCTAssertTrue(SchoolDomainWhitelist.isApprovedDomain("john@students.k12.ca.us"))
    }

    // MARK: - Edge Cases

    func testMultipleAtSymbolsReturnsFalse() {
        XCTAssertFalse(SchoolDomainWhitelist.isApprovedDomain("john@@student.edu"))
        XCTAssertFalse(SchoolDomainWhitelist.isApprovedDomain("john@test@student.edu"))
    }

    func testEmailWithSpecialCharactersInLocalPart() {
        XCTAssertTrue(SchoolDomainWhitelist.isApprovedDomain("john.doe+tag@student.edu"))
        XCTAssertTrue(SchoolDomainWhitelist.isApprovedDomain("john_doe@student.edu"))
    }

    func testSimilarButNotApprovedDomainReturnsFalse() {
        XCTAssertFalse(SchoolDomainWhitelist.isApprovedDomain("john@studentedu.com"))
        XCTAssertFalse(SchoolDomainWhitelist.isApprovedDomain("john@fakestudent.edu"))
    }

    func testAllApprovedDomainsWork() {
        for domain in SchoolDomainWhitelist.approvedDomains {
            XCTAssertTrue(
                SchoolDomainWhitelist.isApprovedDomain("test@\(domain)"),
                "Domain \(domain) should be valid"
            )
        }
    }

    // MARK: - Domain List Tests

    func testApprovedDomainsContainsExpectedValues() {
        let domains = SchoolDomainWhitelist.approvedDomains
        XCTAssertTrue(domains.contains("student.edu"))
        XCTAssertTrue(domains.contains("k12.ca.us"))
        XCTAssertTrue(domains.contains("lausd.net"))
    }

    func testApprovedDomainsAreUnique() {
        let domains = SchoolDomainWhitelist.approvedDomains
        let uniqueDomains = Set(domains)
        XCTAssertEqual(domains.count, uniqueDomains.count)
    }

    func testApprovedDomainsAreNotEmpty() {
        for domain in SchoolDomainWhitelist.approvedDomains {
            XCTAssertFalse(domain.isEmpty)
        }
    }
}
