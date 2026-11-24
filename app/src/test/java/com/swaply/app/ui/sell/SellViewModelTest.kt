package com.swaply.app.ui.sell

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.swaply.app.core.util.Resource
import com.swaply.app.domain.model.DraftProduct
import com.swaply.app.domain.repository.ProductRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SellViewModelTest {

    private val repository: ProductRepository = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()
    private val savedStateHandle = SavedStateHandle()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { repository.getDraft() } returns flowOf(null)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `validation errors appear when submitting empty form`() = runTest(testDispatcher) {
        val viewModel = SellViewModel(repository, savedStateHandle)
        advanceUntilIdle()

        viewModel.submit()

        val state = viewModel.formState.value
        assertNotNull(state.titleError)
        assertNotNull(state.priceError)
        assertNotNull(state.descriptionError)
        coVerify(exactly = 0) { repository.publishProduct(any()) }
    }

    @Test
    fun `valid submission calls repository and emits publish success`() = runTest(testDispatcher) {
        coEvery { repository.publishProduct(any()) } returns Resource.Success(999L)

        val viewModel = SellViewModel(repository, savedStateHandle)
        advanceUntilIdle()

        viewModel.onTitleChange("Vintage Camera")
        viewModel.onPriceChange("150.0")
        viewModel.onDescriptionChange("Fully functional 35mm film camera in mint condition")

        viewModel.events.test {
            viewModel.submit()
            advanceUntilIdle()

            val event = awaitItem()
            assert(event is SellUiEvent.PublishSuccess)
            assertEquals(999L, (event as SellUiEvent.PublishSuccess).productId)
        }

        coVerify(exactly = 1) { repository.publishProduct(any()) }
    }
}
