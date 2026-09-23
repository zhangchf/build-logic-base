import com.zcf.buildlogic.libs

plugins {
    id("org.jetbrains.kotlin.jvm")
}

// Single source of truth for the JDK version: gradle/libs.versions.toml
val javaVersion = libs.findVersion("java").get().requiredVersion.toInt()

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaVersion))
    }
}
