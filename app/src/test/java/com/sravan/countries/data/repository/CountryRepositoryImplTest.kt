package com.sravan.countries.data.repository

import app.cash.turbine.test
import com.sravan.countries.data.remote.CountryApi
import com.sravan.countries.data.remote.dto.CountryDto
import com.sravan.countries.domain.model.Country
import com.sravan.countries.domain.util.Result
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.time.ExperimentalTime

@ExperimentalTime
@ExperimentalCoroutinesApi
class CountryRepositoryImplTest {

    private lateinit var api: CountryApi
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private lateinit var repository: CountryRepositoryImpl

    private val testCountryDtos = listOf(
        CountryDto(
            name = "United States", 
            region = "Americas", 
            code = "US", 
            capital = "Washington, D.C."
        ),
        CountryDto(
            name = "Canada", 
            region = "Americas", 
            code = "CA", 
            capital = "Ottawa"
        )
    )

    private val testCountries = testCountryDtos.map { it.toCountry() }

    @Before
    fun setup() {
        api = mockk()
        repository = CountryRepositoryImpl(api, testDispatcher)
    }

    @Test
    fun `getCountries emits loading state first`() = testScope.runTest {
        // Given
        coEvery { api.getCountries() } returns testCountryDtos

        // When & Then
        repository.getCountries().test {
            val loadingState = awaitItem()
            assertTrue(loadingState is Result.Loading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getCountries emits success with countries when API call succeeds`() = testScope.runTest {
        // Given
        coEvery { api.getCountries() } returns testCountryDtos

        // When & Then
        repository.getCountries().test {
            // Skip loading state
            awaitItem()
            
            // Check success state
            val successState = awaitItem()
            assertTrue(successState is Result.Success)
            assertEquals(testCountries, (successState as Result.Success).data)
            
            awaitComplete()
        }
    }

    @Test
    fun `getCountries uses cache when cache is valid`() = testScope.runTest {
        // First call to populate cache
        coEvery { api.getCountries() } returns testCountryDtos
        repository.getCountries().test {
            // Skip loading and success states
            awaitItem()
            awaitItem()
            awaitComplete()
        }

        // Reset mock to verify no more calls
        coEvery { api.getCountries() } throws RuntimeException("API should not be called")

        // Second call should use cache
        repository.getCountries().test {
            // Skip loading state
            awaitItem()
            
            // Check success state
            val successState = awaitItem()
            assertTrue(successState is Result.Success)
            assertEquals(testCountries, (successState as Result.Success).data)
            assertNotNull(successState.data)
            
            awaitComplete()
        }
    }

    @Test
    fun `getCountries emits error state on HTTP exception`() = testScope.runTest {
        // Given
        val httpException = HttpException(
            Response.error<Any>(404, "Not found".toResponseBody())
        )
        coEvery { api.getCountries() } throws httpException

        // When & Then
        repository.getCountries().test {
            // Skip loading state
            awaitItem()
            
            // Check error state
            val errorState = awaitItem()
            assertTrue(errorState is Result.Error)
            assertEquals("Resource not found on the server.", (errorState as Result.Error).message)
            
            awaitComplete()
        }
    }

    @Test
    fun `getCountries emits error state on IO exception`() = testScope.runTest {
        // Given
        coEvery { api.getCountries() } throws IOException("Network error")

        // When & Then
        repository.getCountries().test {
            // Skip loading state
            awaitItem()
            
            // Check error state
            val errorState = awaitItem()
            assertTrue(errorState is Result.Error)
            assertEquals("Network error. Check your internet connection.", (errorState as Result.Error).message)
            
            awaitComplete()
        }
    }

    @Test
    fun `getCountries emits error state on timeout exception`() = testScope.runTest {
        // Given
        coEvery { api.getCountries() } throws SocketTimeoutException("Timeout")

        // When & Then
        repository.getCountries().test {
            // Skip loading state
            awaitItem()
            
            // Check error state
            val errorState = awaitItem()
            assertTrue(errorState is Result.Error)
            assertEquals("Connection timed out. Please try again.", (errorState as Result.Error).message)
            
            awaitComplete()
        }
    }

    @Test
    fun `getCountries emits error state on unknown host exception`() = testScope.runTest {
        // Given
        coEvery { api.getCountries() } throws UnknownHostException("Unknown host")

        // When & Then
        repository.getCountries().test {
            // Skip loading state
            awaitItem()
            
            // Check error state
            val errorState = awaitItem()
            assertTrue(errorState is Result.Error)
            assertEquals("Couldn't reach server. Check your internet connection.", (errorState as Result.Error).message)
            
            awaitComplete()
        }
    }

    @Test
    fun `getCountries doesnt fall back to cache if no cache exists`() = testScope.runTest {
        // Given - no previous calls to populate cache
        coEvery { api.getCountries() } throws IOException("Network error")

        // When & Then
        repository.getCountries().test {
            // Skip loading state
            awaitItem()
            
            // Check error state
            val errorState = awaitItem()
            assertTrue(errorState is Result.Error)
            assertEquals("Network error. Check your internet connection.", (errorState as Result.Error).message)
            
            awaitComplete()
        }
    }

    @Test
    fun `create initializes repository with API`() {
        // Given
        val mockApi = mockk<CountryApi>()
        
        // When
        val repository = CountryRepositoryImpl.create(mockApi)
        
        // Then
        assertNotNull(repository)
        // Since the API is encapsulated, we can test indirectly
        coEvery { mockApi.getCountries() } returns testCountryDtos
        
        testScope.runTest {
            repository.getCountries().test {
                skipItems(1) // Skip loading
                val result = awaitItem()
                assertTrue(result is Result.Success)
                assertEquals(testCountries, (result as Result.Success).data)
                awaitComplete()
            }
        }
    }
} 