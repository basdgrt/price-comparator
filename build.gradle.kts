plugins {
    kotlin("jvm") version "2.1.20"
}

group = "com.github.basdgrt"
version = "0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("io.arrow-kt:arrow-stack:2.1.0"))

    implementation("io.arrow-kt:arrow-core")
    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("ch.qos.logback:logback-classic:1.5.18")
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.3")
    implementation("org.jsoup:jsoup:1.17.2")
    implementation("org.telegram:telegrambots:6.9.7.1")
    implementation("org.yaml:snakeyaml:2.2")

    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.13.9")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
