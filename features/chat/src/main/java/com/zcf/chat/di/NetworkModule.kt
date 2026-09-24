package com.zcf.chat.di

import com.zcf.network.deepseek.DeepSeekClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers

/**
 * The Hilt wiring lives in this module rather than in `:core:network`, because that is a plain
 * JVM library and cannot apply the Hilt Gradle plugin, so its annotations would never be
 * processed.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideDeepSeekClient(@DeepSeekApiKey apiKey: String): DeepSeekClient =
        DeepSeekClient.create(apiKey = apiKey, ioDispatcher = Dispatchers.IO)
}
