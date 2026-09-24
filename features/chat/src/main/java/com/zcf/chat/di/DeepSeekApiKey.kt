package com.zcf.chat.di

import javax.inject.Qualifier

/**
 * The DeepSeek API key.
 *
 * The qualifier is declared here but bound in `:app`, which is the only module that can see
 * `BuildConfig`. A plain `String` binding would be ambiguous and, more importantly, would make
 * the key easy to confuse with any other string in the graph.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DeepSeekApiKey
