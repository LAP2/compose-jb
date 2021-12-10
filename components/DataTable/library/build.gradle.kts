import org.jetbrains.compose.compose
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.compose.ComposeBuildConfig

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("maven-publish")
}

kotlin {
    jvm("desktop")

    sourceSets {
        named("commonMain") {
            dependencies {
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material)
                api("org.jetbrains.compose.ui:ui-util:${ComposeBuildConfig.composeVersion}")
            }
        }
        named("desktopMain") {}
        named("commonTest") {
//            dependencies {
//                implementation(kotlin("test"))
//            }
        }
    }
}

// TODO it seems that argument isn't applied to the common sourceSet. Figure out why
tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
}

configureMavenPublication(
    groupId = "org.jetbrains.compose.components",
    artifactId = "components-data-table",
    name = "Data table for Compose JB"
)
