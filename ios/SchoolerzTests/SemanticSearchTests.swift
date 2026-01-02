import XCTest
@testable import Schoolerz

final class SemanticSearchTests: XCTestCase {

    // MARK: - Query Expansion Tests

    func testExpandQueryWithDogWalking() {
        let expanded = SemanticSearch.shared.expandQuery("dog walking")

        XCTAssertTrue(expanded.contains("dog walking"))
        XCTAssertTrue(expanded.contains("dog walker"))
        XCTAssertTrue(expanded.contains("pet care"))
        XCTAssertTrue(expanded.contains("walk my dog"))
    }

    func testExpandQueryWithTutoring() {
        let expanded = SemanticSearch.shared.expandQuery("tutoring")

        XCTAssertTrue(expanded.contains("tutoring"))
        XCTAssertTrue(expanded.contains("tutor"))
        XCTAssertTrue(expanded.contains("homework help"))
        XCTAssertTrue(expanded.contains("math help"))
    }

    func testExpandQueryWithBabysitting() {
        let expanded = SemanticSearch.shared.expandQuery("babysitting")

        XCTAssertTrue(expanded.contains("babysitting"))
        XCTAssertTrue(expanded.contains("babysitter"))
        XCTAssertTrue(expanded.contains("childcare"))
        XCTAssertTrue(expanded.contains("nanny"))
    }

    func testExpandQueryWithNoSynonyms() {
        let expanded = SemanticSearch.shared.expandQuery("random unique query")

        XCTAssertEqual(expanded.count, 1)
        XCTAssertTrue(expanded.contains("random unique query"))
    }

    // MARK: - Intent Detection Tests

    func testDetectIntentOffer() {
        XCTAssertEqual(SemanticSearch.shared.detectIntent("I am available for tutoring"), .offer)
        XCTAssertEqual(SemanticSearch.shared.detectIntent("offering dog walking"), .offer)
        XCTAssertEqual(SemanticSearch.shared.detectIntent("I can help with lawn care"), .offer)
        XCTAssertEqual(SemanticSearch.shared.detectIntent("providing babysitting services"), .offer)
    }

    func testDetectIntentRequest() {
        XCTAssertEqual(SemanticSearch.shared.detectIntent("I need a tutor"), .request)
        XCTAssertEqual(SemanticSearch.shared.detectIntent("looking for dog walker"), .request)
        XCTAssertEqual(SemanticSearch.shared.detectIntent("searching for babysitter"), .request)
        XCTAssertEqual(SemanticSearch.shared.detectIntent("help wanted for lawn care"), .request)
    }

    func testDetectIntentNeutral() {
        XCTAssertNil(SemanticSearch.shared.detectIntent("dog walking"))
        XCTAssertNil(SemanticSearch.shared.detectIntent("tutoring services"))
        XCTAssertNil(SemanticSearch.shared.detectIntent("babysitting"))
    }

    // MARK: - Service Type Detection Tests

    func testDetectServiceTypeDogWalking() {
        XCTAssertEqual(SemanticSearch.shared.detectServiceType("walk my dog"), .dogWalking)
        XCTAssertEqual(SemanticSearch.shared.detectServiceType("need pet care"), .dogWalking)
        XCTAssertEqual(SemanticSearch.shared.detectServiceType("looking for puppy sitter"), .dogWalking)
    }

    func testDetectServiceTypeTutoring() {
        XCTAssertEqual(SemanticSearch.shared.detectServiceType("math tutor needed"), .tutoring)
        XCTAssertEqual(SemanticSearch.shared.detectServiceType("homework help"), .tutoring)
        XCTAssertEqual(SemanticSearch.shared.detectServiceType("test prep"), .tutoring)
    }

    func testDetectServiceTypeBabysitting() {
        XCTAssertEqual(SemanticSearch.shared.detectServiceType("need babysitter"), .babysitting)
        XCTAssertEqual(SemanticSearch.shared.detectServiceType("childcare needed"), .babysitting)
        XCTAssertEqual(SemanticSearch.shared.detectServiceType("looking for nanny"), .babysitting)
    }

    func testDetectServiceTypeLawnCare() {
        XCTAssertEqual(SemanticSearch.shared.detectServiceType("lawn mowing"), .lawnCare)
        XCTAssertEqual(SemanticSearch.shared.detectServiceType("yard work"), .lawnCare)
        XCTAssertEqual(SemanticSearch.shared.detectServiceType("grass cutting"), .lawnCare)
    }

    func testDetectServiceTypeTechHelp() {
        XCTAssertEqual(SemanticSearch.shared.detectServiceType("computer help"), .techHelp)
        XCTAssertEqual(SemanticSearch.shared.detectServiceType("phone repair"), .techHelp)
        XCTAssertEqual(SemanticSearch.shared.detectServiceType("laptop assistance"), .techHelp)
    }

    func testDetectServiceTypeNone() {
        XCTAssertNil(SemanticSearch.shared.detectServiceType("random service"))
        XCTAssertNil(SemanticSearch.shared.detectServiceType("something else"))
    }

    // MARK: - Match Score Tests

    func testMatchScoreExactMatch() {
        let post = Post(
            type: .offer,
            authorId: "1",
            authorName: "John",
            neighborhood: "Downtown",
            body: "Offering dog walking services"
        )

        let score = SemanticSearch.shared.matchScore(query: "dog walking", post: post)
        XCTAssertGreaterThan(score, 1.0) // Exact match + synonyms
    }

