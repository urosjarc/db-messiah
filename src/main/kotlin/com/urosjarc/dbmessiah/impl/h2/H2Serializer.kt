package com.urosjarc.dbmessiah.impl.h2

import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.data.Query
import com.urosjarc.dbmessiah.data.TypeSerializer
import kotlin.reflect.KClass


public open class H2Serializer(
    schemas: List<H2Schema> = listOf(),
    globalSerializers: List<TypeSerializer<*>> = listOf(),
    globalInputs: List<KClass<*>> = listOf(),
    globalOutputs: List<KClass<*>> = listOf(),
) : Serializer(
    schemas = schemas,
    globalSerializers = globalSerializers,
    globalInputs = globalInputs,
    globalOutputs = globalOutputs,
    allowAutoUUID = true
) {
    override fun escaped(name: String): String = "\"$name\""

    override fun <T : Any> createTable(table: KClass<T>): Query {
        val T = this.mapper.getTableInfo(kclass = table)

        val col = mutableListOf<String>()
        val constraints = mutableListOf<String>()

        //Primary key
        val default =
            if (T.primaryColumn.autoInc) " AUTO_INCREMENT"
            else if (T.primaryColumn.autoUUID) " DEFAULT RANDOM_UUID()"
            else ""

        col.add("${escaped(T.primaryColumn.name)} ${T.primaryColumn.dbType}$default PRIMARY KEY")

        //Foreign keys
        T.foreignColumns.forEach {
            val notNull = if (it.notNull) " NOT NULL" else ""
            val unique = if (it.unique) " UNIQUE" else ""
            val deleteCascade = if (it.cascadeDelete) " ON DELETE CASCADE" else ""
            val updateCascade = if (it.cascadeUpdate) " ON UPDATE CASCADE" else ""
            col.add("${escaped(it.name)} ${it.dbType}$notNull$unique")
            constraints.add(
                "FOREIGN KEY (${escaped(it.name)}) REFERENCES ${escaped(it.foreignTable)} (${escaped(it.foreignTable.primaryColumn.name)})$updateCascade$deleteCascade"
            )
        }

        //Other columns
        T.otherColumns.forEach {
            val notNull = if (it.notNull) " NOT NULL" else ""
            val unique = if (it.unique) " UNIQUE" else ""
            col.add("${escaped(it.name)} ${it.dbType}$notNull$unique")
        }

        //Connect all column definitions to one string
        val columns = (col + constraints).joinToString(", ")

        //Return created query
        return Query(sql = "CREATE TABLE IF NOT EXISTS ${escaped(T)} ($columns);")
    }

    override fun <T : Any> createProcedure(procedure: KClass<T>, procedureBody: String): Query = TODO("Not implemented")
    override fun <T : Any> callProcedure(procedure: T): Query = TODO("Not implemented")
    override fun <T : Any> dropProcedure(procedure: KClass<T>): Query = TODO("Not implemented")
}
