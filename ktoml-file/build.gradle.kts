import com.akuleshov7.buildutils.configureSigning

import org.jetbrains.kotlin.gradle.targets.jvm.tasks.KotlinJvmTest

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("io.kotest.multiplatform")
    id("com.akuleshov7.buildutils.publishing-configuration")
}

kotlin {
    explicitApi()

    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }

    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
    }

    mingwX64()
    linuxX64()
    macosX64()
    macosArm64()
    ios()
    iosSimulatorArm64()

    sourceSets {
        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
        }

        val commonMain by getting {
            dependencies {
                implementation("com.squareup.okio:okio:${Versions.OKIO}")
                implementation("org.jetbrains.kotlin:kotlin-stdlib:${Versions.KOTLIN}")
                implementation(project(":ktoml-core"))
                api(project(":ktoml-source"))
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation("io.kotest:kotest-framework-engine:${Versions.KOTEST}")
            }
        }

        val jvmTest by getting {
            dependsOn(commonTest)
            dependencies {
                implementation(kotlin("test-junit5"))
            }
        }

        all {
            languageSettings.enableLanguageFeature("InlineClasses")
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.akuleshov7"
            artifactId = "ktoml-file"
            version = version
            from(components["kotlin"])
        }
    }
}

configureSigning()

tasks.withType<KotlinJvmTest> {
    useJUnitPlatform()
}

// ios tests on github are behaving differently than locally - as github moves resources to a different directory
// so, as it is not critical, skipping them
tasks.withType<org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest> {
    if (this.name.contains("ios")) {
        this.enabled = false
    }
}
