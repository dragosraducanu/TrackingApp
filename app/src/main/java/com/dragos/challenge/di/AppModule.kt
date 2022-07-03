package com.dragos.challenge.di

import android.content.Context
import com.dragos.challenge.service.ServiceConnectionManager
import com.dragos.challenge.util.NetworkConnectivityManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Singleton
    @Provides
    fun providesConnectivityManager(
        @ApplicationContext context: Context
    ): NetworkConnectivityManager {
        return NetworkConnectivityManager(context)
    }

    @Singleton
    @Provides
    fun providesServiceConnectionManager(
        @ApplicationContext context: Context
    ): ServiceConnectionManager {
        return ServiceConnectionManager(context)
    }
}