package com.zcf.buildlogicbase.di

import com.zcf.buildlogicbase.BuildConfig
import com.zcf.chat.di.DeepSeekApiKey
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * The one binding that has to be declared in `:app`: `BuildConfig` is generated here, and the
 * chat feature depends on this module rather than the other way round.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppConfigModule {
    @Provides
    @Singleton
    @DeepSeekApiKey
    fun provideDeepSeekApiKey(): String = BuildConfig.DEEPSEEK_API_KEY
}
