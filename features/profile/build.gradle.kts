plugins {
    alias(libs.plugins.common.android.library)
    alias(libs.plugins.common.android.compose)
    alias(libs.plugins.common.android.test)
    alias(libs.plugins.common.detekt)
}

android {
    namespace = "com.zcf.profile"
}

// The unit/instrumented test dependencies come from the common.android.test convention plugin.
dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
}
