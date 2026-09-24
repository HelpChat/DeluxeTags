import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    id("com.gradleup.shadow") version "9.2.2"
}

val majorVersion = "1.10.0"
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

val modernSpigotApi = configurations.create("modernSpigotApi") {
    isCanBeConsumed = false
    isCanBeResolved = true
}

dependencies {
    // Compile against the oldest supported Bukkit API. Newer, optional APIs are
    // accessed through compatibility helpers so accidental baseline bumps fail
    // at compile time.
    compileOnly("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT") {
        // The API's historical Bungee chat snapshot is no longer published.
        // DeluxeTags does not use that package.
        exclude(group = "net.md-5", module = "bungeecord-chat")
    }
    compileOnly("me.clip:placeholderapi:2.11.6")

    implementation("com.github.cryptomorin:XSeries:13.7.0")
    implementation("net.kyori:adventure-text-minimessage:4.17.0")
    implementation("net.kyori:adventure-text-serializer-legacy:4.17.0")
    implementation("com.zaxxer:HikariCP:4.0.3")
    implementation("com.mysql:mysql-connector-j:9.7.0")
    implementation("org.slf4j:slf4j-nop:1.7.36")

    testImplementation("junit:junit:4.13.2")
    modernSpigotApi("org.spigotmc:spigot-api:1.21.9-R0.1-SNAPSHOT")
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
        relocate("com.zaxxer.hikari", "me.clip.deluxetags.libs.hikari")
        relocate("com.mysql", "me.clip.deluxetags.libs.mysql")
        relocate("com.google.protobuf", "me.clip.deluxetags.libs.protobuf")
        relocate("org.slf4j", "me.clip.deluxetags.libs.slf4j")
        mergeServiceFiles()
        archiveFileName.set("DeluxeTags-${project.version}.jar")
    }

    val modernTest by registering(Test::class) {
        description = "Runs the test suite with the modern Spigot 1.21.9 API"
        group = "verification"
        dependsOn(testClasses)
        testClassesDirs = sourceSets.test.get().output.classesDirs
        classpath = files(
            sourceSets.test.get().output,
            sourceSets.main.get().output,
            modernSpigotApi,
            configurations.testRuntimeClasspath.get().filterNot {
                it.name.startsWith("spigot-api-1.8.8-")
            }
        )
        exclude("**/MySqlIntegrationTest.class", "**/ShadedStorageTest.class")
    }

    test {
        exclude("**/MySqlIntegrationTest.class", "**/ShadedStorageTest.class")
    }

    val mysqlIntegrationTest by registering(Test::class) {
        description = "Tests real MySQL using DELUXETAGS_MYSQL_DATABASE/HOST/PORT/USERNAME/PASSWORD"
        group = "verification"
        dependsOn(testClasses)
        testClassesDirs = sourceSets.test.get().output.classesDirs
        classpath = sourceSets.test.get().runtimeClasspath
        include("**/MySqlIntegrationTest.class")
        onlyIf { !System.getenv("DELUXETAGS_MYSQL_DATABASE").isNullOrBlank() }
        outputs.upToDateWhen { false }
    }

    val shadedStorageTest by registering(Test::class) {
        description = "Verifies isolated JDBC/pool loading from the distributable jar"
        group = "verification"
        dependsOn(testClasses, shadowJar)
        testClassesDirs = sourceSets.test.get().output.classesDirs
        classpath = sourceSets.test.get().runtimeClasspath
        include("**/ShadedStorageTest.class")
        systemProperty("deluxetags.shadedJar", shadowJar.get().archiveFile.get().asFile.absolutePath)
        inputs.file(shadowJar.get().archiveFile)
    }

    check {
        dependsOn(modernTest)
        dependsOn(shadedStorageTest)
    }
}

configurations {
    testImplementation {
        extendsFrom(compileOnly.get())
    }
}
