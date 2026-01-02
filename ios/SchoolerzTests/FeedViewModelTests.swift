import XCTest
@testable import Schoolerz

@MainActor
final class FeedViewModelTests: XCTestCase {

    var viewModel: FeedViewModel!

    override func setUp() {
        super.setUp()
        // Use mock repository
        viewModel = FeedViewModel()
    }

    override func tearDown() {
        viewModel = nil
        super.tearDown()
    }

    func testInitialState() {
        XCTAssertFalse(viewModel.isLoading)
        XCTAssertNil(viewModel.error)
        XCTAssertNil(viewModel.selectedFilter)
    }

    func testFetchPostsLoadsData() async {
        await viewModel.fetchPosts()

        XCTAssertFalse(viewModel.isLoading)
        XCTAssertFalse(viewModel.posts.isEmpty)
    }

    func testSetFilterUpdatesState() {
        viewModel.setFilter(.offer)
        XCTAssertEqual(viewModel.selectedFilter, .offer)

        viewModel.setFilter(.request)
        XCTAssertEqual(viewModel.selectedFilter, .request)

        viewModel.setFilter(nil)
        XCTAssertNil(viewModel.selectedFilter)
    }

    func testFilteredPostsReturnsAllWhenNoFilter() async {
        await viewModel.fetchPosts()

        let totalCount = viewModel.posts.count
        let filteredCount = viewModel.filteredPosts.count

        XCTAssertEqual(totalCount, filteredCount)
    }

    func testFilteredPostsFiltersCorrectly() async {
        await viewModel.fetchPosts()

        viewModel.setFilter(.offer)
        let offers = viewModel.filteredPosts

        for post in offers {
            XCTAssertEqual(post.type, .offer)
        }

        viewModel.setFilter(.request)
        let requests = viewModel.filteredPosts

        for post in requests {
            XCTAssertEqual(post.type, .request)
        }
    }

    func testClearErrorSetsNil() {
        // Simulate error state
        viewModel.error = "Test error"
        XCTAssertNotNil(viewModel.error)

        viewModel.clearError()
        XCTAssertNil(viewModel.error)
    }

    func testRefreshPostsWorks() async {
        await viewModel.fetchPosts()
        let initialPosts = viewModel.posts

        await viewModel.refreshPosts()

        XCTAssertFalse(viewModel.isRefreshing)
        XCTAssertFalse(viewModel.posts.isEmpty)
    }

    // MARK: - Search Tests

    func testSearchQueryInitiallyEmpty() {
        XCTAssertEqual(viewModel.searchQuery, "")
    }

    func testSearchQueryUpdates() {
        viewModel.searchQuery = "dog walking"
        XCTAssertEqual(viewModel.searchQuery, "dog walking")
    }

    func testClearSearchResetsQuery() {
        viewModel.searchQuery = "test query"
        viewModel.clearSearch()
        XCTAssertEqual(viewModel.searchQuery, "")
    }

    func testSearchFiltersResults() async {
        await viewModel.fetchPosts()

        viewModel.searchQuery = "tech"
        let results = viewModel.filteredPosts

        // Should find posts matching "tech" via semantic search
        XCTAssertTrue(results.count <= viewModel.posts.count)
    }

    // MARK: - Service Filter Tests

    func testSelectedServiceInitiallyNil() {
        XCTAssertNil(viewModel.selectedService)
    }

    func testOnServiceSelectSetsService() {
        viewModel.onServiceSelect(.dogWalking)
        XCTAssertEqual(viewModel.selectedService, .dogWalking)
    }

    func testOnServiceSelectTogglesSameService() {
        viewModel.onServiceSelect(.dogWalking)
        XCTAssertEqual(viewModel.selectedService, .dogWalking)

        viewModel.onServiceSelect(.dogWalking)
        XCTAssertNil(viewModel.selectedService)
    }

    func testOnServiceSelectChangesService() {
        viewModel.onServiceSelect(.dogWalking)
        XCTAssertEqual(viewModel.selectedService, .dogWalking)

        viewModel.onServiceSelect(.tutoring)
        XCTAssertEqual(viewModel.selectedService, .tutoring)
    }

    func testOnServiceSelectNilClearsFilter() {
        viewModel.onServiceSelect(.dogWalking)
        XCTAssertNotNil(viewModel.selectedService)

        viewModel.onServiceSelect(nil)
        XCTAssertNil(viewModel.selectedService)
    }

    func testServiceFilterFiltersResults() async {
        await viewModel.fetchPosts()

        viewModel.onServiceSelect(.dogWalking)
        let results = viewModel.filteredPosts

        for post in results {
            XCTAssertEqual(post.serviceType, .dogWalking)
        }
    }

    // MARK: - Combined Filter Tests

    func testClearAllFiltersResetsEverything() {
        viewModel.searchQuery = "test"
        viewModel.onServiceSelect(.dogWalking)
        viewModel.selectedFilter = .offer

        viewModel.clearAllFilters()

        XCTAssertEqual(viewModel.searchQuery, "")
        XCTAssertNil(viewModel.selectedService)
        XCTAssertNil(viewModel.selectedFilter)
    }

    func testHasActiveFiltersWithSearchQuery() {
        XCTAssertFalse(viewModel.hasActiveFilters)

        viewModel.searchQuery = "test"
        XCTAssertTrue(viewModel.hasActiveFilters)
    }

    func testHasActiveFiltersWithServiceFilter() {
        XCTAssertFalse(viewModel.hasActiveFilters)

        viewModel.onServiceSelect(.tutoring)
        XCTAssertTrue(viewModel.hasActiveFilters)
    }

    func testHasActiveFiltersWithPostTypeFilter() {
        XCTAssertFalse(viewModel.hasActiveFilters)

        viewModel.selectedFilter = .offer
        XCTAssertTrue(viewModel.hasActiveFilters)
    }

    func testCombinedFiltersApply() async {
        await viewModel.fetchPosts()

        viewModel.selectedFilter = .offer
        viewModel.onServiceSelect(.techHelp)

        let results = viewModel.filteredPosts

        for post in results {
            XCTAssertEqual(post.type, .offer)
            XCTAssertEqual(post.serviceType, .techHelp)
        }
    }

    // MARK: - Error Handling Tests

    func testErrorMessageInitiallyNil() {
        XCTAssertNil(viewModel.errorMessage)
    }

    func testClearErrorClearsMessage() {
        viewModel.errorMessage = "Network error"
        XCTAssertNotNil(viewModel.errorMessage)

        viewModel.clearError()
        XCTAssertNil(viewModel.errorMessage)
    }
}
