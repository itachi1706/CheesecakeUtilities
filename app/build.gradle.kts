import com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.firebase.crashlytics)
}

val isGHActions: Boolean = System.getenv("GITHUB_ACTIONS")?.toBoolean() ?: false

android {
    compileSdk = 34
    namespace = "com.itachi1706.cheesecakeutilities"

    defaultConfig {
        applicationId = "com.itachi1706.cheesecakeutilities"
        minSdk = 19
        targetSdk = 34
        versionCode = 1291
        versionName = "4.10.0"

        vectorDrawables.useSupportLibrary = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    androidResources {
        localeFilters += listOf("en")
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    installation {
        timeOutInMs = 10 * 60 * 1000
    }
    lint {
        abortOnError = !isGHActions
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17

    }
    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources {
            excludes.add("META-INF/DEPENDENCIES")
            excludes.add("META-INF/NOTICE")
            excludes.add("META-INF/LICENSE*")
        }
    }

    val props = Properties()
    val propFile = file("key.properties")
    // check if SAFETY_NET_DEVICE_VERIFICATION_API_KEY is defined in gradle.properties (this is so the key is not in github)
    var googleVerificationApiKey = ""
    if (propFile.canRead()) {
        props.load(FileInputStream(propFile))
        if (props.containsKey("key.safetynet")) {
            googleVerificationApiKey = props.getProperty("key.safetynet")
        }
    }
    buildTypes {
        getByName("release") {
            buildConfigField(
                "String",
                "GOOGLE_VERIFICATION_API_KEY",
                "\"" + googleVerificationApiKey + "\""
            )
            isMinifyEnabled = true
            isShrinkResources = true
            multiDexEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            proguardFile("proguard-google-api-client.pro")
        }
        getByName("debug") {
            buildConfigField(
                "String",
                "GOOGLE_VERIFICATION_API_KEY",
                "\"" + googleVerificationApiKey + "\""
            )
            the<CrashlyticsExtension>().mappingFileUploadEnabled = false
            multiDexEnabled = true
        }
    }
}

dependencies {
    // Espresso Test Dependencies
    androidTestImplementation(libs.junit.ktx)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.rules)
    // JUnit Test Dependencies
    testImplementation(libs.junit)

    // Firebase Dependencies (Global/App) + BoM
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.crashlytics.ktx)
    implementation(libs.firebase.analytics.ktx)
    implementation(libs.firebase.core)
    implementation(libs.firebase.config.ktx)
    implementation(libs.firebase.perf)
    // Barcode Tools
    // TODO: Deprecated. Migrate to https://developers.google.com/ml-kit/vision/barcode-scanning/android
    implementation(libs.play.services.vision)
    implementation(libs.firebase.ml.vision)
    // Vehicle Mileage Tracker
    implementation(libs.firebase.database.ktx)

    // Global Dependencies
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(libs.appupdater)
    implementation(libs.helperlib)
    implementation(libs.multidex)
    implementation(libs.legacy.support.v4)
    implementation(libs.appcompat)
    implementation(libs.fragment.ktx)
    implementation(libs.core.ktx)
    implementation(libs.preference.ktx)
    implementation(libs.cardview)
    implementation(libs.material)
    implementation(libs.gson)
    implementation(libs.commons.io)
    implementation(libs.commons.lang3)
    // Specific Utility Dependencies
    // Navbar Utility
    implementation(libs.picasso)
    implementation(libs.colorpicker)
    // ORD Countdown Utility
    implementation(libs.circleprogress)
    // Application List Utility
    implementation(libs.mpandroidchart)
    // TODO: material-dialogs 2.0.0 breaks a lot of stuff, find time to migrate shit over
    implementation(libs.core)
    implementation(libs.materialscrollbar)
    // SafetyNet Validator
    implementation(libs.play.services.safetynet)
    // Barcode Tools
    implementation(libs.zxing.core)
    // Lyric Finder
    implementation(libs.palette.ktx)
    // Vehicle Mileage Tracker
    implementation(libs.play.services.auth)
    implementation(libs.floatingactionbutton)
    // CEPAS Reader
    implementation(libs.cepaslib)
    // APKMirror Downloader
    implementation(libs.android.advancedwebview) //WebView
    implementation(libs.jsoup) //HTML Parser
    // GPA Calculator
    implementation(libs.eventbus)
    // Unicode Emoticon
    implementation(libs.viewpager2)
    // Others
    implementation(libs.constraintlayout)
    implementation(libs.biometric)
    implementation(libs.attribouter) {
        exclude(group = "com.google.android", module = "flexbox")
    }
    implementation(libs.flexbox)
}

apply(plugin = libs.plugins.google.services.get().pluginId)
