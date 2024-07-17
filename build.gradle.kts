// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.sqldelight) apply false

    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.benmanes.versions)
    alias(libs.plugins.sonarqube)
}

sonarqube {
    properties {
        property("sonar.projectKey", "itachi1706_CheesecakeUtilities")
        property("sonar.organization", "itachi1706")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.androidLint.reportPaths", "app/build/reports/lint-results.xml")
        property(
            "sonar.projectVersion",
            project(":app").extensions.getByType(com.android.build.gradle.AppExtension::class.java).defaultConfig.versionName
                ?: "1.0"
        )
    }
}
