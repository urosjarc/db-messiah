import org.jetbrains.dokka.DokkaConfiguration.Visibility

plugins {
    `java-library`
    `maven-publish`
    `jvm-test-suite`
    kotlin("jvm") version "1.9.22"
    id("org.jetbrains.dokka") version "1.9.10"
    id("com.adarshr.test-logger") version "4.0.0"
    id("org.jetbrains.kotlinx.kover") version "0.7.6"
}

group = "com.urosjarc"
version = "0.0.2-SNAPSHOT"

kotlin {
    explicitApi()
    jvmToolchain(19)
}

repositories {
    mavenCentral()
}

testlogger {
    this.setTheme("mocha")
}
tasks.register<GradleBuild>("github") {
    this.group = "verification"
    this.tasks = listOf("test", "demo", "e2e")
}

tasks.dokkaHtml {
    dokkaSourceSets {
        configureEach {
            documentedVisibilities.set(
                setOf(
                    Visibility.PUBLIC, // Same for both Kotlin and Java
                    Visibility.PRIVATE, // Same for both Kotlin and Java
                    Visibility.PROTECTED, // Same for both Kotlin and Java
                    Visibility.INTERNAL, // Kotlin-specific internal modifier
                    Visibility.PACKAGE, // Java-specific package-private visibility
                )
            )
            includeNonPublic.set(true)
            jdkVersion.set(19)
            reportUndocumented.set(true)
            skipEmptyPackages.set(false)
        }
    }
}

dependencies {
    implementation(kotlin("reflect"))
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.apache.logging.log4j:log4j-api-kotlin:1.4.0")
}

testing {
    suites {
        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter()
                dependencies {
                    implementation("org.jetbrains.kotlin:kotlin-test")
                    implementation("org.apache.logging.log4j:log4j-core:2.17.1")
                    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.20.0")
                    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.20.0")
                }
            }
        }

        register<JvmTestSuite>("e2e") {
            dependencies {
                implementation(project())
                runtimeOnly("com.ibm.db2:jcc:11.5.9.0")
                runtimeOnly("com.h2database:h2:2.2.224")
                runtimeOnly("org.apache.derby:derby:10.17.1.0")
                runtimeOnly("org.mariadb.jdbc:mariadb-java-client:3.3.2")
                runtimeOnly("org.xerial:sqlite-jdbc:3.44.1.0")
                runtimeOnly("com.mysql:mysql-connector-j:8.2.0")
                runtimeOnly("com.microsoft.sqlserver:mssql-jdbc:12.4.2.jre11")
                runtimeOnly("org.postgresql:postgresql:42.7.1")
            }
        }

        register<JvmTestSuite>("demo") {
            dependencies {
                implementation(project())
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
                runtimeOnly("org.xerial:sqlite-jdbc:3.44.1.0")
                runtimeOnly("org.postgresql:postgresql:42.7.1")
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
