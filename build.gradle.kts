plugins {
    `java-library`
    kotlin("jvm") version "1.9.22"
}

group = "com.urosjarc"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("reflect"))

    this.implementation("com.zaxxer:HikariCP:5.1.0")
    this.implementation("org.apache.logging.log4j:log4j-api:2.17.2")
    this.implementation("org.apache.logging.log4j:log4j-api-kotlin:1.2.0")
    this.implementation("org.apache.logging.log4j:log4j-core:2.17.1")

    this.testImplementation("com.mockrunner:mockrunner-core:2.0.7")
    this.testImplementation("com.mockrunner:mockrunner-jdbc:2.0.7")
    this.testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(19)
}
