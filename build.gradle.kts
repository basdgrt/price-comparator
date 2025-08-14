plugins {
    kotlin("jvm") version "2.1.20"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.github.basdgrt"
version = "0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("io.arrow-kt:arrow-stack:2.1.0"))

    implementation("io.arrow-kt:arrow-core")
    implementation("io.arrow-kt:arrow-fx-coroutines")
    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("ch.qos.logback:logback-classic:1.5.18")
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.3")
    implementation("org.jsoup:jsoup:1.17.2")
    implementation("org.telegram:telegrambots:6.9.7.1")
    implementation("org.yaml:snakeyaml:2.2")

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.7.3")

    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.13.9")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

tasks.shadowJar {
    archiveBaseName.set("price-comparator")
    archiveClassifier.set("")
    archiveVersion.set("")
    mergeServiceFiles()
    manifest {
        attributes(mapOf(
            "Main-Class" to "com.github.basdgrt.MainKt"
        ))
    }

    // Ensure resources are included in the fat jar
    from("src/main/resources") {
        include("*.yaml")
        into("resources")
    }
}
