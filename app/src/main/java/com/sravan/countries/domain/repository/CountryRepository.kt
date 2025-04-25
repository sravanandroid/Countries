package com.sravan.countries.domain.repository

import com.sravan.countries.domain.model.Country
import com.sravan.countries.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface CountryRepository {
    fun getCountries(): Flow<Result<List<Country>>>
}