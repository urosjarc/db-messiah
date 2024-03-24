# db-messiah

Kotlin lib. for enterprise database development

## Setup

In the setup you will have to provide JDBC driver for database that you will be using...
Here is a list of drivers that this library uses for E2E tests. Please use `runtimeOnly`
since this drivers are only needed at runtime and we don't need their API interfaces.

```kotlin 
runtimeOnly("com.ibm.db2:jcc:11.5.9.0")
runtimeOnly("com.h2database:h2:2.2.224")
runtimeOnly("org.apache.derby:derby:10.17.1.0")
runtimeOnly("org.mariadb.jdbc:mariadb-java-client:3.3.2")
runtimeOnly("org.xerial:sqlite-jdbc:3.44.1.0")
runtimeOnly("com.mysql:mysql-connector-j:8.2.0")
runtimeOnly("com.microsoft.sqlserver:mssql-jdbc:12.4.2.jre11")
runtimeOnly("org.postgresql:postgresql:42.7.1")
runtimeOnly("com.oracle.database.jdbc:ojdbc11:23.3.0.23.09")
```

## Logging
When you are using library for the first time you will be greeted with following message...

```shell

ERROR StatusLogger Log4j2 could not find a logging implementation. Please add log4j-core to the classpath. Using SimpleLogger to log to the console...
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
```

Library uses Log4j interface for logging across the sistem.
But since this is only an interface you will have to provide your implementation where
actual logging will be happening, this error is asking joust that...
So you have to add logging implementation, recommender implementation is...

```kotlin
implementation("org.apache.logging.log4j:log4j-slf4j2-impl:XX.XX.XX")
```

After you add implementation you have to add configuration to the log4j logging system...
The recommend configuration is located in `src/test/resources/log4j2.xml` where you can
find logger for:

* Async Rolling log files (one log file per session)
* Async Console logger with pretty padding for maximum readability.
