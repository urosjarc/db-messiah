plugins {
    `java-library`
    kotlin("jvm") version "1.9.22"
    id("com.adarshr.test-logger") version "4.0.0"
    `jvm-test-suite`
}

group = "com.urosjarc"
version = "0.1.0"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(19)
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

}



testing {
    suites {

        /**
         * Chared configuration
         */
        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter()
                dependencies {
                    implementation("org.jetbrains.kotlin:kotlin-test")
                }
            }
        }

        /**
         * Unit tests
         */
        val test by getting(JvmTestSuite::class) {}

        /**
         * E2E tests
         */
        register<JvmTestSuite>("e2e") {
            dependencies { implementation(project()) }
            targets {
                all {
                    testTask.configure {
                        shouldRunAfter(test)
                    }
                }
            }
        }
    }
}

testlogger {
    this.setTheme("mocha")
}
