plugins {
    kotlin("jvm") version "2.3.10"
    id("com.gradleup.shadow") version "8.3.0"
    id("xyz.jpenilla.run-paper") version "2.3.1"
    `maven-publish`
}

group = "io.zlero"
version = "1.0.5"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))

    implementation("org.jetbrains.exposed:exposed-core:0.44.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.44.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.44.1")

    implementation("org.xerial:sqlite-jdbc:3.43.0.0")
    implementation("com.mysql:mysql-connector-j:8.1.0")
    implementation("com.h2database:h2:2.2.224")

    implementation("com.zaxxer:HikariCP:5.1.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
}

val targetJavaVersion = 21
kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks {
    runServer {
        minecraftVersion("1.20.4")
    }

    shadowJar {
        archiveBaseName.set("CRFramework")
        archiveClassifier.set("")
        archiveVersion.set(version.toString())

        // 드라이버류만 relocate (다른 플러그인과 충돌 방지)
        // Exposed / kotlinx-coroutines 는 공개 API에 노출되므로 원본 경로 유지
        relocate("org.xerial.sqlite", "io.zlero.cRFramework.libs.sqlite")
        relocate("com.zaxxer.hikari", "io.zlero.cRFramework.libs.hikari")

        dependencies {
            exclude(dependency("org.jetbrains:annotations"))
        }

        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    build {
        dependsOn(shadowJar)
    }

    jar {
        enabled = false
    }

    processResources {
        val props = mapOf("version" to version)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId    = "io.zlero"
            artifactId = "CRFramework"
            version    = project.version.toString()
            artifact(tasks.shadowJar)
        }
    }
}