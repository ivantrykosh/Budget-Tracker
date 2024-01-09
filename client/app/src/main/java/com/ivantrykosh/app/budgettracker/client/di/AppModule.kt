package com.ivantrykosh.app.budgettracker.client.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ivantrykosh.app.budgettracker.client.common.Constants
import com.ivantrykosh.app.budgettracker.client.data.remote.AccountApi
import com.ivantrykosh.app.budgettracker.client.data.remote.AuthApi
import com.ivantrykosh.app.budgettracker.client.data.remote.TransactionApi
import com.ivantrykosh.app.budgettracker.client.data.remote.UserApi
import com.ivantrykosh.app.budgettracker.client.data.repository.AccountRepositoryImpl
import com.ivantrykosh.app.budgettracker.client.data.repository.AuthRepositoryImpl
import com.ivantrykosh.app.budgettracker.client.data.repository.TransactionRepositoryImpl
import com.ivantrykosh.app.budgettracker.client.data.repository.UserRepositoryImpl
import com.ivantrykosh.app.budgettracker.client.domain.repository.AccountRepository
import com.ivantrykosh.app.budgettracker.client.domain.repository.AuthRepository
import com.ivantrykosh.app.budgettracker.client.domain.repository.TransactionRepository
import com.ivantrykosh.app.budgettracker.client.domain.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Object with provided APIs and repositories
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    val gson: Gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd")
        .create()

    val client = OkHttpClient.Builder()
        .retryOnConnectionFailure(true)
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .build()

    /**
     * Provide auth API with Retrofit
     */
    @Provides
    @Singleton
    fun provideAuthApi(): AuthApi {
        return Retrofit.Builder()
            .baseUrl(Constants.URL.BASE_URL + Constants.URL.AUTH_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(AuthApi::class.java)
    }

    /**
     * Provide auth repository with api instance
     *
     * @param api instance of AuthApi
     */
    @Provides
    @Singleton
    fun provideAuthRepository(api: AuthApi): AuthRepository {
        return AuthRepositoryImpl(api)
    }

    /**
     * Provide user API with Retrofit
     */
    @Provides
    @Singleton
    fun provideUserApi(): UserApi {
        return Retrofit.Builder()
            .baseUrl(Constants.URL.BASE_URL + Constants.URL.USER_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(UserApi::class.java)
    }

    /**
     * Provide user repository with api instance
     *
     * @param api instance of UserApi
     */
    @Provides
    @Singleton
    fun provideUserRepository(api: UserApi): UserRepository {
        return UserRepositoryImpl(api)
    }

    /**
     * Provide account API with Retrofit
     */
    @Provides
    @Singleton
    fun provideAccountApi(): AccountApi {
        return Retrofit.Builder()
            .baseUrl(Constants.URL.BASE_URL + Constants.URL.ACCOUNT_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(AccountApi::class.java)
    }

    /**
     * Provide account repository with api instance
     *
     * @param api instance of AccountApi
     */
    @Provides
    @Singleton
    fun provideAccountRepository(api: AccountApi): AccountRepository {
        return AccountRepositoryImpl(api)
    }

    /**
     * Provide transaction API with Retrofit
     */
    @Provides
    @Singleton
    fun provideTransactionApi(): TransactionApi {
        return Retrofit.Builder()
            .baseUrl(Constants.URL.BASE_URL + Constants.URL.TRANSACTION_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(TransactionApi::class.java)
    }

    /**
     * Provide transaction repository with api instance
     *
     * @param api instance of TransactionApi
     */
    @Provides
    @Singleton
    fun provideTransactionRepository(api: TransactionApi): TransactionRepository {
        return TransactionRepositoryImpl(api)
    }
}