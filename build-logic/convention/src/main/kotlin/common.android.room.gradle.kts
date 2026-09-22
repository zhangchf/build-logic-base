import com.zcf.buildlogic.libs

plugins {
    id("androidx.room")
    id("com.google.devtools.ksp")
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    "implementation"(libs.findLibrary("room-runtime").get())
    "implementation"(libs.findLibrary("room-ktx").get())
    "ksp"(libs.findLibrary("room-compiler").get())
}
