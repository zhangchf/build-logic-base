import com.zcf.buildlogic.libs

// Apply after common.android.application / common.android.library.
dependencies {
    "testImplementation"(libs.findLibrary("junit").get())

    "androidTestImplementation"(libs.findLibrary("androidx-junit").get())
    "androidTestImplementation"(libs.findLibrary("androidx-espresso-core").get())
}
