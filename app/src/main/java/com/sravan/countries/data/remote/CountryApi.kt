package com.sravan.countries.data.remote

import com.sravan.countries.data.remote.dto.CountryDto
import retrofit2.http.GET

interface CountryApi {
    @GET("peymano-wmt/32dcb892b06648910ddd40406e37fdab/raw/db25946fd77c5873b0303b858e861ce724e0dcd0/countries.json")
    suspend fun getCountries(): List<CountryDto>
}