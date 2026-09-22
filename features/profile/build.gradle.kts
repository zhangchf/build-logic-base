plugins {
    alias(libs.plugins.common.android.library)
    alias(libs.plugins.common.android.compose)
}

android {
    namespace = "com.zcf.profile"
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
