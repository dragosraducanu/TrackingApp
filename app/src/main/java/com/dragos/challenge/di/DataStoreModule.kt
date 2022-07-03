package com.dragos.challenge.di

import android.content.Context
import com.dragos.challenge.data.source.db.CacheLocation
import com.dragos.challenge.data.source.db.TrackingDataStore
import com.dragos.challenge.data.source.db.TrackingDataStoreContract
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DataStoreModule {
    @Singleton
    @Provides
    fun providesTrackingDataStore(@ApplicationContext context: Context): TrackingDataStoreContract =
        TrackingDataStore(context)

    @Singleton
    @Provides
    fun providesCacheDir(@ApplicationContext context: Context): CacheLocation =
        CacheLocation(context.cacheDir)
}