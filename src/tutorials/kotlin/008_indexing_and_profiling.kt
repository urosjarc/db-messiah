package indexing_and_profiling

import com.urosjarc.dbmessiah.Profiler
import com.urosjarc.dbmessiah.domain.Table
import com.urosjarc.dbmessiah.impl.sqlite.SqliteSerializer
import com.urosjarc.dbmessiah.impl.sqlite.SqliteService
import com.urosjarc.dbmessiah.serializers.AllTS
import java.util.*

/**
 * This library will not provide API for managing indexes since it would encourage
 * premature creation of unnecessary indexes at the creation time.
 * Indexes take additional disk space, and they can slow down update operations (UPDATE, INSERT, DELETE).
 * Many times unnecessary indexes are created by developer and then forgotten forever. Indexing your database
 * is a delicate process which must be done with care and attention, that's why library provides specialized
 * Profiler class which can monitor and profile all executed queries. Profiler will log type of the queries, execution time and
 * how many times specific query was executed in the service lifetime. Ideally you would run whole E2E suite of tests
 * and check profiling logs once finished. Base on those logs you would create indexes and repeat the E2E tests to check if the performance
 * of your queries increased. You would repeat this exact process multiple times until satisfied performance is achieved.
 * Let's see how would you do this in practice.
 */

/**
 * Define your database
 */
data class Parent(var pk: Int? = null, var value: String)

val service = SqliteService(
    config = Properties().apply { this["jdbcUrl"] = "jdbc:sqlite::memory:" },
    ser = SqliteSerializer(tables = listOf(Table(Parent::pk)), globalSerializers = AllTS.sqlite)
)

fun indexing_and_profiling() {

    /**
     * Profiler is by default in non-active state, to start measuring metrics
     * you have to activate the flag.
     */
    Profiler.active = true

    service.autocommit { comm ->
        /**
         * Execute some queries...
         */
        repeat(20) {
            comm.table.drop<Parent>()
            comm.table.create<Parent>()
            comm.row.insert(row = Parent(value = "Hello World"))
            comm.table.select<Parent>()
        }
    }

    /**
     * Once you are done you can deactivate the flag.
     */
    Profiler.active = false

    /**
     * Now let's examine Profiler logs.
     * Profiler logs is a hash map composed of keys which represents hash code of SQL statement,
     * and of value which is object who is holding accumulated metrics for that specific SQL statement.
     * For example if query "SELECT * FROM Parent WHERE Parent.col = ?" is executed once with duration of 10 ms and then again executed
     * with the same duration the query log will hold the sum of those values...
     *
     * QueryLog(
     *     type = QUERY,
     *     duration = 20ms,
     *     repetitions=2,
     *     sql="SELECT * FROM Parent WHERE Parent.col = ?"
     * )
     *
     * Because of this accumulation feature you can use Profiler in long-running experiments
     * without the possibility of out of memory exception.
     */
    println("\n\nStatistics of all executed queries:\n")
    for((hash, ql) in Profiler.logs) {
        println("\tQuery with signature[$hash] and type '${ql.type}' was executed ${ql.repetitions} times with average time ${ql.duration / ql.repetitions}")
        println("\tQuery SQL: ${ql.sql}")
        break
    }

    /**
     * Here is additional example on how you would get top 10 slowest queries by average execution time...
     */
    val top10 = Profiler.logs.values
        .filter { !it.sql.contains("DROP TABLE") }
        .sortedByDescending { it.duration / it.repetitions }

    println("\n\nStatistics of top 10 slowest queries by execution time:\n")
    for(ql in top10) {
        println("\tQuery with signature[${ql.hashCode()}] and type '${ql.type}' was executed ${ql.repetitions} times with average time ${ql.duration / ql.repetitions}")
        println("\tQuery SQL: ${ql.sql}")
        println()
    }



}
