package com.egiwon.deepdiverecyclerview1.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class InterceptorModule {

    @Provides
    @Singleton
    fun provideQueryInterceptor(): Interceptor {
        return Interceptor { chain ->
            val url = chain.request()
                .url
                .newBuilder()
                .addQueryParameter("client_id", "tdMHi4W2So4dENvnSXVO_eIN4px0nSZr6M-Y30gyHz0")
                .build()

            val requestBuilder = chain.request().newBuilder().url(url)
            chain.proceed(requestBuilder.build())
        }
    }
}
