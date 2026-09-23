import com.zcf.buildlogic.libs

// Apply after common.android.compose.
// The androidTest source set does not inherit the BOM declared for `implementation`,
// so Compose test dependencies would resolve without a version.
dependencies {
    "androidTestImplementation"(platform(libs.findLibrary("androidx-compose-bom").get()))
    "androidTestImplementation"(libs.findLibrary("androidx-compose-ui-test-junit4").get())
    "debugImplementation"(libs.findLibrary("androidx-compose-ui-test-manifest").get())
}
