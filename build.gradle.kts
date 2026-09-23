// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
    alias(libs.plugins.spotless)
    alias(libs.plugins.detekt)
}

// Keeps gradle/gradle-daemon-jvm.properties in sync with the `java` version in
// gradle/libs.versions.toml. After bumping it, run "./gradlew updateDaemonJvm" and commit the file.
tasks.withType<org.gradle.buildconfiguration.tasks.UpdateDaemonJvm> {
    languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get().toInt()))
}

spotless {
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**/*.kt", "**/bin/**/*.kt", "**/.gradle/**/*.kt")
        ktlint()
    }
    kotlinGradle {
        target("**/*.gradle.kts")
        targetExclude("**/build/**/*.gradle.kts", "**/bin/**/*.gradle.kts", "**/.gradle/**/*.gradle.kts")
        ktlint()
    }
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
}

dependencies {
    detektPlugins(libs.detekt.compose)
}
