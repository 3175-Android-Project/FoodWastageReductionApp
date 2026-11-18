import java.net.URI
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

val localSettings = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.isFile) file.inputStream().use { load(it) }
}
val verificationUrl = providers.gradleProperty("grabbitVerificationUrl")
    .orElse(localSettings.getProperty("grabbitVerificationUrl", ""))
    .get().trim().trimEnd('/')
if (verificationUrl.isNotEmpty()) {
    val uri = URI(verificationUrl)
    require(uri.host != null && uri.userInfo == null && uri.query == null && uri.fragment == null &&
            (uri.scheme == "https" || (uri.scheme == "http" && uri.host == "10.0.2.2"))) {
        "grabbitVerificationUrl must be an HTTPS server or the Android emulator's 10.0.2.2 address."
    }
}

android {
    namespace = "com.project.foodwastagereductionapp"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.project.foodwastagereductionapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        val releaseVerificationUrl = verificationUrl.takeIf { it.startsWith("https://") }.orEmpty()
        buildConfigField("String", "EMAIL_VERIFICATION_URL", "\"$releaseVerificationUrl\"")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            if (providers.gradleProperty("grabbitAccountTestBuild").orNull == "true") {
                applicationIdSuffix = ".accounttest"
            }
            buildConfigField("String", "EMAIL_VERIFICATION_URL", "\"$verificationUrl\"")
        }
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    implementation(libs.room.runtime)
    implementation(libs.shimmer)
    implementation(libs.exifinterface)
    annotationProcessor(libs.room.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}

tasks.register<Exec>("startEmailServer") {
    group = "application"
    description = "Start the local Grabbit email verification server for the emulator."
    enabled = verificationUrl == "http://10.0.2.2:8765"
    workingDir(rootProject.projectDir)
    commandLine("python3", "-B", rootProject.file("email_server/start.py"))
}
