package com.urosjarc.dbmessiah

import com.urosjarc.dbmessiah.data.*
import com.urosjarc.dbmessiah.exceptions.DriverException
import org.apache.logging.log4j.kotlin.logger
import java.sql.*


/**
 * A class representing a database driver.
 *
 * @property conn The connection to the database.
 */
public open class Driver(private val conn: Connection) {

    /** @suppress */
    private val log = this.logger()

    /**
     * Prepares the given query by replacing question marks in the SQL template with the corresponding values
     * and setting the values to the provided PreparedStatement.
     *
     * @param ps The [PreparedStatement] object where the values are set.
     * @param query The [Query] object representing the SQL query and its values.
     */
    private fun prepareQuery(ps: PreparedStatement, query: Query) {
        var rawSql = query.sql
        //Apply values to prepared statement
        (query.values).forEachIndexed { i, queryValue: QueryValue ->
            rawSql = rawSql.replaceFirst("?", queryValue.escapped)
            if (queryValue.value == null) ps.setNull(i + 1, queryValue.jdbcType.vendorTypeNumber) //If value is null encoding is done with setNull function !!!
            //If value is not null encoding is done over user defined encoder !!!
            else {
                /**
                 * TODO: Properties from KClass.memberProperties are getting inline values from provided objects
                 * TODO: that are having null value inside (inline Id(val uuid: UUID)) and
                 * TODO: because of that if I do uuid.toString() inside Id class there will be NullPointerException.
                 */
                try {
                    (queryValue.encoder as Encoder<Any?>)(ps, i + 1, queryValue.value)
                } catch (e: NullPointerException){
                    ps.setNull(i+1, queryValue.jdbcType.vendorTypeNumber)
                }
            }
        }
        this.log.info("Prepare query: ${rawSql.replace("\\s+".toRegex(), " ")}")
    }

    /**
     * Tries to close the provided PreparedStatement and ResultSet objects.
     *
     * @param ps The [PreparedStatement] object to be closed.
     * @param rs The [ResultSet] object to be closed.
     */
    private fun closeAll(ps: PreparedStatement? = null, rs: ResultSet? = null) {
        try {
            ps?.close()
        } catch (e: Throwable) {
            this.log.error("Closing prepared statement failed", e)
        }
        try {
            rs?.close()
        } catch (e: Throwable) {
            this.log.error("Closing resultSet failed", e)
        }
    }

    /**
     * Creates batch groups of 1000 and then execute them one by one on the database connection.
     * Groups are created executed separately in order to not block database process.
     *
     * @param batchQuery The [BatchQuery] object representing the batch query to be executed.
     * @throws DriverException If number of updates does not match number of provided rows.
     * @throws DriverException If there is an error processing the batch query results.
     */
    internal fun batch(batchQuery: BatchQuery) {
        var ps: PreparedStatement? = null
        var numUpdates = 0
        val batchSize = batchQuery.valueMatrix.size

        try {
            //Prepare statement and query
            ps = conn.prepareStatement(batchQuery.sql)

            var i = 0
            for (values in batchQuery.valueMatrix) {
                this.prepareQuery(ps = ps, query = Query(sql = batchQuery.sql, values = values.toTypedArray()))
                ps.addBatch()
                if (++i % 1_000 == 0) {
                    val exeCount = Profiler.logBatch(query = batchQuery) { ps.executeBatch() }
                    numUpdates += exeCount.sum()
                    ps.clearParameters()
                    i = 0
                }
            }
            if (i > 0) {
                val exeCount = Profiler.logBatch(query = batchQuery) { ps.executeBatch() }
                numUpdates += exeCount.sum()
            }

            //Close everything
            this.closeAll(ps = ps)

            if (numUpdates != batchSize)
                throw DriverException("Driver reported unexpected number of updates '$numUpdates' for batch of '${batchSize}' rows")

        } catch (e: Throwable) {
            this.closeAll(ps = ps)
            throw DriverException(msg = "Failed to process batch results for: $batchQuery", cause = e)
        }
    }

