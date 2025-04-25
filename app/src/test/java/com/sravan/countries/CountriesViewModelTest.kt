package com.sravan.countries.presentation

import app.cash.turbine.test
import com.sravan.countries.domain.model.Country
import com.sravan.countries.domain.use_case.GetCountriesUseCase
import com.sravan.countries.domain.util.Result
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class CountriesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getCountriesUseCase: GetCountriesUseCase
    private lateinit var viewModel: CountriesViewModel

    // Sample test data
    private val sampleCountries = listOf(
        Country(
            name = "United States",
            region = "Americas",
            code = "US",
            capital = "Washington, D.C."
        ),
        Country(
            name = "Germany",
            region = "Europe",
            code = "DE",
            capital = "Berlin"
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getCountriesUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init should load countries and update state with success`() = runTest {
        // Given
        coEvery { getCountriesUseCase(any()) } returns flowOf(Result.Success(sampleCountries))

        // When
        viewModel = CountriesViewModel(getCountriesUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(sampleCountries, viewModel.state.value.countries)
        assertEquals(false, viewModel.state.value.isLoading)
        assertEquals("", viewModel.state.value.error)
    }

    @Test
    fun `init should update state with error when use case returns error`() = runTest {
        // Given
        val errorMessage = "Network error"
        coEvery { getCountriesUseCase(any()) } returns flowOf(Result.Error(errorMessage))

        // When
        viewModel = CountriesViewModel(getCountriesUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(emptyList<Country>(), viewModel.state.value.countries)
        assertEquals(false, viewModel.state.value.isLoading)
        assertEquals(errorMessage, viewModel.state.value.error)
    }

    @Test
    fun `refreshCountries should force refresh from repository`() = runTest {
        // Given
        val initialCountries = sampleCountries
        val refreshedCountries = sampleCountries.reversed()

        // Setup the use case to return different results based on the forceRefresh parameter
        coEvery { getCountriesUseCase(false) } returns flowOf(Result.Success(initialCountries))
        coEvery { getCountriesUseCase(true) } returns flowOf(Result.Success(refreshedCountries))

        // When - Initial load
        viewModel = CountriesViewModel(getCountriesUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - Check initial state
        assertEquals(initialCountries, viewModel.state.value.countries)

        // When - Refresh
        viewModel.refreshCountries()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - Check refreshed state
        assertEquals(refreshedCountries, viewModel.state.value.countries)
        assertEquals(false, viewModel.state.value.isLoading)
    }

    @Test
    fun `exception in flow should be caught and update error state`() = runTest {
        // Given
        val exception = RuntimeException("Test exception")
        coEvery { getCountriesUseCase(any()) } returns flow { throw exception }

        // When
        viewModel = CountriesViewModel(getCountriesUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(viewModel.state.value.error.isNotEmpty())
        assertEquals("Test exception", viewModel.state.value.error)
        assertEquals(false, viewModel.state.value.isLoading)
    }
}