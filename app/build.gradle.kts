plugins {
    alias(libs.plugins.android.library)
}

val pluginName = "GeoLocationAndroid"
val pluginPackageName = "org.godotengine.plugin.android.geolocationgodot"

android {
    namespace = "org.godotengine.plugin.android.geolocationgodot"
    compileSdk = 34

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        manifestPlaceholders += mapOf()
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        manifestPlaceholders["godotPluginName"] = pluginName
        manifestPlaceholders["godotPluginPackageName"] = pluginPackageName
        buildConfigField("String", "GODOT_PLUGIN_NAME", "\"${pluginName}\"")
        setProperty("archivesBaseName", pluginName)
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildToolsVersion = "34.0.0"
    ndkVersion = "27.0.11902837 rc2"
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.godot)
    implementation(libs.play.services)
}