package com.sravan.countries.data.repository

import com.sravan.countries.data.remote.CountryApi
import com.sravan.countries.domain.model.Country
import com.sravan.countries.domain.util.Result
import com.sravan.countries.domain.repository.CountryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

/**
 * Implementation of [CountryRepository] that fetches country data from a remote API.
 * Includes caching mechanism and robust error handling.
 */
class CountryRepositoryImpl(
    private val api: CountryApi,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : CountryRepository {

    // In-memory cache with timestamp for countries data
    private var cachedCountries: List<Country>? = null
    private var cacheTimestamp: Long = 0
    private val cacheValidityDuration = TimeUnit.MINUTES.toMillis(15)

    override fun getCountries(): Flow<Result<List<Country>>> = flow {
        emit(Result.Loading())

        try {
            // Check if we have a valid cache
            val currentTime = System.currentTimeMillis()
            val cache = cachedCountries
            val cacheIsValid = !cache.isNullOrEmpty() &&
                               currentTime - cacheTimestamp < cacheValidityDuration
            
            // Return cache if valid
            if (cacheIsValid && cache != null) {
                emit(Result.Success(cache))
                return@flow
            }
            
            // Fetch from network
            val countries = withContext(ioDispatcher) {
                api.getCountries().map { it.toCountry() }
            }
            
            // Update cache
            cachedCountries = countries
            cacheTimestamp = System.currentTimeMillis()
            
            emit(Result.Success(countries))
        } catch (e: SocketTimeoutException) {
            emit(Result.Error(message = "Connection timed out. Please try again."))
            emitCachedDataIfAvailable(this)
        } catch (e: UnknownHostException) {
            // Handle specific network error for DNS issues
            emit(Result.Error(message = "Couldn't reach server. Check your internet connection."))
            emitCachedDataIfAvailable(this)
        } catch (e: HttpException) {
            val errorMsg = when (e.code()) {
                401 -> "Unauthorized access. Please sign in again."
                403 -> "Access forbidden. You don't have permission to access this resource."
                404 -> "Resource not found on the server."
                500, 501, 502, 503 -> "Server error. Please try again later."
                else -> "Server error: ${e.code()}"
            }
            emit(Result.Error(message = errorMsg))
            emitCachedDataIfAvailable(this)
        } catch (e: IOException) {
            emit(Result.Error(message = "Network error. Check your internet connection."))
            emitCachedDataIfAvailable(this)
        } catch (e: Exception) {
            // Note: We don't specifically catch CancellationException since
            // it needs to propagate for coroutine cancellation to work properly
            emit(Result.Error(message = "An unexpected error occurred: ${e.localizedMessage ?: "Unknown error"}"))
            emitCachedDataIfAvailable(this)
        }
    }.flowOn(ioDispatcher)
    
    /**
     * Helper function to emit cached data if available
     */
    private suspend fun emitCachedDataIfAvailable(flowCollector: kotlinx.coroutines.flow.FlowCollector<Result<List<Country>>>) {
        val cache = cachedCountries
        if (!cache.isNullOrEmpty()) {
            flowCollector.emit(Result.Success(cache, isCached = true))
        }
    }

    companion object {
        fun create(api: CountryApi = CountryApi.create()): CountryRepository {
            return CountryRepositoryImpl(api)
        }
    }
}