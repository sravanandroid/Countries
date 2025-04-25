package com.sravan.countries.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.sravan.countries.domain.model.Country

data class CountryDto(
    @SerializedName("name") var name: String? = null,
    @SerializedName("region") var region: String? = null,
    @SerializedName("code") var code: String? = null,
    @SerializedName("capital") var capital: String? = null,
) {
    fun toCountry(): Country {
        return Country(
            name = name ?: "N/A",
            region = region ?: "N/A",
            code = code ?: "N/A",
            capital = capital ?: "N/A"
        )
    }
}
