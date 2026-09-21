package com.zcf.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension

internal fun ApplicationExtension.configureAndroidCompose() {
    buildFeatures {
        compose = true
    }
}

internal fun LibraryExtension.configureAndroidCompose() {
    buildFeatures {
        compose = true
    }
}
