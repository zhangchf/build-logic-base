plugins {
    alias(libs.plugins.common.jvm.library)
    alias(libs.plugins.common.detekt)
}

dependencies {
    implementation(libs.retrofit)
}
