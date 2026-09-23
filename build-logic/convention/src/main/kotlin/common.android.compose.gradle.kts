import com.android.build.api.dsl.CommonExtension
import com.zcf.buildlogic.libs

plugins {
    id("org.jetbrains.kotlin.plugin.compose")
}

extensions.configure<CommonExtension> {
    buildFeatures.compose = true
}

dependencies {
    "implementation"(platform(libs.findLibrary("androidx-compose-bom").get()))
    "implementation"(libs.findLibrary("androidx-compose-ui").get())
}
