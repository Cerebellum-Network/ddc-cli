plugins {
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.allopen") version "1.6.10"
    id("io.quarkus") version "2.5.0.Final"
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://jitpack.io")

    flatDir {
        dirs("libs")
    }
}

val smallryeMutinyVertx = "2.9.0"
dependencies {
    implementation(kotlin("stdlib"))

    implementation(enforcedPlatform("io.quarkus:quarkus-bom:2.5.0.Final"))
    implementation("io.quarkus:quarkus-config-yaml")
    implementation("io.quarkus:quarkus-picocli")
    implementation("io.quarkus:quarkus-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.quarkus:quarkus-arc")

    // Crypto
    implementation("com.google.crypto.tink:tink:1.5.0")
    implementation("com.github.cerebellum-network:ddc-encryption-impl-kotlin:1.5.0")
    implementation("cash.z.ecc.android:kotlin-bip39:1.0.2")
    implementation("commons-codec:commons-codec:1.15")
    implementation("com.github.yeeco:schnorrkel-java:v1.0.4")
    implementation("com.github.komputing.kethereum:bip39:0.85.3")
    implementation("com.github.komputing.kethereum:bip32:0.85.3")
    implementation("com.github.komputing.kethereum:model:0.85.3")

    // DDC
    api("com.github.Cerebellum-Network.cere-ddc-sdk-kotlin:core:1.0.4.Final")
    api("com.github.Cerebellum-Network.cere-ddc-sdk-kotlin:proto:1.0.4.Final")
    api("com.github.Cerebellum-Network.cere-ddc-sdk-kotlin:content-addressable-storage:1.0.4.Final")
    api("com.github.Cerebellum-Network.cere-ddc-sdk-kotlin:key-value-storage:1.0.4.Final")

    //Ktor
    implementation("io.ktor:ktor-client-java:1.6.5")

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
