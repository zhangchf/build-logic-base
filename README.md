# build-logic-base

A modern, multi-module Android project demonstration featuring a **DeepSeek AI Chat Application** built with Jetpack Compose, Kotlin Coroutines/Flow, Hilt, Retrofit/OkHttp with real-time SSE streaming, and centralized **Gradle Convention Plugins** (`build-logic`).

---

## 🌟 Key Features

* **DeepSeek AI Integration**:
  * Real-time **Server-Sent Events (SSE)** streaming response for interactive chat.
  * Support for **DeepSeek Chat** (`deepseek-chat`) and **DeepSeek Reasoner** (`deepseek-reasoner`).
  * Live **Reasoning / Thinking Process** visualization with collapsible reasoning UI panels.
  * Token usage reporting (`promptTokens` and `completionTokens`).
* **Modular Multi-Module Architecture**:
  * Clean separation into `:app`, `:core:network`, `:features:chat`, `:features:profile`, and `:build-logic`.
  * Scalable feature-based architecture following modern Android development best practices.
* **Centralized Build Logic (`build-logic`)**:
  * Custom Gradle Convention Plugins written in Kotlin DSL.
  * Standardized dependency management via Gradle Version Catalogs (`libs.versions.toml`).
  * Unified Java 21 Toolchain configuration.
* **Code Quality & Static Analysis**:
  * **Spotless** with `ktlint` for code formatting.
  * **Detekt** with `detekt-compose` rules for static code analysis.

---

## 📁 Module Structure

```
build-logic-base/
├── app/                  # Main Android Application entry point & Hilt app module
├── core/
│   └── network/          # Core network library (DeepSeek API, SSE StreamReader, Retrofit client)
├── features/
│   ├── chat/             # Chat UI, ViewModel, Repository, and domain models
│   └── profile/          # Profile feature module placeholder
├── build-logic/
│   └── convention/       # Gradle convention plugins for application, library, Compose, Hilt, Room, Detekt
├── config/
│   └── detekt/           # Shared Detekt configuration (detekt.yml)
└── docs/                 # Documentation (e.g., javaToolchain.md)
```

| Module | Type | Description |
| :--- | :--- | :--- |
| **`:app`** | Android Application | Main launcher activity ([`MainActivity`](file:///Users/zhangchf/AndroidStudioProjects/build-logic-base/app/src/main/java/com/zcf/buildlogicbase/MainActivity.kt)), Hilt application setup, and `DEEPSEEK_API_KEY` injection. |
| **`:core:network`** | Kotlin/Android Library | Network infrastructure containing [`DeepSeekClient`](file:///Users/zhangchf/AndroidStudioProjects/build-logic-base/core/network/src/main/java/com/zcf/network/deepseek/DeepSeekClient.kt), SSE line parser, Retrofit API interfaces, and unit tests. |
| **`:features:chat`** | Android Library | Complete AI Chat feature with Jetpack Compose screens ([`ChatScreen`](file:///Users/zhangchf/AndroidStudioProjects/build-logic-base/features/chat/src/main/java/com/zcf/chat/ui/ChatScreen.kt)), state management ([`ChatViewModel`](file:///Users/zhangchf/AndroidStudioProjects/build-logic-base/features/chat/src/main/java/com/zcf/chat/ui/ChatViewModel.kt)), and repositories. |
| **`:features:profile`** | Android Library | Feature module placeholder for user profile functionality. |
| **`:build-logic:convention`** | Included Build | Custom Kotlin DSL convention plugins enforcing uniform build configurations across modules. |

---

## 🛠 Tech Stack & Dependencies

* **Language**: [Kotlin](https://kotlinlang.org/) (2.4+)
* **JDK Toolchain**: Java 21 (configured via Foojay Resolver and Version Catalog)
* **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (Compose BOM, Material 3)
* **Dependency Injection**: [Hilt](https://dagger.dev/hilt/) (2.60+) with KSP
* **Asynchronous Programming**: Kotlin Coroutines & Flow
* **Networking**:
  * [Retrofit 3.0](https://square.github.io/retrofit/)
  * [OkHttp 4.12](https://square.github.io/okhttp/) (Streaming SSE support)
  * `kotlinx.serialization`
* **Local Persistence**: [Room](https://developer.android.com/training/data-storage/room) (Convention setup available)
* **Static Analysis & Formatting**:
  * [Spotless](https://github.com/diffplug/spotless) (`ktlint`)
  * [Detekt](https://detekt.dev/) with `detekt-compose`

---

## 🚀 Getting Started

### Prerequisites

* **Android Studio**: Ladybug / Jellyfish or newer recommended.
* **JDK**: Java 21 (Gradle automatically provisions Java 21 via the Foojay resolver toolchain if needed).

### Setup DeepSeek API Key

To enable the DeepSeek AI Chat functionality:

1. Obtain an API Key from the [DeepSeek Platform](https://platform.deepseek.com/).
2. Add your key to `local.properties` in the project root directory:

```properties
DEEPSEEK_API_KEY=your_actual_deepseek_api_key_here
```

*(Note: If the key is omitted or left empty, the app will run and show an in-app banner explaining that the API key is missing).*

---

## 💻 Building & Verification Commands

Run the following Gradle tasks from the project root:

### Build the Application
```bash
./gradlew assembleDebug
```

### Run Unit Tests
```bash
./gradlew test
```

### Apply Code Formatting (Spotless / ktlint)
```bash
./gradlew spotlessApply
```

### Check Code Formatting & Quality
```bash
./gradlew spotlessCheck detekt
```

### Synchronize Gradle Daemon JVM
If JDK versions are updated in `gradle/libs.versions.toml`:
```bash
./gradlew updateDaemonJvm
```

---

## 📐 Architecture Highlights & Toolchains

* **Single Source of Truth JDK**: The JDK version is declared once in `gradle/libs.versions.toml` (`java = "21"`) and reused by Gradle daemons, module toolchains, `compileOptions`, and Kotlin `jvmTarget`. For complete architecture details, refer to [javaToolchain.md](file:///Users/zhangchf/AndroidStudioProjects/build-logic-base/docs/javaToolchain.md).
* **Convention Plugins**:
  Located in `build-logic/convention`, these plugins are reused across modules:
- `common.android.application` / `common.android.library` — Android setup (`compileSdk`, `minSdk`, Java 21 `compileOptions` + `jvmTarget`)
- `common.android.compose` / `common.android.compose.test` — Compose compiler, BOM, UI and test artifacts (brings Compose automatically, so modules don't repeat BOM boilerplate)
- `common.android.test` — JUnit / Espresso / Compose UI test dependencies
- `common.android.hilt` — Hilt + KSP setup
- `common.android.room` — Room + KSP setup
- `common.jvm.library` — pure Kotlin/JVM module toolchain
- `common.detekt` — shared Detekt analysis with `config/detekt/detekt.yml`

## How the Streaming Works

1. The user hits send in `features:chat`; `ChatViewModel` builds a `ChatMessage` history and delegates to `ChatRepository`.
2. `:core:network`'s `DeepSeekClient.streamChat()` opens a Retrofit/OkHttp call and reads the response as an SSE line stream, emitting `ChatStreamEvent`s (`Reasoning`, `Content`, `Usage`, `Finished`, `Failed` / cancellation).
3. OkHttp's read timeout is raised to 5 minutes because DeepSeek thinking can produce no bytes for a long time before the first token arrives.
4. `ChatViewModel` applies each event back to a single streaming `ChatMessage` bubble, so content and reasoning render live; Stop cancels the stream and always leaves a consistent transcript.