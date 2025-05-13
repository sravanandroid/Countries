package com.sravan.countries.domain.use_case

import app.cash.turbine.test
import com.sravan.countries.domain.model.Country
import com.sravan.countries.domain.repository.CountryRepository
import com.sravan.countries.domain.util.Result
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.time.ExperimentalTime

@ExperimentalTime
class GetCountriesUseCaseTest {

    private lateinit var repository: CountryRepository
    private lateinit var useCase: GetCountriesUseCase

    private val testCountries = listOf(
        Country(name = "United States", region = "Americas", code = "US", capital = "Washington, D.C."),
        Country(name = "Canada", region = "Americas", code = "CA", capital = "Ottawa")
    )

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetCountriesUseCase(repository)
    }

    @Test
    fun `invoke should return countries from repository`() = runTest {
        // Given
        every { repository.getCountries() } returns flowOf(Result.Success(testCountries))

        // When & Then
        useCase().test {
            val result = awaitItem()
            assertTrue(result is Result.Success)
            assertEquals(testCountries, (result as Result.Success).data)
            awaitComplete()
        }

        verify { repository.getCountries() }
    }

    @Test
    fun `invoke should return error from repository`() = runTest {
        // Given
        val errorMessage = "Failed to fetch countries"
        every { repository.getCountries() } returns flowOf(Result.Error(errorMessage))

        // When & Then
        useCase().test {
            val result = awaitItem()
            assertTrue(result is Result.Error)
            assertEquals(errorMessage, (result as Result.Error).message)
            awaitComplete()
        }

        verify { repository.getCountries() }
    }

    @Test
    fun `invoke should return loading state from repository`() = runTest {
        // Given
        every { repository.getCountries() } returns flowOf(Result.Loading())

        // When & Then
        useCase().test {
            val result = awaitItem()
            assertTrue(result is Result.Loading)
            awaitComplete()
        }

        verify { repository.getCountries() }
    }

    @Test
    fun `create should initialize useCase with repository`() {
        // Given
        val mockRepository = mockk<CountryRepository>()
        
        // When
        val useCase = GetCountriesUseCase.create(mockRepository)
        
        // Then
        // Since the repository is encapsulated, we can't directly check it
        // But we can verify it works by testing its behavior
        every { mockRepository.getCountries() } returns flowOf(Result.Success(testCountries))
        
        runTest {
            useCase().test {
                val result = awaitItem()
                assertTrue(result is Result.Success)
                assertEquals(testCountries, (result as Result.Success).data)
                awaitComplete()
            }
        }
    }
} 