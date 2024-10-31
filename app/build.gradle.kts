plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "gt.edu.umg.dangermap"
    compileSdk = 34

    defaultConfig {
        applicationId = "gt.edu.umg.dangermap"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.play.services.maps)

    // Agregar dependencias de Glide
    implementation(libs.glide)
    annotationProcessor(libs.compiler)
    implementation(libs.play.services.location) // O la versión más reciente
    implementation(libs.play.services.maps)
    implementation(libs.recyclerview)


    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}