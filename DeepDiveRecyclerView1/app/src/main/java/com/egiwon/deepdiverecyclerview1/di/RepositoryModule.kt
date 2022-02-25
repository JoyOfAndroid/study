package com.egiwon.deepdiverecyclerview1.di

import com.egiwon.deepdiverecyclerview1.data.ImageRepository
import com.egiwon.deepdiverecyclerview1.data.ImageRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindImageRepository(repository: ImageRepositoryImpl): ImageRepository
}
