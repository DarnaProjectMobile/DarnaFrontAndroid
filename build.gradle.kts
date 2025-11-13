// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false

    // ✅ Hilt plugin (ne pas dupliquer avec "dagger.hilt.android.plugin")
    id("com.google.dagger.hilt.android") version "2.51" apply false
}

buildscript {
    dependencies {
        // ✅ Ajoute ceci pour que KAPT trouve Hilt lors du traitement d’annotations
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.51")
    }
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}
