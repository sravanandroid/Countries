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

/**
 * Implementation of [CountryRepository] that fetches country data from a remote API.
 */
class CountryRepositoryImpl(
    private val api: CountryApi,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : CountryRepository {

    override fun getCountries(): Flow<Result<List<Country>>> = flow {
        emit(Result.Loading())

        try {
            val countries = withContext(ioDispatcher) {
                api.getCountries().map { it.toCountry() }
            }
            emit(Result.Success(countries))
        } catch (e: HttpException) {
            emit(Result.Error(message = "Server error: ${e.code()}"))
        } catch (e: IOException) {
            emit(Result.Error(message = "Couldn't reach server. Check your internet connection."))
        } catch (e: Exception) {
            emit(Result.Error(message = "An unexpected error occurred: ${e.localizedMessage ?: "Unknown error"}"))
        }
    }.flowOn(ioDispatcher)

    companion object {
        fun create(api: CountryApi = CountryApi.create()): CountryRepository {
            return CountryRepositoryImpl(api)
        }
    }
}