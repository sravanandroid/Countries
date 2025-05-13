package com.sravan.countries.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sravan.countries.domain.model.Country
import com.sravan.countries.domain.use_case.GetCountriesUseCase
import com.sravan.countries.domain.util.Result
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart

/**
 * ViewModel responsible for managing country data state.
 * Fetches countries using GetCountriesUseCase and updates the UI state
 * based on the result (success, error, or loading).
 * Supports state restoration across process death.
 */
class CountriesViewModel(
    private val savedStateHandle: SavedStateHandle = SavedStateHandle(),
    private val getCountriesUseCase: GetCountriesUseCase = GetCountriesUseCase.create()
) : ViewModel() {

    companion object {
        private const val KEY_STATE = "countries_state"
        private const val KEY_IS_CACHED = "is_data_from_cache"
    }

    // Initialize MutableLiveData with a default value to avoid nullability warnings
    private val _state = MutableLiveData(CountriesState())
    val state: LiveData<CountriesState> = _state
    
    // Flag to track if data came from cache
    var isDataFromCache: Boolean = false
        get() = savedStateHandle[KEY_IS_CACHED] ?: field
        set(value) {
            field = value
            savedStateHandle[KEY_IS_CACHED] = value
        }

    init {
        // Restore state if available, otherwise initialize and fetch
        val savedState: CountriesState? = savedStateHandle[KEY_STATE]
        savedState?.let { state ->
            _state.value = state
            // If we had no countries but weren't in an error state, refresh data
            if (state.countries.isEmpty() && state.error.isEmpty() && !state.isLoading) {
                getCountries()
            }
        } ?: run {
            // State was null (not saved), so use default and fetch data
            getCountries()
        }
    }

    /**
     * Fetches countries from the repository and updates the UI state accordingly.
     * Uses structured concurrency with flow operators for better error handling.
     */
    private fun getCountries() {
        getCountriesUseCase()
            .onStart {
                updateState { it.copy(isLoading = true, error = "") }
            }
            .catch { exception ->
                updateState { 
                    it.copy(
                        isLoading = false,
                        error = exception.message ?: "An unexpected error occurred"
                    )
                }
            }
            .onEach { result ->
                when (result) {
                    is Result.Success -> {
                        // Set the cached flag if the data is from cache
                        isDataFromCache = result.isCached
                        
                        updateState { 
                            it.copy(
                                countries = result.data ?: emptyList(),
                                isLoading = false,
                                error = ""
                            )
                        }
                    }

                    is Result.Error -> {
                        updateState { 
                            it.copy(
                                error = result.message ?: "An unexpected error occurred",
                                isLoading = false
                            )
                        }
                    }

                    is Result.Loading -> {
                        updateState {
                            it.copy(
                                isLoading = true,
                                error = ""
                            )
                        }
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Updates the state and saves it to the SavedStateHandle for process death restoration
     */
    private inline fun updateState(update: (CountriesState) -> CountriesState) {
        val currentState = _state.value ?: CountriesState()
        val newState = update(currentState)
        _state.value = newState
        savedStateHandle[KEY_STATE] = newState
    }

    /**
     * Public method to refresh countries data
     */
    fun refreshCountries() {
        getCountries()
    }
}