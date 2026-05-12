import java.net.URL
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

// Carrega local.properties manualmente (Gradle não faz isso por padrão).
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) load(file.inputStream())
}

android {
    namespace = "com.example.pi_maya"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.pi_maya"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Supabase config — preencha em local.properties:
        // SUPABASE_URL=https://seu-projeto.supabase.co
        // SUPABASE_ANON_KEY=eyJh...
        // (URL e ANON_KEY do Supabase ainda são necessários para o realtime do chat,
        //  que vai direto cliente ↔ Supabase. Tudo o mais passa pela API.)
        val supabaseUrl = localProperties.getProperty("SUPABASE_URL", "")
        val supabaseAnonKey = localProperties.getProperty("SUPABASE_ANON_KEY", "")
        val apiUrl = localProperties.getProperty("API_URL", "")
        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"$supabaseAnonKey\"")
        buildConfigField("String", "API_URL", "\"$apiUrl\"")
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
        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    androidResources {
        // .task do MediaPipe não pode ser comprimido pelo APK builder
        noCompress += listOf("task", "tflite")
    }
}

// ----------------------------------------------------------------------------
// Download automático do modelo MediaPipe Pose Landmarker (Lite)
// ~5MB. Roda apenas se o arquivo ainda não existe.
// ----------------------------------------------------------------------------
val mediapipeModelUrl =
    "https://storage.googleapis.com/mediapipe-models/pose_landmarker/pose_landmarker_lite/float16/1/pose_landmarker_lite.task"
val mediapipeModelFile =
    layout.projectDirectory.file("src/main/assets/pose_landmarker_lite.task").asFile

val downloadPoseModel by tasks.registering {
    val outputFile = mediapipeModelFile
    val url = mediapipeModelUrl
    outputs.file(outputFile)
    onlyIf { !outputFile.exists() }
    doLast {
        outputFile.parentFile.mkdirs()
        logger.lifecycle("Baixando modelo MediaPipe Pose Landmarker...")
        URL(url).openStream().use { input ->
            outputFile.outputStream().use { output -> input.copyTo(output) }
        }
        logger.lifecycle("Modelo salvo em ${outputFile.path} (${outputFile.length() / 1024} KB)")
    }
}

androidComponents {
    onVariants { variant ->
        // Garante que o modelo seja baixado antes de empacotar os assets
        afterEvaluate {
            tasks.named("merge${variant.name.replaceFirstChar { it.uppercase() }}Assets") {
                dependsOn(downloadPoseModel)
            }
        }
    }
}

dependencies {
    // Core / UI
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.fragment)
    implementation(libs.recyclerview)
    implementation(libs.swiperefresh)
    implementation(libs.viewpager2)
    implementation(libs.preference)

    // Lifecycle / ViewModel / LiveData
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)
    implementation(libs.lifecycle.runtime)
    implementation(libs.lifecycle.process)

    // Navigation
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)

    // Image loading
    implementation(libs.glide)

    // Security
    implementation(libs.security.crypto)
    implementation(libs.biometric)

    // CameraX (para Fase 3 - MediaPipe)
    implementation(libs.camera.core)
    implementation(libs.camera.camera2)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.view)

    // MediaPipe Tasks Vision (para Fase 3)
    implementation(libs.mediapipe.tasks.vision)

    // Desugaring para java.time
    coreLibraryDesugaring(libs.desugar)

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
