package com.ivantrykosh.app.budgettracker.client.di

import com.ivantrykosh.app.budgettracker.client.common.Constants
import com.ivantrykosh.app.budgettracker.client.data.remote.AuthApi
import com.ivantrykosh.app.budgettracker.client.data.remote.UserApi
import com.ivantrykosh.app.budgettracker.client.data.repository.AuthRepositoryImpl
import com.ivantrykosh.app.budgettracker.client.data.repository.UserRepositoryImpl
import com.ivantrykosh.app.budgettracker.client.domain.repository.AuthRepository
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

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    val client = OkHttpClient.Builder()
        .retryOnConnectionFailure(true)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideAuthApi(): AuthApi {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL + Constants.AUTH_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(api: AuthApi): AuthRepository {
        return AuthRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun provideUserApi(): UserApi {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL + Constants.USER_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UserApi::class.java)
    }

    @Provides
    @Singleton
    fun provideUserRepository(api: UserApi): UserRepository {
        return UserRepositoryImpl(api)
    }
}