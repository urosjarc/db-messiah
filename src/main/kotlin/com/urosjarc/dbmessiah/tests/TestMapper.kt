package com.urosjarc.dbmessiah.tests

import com.urosjarc.dbmessiah.domain.table.TableInfo
import com.urosjarc.dbmessiah.exceptions.FatalMapperException
import com.urosjarc.dbmessiah.extend.ext_javaFields
import com.urosjarc.dbmessiah.extend.ext_notUnique
import kotlin.reflect.KProperty1

class TestMapper(val tableInfos: List<TableInfo>) {
    /**
     * Check for emptyness
     */

    fun `1-th Test - If at least one table has been created`() {
        if (this.tableInfos.size == 0) throw FatalMapperException("No table info was created")
    }

    fun `2-th Test - If table escapers are consistent`() {
        val firstEscapedChar = this.tableInfos.first().escaper
        this.tableInfos.forEach {
            if (it.escaper != firstEscapedChar) {
                throw FatalMapperException("Found first inconsistent escaper ${it.escaper} on table ${it.path}")
            }
        }
    }

    /**
     * REGISTERING MULTIPLE TIMES
     */
    fun `3-th Test - If all tables have unique path, kclass, primaryKey`() {
        val notUniquePath = this.tableInfos.map { it.path }.ext_notUnique
        val notUniqueKClass = this.tableInfos.map { it.kclass }.ext_notUnique
        if (notUniquePath.isNotEmpty()) throw FatalMapperException("Following tables have been created multiple times: ${notUniquePath.keys}")
        if (notUniqueKClass.isNotEmpty()) throw FatalMapperException("Following tables have been created multiple times: ${notUniqueKClass.keys}")
    }

    fun `5-th Test - If all tables own their own columns`() {
        this.tableInfos.forEach { T: TableInfo ->
            val javaFields = T.kclass.ext_javaFields
            if (!javaFields.contains(T.primaryKey.kprop))
                throw FatalMapperException("Table ${T.path} does own primary key: ${T.primaryKey}")
            T.foreignKeys.forEach {
                if (!javaFields.contains(it.kprop))
                    throw FatalMapperException("Table ${T.path} does own foreign key: $it")
            }
            T.otherColumns.forEach {
                if (!javaFields.contains(it.kprop))
                    throw FatalMapperException("Table ${T.path} does own column: $it")
            }
        }
    }

    fun `4-th Test - If all columns are unique`() {
        this.tableInfos.forEach { T ->
            val notUnique = (listOf(T.primaryKey) + T.foreignKeys + T.otherColumns).ext_notUnique
            if (notUnique.isNotEmpty()) throw FatalMapperException("Table ${T.path} does not have unique columns: ${notUnique.keys}")
        }
    }

    fun `6-th Test - If all foreign columns are connected to registered table`() {
        this.tableInfos.forEach { T ->
            T.foreignKeys.forEach {
                if (!this.tableInfos.contains(it.foreignTable))
                    throw FatalMapperException("Foreign key $it does not points to registered table: $it")
            }
        }
    }

    fun `7-th Test - If all columns have been inited and connected with parent table`() {
        this.tableInfos.forEach { T ->
            (listOf(T.primaryKey) + T.foreignKeys + T.otherColumns).forEach {
                if (!it.inited) throw FatalMapperException("Foreign key ${it.path} is not initialized and connected to foreign table")
                if (T != it.table) throw FatalMapperException("Column ${it.path} have parent ${it.table.path} but it should have parent: ${T.path}")
            }
        }
    }

}
