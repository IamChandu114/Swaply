package com.swaply.app.ui.search

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.swaply.app.core.util.Resource
import com.swaply.app.domain.model.Product
import com.swaply.app.domain.repository.ProductRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val repository: ProductRepository = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()
    private val savedStateHandle = SavedStateHandle()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { repository.getRecentSearches() } returns flowOf(listOf("shoes", "phone"))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `query triggers debounced search and saves recent search`() = runTest(testDispatcher) {
        val mockProduct = Product(
            id = 1L,
            title = "iPhone 15",
            description = "Flagship phone",
            price = 999.0,
            discountPercentage = 0.0,
            rating = 4.9,
            stock = 10,
            brand = "Apple",
            category = "smartphones",
            thumbnail = "thumb.png",
            images = emptyList()
        )
        every { repository.searchProducts("iPhone") } returns flowOf(Resource.Success(listOf(mockProduct)))

        val viewModel = SearchViewModel(repository, savedStateHandle)
        viewModel.onQueryChange("iPhone")

        // Before debounce time
        advanceTimeBy(100L)
        coVerify(exactly = 0) { repository.searchProducts(any()) }

        // After debounce time (300ms)
        advanceTimeBy(250L)
        coVerify(exactly = 1) { repository.saveRecentSearch("iPhone") }
        coVerify(exactly = 1) { repository.searchProducts("iPhone") }

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.results.size)
            assertEquals("iPhone 15", state.results[0].title)
            assertFalse(state.isLoading)
        }
    }

    @Test
    fun `blank query resets results without searching`() = runTest(testDispatcher) {
        val viewModel = SearchViewModel(repository, savedStateHandle)
        viewModel.onQueryChange("")

        advanceTimeBy(350L)
        coVerify(exactly = 0) { repository.searchProducts(any()) }

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(0, state.results.size)
            assertFalse(state.isLoading)
        }
    }
}
