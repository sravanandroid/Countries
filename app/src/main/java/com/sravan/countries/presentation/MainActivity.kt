package com.sravan.countries.presentation

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.sravan.countries.R
import com.sravan.countries.databinding.ActivityMainBinding
import com.sravan.countries.util.NetworkUtil
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * Displays the list of countries and handles all UI interactions.
 */
class MainActivity : AppCompatActivity() {

    companion object {
        private const val KEY_SCROLL_POSITION = "scroll_position"
    }

    private lateinit var binding: ActivityMainBinding
    private val viewModel by viewModels<CountriesViewModel>()
    private val adapter = CountryAdapter()
    private var snackbar: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupRecyclerView()
        setupSwipeRefresh()
        setupRetryButton()
        observeViewModel()
        observeNetworkChanges()
        
        // Restore scroll position after configuration change
        savedInstanceState?.getInt(KEY_SCROLL_POSITION, 0)?.let { position ->
            binding.recyclerView.scrollToPosition(position)
        }
    }
    
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save scroll position for configuration changes
        (binding.recyclerView.layoutManager as? LinearLayoutManager)?.let { layoutManager ->
            val position = layoutManager.findFirstVisibleItemPosition()
            outState.putInt(KEY_SCROLL_POSITION, position)
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            adapter = this@MainActivity.adapter
            setHasFixedSize(true)
            itemAnimator = DefaultItemAnimator()
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            if (NetworkUtil.isNetworkAvailable(this)) {
                viewModel.refreshCountries()
            } else {
                binding.swipeRefreshLayout.isRefreshing = false
                showNetworkErrorMessage()
            }
        }
    }

    private fun setupRetryButton() {
        binding.retryButton.setOnClickListener {
            if (NetworkUtil.isNetworkAvailable(this)) {
                viewModel.refreshCountries()
            } else {
                showNetworkErrorMessage()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.state.observe(this) { state ->
            updateListState(state)
            updateLoadingState(state)
            updateErrorState(state)
            updateEmptyState(state)
            
            // Show toast for cached data if the state has the isCached flag
            if (state.countries.isNotEmpty() && viewModel.isDataFromCache) {
                Toast.makeText(
                    this,
                    R.string.showing_cached_data,
                    Toast.LENGTH_SHORT
                ).show()
                // Reset the flag after showing the toast
                viewModel.isDataFromCache = false
            }
        }
    }
    
    private fun observeNetworkChanges() {
        // Observe network status changes
        lifecycleScope.launch {
            NetworkUtil.observeNetworkStatus(this@MainActivity)
                .distinctUntilChanged()
                .collect { isConnected ->
                    if (isConnected) {
                        // Network restored
                        dismissNetworkErrorMessage()
                        if (adapter.currentList.isEmpty() && !binding.errorLayout.isVisible) {
                            viewModel.refreshCountries()
                        }
                    } else {
                        // Network lost
                        if (adapter.currentList.isEmpty()) {
                            showNetworkErrorMessage()
                        }
                    }
                }
        }
    }

    private fun updateListState(state: CountriesState) {
        adapter.submitList(state.countries)
    }

    private fun updateLoadingState(state: CountriesState) {
        binding.apply {
            progressBar.isVisible = state.isLoading && state.countries.isEmpty()
            swipeRefreshLayout.isRefreshing = state.isLoading && state.countries.isNotEmpty()
        }
    }

    private fun updateErrorState(state: CountriesState) {
        binding.apply {
            errorLayout.isVisible = state.error.isNotEmpty() && state.countries.isEmpty()
            errorTextView.text = state.error
        }
    }

    private fun updateEmptyState(state: CountriesState) {
        binding.apply {
            emptyTextView.isVisible = state.countries.isEmpty() && 
                !state.isLoading && 
                state.error.isEmpty()
        }
    }
    
    private fun showNetworkErrorMessage() {
        snackbar?.dismiss()
        
        snackbar = Snackbar.make(
            binding.root,
            R.string.network_error,
            Snackbar.LENGTH_INDEFINITE
        ).apply {
            setAction(R.string.retry) {
                if (NetworkUtil.isNetworkAvailable(this@MainActivity)) {
                    viewModel.refreshCountries()
                }
            }
            show()
        }
    }
    
    private fun dismissNetworkErrorMessage() {
        snackbar?.dismiss()
        snackbar = null
    }
    
    override fun onDestroy() {
        // Clean up resources
        dismissNetworkErrorMessage()
        if (isFinishing) {
            binding.recyclerView.adapter = null
        }
        super.onDestroy()
    }
}