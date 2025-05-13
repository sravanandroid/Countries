package com.sravan.countries.domain.util

/**
 * A sealed class representing the result of an operation with different states.
 *
 * @param T The type of data this result may contain
 * @param data Optional data payload associated with the result
 * @param message Optional message describing the result (used for errors)
 * @param isCached Optional flag indicating if the data comes from a cache
 *
 * This class provides three subclasses:
 * - [Success]: Represents a successful operation containing data, possibly from cache
 * - [Error]: Represents a failed operation with an error message and optional data
 * - [Loading]: Represents an operation in progress with optional data
 */
sealed class Result<T>(
    val data: T? = null, 
    val message: String? = null,
    val isCached: Boolean = false
) {
    class Success<T>(data: T, isCached: Boolean = false) : Result<T>(data, isCached = isCached)
    class Error<T>(message: String, data: T? = null) : Result<T>(data, message)
    class Loading<T>(data: T? = null) : Result<T>(data)
}
