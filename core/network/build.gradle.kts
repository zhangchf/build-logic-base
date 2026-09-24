plugins {
    alias(libs.plugins.common.jvm.library)
    alias(libs.plugins.kotlin.serialization)
}

// Retrofit, OkHttp and the SSE reading stay behind this module's public surface: it exposes
// DeepSeekClient and its models, and nothing else. Callers never name an HTTP type.
dependencies {
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)

    // common.jvm.library brings no test dependencies of its own.
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.okhttp.mockwebserver)
}
