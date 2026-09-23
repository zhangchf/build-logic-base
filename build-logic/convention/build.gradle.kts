plugins {
    `kotlin-dsl`
}

group = "com.zcf.buildlogic"

java {
    toolchain {
        // Single source of truth for the JDK version: gradle/libs.versions.toml
        languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get().toInt()))
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.compiler.gradlePlugin)
    compileOnly(libs.hilt.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.room.gradlePlugin)
    compileOnly(libs.detekt.gradlePlugin)
}
