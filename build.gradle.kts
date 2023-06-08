plugins {
    kotlin("multiplatform") version "1.8.0"
    id("maven-publish")
}

group = "io.violabs"
version = "2.0.0"

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    sourceSets {
        val commonMain by getting
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("io.mockk:mockk:1.13.5")
                implementation("org.mockito:mockito-core:5.1.1")
                // https://mvnrepository.com/artifact/org.mockito.kotlin/mockito-kotlin
                implementation("org.mockito.kotlin:mockito-kotlin:4.1.0")
            }
        }
        val jvmTest by getting
    }
}
