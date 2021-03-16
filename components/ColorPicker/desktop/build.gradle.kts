import org.jetbrains.compose.compose

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

kotlin {
    jvm {}
    sourceSets {
        named("jvmMain") {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(project(":ColorPicker:common"))
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "org.jetbrains.compose.colorpicker.demo.MainKt"
    }
}