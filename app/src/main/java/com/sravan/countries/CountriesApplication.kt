package com.sravan.countries

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Main application class for the Countries app.
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection.
 */
@HiltAndroidApp
class CountriesApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}