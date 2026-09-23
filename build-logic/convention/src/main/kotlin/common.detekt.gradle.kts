import com.zcf.buildlogic.libs

// Static analysis for a module's Kotlin sources. The shared rules live in
// config/detekt/detekt.yml, which every module applying this plugin reuses.
plugins {
    id("dev.detekt")
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom(files(rootProject.file("config/detekt/detekt.yml")))
}

dependencies {
    "detektPlugins"(libs.findLibrary("detekt-compose").get())
}
