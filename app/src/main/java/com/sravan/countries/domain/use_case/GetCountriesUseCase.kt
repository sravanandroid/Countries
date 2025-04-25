package com.sravan.countries.domain.use_case

import com.sravan.countries.domain.model.Country
import com.sravan.countries.domain.repository.CountryRepository
import com.sravan.countries.domain.util.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case that retrieves a list of countries from the repository.
 *
 * This class uses the operator invoke pattern to allow the class instance to be called like a function.
 * It returns a Flow of Result containing a list of Country objects from the repository.
 */
class GetCountriesUseCase @Inject constructor(
    private val repository: CountryRepository
) {
    /**
     * Returns a Flow of Result containing a list of Country objects from the repository.
     */
    operator fun invoke(): Flow<Result<List<Country>>> {
        return repository.getCountries()
    }
}