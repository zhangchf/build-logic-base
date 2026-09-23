plugins {
    alias(libs.plugins.common.android.application)
    alias(libs.plugins.common.android.compose)
    alias(libs.plugins.common.android.compose.test)
    alias(libs.plugins.common.android.test)
}

android {
    namespace = "com.zcf.buildlogicbase"

    defaultConfig {
        applicationId = "com.zcf.buildLogicbase"
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
}

// common.android.compose brings the Compose BOM + androidx.compose.ui, common.android.compose.test
// the Compose test artifacts and common.android.test the junit/espresso dependencies.
dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
