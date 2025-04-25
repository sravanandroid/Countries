package com.sravan.countries.presentation

import com.sravan.countries.domain.model.Country

/**
 * Represents the UI state for the countries screen.
 * Contains the list of countries to display, error message if any,
 * and loading state indicator.
 */
data class CountriesState(
    val countries: List<Country> = emptyList(),
    val error: String = "",
    val isLoading: Boolean = false
)
