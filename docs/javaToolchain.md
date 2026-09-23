# Java Toolchain Setup & Architecture

## Overview
This document outlines how Java Toolchains targeting **Java 21** are configured across this repository, explaining the rationale behind the setup for Gradle Daemons, pure Kotlin/JVM modules, and Android modules.

---

## Single Source of Truth

The JDK version is declared **once** in `gradle/libs.versions.toml`:

```toml
[versions]
java = "21"
```

Every other place derives from it:

| Consumer | How it reads the version |
| :--- | :--- |
| Gradle Daemon (`gradle/gradle-daemon-jvm.properties`) | `UpdateDaemonJvm` task in root `build.gradle.kts` |
| Module toolchains (`java.toolchain`) | `libs.findVersion("java")` in the convention plugins |
| Java `source`/`target` compatibility | `JavaVersion.toVersion(javaVersion)` |
| Kotlin `jvmTarget` | `JvmTarget.fromTarget(javaVersion.toString())` |

**To upgrade the JDK:**

1. Change `java` in `gradle/libs.versions.toml`.
2. Run `./gradlew updateDaemonJvm` and commit `gradle/gradle-daemon-jvm.properties`.
3. Run `./gradlew assembleDebug spotlessCheck detekt` to verify.

---

## Key Components

### 1. Gradle Daemon JVM (`gradle-daemon-jvm.properties`)
* **Purpose**: Enforces the JVM version used to run the Gradle Daemon itself.
* **Configuration** in root `build.gradle.kts`:
  ```kotlin
  // Keeps gradle/gradle-daemon-jvm.properties in sync with the `java` version in
  // gradle/libs.versions.toml. After bumping it, run "./gradlew updateDaemonJvm" and commit the file.
  tasks.withType<org.gradle.buildconfiguration.tasks.UpdateDaemonJvm> {
      languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get().toInt()))
  }
  ```
* **Generation**: Running `./gradlew updateDaemonJvm` creates/updates `gradle/gradle-daemon-jvm.properties` with `toolchainVersion=21` plus per-platform download URLs. The file is checked in on purpose, so every developer and CI runner gets the same daemon JVM.
* **Precedence**: the daemon JVM criteria takes precedence over `JAVA_HOME` and `org.gradle.java.home`. A locally installed JDK that matches the criteria (any vendor, since no `toolchainVendor` is set) is reused; otherwise Gradle downloads one from the recorded URLs. This is why CI's `actions/setup-java` JDK 21 works without an extra download.
* **Optional vendor pinning**: run `./gradlew updateDaemonJvm --jvm-vendor=adoptium` to record `toolchainVendor` as well. Be aware this makes JDKs of other vendors (for example the JetBrains Runtime or Zulu used locally) *not* match the criteria, so each machine provisions the pinned vendor once.

### 2. Automatic Toolchain Provisioning (Foojay Resolver)
* **Purpose**: Enables Gradle to automatically download JDK 21 if it is not installed on a local machine or CI runner.
* **Configuration** in `settings.gradle.kts`:
  ```kotlin
  plugins {
      id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
  }
  ```

---

## Module Convention Plugins (`build-logic/convention`)

### Pure JVM Modules (`common.jvm.library.gradle.kts`)
For standard Kotlin JVM modules, setting `java.toolchain` is sufficient:

```kotlin
import com.zcf.buildlogic.libs

val javaVersion = libs.findVersion("java").get().requiredVersion.toInt()

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaVersion))
    }
}
```

*Note: The Kotlin Gradle Plugin (KGP) automatically detects `java.toolchain` in pure JVM projects and configures `jvmTarget` and JDK paths accordingly.*

---

### Android Modules (`common.android.application` & `common.android.library`)
For Android modules, `java.toolchain` is combined with explicit `compileOptions` and Kotlin `jvmTarget`, all derived from the same version:

```kotlin
import com.zcf.buildlogic.libs
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application") // or com.android.library
}

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
```

#### Rationale: Toolchain vs. Bytecode Target in Android
* **`java.toolchain`**: Specifies the JDK installation used to execute compiler tools (`javac`, `kotlinc`). Verified in this repository: with the daemon running on a different JDK, an Android module still compiles with the toolchain JDK.
* **`compileOptions` (`sourceCompatibility` & `targetCompatibility`)**: Defines the Java language level and class file bytecode target processed by Android D8/R8 desugaring. Since AGP 8.1 these default to the toolchain version, and since Kotlin 2.2 `kotlin.compilerOptions.jvmTarget` does too — they are set explicitly here so the intended bytecode target is visible in the build script and independent of plugin defaults, and they are derived from the same version so they cannot drift from the toolchain.
* Java 21 bytecode (class file version 65) is accepted by D8/R8 in AGP 9.4.1 for the current `minSdk = 24`.

---

### Build Logic Convention Build (`build-logic/convention/build.gradle.kts`)
The convention build itself configures `java.toolchain` for consistency:

```kotlin
java {
    toolchain {
        // Single source of truth for the JDK version: gradle/libs.versions.toml
        languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get().toInt()))
    }
}
```

---

## Summary Matrix

| Target Scope | Configuration Location | Toolchain / Target Mechanism |
| :--- | :--- | :--- |
| **Gradle Daemon** | `build.gradle.kts` & `gradle-daemon-jvm.properties` | `UpdateDaemonJvm` task |
| **JDK Resolver** | `settings.gradle.kts` | `foojay-resolver-convention` |
| **Pure JVM Modules** | `common.jvm.library.gradle.kts` | `java.toolchain` |
| **Android Modules** | `common.android.application.gradle.kts` / `common.android.library.gradle.kts` | `java.toolchain` + `compileOptions` + `kotlin.compilerOptions` |
| **Convention Logic** | `build-logic/convention/build.gradle.kts` | `java.toolchain` |
