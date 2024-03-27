<h1 align="center">db-messiah</h1>
<h3 align="center">Kotlin lib. for enterprise database development</h3>
<br>
<table width="100%" border="0">
    <tr>
        <td width="33%">
            <h3 align="center"><a href="https://github.com/urosjarc/db-messiah-extra">db-messiah-extra</a></h3>
            <p align="center">Extra utils for db-messiah. Support for kotlinx.datetime types, etc... </p>
        </td>
        <td width="33%" align="center">
                <p><a href="#get-started">Get started</a></p>
                <p><a href="#tutorials">Tutorials</a></p>
                <p><a href="#configuration">Configuration</a></p>
                <p><a href="#specifications">Specifications</a></p>
                <p><a href="#motivation">Motivation</a></p>
        </td>
        <td width="33%">
            <h3 align="center"><a href="https://github.com/urosjarc/db-analyser">db-analyser</a></h3>
            <p align="center">GUI for helping you inspect and write very complex JOIN statements in SQL or db-messiah.
        </td>
    </tr>
</table>
<br>

<h2 align="center">Get started</h2>

<h3>Dependencies</h3>

```kotlin
implementation("com.urosjarc:db-messiah:0.0.1")                     // Required
implementation("com.urosjarc:db-messiah-extra:0.0.1")               // Optional extra utils
implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.20.0") // Optional logging
```


<h3>Drivers</h3>

```kotlin
runtimeOnly("org.xerial:sqlite-jdbc:3.44.1.0") // Lets continue with sqlite driver...
runtimeOnly("com.ibm.db2:jcc:11.5.9.0")
runtimeOnly("com.h2database:h2:2.2.224")
runtimeOnly("org.apache.derby:derby:10.17.1.0")
runtimeOnly("org.mariadb.jdbc:mariadb-java-client:3.3.2")
runtimeOnly("com.mysql:mysql-connector-j:8.2.0")
runtimeOnly("com.microsoft.sqlserver:mssql-jdbc:12.4.2.jre11")
runtimeOnly("org.postgresql:postgresql:42.7.1")
runtimeOnly("com.oracle.database.jdbc:ojdbc11:23.3.0.23.09")
```

<h3>Domain</h3>

```kotlin

/** PARENT */

data class Parent(
    var pk: Int? = null, // Auto-incremental primary key
    val value: String    // NOT NULL column
)

/** CHILD */

data class Child(
    val pk: Int,         // Non auto-incremental primary key
    val parent_pk: Int,  // Foreign key, NOT NULL
    val value: String?   // Column with posibility of NULL
)
```

<h3>Database</h3>

```kotlin

/** SCHEMA */

val serializer = SqliteSerializer(
    globalSerializers = BasicTS.sqlite,
    tables = listOf(
        Table(Parent::pk),
        Table(
            Child::pk, foreignKeys = listOf(
                Child::parent_pk to Parent::class
            )
        ),
    ),
)

/** CONFIG */

val config = Properties().apply {
    this["jdbcUrl"] = "jdbc:sqlite::memory:"
    this["username"] = "root"
    this["password"] = "root"
}

/** SERVICE */

val sqlite = SqliteService(
    config = config,
    ser = serializer
)
```

<h3>Operations</h3>

```kotlin
sqlite.autocommit {
    
    /** CREATE */
    
    it.table.create<Parent>()
    it.table.create<Child>()

    /** INSERT */
    
    val parent = Parent(value = "Hello World!")
    it.row.insert(row = parent)
    assert(parent.pk != null)

    /** INSERT */
    
    val child = Child(pk = 1, parent_pk = parent.pk, value = "Hello World!")
    it.row.insert(row = child)

    /** SELECT */
    
    val parents = it.table.select<Parent>()
    assert(parents.contains(parent))

    /** UPDATE */
    
    parent.value = "How are you?"
    it.table.update(parent)

    /** WHERE */
    
    val someChildren = it.query.get(output = Child::class, input = parent) {
        """ ${it.SELECT<Child>()} WHERE ${it.column(Child::value)} = ${it.input(Parent::value)} """
    }
    assert(someChildren.size > 0)

    /** JOIN */
    
    val moreChildren = it.query.get(output = Child::class, input = parent) {
        """
            ${it.SELECT<Child>()}
            INNER JOIN ${it.table<Parent>()} ON ${it.column(Parent::pk)} = ${it.column(Child::parent_pk)}
            WHERE ${it.column(Parent::value)} = ${it.input(Parent::value)}
        """
    }
    assert(moreChildren.size > 0)
}
```

<h3>Transactions</h3>

```kotlin
service.transaction { // Any exception inside will trigger rollback ALL!
    //...
    val savePoint1 = it.roolback.savePoint()
    //...
    val savePoint2 = it.roolback.savePoint()
    //...
    it.roolback.to(point = savePoint2)
    //...
}

/** ISOLATION */

service.transaction(isolation = Isolation.READ_UNCOMMITTED) {
    //...
}
```

<h3>Custom Serializers</h3>

```kotlin

/** SERIALIZE: Instant(TIMESTAMP) */

val TIMESTAMP = TypeSerializer<Instant>(
    kclass = Instant::class,
    dbType = "TIMESTAMP",
    jdbcType = JDBCType.TIMESTAMP,
    decoder = { rs, i, info -> rs.getTimestamp(i).toInstant().toKotlinInstant() },
    encoder = { ps, i, x -> ps.setTimestamp(i, Timestamp.from(x.toJavaInstant())) }
)

/** REGISTRATION */

val serializer = SqliteSerializer(
    globalSerializers = BasicTS.sqlite + listOf(TIMESTAMP)
            tables = listOf ( ... ),
)
```

<br>
<h3 align="center">Tutorials</h3>

<p align="center">
All tutorials are defined as kotlin files inside <a href="https://github.com/urosjarc/db-messiah/tree/master/src/tutorials/kotlin">src/tutorials/kotlin</a> directory.
<br>All tutorials are subjects of e2e testing suite.
</p>

<br>
<h3 align="center">Configuration</h3>

<p align="center">
Service config property object is passed on initialization directly to the <a href="https://github.com/brettwooldridge/HikariCP">HikariCP</a> library
<br>which handles everything around database <a href="https://github.com/brettwooldridge/HikariCP?tab=readme-ov-file#gear-configuration-knobs-baby">connection configuration</a>.
</p>

<br>
<h3 align="center">Logging</h3>

<p align="center">
The recommend logging configuration is located in <code>src/test/resources/log4j2.xml</code> where you can
<br>find logger for non-blocking Async Rolling log files (one log file per session) and non-blocking async console logger with pretty
<br>padding for maximum readability. For detailed explanation read about <a href="https://logging.apache.org/log4j/2.x/manual/appenders.html">Log4j Appenders</a>.
</p>
