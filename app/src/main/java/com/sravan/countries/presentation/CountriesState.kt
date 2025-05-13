package com.sravan.countries.presentation

import android.os.Parcelable
import com.sravan.countries.domain.model.Country
import kotlinx.parcelize.Parcelize

/**
 * Represents the UI state for the countries screen.
 * Contains the list of countries to display, error message if any,
 * and loading state indicator.
 */
@Parcelize
data class CountriesState(
    val countries: List<Country> = emptyList(),
    val error: String = "",
    val isLoading: Boolean = false
) : Parcelable
