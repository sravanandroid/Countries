package com.sravan.countries.presentation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sravan.countries.R
import com.sravan.countries.databinding.ItemCountryBinding
import com.sravan.countries.domain.model.Country

/**
 * Adapter for displaying a list of countries in a RecyclerView
 */
class CountryAdapter : ListAdapter<Country, CountryAdapter.CountryViewHolder>(CountryDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryViewHolder {
        val binding = ItemCountryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CountryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CountryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CountryViewHolder(
        private val binding: ItemCountryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(country: Country) {
            with(binding) {
                // Format the country name and region
                countryNameRegionText.text = itemView.context.getString(
                    R.string.country_name_region,
                    country.name,
                    country.region
                )
                
                countryCodeText.text = country.code
                capitalText.text = country.capital
            }
        }
    }

    companion object {
        private val CountryDiffCallback = object : DiffUtil.ItemCallback<Country>() {
            override fun areItemsTheSame(oldItem: Country, newItem: Country): Boolean {
                return oldItem.code == newItem.code
            }

            override fun areContentsTheSame(oldItem: Country, newItem: Country): Boolean {
                return oldItem == newItem
            }
        }
    }
} 