    /**
     * Updates the database with the given query.
     *
     * @param query The [Query] object representing the SQL query and its values.
     * @return The number of rows affected by the update.
     * @throws DriverException If there is an error processing the update query.
     */
    public fun update(query: Query): Int {
        var ps: PreparedStatement? = null

        try {
            //Prepare statement
            ps = conn.prepareStatement(query.sql)
            this.prepareQuery(ps = ps, query = query)

            //Get info
            val count: Int = Profiler.logUpdate(query = query) { ps.executeUpdate() }

            //Close all
            this.closeAll(ps = ps)

            //Return count
            return count
        } catch (e: Throwable) {
            this.closeAll(ps = ps)
            throw DriverException(msg = "Failed to process update results for: $query", cause = e)
        }
    }

    /**
     * Inserts a record into the database based on the provided query and returns the generated primary key.
     * Method will try to fetch generated primary key normally over JDBC. If fetching primary key fails
     * or is not supported by database driver by some reason, new additional database call will be made which will try to
     * fetch primary key by force. SQL query that will try to fetch primary key by force is defined for each database
     * separately inside specific database [Serializer] which overrides [Serializer.selectLastId] string.
     *
     * @param query The [Query] object representing the SQL query and its values.
     * @param generatedKey The name of the generated key column, if any.
     * @param onGeneratedKeysFail The SQL statement to be executed if the generated key retrieval fails.
     * @param primaryColumn The [PrimaryColumn] object representing the primary column of the table.
     * @return The inserted data.
     * @throws DriverException If there is an error processing the insert query or retrieving the inserted data.
     */
    internal fun insert(query: Query, generatedKey: String?, onGeneratedKeysFail: String?, primaryColumn: PrimaryColumn): Any {
        var ps: PreparedStatement? = null
        var rs: ResultSet? = null
        var rs2: ResultSet? = null

        //Execute query
        try {
            //Prepare statement
            ps = if (generatedKey == null)
                conn.prepareStatement(query.sql, Statement.RETURN_GENERATED_KEYS)
            else conn.prepareStatement(query.sql, arrayOf(generatedKey))

            this.prepareQuery(ps = ps, query = query)

            //Get info
            val numUpdates = Profiler.logInsert(query = query) { ps.executeUpdate() }

            //If no updates happend close all (very low chance of this to happend since driver will create error!
            if (numUpdates == 0) throw DriverException(msg = "Failed to create any insert change with: $query")
            //Continue with getting ids for inserts
        } catch (e: Throwable) {
            this.closeAll(ps = ps)
            throw DriverException(msg = "Failed to process insert results from: $query", cause = e)
        }

        //Try fetching ids normaly
        try {
            rs = ps.generatedKeys
            if (rs.next()) {
                val data = primaryColumn.decode(rs, 1) ?: throw DriverException(msg = "Failed to decode primary key value from: $query")
                this.closeAll(ps = ps, rs = rs)
                return data
            }
            rs.close()
        } catch (e: SQLException) {
            this.log.warn(e.message.toString())
            //Auto reurn id is probably not supported
            //Continue with execution
            rs?.close()
        } catch (e: Throwable) {
            this.closeAll(ps = ps, rs = rs)
            throw DriverException(msg = "Failed to process id results from: $query", cause = e)
        }


        //Try fetching ids with force if onGeneragedKeysFail sql is not null!!!
        try {
            if (onGeneratedKeysFail != null) {
                // This is not profiled since user can't modify the query.
                rs2 = ps.connection.prepareStatement(onGeneratedKeysFail).executeQuery()
                if (rs2.next()) {
                    val data = primaryColumn.decode(rs2, 1) ?: throw DriverException(msg = "Failed to decode primary key value from: $query")
                    this.closeAll(ps = ps, rs = rs2)
                    return data
                }
                rs2.close()
            }
        } catch (e: Throwable) {
            this.closeAll(ps = ps, rs = rs2)
            throw DriverException(msg = "Failed to process id results with '$onGeneratedKeysFail' for: $query", cause = e)
        }

        this.closeAll(ps = ps)
        throw DriverException(msg = "Could not retrieve inserted id normally nor with force from: $query")
    }

