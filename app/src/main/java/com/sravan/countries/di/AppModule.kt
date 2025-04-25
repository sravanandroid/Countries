package com.sravan.countries.di

import com.sravan.countries.BuildConfig
import com.sravan.countries.data.remote.CountryApi
import com.sravan.countries.domain.repository.CountryRepository
import com.sravan.countries.domain.use_case.GetCountriesUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton
import com.sravan.countries.data.repository.CountryRepositoryImpl
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    private const val BASE_URL = "https://gist.githubusercontent.com/"
    private const val TIMEOUT_SECONDS = 120L

    /**
     * Provides an OkHttpClient with logging interceptor (only in debug builds)
     * and configured timeouts.
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(
                        HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BODY
                        }
                    )
                }
            }
            .retryOnConnectionFailure(true)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Provides the Retrofit API client with the configured OkHttpClient.
     */
    @Provides
    @Singleton
    fun provideCountryApi(okHttpClient: OkHttpClient): CountryApi {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CountryApi::class.java)
    }

    @Provides
    @Singleton
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Singleton
    fun provideCountryRepository(
        api: CountryApi,
        ioDispatcher: CoroutineDispatcher
    ): CountryRepository {
        return CountryRepositoryImpl(api, ioDispatcher)
    }

    @Provides
    @Singleton
    fun provideGetCountriesUseCase(repository: CountryRepository): GetCountriesUseCase {
        return GetCountriesUseCase(repository)
    }
}