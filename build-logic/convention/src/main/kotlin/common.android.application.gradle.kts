import com.zcf.buildlogic.libs
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("common.detekt")
}

// Single source of truth for the JDK version: gradle/libs.versions.toml
val javaVersion = libs.findVersion("java").get().requiredVersion.toInt()

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaVersion))
    }
}

android {
    compileSdk = 37

    defaultConfig {
        minSdk = 24
        targetSdk = 37
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(javaVersion)
        targetCompatibility = JavaVersion.toVersion(javaVersion)
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget(javaVersion.toString()))
    }
}