    /**
     * Executes custom user defined queries in single database call.
     *
     * @param query The [Query] object representing the SQL query and its values.
     * @param decodeResultSet A user defined function that decodes results set to list of objects (rows).
     *                        It accepts index of a query that was executed in database call, and result set for that specific query.
     * @return The result of the query as a mutable list of lists, where each inner list represents a row of the result set.
     * @throws DriverException If there is an error executing the query.
     */
    internal fun execute(query: Query, decodeResultSet: (i: Int, rs: ResultSet) -> List<Any>): MutableList<List<Any>> {
        var ps: PreparedStatement? = null
        var rs: ResultSet? = null
        val returned = mutableListOf<List<Any>>()

        try {
            ps = conn.prepareStatement(query.sql)
            this.prepareQuery(ps = ps, query = query)
            var isResultSet = Profiler.logExecute(query = query) { ps.execute() }

            var count = 0
            do {
                if (isResultSet) {
                    rs = ps.resultSet
                    returned.add(decodeResultSet(count, rs))
                    rs.close()
                } else {
                    if (ps.updateCount == -1) break
                } //If there is no more result finish

                count++
                isResultSet = ps.moreResults //Get next result
            } while (isResultSet)

            return returned
        } catch (e: Throwable) {
            this.closeAll(ps = ps, rs = rs)
            throw DriverException(msg = "Failed to return query results from: $query", cause = e)
        }
    }

    /**
     * Executes a database query with the given query and a function to decode the result set.
     *
     * @param query The query to execute.
     * @param decodeResultSet The function used to decode each row of the result set.
     *                        It takes a ResultSet as input and returns an instance of type T.
     * @return A list of objects of type T, obtained by decoding each row of the result set.
     * @throws DriverException if there is an error executing the query or decoding the result set.
     */
    public fun <T> query(query: Query, decodeResultSet: (rs: ResultSet) -> T): List<T> {
        var ps: PreparedStatement? = null
        var rs: ResultSet? = null

        try {
            //Prepare statement and query
            ps = conn.prepareStatement(query.sql)
            this.prepareQuery(ps = ps, query = query)

            //Define results
            val objs = mutableListOf<T>()

            //Create result set and fill element
            rs = Profiler.logQuery(query = query) { ps.executeQuery() }
            while (rs.next()) {
                objs.add(decodeResultSet(rs))
            }

            //Close everything
            this.closeAll(ps = ps, rs = rs)

            //Return objects
            return objs

        } catch (e: Throwable) {
            this.closeAll(ps = ps, rs = rs)
            throw DriverException(msg = "Failed to return query results from: $query", cause = e)
        }
    }

    /**
     * Executes a procedure call with the given query and a function to decode the result set.
     *
     * @param query The [Query] object representing the SQL query where procedure call is happening and its argument values.
     * @param decodeResultSet The function used to decode result set rows into a list of objects (rows).
     *                        It accepts the index of the executed query and the result set for that specific query.
     * @return The result of the query as a mutable list of lists, where each inner list represents a row of the result set.
     * @throws DriverException If there is an error executing the query.
     */
    internal fun call(query: Query, decodeResultSet: (i: Int, rs: ResultSet) -> List<Any>): MutableList<List<Any>> {
        var ps: CallableStatement? = null
        var rs: ResultSet? = null
        val returned = mutableListOf<List<Any>>()

        try {
            //Prepare statement and query
            ps = conn.prepareCall(query.sql)
            this.prepareQuery(ps = ps, query = query)
            var isResultSet = Profiler.logCall(query = query) { ps.execute() }

            var count = 0
            do {
                if (isResultSet) {
                    rs = ps.resultSet
                    returned.add(decodeResultSet(count, rs))
                    rs.close()
                } else {
                    if (ps.updateCount == -1) break
                } //If there is no more result finish

                count++
                isResultSet = ps.moreResults //Get next result
            } while (isResultSet)

            return returned
        } catch (e: Throwable) {
            this.closeAll(ps = ps, rs = rs)
            throw DriverException(msg = "Failed to return query results from: $query", cause = e)
        }
    }

}
