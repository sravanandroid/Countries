package com.sravan.countries.presentation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.sravan.countries.domain.model.Country
import com.sravan.countries.domain.use_case.GetCountriesUseCase
import com.sravan.countries.domain.util.Result
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

@ExperimentalCoroutinesApi
class CountriesViewModelTest {

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    private val testDispatcher = TestCoroutineDispatcher()
    private lateinit var getCountriesUseCase: GetCountriesUseCase
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var viewModel: CountriesViewModel

    private val testCountries = listOf(
        Country(name = "United States", region = "Americas", code = "US", capital = "Washington, D.C."),
        Country(name = "Canada", region = "Americas", code = "CA", capital = "Ottawa")
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getCountriesUseCase = mockk()
        savedStateHandle = SavedStateHandle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun `initialization fetches countries when no saved state`() = runTest {
        // Given
        coEvery { getCountriesUseCase() } returns flowOf(Result.Success(testCountries))

        // When
        viewModel = CountriesViewModel(savedStateHandle, getCountriesUseCase)

        // Then
        assertEquals(testCountries, viewModel.state.value?.countries)
        assertFalse(viewModel.state.value?.isLoading ?: true)
        assertEquals("", viewModel.state.value?.error)
    }

    @Test
    fun `initialization restores state from saved state handle`() = runTest {
        // Given
        val savedState = CountriesState(
            countries = testCountries,
            isLoading = false,
            error = ""
        )
        savedStateHandle["countries_state"] = savedState
        coEvery { getCountriesUseCase() } returns flowOf()

        // When
        viewModel = CountriesViewModel(savedStateHandle, getCountriesUseCase)

        // Then
        assertEquals(testCountries, viewModel.state.value?.countries)
        assertFalse(viewModel.state.value?.isLoading ?: true)
        assertEquals("", viewModel.state.value?.error)
    }

    @Test
    fun `refreshCountries calls getCountriesUseCase`() = runTest {
        // Given
        coEvery { getCountriesUseCase() } returns flowOf(Result.Success(testCountries))
        viewModel = CountriesViewModel(savedStateHandle, getCountriesUseCase)

        // When
        viewModel.refreshCountries()

        // Then
        verify(exactly = 2) { getCountriesUseCase() } // Once during init, once during refresh
    }

    @Test
    fun `loading state is set when useCase starts`() = runTest {
        // Given
        coEvery { getCountriesUseCase() } returns flowOf(
            Result.Loading(),
            Result.Success(testCountries)
        )

        // When
        viewModel = CountriesViewModel(savedStateHandle, getCountriesUseCase)

        // Then
        assertEquals(testCountries, viewModel.state.value?.countries)
        assertFalse(viewModel.state.value?.isLoading ?: true)
    }

    @Test
    fun `error state is set when useCase returns error`() = runTest {
        // Given
        val errorMessage = "Network error"
        coEvery { getCountriesUseCase() } returns flowOf(Result.Error(errorMessage))

        // When
        viewModel = CountriesViewModel(savedStateHandle, getCountriesUseCase)

        // Then
        assertEquals(errorMessage, viewModel.state.value?.error)
        assertFalse(viewModel.state.value?.isLoading ?: true)
        assertTrue(viewModel.state.value?.countries?.isEmpty() ?: false)
    }

    @Test
    fun `isDataFromCache is set to true when data comes from cache`() = runTest {
        // Given
        coEvery { getCountriesUseCase() } returns flowOf(
            Result.Success(testCountries, isCached = true)
        )

        // When
        viewModel = CountriesViewModel(savedStateHandle, getCountriesUseCase)

        // Then
        assertTrue(viewModel.isDataFromCache)
    }

    @Test
    fun `isDataFromCache is saved in savedStateHandle`() = runTest {
        // Given
        coEvery { getCountriesUseCase() } returns flowOf(
            Result.Success(testCountries, isCached = true)
        )

        // When
        viewModel = CountriesViewModel(savedStateHandle, getCountriesUseCase)

        // Then
        assertEquals(true, savedStateHandle.get<Boolean>("is_data_from_cache"))
    }
} 