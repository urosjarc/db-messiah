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

    //Drivers
    this.runtimeOnly("org.xerial:sqlite-jdbc:3.44.1.0")
    this.runtimeOnly("com.mysql:mysql-connector-j:8.2.0")
    this.runtimeOnly("com.microsoft.sqlserver:mssql-jdbc:12.4.2.jre11")
    this.runtimeOnly("org.postgresql:postgresql:42.7.1")

    //Pool connection
    this.implementation("com.zaxxer:HikariCP:5.1.0")

    //Tracking metrics
    this.implementation("io.dropwizard.metrics:metrics-core:4.2.25")

    //Logging api
    this.implementation("org.apache.logging.log4j:log4j-api-kotlin:1.4.0")

    //TODO For testing
    this.implementation("org.apache.logging.log4j:log4j-core:2.17.1") //TODO: Remove me on production
    this.implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.20.0")
    this.implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.20.0")

    //Testing
    this.testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(19)
}
