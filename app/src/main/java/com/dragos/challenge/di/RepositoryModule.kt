package com.dragos.challenge.di

import com.dragos.challenge.data.repository.ImageRepository
import com.dragos.challenge.data.repository.ImageRepositoryContract
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {
    @Binds
    fun bindsImageRepository(impl: ImageRepository): ImageRepositoryContract
}