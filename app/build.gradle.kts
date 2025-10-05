plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.fitgym"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.fitgym"
        minSdk = 34
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(platform("com.google.firebase:firebase-bom:33.16.0"))
    implementation("com.google.firebase:firebase-analytics")

    // Autenticazione (serve a registrare/login utenti)
    implementation("com.google.firebase:firebase-auth-ktx")

    // Firestore (salva dati utenti in un database)
    implementation("com.google.firebase:firebase-firestore-ktx")

    // WorkManager per le notifiche
    implementation("androidx.work:work-runtime-ktx:2.9.1")



    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.junit.ktx)
    androidTestImplementation(libs.androidx.junit) //Instrumented Test
    androidTestImplementation(libs.androidx.espresso.core) //Instrumented Test
    testImplementation(libs.junit) //Unit Test
    testImplementation(kotlin("test")) //Unit Test
    androidTestImplementation ("androidx.test.espresso:espresso-contrib:3.6.0")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1")


}