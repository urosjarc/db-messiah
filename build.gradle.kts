plugins {
    kotlin("jvm") version "1.9.21"
}

group = "com.urosjarc"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("reflect"))

    this.implementation("com.zaxxer:HikariCP:5.1.0")
    this.implementation("org.apache.logging.log4j:log4j-api:2.17.2")
    this.implementation("org.apache.logging.log4j:log4j-api-kotlin:1.2.0")
    this.testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(19)
}
