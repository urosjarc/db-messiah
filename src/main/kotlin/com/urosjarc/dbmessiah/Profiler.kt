package com.urosjarc.dbmessiah

import com.urosjarc.dbmessiah.Profiler.Companion.logs
import com.urosjarc.dbmessiah.data.BatchQuery
import com.urosjarc.dbmessiah.data.Query
import com.urosjarc.dbmessiah.domain.QueryLog
import kotlin.time.measureTimedValue

/**
 * Measures and stores the execution time of a query.
 * @property active Flag that tells if profiler is in active state of logging.
 * @property logs The list of all query logs that were executed on the current service.
 */
public class Profiler {
    /**
     * The [Companion] class represents a collection of utility methods for logging query execution time.
     *
     * @property active Indicates whether the profiler is in the active logging state.
     * @property logs A mutable map that stores the query log entries.
     */
    public companion object {
        public var active: Boolean = false
        public val logs: MutableMap<Int, QueryLog> = mutableMapOf()

        /**
         * Executes the provided code block and measures execution time.
         *
         * @param query The [BatchQuery] object representing the batch query.
         * @param code The code block to be executed.
         * @return The result of the code block.
         */
        public fun <T> logBatch(query: BatchQuery, code: () -> T): T =
            this.log(type = QueryLog.Type.BATCH, sql = query.sql, repetitions = query.valueMatrix.size, code)

        /**
         * Executes the provided code block and measures execution time.
         *
         * @param query The [Query] object representing the execution query.
         * @param code The code block to be executed.
         * @return The result of the code block.
         */
        public fun <T> logUpdate(query: Query, code: () -> T): T =
            this.log(type = QueryLog.Type.UPDATE, sql = query.sql, repetitions = 1, code)

        /**
         * Executes the provided code block and measures execution time.
         *
         * @param query The [Query] object representing the execution query.
         * @param code The code block to be executed.
         * @return The result of the code block.
         */
        public fun <T> logInsert(query: Query, code: () -> T): T =
            this.log(type = QueryLog.Type.INSERT, sql = query.sql, repetitions = 1, code)

        /**
         * Executes the provided code block and measures execution time.
         *
         * @param query The [Query] object representing the execution query.
         * @param code The code block to be executed.
         * @return The result of the code block.
         */
        public fun <T> logExecute(query: Query, code: () -> T): T =
            this.log(type = QueryLog.Type.EXECUTE, sql = query.sql, repetitions = 1, code)

        /**
         * Executes the provided code block and measures execution time.
         *
         * @param query The [Query] object representing the execution query.
         * @param code The code block to be executed.
         * @return The result of the code block.
         */
        public fun <T> logQuery(query: Query, code: () -> T): T =
            this.log(type = QueryLog.Type.QUERY, sql = query.sql, repetitions = 1, code)

        /**
         * Executes the provided code block and measures execution time.
         *
         * @param query The [Query] object representing the execution query.
         * @param code The code block to be executed.
         * @return The result of the code block.
         */
        public fun <T> logCall(query: Query, code: () -> T): T =
            this.log(type = QueryLog.Type.CALL, sql = query.sql, repetitions = 1, code)

        /**
         * Measures and stores the execution time of a query if the Profiler is in active state.
         *
         * @param type The type of the query log.
         * @param sql The SQL string for the query.
         * @param repetitions The number of repetitions for the query.
         * @param code The code block to be measured.
         * @return The result of the code block.
         */
        private fun <T> log(type: QueryLog.Type, sql: String, repetitions: Int, code: () -> T): T {
            if (!this.active) return code()

            val (returned, duration) = measureTimedValue { code() }

            val ql = QueryLog(
                type = type,
                duration = duration,
                sql = sql,
                repetitions = 0
            )

            val log = this.logs.getOrPut(key = ql.hashCode()) { ql }

            log.repetitions += repetitions
            log.duration += duration

            return returned
        }
    }
}
