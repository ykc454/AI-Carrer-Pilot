import java.util.Properties
val localProperties = Properties().apply {
    load(rootProject.file("local.properties").inputStream())
}

val gemini_api_key = localProperties.getProperty("GEMINI_API_KEY")
    ?: throw GradleException("API_KEY not found in local.properties")

val newsApiKey = localProperties.getProperty("NEWS_API_KEY")
    ?: throw GradleException("News_Api_Key not found")

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.plugin)
    alias(libs.plugins.ksp.plugin)
    id("com.google.gms.google-services")
}
android {
    namespace = "com.example.aicareerpilot"
    compileSdk = 36 // Standard for 2026

    defaultConfig {
        applicationId = "com.example.aicareerpilot"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    buildTypes {
        debug {

            buildConfigField(
                "String",
                "GEMINI_API_KEY",
                "\"$gemini_api_key\""
            )

            buildConfigField(
                "String",
                "NEWS_API_KEY",
                "\"$newsApiKey\""
            )
        }

        release {

            buildConfigField(
                "String",
                "GEMINI_API_KEY",
                "\"$gemini_api_key\""
            )

            buildConfigField(
                "String",
                "NEWS_API_KEY",
                "\"$newsApiKey\""
            )
        }
    }
}

dependencies {

    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Material Icons
    implementation("androidx.compose.material:material-icons-extended:1.7.8")

    // Splash Screen
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Markdown Renderer
    implementation("com.mikepenz:multiplatform-markdown-renderer-m3:0.33.0")

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.ui.text)
    ksp(libs.hilt.compiler)

    // Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Room Database
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Gemini AI
//    implementation(libs.google.ai.client)
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")

    // PDF
    implementation(libs.pdfbox.android)

    // Lottie Animation
    implementation(libs.lottie.compose)

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.9.0"))
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-auth-ktx")

    // Google Sign In (Credential Manager)
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    // Testing
    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    debugImplementation(libs.androidx.compose.ui.tooling)

    //Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")


    // DOCX/DOC Support
    implementation("org.apache.poi:poi:5.2.5")
    implementation("org.apache.poi:poi-ooxml:5.2.5")

    // Needed for Android compatibility
    implementation("org.apache.xmlbeans:xmlbeans:5.1.1")

    implementation("com.squareup.retrofit2:converter-scalars:2.11.0")
}