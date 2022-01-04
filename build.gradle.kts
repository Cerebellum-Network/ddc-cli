plugins {
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.allopen") version "1.6.10"
    id("io.quarkus") version "2.5.0.Final"
}

repositories {
    mavenLocal()
    mavenCentral()

    maven { url = uri("https://jitpack.io") }

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
    implementation("io.quarkus:quarkus-vertx")
    implementation("io.quarkus:quarkus-vertx-http")

    // Crypto
    implementation("com.google.crypto.tink:tink:1.5.0")
    implementation("com.github.cerebellum-network:ddc-encryption-impl-kotlin:1.5.0")
    implementation("cash.z.ecc.android:kotlin-bip39:1.0.2")
    implementation("commons-codec:commons-codec:1.15")
    implementation("com.github.yeeco:schnorrkel-java:v1.0.4")
    implementation("org.web3j:crypto:5.0.0")
    implementation("net.i2p.crypto:eddsa:0.3.0")

    // Smallrye
    implementation("io.smallrye.reactive:smallrye-mutiny-vertx-core:$smallryeMutinyVertx")
    implementation("io.smallrye.reactive:smallrye-mutiny-vertx-web-client:$smallryeMutinyVertx")

    // DDC
    implementation("com.github.cerebellum-network:ddc-client-kotlin:1.2.0.Final")
    api("com.github.cerebellum-network.cere-ddc-sdk-kotlin:core:0.2.0.Final")
    api("com.github.cerebellum-network.cere-ddc-sdk-kotlin:object-storage:0.2.0.Final")

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
