/**
 * There are 3 main exceptions classes: [ReportIssue], [UserWarning], other exception...
 * If you get UserWarning exception then error if fixable by appropriate actions made by the user.
 * If you get ReportIssue exception then the error can't be fixable by user and the error should be reported.
 * If the system can't recognize the source of the problem the exception will not be of type [ReportIssue] nor [UserWarning].
 */

/**
 * UserWarning subtypes:
 *      - SchemaException: It can happen on the start if user creates inconsistent or bad database structure.
 *      - SerializationException: It can happen at runtime when inconsistency is found before, during or after serialization process from db to kotlin or back.
 *
 * ReportIssue subtypes:
 *      - MapperException: Error that is created on Mapper that creates a maps on the start of reflection scanning.
 *      - DbValueException: Error when trying to retrieve or set value from db column, argument, etc...
 *
 * Other exceptions:
 *      - ConnectionException: Any error created on related to db connection in the JDBC driver.
 *      - DriverException: Any error created on driver that executes db queries and sends them to JDBC driver.
 *
 */
