import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java`
    id("com.github.johnrengelman.shadow") version "8.1.0"
}

val majorVersion = "1.8.3"
val buildVersion = "DEV-" + System.getenv("BUILD_NUMBER")
val release = "Release"

group = "me.clip"
version = "$majorVersion-$buildVersion"

repositories {
    mavenCentral()

    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.19.4-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.3")

    implementation("com.github.cryptomorin:XSeries:9.3.0")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks {
    processResources {
        eachFile { expand("version" to project.version) }
    }

    build {
        dependsOn("shadowJar")
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