package com.ivantrykosh.app.budgettracker.client.common

/**
 * Resource class with Success, Error and Loading class
 */
sealed class Resource<T> (val data: T? = null, val statusCode: Int? = null) {

    /**
     * Success class. Used if response is successful
     *
     * @param data response data
     */
    class Success<T>(data: T) : Resource<T>(data)

    /**
     * Error class. Used if response has error
     *
     * @param statusCode status code of response
     * @param data response data
     */
    class Error<T>(statusCode: Int, data: T? = null) : Resource<T>(data, statusCode)

    /**
     * Loading class. Used if response isn't got yet
     *
     * @param data response data
     */
    class Loading<T>(data: T? = null) : Resource<T>(data)
}
