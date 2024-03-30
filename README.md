<h1 align="center">db-messiah</h1>
<h3 align="center">Kotlin lib. for enterprise database development</h3>
<p align="center"><b>+290 unit, +210 e2e, +30 initialization, +22 tutorial, +8 README.md tests</b></p>
<br>
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
                <p><a href="#features">Features</a></p>
                <p><a href="#specifications">Specifications</a></p>
        </td>
        <td width="33%">
            <h3 align="center"><a href="https://github.com/urosjarc/db-analyser">db-analyser</a></h3>
            <p align="center">GUI for db analysis, to help you create complex JOIN statements for SQL or db-messiah.
        </td>
    </tr>
</table>
<br>
<br>

<h2 align="center">Get started</h2>

```kotlin
/** DEPENDENCIES */

implementation("com.urosjarc:db-messiah:0.0.1") // Required
implementation("com.urosjarc:db-messiah-extra:0.0.1") // Optional
implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.20.0") //Optional

/** DRIVERS */

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

<br><h3 align="center">Primary keys</h3>

```kotlin
/** TYPE SAFE ID */

@JvmInline
value class Id<T>(val value: Int) {
    override fun toString(): String = this.value.toString()
}

/** TYPE SAFE UID */

@JvmInline
value class UId<T>(val value: UUID = UUID.randomUUID()) {
    override fun toString(): String = this.value.toString()
}
```

<br><h3 align="center">Domain</h3>

```kotlin
/** PARENT */

data class Parent(
    var pk: Id<Parent>? = null, // INTEGER Auto-incremental primary key
    var value: String           // NOT NULL column
)

/** CHILD */

data class Child(
    val pk: UId<Child> = UId(), // UUID Manual primary key
    val parent_pk: Id<Parent>,  // Foreign key, NOT NULL
    val value: String?          // NULL column
)

/** UNSAFE */

data class Unsafe(
//  var pk: Int? = null,                 // Unsafe INTEGER auto-incremental primary key
    val pk: UUID = UUID.randomUUID(),    // Unsafe UUID manual primary key
    val created: Instant = Instant.now() // Support for java.time.*
)
```

<br><h3 align="center">Database</h3>

```kotlin
/** SCHEMA */

