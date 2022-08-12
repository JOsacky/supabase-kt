plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose") version Versions.COMPOSE
}

repositories {
    mavenCentral()
    google()
}

group = "io.github.jan-tennert.supacompose.web"
version = Versions.SUPACOMPOSE

kotlin {
    js("web", IR) {
        browser()
        binaries.executable()
    }
    sourceSets {
        val webMain by getting {
            dependencies {
                api(project(":Supacompose-Auth"))
                api(compose.web.core)
                api(compose.runtime)
            }
        }
    }
}