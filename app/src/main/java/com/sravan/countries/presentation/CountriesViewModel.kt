package com.sravan.countries.presentation

import androidx.lifecycle.ViewModel
import com.sravan.countries.domain.use_case.GetCountriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.sravan.countries.domain.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart

/**
 * ViewModel responsible for managing country data state.
 * Fetches countries using GetCountriesUseCase and updates the UI state
 * based on the result (success, error, or loading).
 */
@HiltViewModel
class CountriesViewModel @Inject constructor(
    private val getCountriesUseCase: GetCountriesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CountriesState())
    val state: StateFlow<CountriesState> = _state.asStateFlow()

    init {
        getCountries()
    }

    /**
     * Fetches countries from the repository and updates the UI state accordingly.
     * Uses structured concurrency with flow operators for better error handling.
     */
    private fun getCountries() {
        getCountriesUseCase()
            .onStart {
                _state.value = _state.value.copy(isLoading = true, error = "")
            }
            .catch { exception ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = exception.message ?: "An unexpected error occurred"
                )
            }
            .onEach { result ->
                when (result) {
                    is Result.Success -> {
                        _state.value = _state.value.copy(
                            countries = result.data ?: emptyList(),
                            isLoading = false,
                            error = ""
                        )
                    }

                    is Result.Error -> {
                        _state.value = _state.value.copy(
                            error = result.message ?: "An unexpected error occurred",
                            isLoading = false
                        )
                    }

                    is Result.Loading -> {
                        _state.value = _state.value.copy(
                            isLoading = true,
                            error = ""
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun refreshCountries() {
        getCountries()
    }
}