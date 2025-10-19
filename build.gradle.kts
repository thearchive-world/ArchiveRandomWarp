plugins {
    `java`
    id("com.gradleup.shadow") version "9.2.2"
}

group = "archive.rtp"
version = project.property("plugin_version")!!.toString()
val apiVersion = project.property("api_version").toString()

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }
    maven("https://jitpack.io") {
        name = "jitpack"
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:$apiVersion-R0.1-SNAPSHOT")
    compileOnly("com.github.CodingAir:WarpSystem-API:5.1.6")
    compileOnly("com.github.CodingAir:CodingAPI:1.97")
}

val targetJavaVersion = 21
java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    }
}

tasks {
    withType(JavaCompile::class).configureEach {
        options.encoding = "UTF-8"
        options.release.set(targetJavaVersion)
    }

    processResources {
        val projectVersion = project.version
        filesMatching("plugin.yml") {
            val props = mapOf(
                "version" to projectVersion,
                "api_version" to apiVersion
            )
            filteringCharset = "UTF-8"
            expand(props)
        }
    }

    jar {
        enabled = false
    }

    shadowJar {
        archiveClassifier = ""
        relocate("de.codingair.codingapi", "de.codingair.warpsystem.lib.codingapi")
    }

    build {
        dependsOn(shadowJar)
    }
}


