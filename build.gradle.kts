import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    id("com.gradleup.shadow") version "8.3.6"
}

val majorVersion = "1.8.3"
val buildNumber = System.getenv("BUILD_NUMBER") ?: "LOCAL"
val buildVersion = "DEV-$buildNumber"
val release = "Release"

group = "me.clip"
version = "$majorVersion-$buildVersion"

repositories {
    mavenCentral()

    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.extendedclip.com/releases/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.6")

    implementation("com.github.cryptomorin:XSeries:13.2.0")
}

tasks {
    processResources {
        eachFile { expand("version" to project.version) }
    }

    build {
        dependsOn("shadowJar")
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    withType<ShadowJar> {
        relocate("com.cryptomorin.xseries", "me.clip.deluxetags.libs.xseries")
        archiveFileName.set("DeluxeTags-${project.version}.jar")
    }
}

configurations {
    testImplementation {
        extendsFrom(compileOnly.get())
    }
}