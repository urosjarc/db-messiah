plugins {
    `java-library`
    `maven-publish`
    `jvm-test-suite`
    kotlin("jvm") version "1.9.22"
    id("com.adarshr.test-logger") version "4.0.0"
    id("org.jetbrains.kotlinx.kover") version "0.7.6"
}

group = "com.urosjarc"
version = "0.0.1-SNAPSHOT"

kotlin {
    jvmToolchain(19)
}

repositories {
    mavenCentral()
}

testlogger {
    this.setTheme("mocha")
}

dependencies {
    implementation(kotlin("reflect"))
    //Pool connection
    this.implementation("com.zaxxer:HikariCP:5.1.0")
    this.implementation("org.apache.logging.log4j:log4j-api-kotlin:1.4.0")
}

testing {
    suites {
        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter()
                dependencies {
                    this.implementation("org.jetbrains.kotlin:kotlin-test")
                    this.implementation("org.apache.logging.log4j:log4j-core:2.17.1")
                    this.implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.20.0")
                    this.implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.20.0")
                }
            }
        }

        register<JvmTestSuite>("e2e") {
            dependencies {
                this.implementation(project())
                this.implementation("org.jetbrains.lets-plot:lets-plot-kotlin-jvm:4.6.0")
                this.implementation("com.zaxxer:HikariCP:5.1.0")
                this.implementation("io.dropwizard.metrics:metrics-core:4.2.25")
                this.runtimeOnly("com.ibm.db2:jcc:11.5.9.0")
                this.runtimeOnly("com.h2database:h2:2.2.224")
                this.runtimeOnly("org.apache.derby:derby:10.17.1.0")
                this.runtimeOnly("org.mariadb.jdbc:mariadb-java-client:3.3.2")
                this.runtimeOnly("org.xerial:sqlite-jdbc:3.44.1.0")
                this.runtimeOnly("com.mysql:mysql-connector-j:8.2.0")
                this.runtimeOnly("com.microsoft.sqlserver:mssql-jdbc:12.4.2.jre11")
                this.runtimeOnly("org.postgresql:postgresql:42.7.1")
            }
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = rootProject.group as String
            artifactId = rootProject.name
            version = rootProject.version as String
            from(components["java"])

            pom {
                name = "Db Messiah"
                description = "Kotlin lib. for enterprise database development"
                url = "https://github.com/urosjarc/db-messiah"
                licenses {
                    license {
                        name = "The Apache License, Version 2.0"
                        url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                    }
                }
                developers {
                    developer {
                        id = "urosjarc"
                        name = "Uroš Jarc"
                        email = "jar.fmf@gmail.com"
                    }
                }
            }
        }
    }
}

/**
 * Test coverage for unit and e2e tests
 */

val e2eSources = listOf(
    "**/dbmessiah/impl/**",
    "**/dbmessiah/domain/queries/**",
    "**/dbmessiah/Driver.class",
    "**/dbmessiah/Service.class",
)

val noLogicSources = listOf(
    "**/dbmessiah/exceptions/**",
    "**/dbmessiah/types/**",
    "**/dbmessiah/Schema.class",
    "**/dbmessiah/TransConn.class",
)

koverReport {
    filters {
        // filters for all reports
    }

    verify {
        // verification rules for all reports
    }

    defaults {
        xml { /* default XML report config */ }
        html { /* default HTML report config */ }
        verify { /* default verification config */ }
        log { /* default logging config */ }
    }
}
