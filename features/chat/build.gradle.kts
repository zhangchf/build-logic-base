// Order matters: common.android.compose and common.android.hilt both configure extensions that
// common.android.library has to have created first.
plugins {
    alias(libs.plugins.common.android.library)
    alias(libs.plugins.common.android.compose)
    alias(libs.plugins.common.android.hilt)
    alias(libs.plugins.common.android.test)
}

android {
    namespace = "com.zcf.chat"
}

// core:network keeps every HTTP type behind its own public surface, so this module only ever
// names DeepSeekClient and its models.
dependencies {
    implementation(project(":core:network"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.kotlinx.coroutines.android)

    debugImplementation(libs.androidx.compose.ui.tooling)
}
