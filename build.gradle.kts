import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    id("com.gradleup.shadow") version "9.2.2"
}

val majorVersion = "1.8.4"
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
    compileOnly("org.spigotmc:spigot-api:1.21.9-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.6")

    implementation("com.github.cryptomorin:XSeries:13.7.0")
    implementation("net.kyori:adventure-text-minimessage:4.17.0")
    implementation("net.kyori:adventure-text-serializer-legacy:4.17.0")

    testImplementation("junit:junit:4.13.2")
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
        relocate("net.kyori", "me.clip.deluxetags.libs.kyori")
        archiveFileName.set("DeluxeTags-${project.version}.jar")
    }
}

configurations {
    testImplementation {
        extendsFrom(compileOnly.get())
    }
}