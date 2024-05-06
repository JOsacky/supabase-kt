import com.android.build.gradle.internal.lint.AndroidLintAnalysisTask
import com.android.build.gradle.internal.lint.LintModelWriterTask
import com.android.build.gradle.internal.tasks.LintModelMetadataTask

plugins {
    id(libs.plugins.kotlin.multiplatform.get().pluginId)
    id(libs.plugins.android.library.get().pluginId)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
}

description = "Extends supabase-kt with a Apollo GraphQL Client"

repositories {
    mavenCentral()
}

@OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
kotlin {
    applyDefaultHierarchyTemplate()
    jvmToolchain(8)
    jvm()
    androidTarget {
        publishLibraryVariants("release", "debug")
    }
    js(IR) {
        browser {
            testTask {
                enabled = false
            }
        }
        nodejs {
            testTask {
                enabled = false
            }
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    sourceSets {
        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
            languageSettings.optIn("io.github.jan.supabase.annotations.SupabaseInternal")
            languageSettings.optIn("io.github.jan.supabase.annotations.SupabaseExperimental")
            compilerOptions.freeCompilerArgs.add("-Xexpect-actual-classes")
        }
        val commonMain by getting {
            dependencies {
                api(compose.ui)
                implementation(project(":gotrue-kt"))
                implementation(compose.material3)
            }
        }
        val nonJvmMain by creating {
            dependsOn(commonMain)
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.androidsvg)
            }
        }
        val iosMain by getting {
            dependsOn(nonJvmMain)
        }
        val jsMain by getting {
            dependsOn(nonJvmMain)
        }
    }
}

android {
    compileSdk = 34
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    namespace = "io.github.jan.supabase.compose.auth.ui.library"
    defaultConfig {
        minSdk = 21
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

//see https://github.com/JetBrains/compose-multiplatform/issues/4739
tasks.withType<LintModelWriterTask> {
    dependsOn("generateResourceAccessorsForAndroidUnitTest")
}
tasks.withType<LintModelMetadataTask> {
    dependsOn("generateResourceAccessorsForAndroidUnitTest")
}
tasks.withType<AndroidLintAnalysisTask> {
    dependsOn("generateResourceAccessorsForAndroidUnitTest")
}