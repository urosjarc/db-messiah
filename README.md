<h1 align="center">db-messiah</h1>
<h3 align="center">Kotlin lib. for enterprise database development</h3>
<table width="100%" border="0">
    <tr>
        <td width="33%">
            <h3 align="center"><a href="https://github.com/urosjarc/db-messiah-extra">db-messiah-extra</a></h3>
            <p align="center">Extra utils for db-messiah. Support for kotlinx.datetime types, etc... </p>
        </td>
        <td width="33%" align="center">
                <p><a>Get started</a></p>
                <p><a>Tutorials</a></p>
                <p><a>Configuration</a></p>
                <p><a>Specifications</a></p>
                <p><a>Features</a></p>
                <p><a>Motivation</a></p>
        </td>
        <td width="33%">
            <h3 align="center"><a href="https://github.com/urosjarc/db-analyser">db-analyser</a></h3>
            <p align="center">GUI for helping you inspect and write very complex JOIN statements in SQL or db-messiah.
        </td>
    </tr>
</table>
<br><br>

<h2 align="center">Get started</h2>

```kotlin
implementation("com.urosjarc:db-messiah:0.0.1")                     // Required
implementation("com.urosjarc:db-messiah-extra:0.0.1")               // Optional extra utils
implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.20.0") // Optional logging

// Pick your database driver
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

<h3 align="center">Domain</h3>

```kotlin
data class Parent(
    var pk: Int? = null, // Auto-incremental primary key
    val value: String    // NOT NULL column
)

data class Child(
    val pk: Int,         // Non auto-incremental primary key
    val parent_pk: Int,  // Foreign key, NOT NULL
    val value: String?   // Column with posibility of NULL
)
```

<h3 align="center">Database</h3>

```kotlin
// SCHEMA DEFINITION
val serializer = SqliteSerializer(
    globalSerializers = BasicTS.sqlite,
    tables = listOf(
        Table(Parent::pk),
        Table(Child::pk, foreignKeys = listOf(
            Child::parent_pk to Parent::class
        )),
    ),
)

// JDBC CONFIG
val config = Properties().apply {
    this["jdbcUrl"] = "jdbc:sqlite::memory:"
    this["username"] = "root"
    this["password"] = "root"
}

val sqlite = SqliteService(
    config = config,
    ser = serializer
)
```

<h3 align="center">Operations</h3>

```kotlin
sqlite.autocommit {
    //CREATE
    it.table.create<Parent>()
    it.table.create<Child>()

    //INSERT
    val parent = Parent(value = "Hello World!")
    it.row.insert(row = parent)
    assert(parent.pk != null)
    
    //INSERT
    val child = Child(pk = 1, parent_pk = parent.pk, value = "Hello World!")
    it.row.insert(row = child)

    //SELECT
    val parents = it.table.select<Parent>()
    assert(parents.contains(parent))
    
    //UPDATE
    parent.value = "How are you?"
    it.table.update(parent)
    
    //WHERE
    val someChildren = it.query.get(output = Child::class, input = parent) { 
        """ ${it.SELECT<Child>()} WHERE ${it.column(Child::value)} = ${it.input(Parent::value)} """
    }
    assert(someChildren.size > 0)
    
    //JOIN
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
<h3 align="center">Transactions</h3>

```kotlin
service.transaction { // Any exception inside will trigger rollback ALL!
    ...
    val savePoint1 = it.roolback.savePoint()
    ...
    val savePoint2 = it.roolback.savePoint()
    ...
    it.roolback.to(point = savePoint2)
}

service.transaction(isolation = Isolation.READ_UNCOMMITTED) {
    ...
}
```

<h3 align="center">Custom Serializers</h3>

```kotlin
val TIMESTAMP = TypeSerializer<Instant>(
    kclass = Instant::class,
    dbType = "TIMESTAMP",
    jdbcType = JDBCType.TIMESTAMP,
    decoder = { rs, i, info -> rs.getTimestamp(i).toInstant().toKotlinInstant() },
    encoder = { ps, i, x -> ps.setTimestamp(i, Timestamp.from(x.toJavaInstant())) }
)

//Apply your custom serializer to global serializers
val serializer = SqliteSerializer(
    globalSerializers = BasicTS.sqlite + listOf(TIMESTAMP)
    tables = listOf( ... ),
)
```

<h2 align="center">Tutorials</h2>

<table width="100%">
    <tr>
        <td width="33.3%"></td>
        <td width="33.3%" align="center">
                <p>Sqlite: <a>basics</a> / <a>queries</a></p>
                <p>Postgresql: <a>basics</a> / <a>queries</a></p>
                <p><a>Primary keys</a></p>
                <p><a>Constraints</a></p>
                <p><a>Transactions</a></p>
                <p><a>Procedures</a></p>
                <p><a>Indexing and Profiling</a></p>
                <p><a>Exceptions</a></p>
                <p><a>Custom type serializers</a></p>
                <p><a>Custom database serializers</a></p>
        </td>
        <td width="33.3%"></td>
    </tr>
</table>

<h2 align="center">Configuration</h2>

<h3 align="center">JDBC</h3>

Service config property object is passed on initialization directly to the [HikariCP](https://github.com/brettwooldridge/HikariCP) library
which handles everything around database [connection configuration](https://github.com/brettwooldridge/HikariCP?tab=readme-ov-file#gear-configuration-knobs-baby).

<h3 align="center">Logging</h3>

The recommend logging configuration is located in `src/test/resources/log4j2.xml` where you can
find logger for non-blocking Async Rolling log files (one log file per session) and non-blocking async console logger with pretty
padding for maximum readability. For detailed explanation read about [Log4j Appenders](https://logging.apache.org/log4j/2.x/manual/appenders.html).