val serializer = SqliteSerializer(
    globalSerializers = BasicTS.sqlite + listOf(
        //        constructor    deconstructor
        IdTS.int({ Id<Any>(it) }, { it.value }), // Serializer for Id<T>
        //                 constructor
        IdTS.uuid.sqlite({ UId<Any>(it) })        // Serializer for UId<T>
    ),
    tables = listOf(
        Table(Parent::pk),
        Table(
            Child::pk,
            foreignKeys = listOf( // Foreign keys
                Child::parent_pk to Parent::class
            ),
            constraints = listOf( // Column constraints
                Child::parent_pk to listOf(C.UNIQUE, C.CASCADE_DELETE, C.CASCADE_UPDATE)
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

<br><h3 align="center">Diagrams</h3>

<table width="100%">
<tr>
<td width="50%">

```kotlin
/** PlantUML */

File("./build/db.plantuml").writeText(
    serializer.plantUML(
        withPrimaryKey = true,
        withForeignKeys = true,
        withOtherColumns = false
    )
)
```

</td> <td width="50%"> <img width="100%" src="https://github.com/urosjarc/db-messiah/blob/master/docs/chinook.png"> </td> </tr>
</table>

<br><h3 align="center">Operations</h3>

```kotlin
sqlite.autocommit {

    Profiler.active = true // Activate profiler

    /** CREATE */

    it.table.create<Parent>()
    it.table.create<Child>()

    /** INSERT */

    val parent = Parent(value = "Hello World!")
    it.row.insert(row = parent)
    assert(parent.pk != null)

    /** INSERT */

    val child = Child(pk = UId(), parent_pk = parent.pk!!, value = "child value")
    it.row.insert(row = child)

    /** SELECT */

    val parents = it.table.select<Parent>()
    assert(parents.contains(parent))

    /** UPDATE */

    parent.value = "child value"
    it.row.update(parent)

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

    Profiler.active = false // Deactivate profiler
}
```

<br><h3 align="center">Transactions</h3>

```kotlin
sqlite.transaction { // Any exception inside will trigger rollback ALL!
    //...
    val savePoint = it.roolback.savePoint()
    //...
    it.roolback.to(point = savePoint)
    //...
}

/** ISOLATION */

sqlite.transaction(isolation = Isolation.READ_UNCOMMITTED) {
    //...
}
```

<br><h3 align="center">Serializers</h3>

```kotlin
/** SERIALIZE: Instant(TIMESTAMP) */

val DURATION = TypeSerializer(
    kclass = Duration::class,
    dbType = "INTEGER",
    jdbcType = JDBCType.INTEGER,
    decoder = { rs, i, _ -> Duration.ofSeconds(rs.getLong(i)) },
    encoder = { ps, i, x -> ps.setLong(i, x.toSeconds()) }
)

/** REGISTRATION */

SqliteSerializer(
    globalSerializers = BasicTS.sqlite + JavaTimeTS.sqlite + listOf(DURATION),
    tables = listOf(Table(Unsafe::pk))
)
```

<br><h3 align="center">Profiler</h3>

```kotlin
/** TOP 5 SLOWEST QUERIES */

val top5 = Profiler.logs.values
    .filter { !it.sql.contains("DROP TABLE") }
    .filter { !it.sql.contains("CREATE TABLE") }
    .sortedByDescending { it.duration / it.repetitions }
    .subList(0, 5)

for (ql in top5) {
    println("\nQuery: ${ql.sql}")
    println("  * Type: ${ql.type}")
    println("  * Exec: ${ql.repetitions}")
    println("  * Time: ${ql.duration / ql.repetitions}")
}

/**
Query: SELECT * FROM "main"."Parent"
 * Type: QUERY
 * Exec: 1
 * Time: 318.53us

Query: INSERT INTO "main"."Parent" ("value") VALUES (?)
 * Type: INSERT
 * Exec: 1
 * Time: 317.288us

Query: INSERT INTO "main"."Child" ("pk", "parent_pk", "value") VALUES (?, ?, ?)
 * Type: UPDATE
 * Exec: 1
 * Time: 185.032us

Query: UPDATE "main"."Parent" SET "value" = ? WHERE "main"."Parent"."pk" = ?
 * Type: UPDATE
 * Exec: 1
 * Time: 153.494us

Query:  SELECT * FROM "main"."Child" WHERE "main"."Child"."value" = ?
 * Type: QUERY
 * Exec: 1
 * Time: 149.654us
 */
```

<br> <h3 align="center">Tutorials</h3>

<p align="center">
All tutorials are tested and defined as kotlin files inside <a href="https://github.com/urosjarc/db-messiah/tree/master/src/tutorials/kotlin">src/tutorials/kotlin</a> directory.<br>
Sample project is inside <a href="https://github.com/urosjarc/db-messiah/tree/master/src/chinook/kotlin">src/chinook/kotlin</a> directory and it implements<br>
for all supported databases famous <a href="https://github.com/lerocha/chinook-database">Chinook sample database</a>.<br>

</p>

<br> <h3 align="center">Configuration</h3>

<p align="center">
User provided config property object is on initialization passed directly to the <a href="https://github.com/brettwooldridge/HikariCP">HikariCP</a> library
<br>which handles everything around database <a href="https://github.com/brettwooldridge/HikariCP?tab=readme-ov-file#gear-configuration-knobs-baby">connection pooling and configuration</a>.
</p>

<br><h3 align="center">Testing</h3>
<p align="center">Only at initialization library uses <a href = "https://kotlinlang.org/docs/reflection.html">Kotlin reflection</a>, to introspect the user provided db structure.<br>
System will create <a href="https://github.com/urosjarc/db-messiah/blob/master/src/main/kotlin/com/urosjarc/dbmessiah/MapperCache.kt">mapping rules</a> on how kotlin classes will be mapped into db tables.<br>
System will also test resulting rules, in order to find any user errors or inconsistency.<br>
The list of all tests can be found inside <a href="https://github.com/urosjarc/db-messiah/blob/master/src/main/kotlin/com/urosjarc/dbmessiah/SerializerTests.kt">SerializerTests.kt</a>.<br>
System will for E2E testing use db servers defined inside docker <a href="https://github.com/urosjarc/db-messiah/tree/master/compose.yaml">compose.yaml</a> file.<br>
You can start start those servers with <code>docker-compose up</code>.

</p>


<br><h3 align="center">Logging</h3>

<p align="center">
The recommend logging configuration is located here <a href="https://github.com/urosjarc/db-messiah/blob/master/src/test/resources/log4j2.xml">src/test/resources/log4j2.xml</a>,<br>
where you can find logger for non-blocking Async Rolling log files (one log file per session)<br>
and non-blocking async console logger with pretty padding for maximum readability.<br>
For detailed explanation read about <a href="https://logging.apache.org/log4j/2.x/manual/appenders.html">Log4j Appenders</a>.
</p>

<br><h3 align="center">Features</h3>

|          | Escape |         Schema         |     Auto INT PK    |    Auto UUID PK    |  UUID column |    Many queries    |       Cascade      |        Procedure       |
|:--------:|:------:|:----------------------:|:------------------:|:------------------:|:------------:|:------------------:|:------------------:|:----------------------:|
|    DB2   |  "%s"  | :large_orange_diamond: | :white_check_mark: |         :x:        |   CHAR(36)   |         :x:        |         :x:        | :large_orange_diamond: |
|   Derby  |  "%s"  |   :white_check_mark:   | :white_check_mark: |         :x:        |   CHAR(36)   |         :x:        |         :x:        |           :x:          |
|    H2    |  "%s"  |   :white_check_mark:   | :white_check_mark: | :white_check_mark: |     UUID     |         :x:        | :white_check_mark: |           :x:          |
|   Maria  | \`%s\` |   :white_check_mark:   | :white_check_mark: |         :x:        |     UUID     |         :x:        |         :x:        |   :white_check_mark:   |
|  MS SQL  |  [%s]  |   :white_check_mark:   | :white_check_mark: |         :x:        | UNIQUEIDE... | :white_check_mark: |         :x:        |   :white_check_mark:   |
|   MySQL  | \`%s\` |   :white_check_mark:   | :white_check_mark: |         :x:        |   CHAR(36)   |         :x:        |         :x:        |   :white_check_mark:   |
|  Oracle  |  "%s"  | :large_orange_diamond: | :white_check_mark: |         :x:        | VARCHAR2(36) |         :x:        | :white_check_mark: |   :white_check_mark:   |
| Postgres |  "%s"  |   :white_check_mark:   | :white_check_mark: | :white_check_mark: |     UUID     | :white_check_mark: |      :ok_hand:     |           :x:          |
|  Sqlite  |  "%s"  |           :x:          | :white_check_mark: |         :x:        |   CHAR(36)   |         :x:        |         :x:        |           :x:          |

<br><br><h3 align="center">Type system</h3>

|      Class     |    COLUMN    |               Databases              |        db-messiah         |        db-messiah-extra         |
|:--------------:|:------------:|:------------------------------------:|:-------------------------:|:-------------------------------:|
|     Boolean    |     BOOL     |          :white_check_mark:          |    :white_check_mark:     |               :x:               |
|      Char      |     CHAR     |          :white_check_mark:          |    :white_check_mark:     |               :x:               |
|     String     | VARCHAR(100) |          :white_check_mark:          |    :white_check_mark:     |               :x:               |
|      Float     |     FLOAT    |          :white_check_mark:          |    :white_check_mark:     |               :x:               |
|     Double     |    DOUBLE    |          :white_check_mark:          |    :white_check_mark:     |               :x:               |
|  Byte / UByte  |    TINYINT   |          :white_check_mark:          |    :white_check_mark:     |               :x:               |
| Short / UShort |   SMALLINT   |          :white_check_mark:          |    :white_check_mark:     |               :x:               |
|   Int / Uint   |    INTEGER   |          :white_check_mark:          |    :white_check_mark:     |               :x:               |
|  Long / ULong  |    BIGINT    |          :white_check_mark:          |    :white_check_mark:     |               :x:               |
|     Instant    |   DATETIME   | Sqlite, Mysql, MSSql, Maria, H2, DB2 |  Java :white_check_mark:  |   kotlinx :white_check_mark:    |
|     Instant    |   TIMESTAMP  |        Derby, Postgres, Oracle       |  Java :white_check_mark:  |   kotlinx :white_check_mark:    |
|    LocalDate   |     DATE     |          :white_check_mark:          |  Java :white_check_mark:  |   kotlinx :white_check_mark:    |
|    LocalTime   |     TIME     |     :white_check_mark: but Oracle    |  Java :white_check_mark:  |   kotlinx :white_check_mark:    |
|    LocalTime   | NUMBER(5, 0) |                Oracle                |  Java :white_check_mark:  |   kotlinx :white_check_mark:    |>

<br><br><h2 align="center">Specifications</h3>

<p align="center">

</p>

<br><br><h3 align="center">PRIMARY KEY</h3>
<img width="100%" src="https://github.com/urosjarc/db-messiah/blob/master/docs/constraints/PRIMARY_KEY.png">

<br><br><h3 align="center">INSERT ROW</h3>
<img width="100%" src="https://github.com/urosjarc/db-messiah/blob/master/docs/queries/ROW_INSERT.png">

<br><br><h3 align="center">INSERT BATCH</h3>
<img width="100%" src="https://github.com/urosjarc/db-messiah/blob/master/docs/queries/BATCH_INSERT.png">


<br><br><h3 align="center">UPDATE / DELETE</h3>
<table align="center" width="100%">
    <tr>
        <td width="25%" align="center">ROW UPDATE</td>
        <td width="25%" align="center">BATCH UPDATE</td>
        <td width="25%" align="center">ROW DELETE</td>
        <td width="25%" align="center">BATCH DELETE</td>
    </tr>
    <tr>
        <td width="25%"><img width="100%" src="https://github.com/urosjarc/db-messiah/blob/master/docs/queries/ROW_UPDATE.png"></td>
        <td width="25%"><img width="100%" src="https://github.com/urosjarc/db-messiah/blob/master/docs/queries/BATCH_UPDATE.png"></td>
        <td width="25%"><img width="100%" src="https://github.com/urosjarc/db-messiah/blob/master/docs/queries/ROW_DELETE.png"></td>
        <td width="25%"><img width="100%" src="https://github.com/urosjarc/db-messiah/blob/master/docs/queries/BATCH_DELETE.png"></td>
    </tr>
</table>

<br><br><h3 align="center">CONSTRAINTS</h3>
<table align="center" width="100%">
    <tr>
        <td width="25%" align="center">CASCADE DELETE</td>
        <td width="25%" align="center">CASCADE UPDATE</td>
        <td width="25%" align="center">NOT NULL</td>
        <td width="25%" align="center">UNIQUE</td>
    </tr>
    <tr>
        <td width="25%"><img width="100%" src="https://github.com/urosjarc/db-messiah/blob/master/docs/constraints/CASCADE_DELETE.png"></td>
        <td width="25%"><img width="100%" src="https://github.com/urosjarc/db-messiah/blob/master/docs/constraints/CASCADE_UPDATE.png"></td>
        <td width="25%"><img width="100%" src="https://github.com/urosjarc/db-messiah/blob/master/docs/constraints/NOT_NULL.png"></td>
        <td width="25%"><img width="100%" src="https://github.com/urosjarc/db-messiah/blob/master/docs/constraints/UNIQUE.png"></td>
    </tr>
</table>
