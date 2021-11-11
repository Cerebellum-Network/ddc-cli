plugins {
    kotlin("jvm") version "1.5.10"
    kotlin("plugin.allopen") version "1.5.10"
    id("io.quarkus") version "2.1.2.Final"
}

repositories {
    mavenLocal()
    mavenCentral()

    maven { url = uri("https://jitpack.io") }
}

val smallryeMutinyVertx = "2.9.0"
dependencies {
    implementation(enforcedPlatform("io.quarkus:quarkus-bom:2.1.2.Final"))
    implementation("io.quarkus:quarkus-config-yaml")
    implementation("io.quarkus:quarkus-picocli")
    implementation("io.quarkus:quarkus-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-vertx")

    // Crypto
    implementation("com.google.crypto.tink:tink:1.5.0")
    implementation("com.github.cerebellum-network:ddc-encryption-impl-kotlin:1.5.0")

    // Smallrye
    implementation("io.smallrye.reactive:smallrye-mutiny-vertx-core:$smallryeMutinyVertx")
    implementation("io.smallrye.reactive:smallrye-mutiny-vertx-web-client:$smallryeMutinyVertx")

    // DDC
    implementation("com.github.cerebellum-network:ddc-client-kotlin:1.1.3.Final")

    // JSON
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    testImplementation("io.quarkus:quarkus-junit5")
}

group = "com.github.cerebellum-network"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

allOpen {
    annotation("javax.ws.rs.Path")
    annotation("javax.enterprise.context.ApplicationScoped")
    annotation("io.quarkus.test.junit.QuarkusTest")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
    kotlinOptions.javaParameters = true
}
