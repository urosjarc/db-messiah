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
                implementation(project())
                this.implementation("org.jetbrains.lets-plot:lets-plot-kotlin-jvm:4.6.0")
                this.implementation("org.jetbrains.lets-plot:lets-plot-image-export:4.2.0")
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

testlogger {
    this.setTheme("mocha")
}

kotlin {
    jvmToolchain(19)
}