    func testMatchScoreSynonymMatch() {
        let post = Post(
            type: .offer,
            authorId: "1",
            authorName: "John",
            neighborhood: "Downtown",
            body: "I can walk your pet"
        )

        let score = SemanticSearch.shared.matchScore(query: "dog walking", post: post)
        XCTAssertGreaterThan(score, 0.0)
    }

    func testMatchScoreNoMatch() {
        let post = Post(
            type: .offer,
            authorId: "1",
            authorName: "John",
            neighborhood: "Downtown",
            body: "Offering lawn mowing services"
        )

        let score = SemanticSearch.shared.matchScore(query: "babysitting", post: post)
        XCTAssertEqual(score, 0.0)
    }

    func testMatchScoreAuthorMatch() {
        let post = Post(
            type: .offer,
            authorId: "1",
            authorName: "John Smith",
            neighborhood: "Downtown",
            body: "Offering services"
        )

        let score = SemanticSearch.shared.matchScore(query: "john", post: post)
        XCTAssertGreaterThan(score, 0.0)
    }

    func testMatchScoreNeighborhoodMatch() {
        let post = Post(
            type: .offer,
            authorId: "1",
            authorName: "John",
            neighborhood: "Downtown",
            body: "Offering services"
        )

        let score = SemanticSearch.shared.matchScore(query: "downtown", post: post)
        XCTAssertGreaterThan(score, 0.0)
    }

    func testMatchScoreEmptyQuery() {
        let post = Post(
            type: .offer,
            authorId: "1",
            authorName: "John",
            neighborhood: "Downtown",
            body: "Offering dog walking"
        )

        let score = SemanticSearch.shared.matchScore(query: "", post: post)
        XCTAssertEqual(score, 1.0)
    }

    // MARK: - Ranking Tests

    func testRankPostsByRelevance() {
        let posts = [
            Post(type: .offer, authorId: "1", authorName: "A", neighborhood: "X", body: "Lawn care services"),
            Post(type: .offer, authorId: "2", authorName: "B", neighborhood: "Y", body: "Dog walking available"),
            Post(type: .offer, authorId: "3", authorName: "C", neighborhood: "Z", body: "Dog walker with experience")
        ]

        let ranked = SemanticSearch.shared.rankPosts(posts, query: "dog walking")

        XCTAssertEqual(ranked.count, 2)
        XCTAssertEqual(ranked[0].authorName, "B") // Higher score for exact match
    }

    func testRankPostsEmptyQuery() {
        let posts = [
            Post(type: .offer, authorId: "1", authorName: "A", neighborhood: "X", body: "Service A"),
            Post(type: .offer, authorId: "2", authorName: "B", neighborhood: "Y", body: "Service B")
        ]

        let ranked = SemanticSearch.shared.rankPosts(posts, query: "")

        XCTAssertEqual(ranked.count, 2)
    }

    // MARK: - Search Posts Integration Tests

    func testSearchPostsWithQuery() {
        let posts = [
            Post(type: .offer, authorId: "1", authorName: "A", neighborhood: "X", body: "Dog walking"),
            Post(type: .request, authorId: "2", authorName: "B", neighborhood: "Y", body: "Need tutor"),
            Post(type: .offer, authorId: "3", authorName: "C", neighborhood: "Z", body: "Babysitting")
        ]

        let results = SemanticSearch.shared.searchPosts(posts, query: "dog", serviceType: nil, postType: nil)

        XCTAssertEqual(results.count, 1)
        XCTAssertEqual(results[0].body, "Dog walking")
    }

    func testSearchPostsWithServiceFilter() {
        let posts = [
            Post(type: .offer, authorId: "1", authorName: "A", neighborhood: "X", body: "Dog walking", serviceType: .dogWalking),
            Post(type: .offer, authorId: "2", authorName: "B", neighborhood: "Y", body: "Tutoring", serviceType: .tutoring),
            Post(type: .offer, authorId: "3", authorName: "C", neighborhood: "Z", body: "Pet care", serviceType: .dogWalking)
        ]

        let results = SemanticSearch.shared.searchPosts(posts, query: "", serviceType: .dogWalking, postType: nil)

        XCTAssertEqual(results.count, 2)
    }

    func testSearchPostsWithPostTypeFilter() {
        let posts = [
            Post(type: .offer, authorId: "1", authorName: "A", neighborhood: "X", body: "Offering service"),
            Post(type: .request, authorId: "2", authorName: "B", neighborhood: "Y", body: "Need help"),
            Post(type: .offer, authorId: "3", authorName: "C", neighborhood: "Z", body: "Available")
        ]

        let results = SemanticSearch.shared.searchPosts(posts, query: "", serviceType: nil, postType: .request)

        XCTAssertEqual(results.count, 1)
        XCTAssertEqual(results[0].type, .request)
    }

    func testSearchPostsWithCombinedFilters() {
        let posts = [
            Post(type: .offer, authorId: "1", authorName: "A", neighborhood: "X", body: "Dog walking", serviceType: .dogWalking),
            Post(type: .request, authorId: "2", authorName: "B", neighborhood: "Y", body: "Need dog walker", serviceType: .dogWalking),
            Post(type: .offer, authorId: "3", authorName: "C", neighborhood: "Z", body: "Tutoring", serviceType: .tutoring)
        ]

        let results = SemanticSearch.shared.searchPosts(posts, query: "dog", serviceType: .dogWalking, postType: .offer)

        XCTAssertEqual(results.count, 1)
        XCTAssertEqual(results[0].authorName, "A")
    }
}
