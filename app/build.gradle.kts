import java.util.Properties

plugins {
    alias(libs.plugins.common.android.application)
    alias(libs.plugins.common.android.compose)
    alias(libs.plugins.common.android.compose.test)
    alias(libs.plugins.common.android.hilt)
    alias(libs.plugins.common.android.test)
}

// Read through providers.fileContents rather than a plain File read so local.properties stays a
// tracked configuration-cache input. getOrElse, not get: CI has no local.properties at all and
// must still build.
val deepSeekApiKey: String = providers
    .fileContents(rootProject.layout.projectDirectory.file("local.properties"))
    .asText
    .map { text ->
        val properties = Properties()
        properties.load(text.reader())
        properties.getProperty("DEEPSEEK_API_KEY").orEmpty()
    }
    .getOrElse("")
    .replace("\\", "\\\\")
    .replace("\"", "\\\"")

android {
    namespace = "com.zcf.buildlogicbase"

    defaultConfig {
        applicationId = "com.zcf.buildLogicbase"
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "DEEPSEEK_API_KEY", "\"$deepSeekApiKey\"")
    }

    buildFeatures {
        buildConfig = true
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
    implementation(project(":features:chat"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
