package com.sravan.countries.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Domain model representing a country.
 * Implements Parcelable for state restoration.
 */
@Parcelize
data class Country(
    val name: String,
    val region: String,
    val code: String,
    val capital: String
) : Parcelable
