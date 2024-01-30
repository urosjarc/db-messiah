package com.urosjarc.dbmessiah.tests

import com.urosjarc.dbmessiah.DbMessiahRepository
import com.urosjarc.dbmessiah.domain.table.TableInfo
import com.urosjarc.dbmessiah.exceptions.MapperException
import com.urosjarc.dbmessiah.exceptions.SerializerException
import com.urosjarc.dbmessiah.extend.ext_notUnique

class TestMapper(val mapper: DbMessiahRepository) {
    /**
     * Check for emptyness
     */

    fun `1-th Test - If at least one table has been created`() {
        if (this.mapper.tableInfos.isEmpty()) throw MapperException("No table info was created")
    }

    fun `2-th Test - If table escapers are consistent`() {
        val firstEscapedChar = this.mapper.tableInfos.first().escaper
        this.mapper.tableInfos.forEach {
            if (it.escaper != firstEscapedChar) {
                throw MapperException("Found first inconsistent escaper ${it.escaper} on table ${it.path}")
            }
        }
    }

    /**
     * REGISTERING MULTIPLE TIMES
     */
    fun `3-th Test - If all tables have unique path, kclass, primaryKey`() {
        val notUniquePath = this.mapper.tableInfos.map { it.path }.ext_notUnique
        val notUniqueKClass = this.mapper.tableInfos.map { it.kclass }.ext_notUnique
        if (notUniquePath.isNotEmpty()) throw MapperException("Following tables have been created multiple times: ${notUniquePath.keys}")
        if (notUniqueKClass.isNotEmpty()) throw MapperException("Following tables have been created multiple times: ${notUniqueKClass.keys}")
    }

    fun `5-th Test - If all tables own their own columns`() {
        this.mapper.tableInfos.forEach { T: TableInfo ->
            val kprops = this.mapper.getKProps(kclass = T.kclass)
            if (!kprops.contains(T.primaryKey.kprop))
                throw MapperException("Table ${T.path} does own primary key: ${T.primaryKey}")
            T.foreignKeys.forEach {
                if (!kprops.contains(it.kprop))
                    throw MapperException("Table ${T.path} does own foreign key: $it")
            }
            T.otherColumns.forEach {
                if (!kprops.contains(it.kprop))
                    throw MapperException("Table ${T.path} does own column: $it")
            }
        }
    }

    fun `4-th Test - If all columns are unique`() {
        this.mapper.tableInfos.forEach { T ->
            val notUnique = (listOf(T.primaryKey) + T.foreignKeys + T.otherColumns).ext_notUnique
            if (notUnique.isNotEmpty()) throw MapperException("Table ${T.path} does not have unique columns: ${notUnique.keys}")
        }
    }

    fun `6-th Test - If all foreign columns are connected to registered table`() {
        this.mapper.tableInfos.forEach { T ->
            T.foreignKeys.forEach {
                if (!this.mapper.tableInfos.contains(it.foreignTable))
                    throw MapperException("Foreign key $it does not points to registered table: $it")
            }
        }
    }

    fun `7-th Test - If all columns have been inited and connected with parent table`() {
        this.mapper.tableInfos.forEach { T ->
            (listOf(T.primaryKey) + T.foreignKeys + T.otherColumns).forEach {
                if (!it.inited) throw MapperException("Foreign key ${it.path} is not initialized and connected to foreign table")
                if (T != it.table) throw MapperException("Column ${it.path} have parent ${it.table.path} but it should have parent: ${T.path}")
            }
        }
    }

    fun `8-th Test - If all primary keys that have auto inc are of type integer`() {
        nextTable@ for (T in this.mapper.tableInfos) {
            if (T.primaryKey.autoIncrement) {
                for (it in listOf("INT", "INTEGER")) {
                    val dbType = T.primaryKey.dbType.split("(").first()
                    if (dbType == it) continue@nextTable
                }
                throw SerializerException("Primary key ${T.primaryKey.path} of type '${T.primaryKey.dbType}' has constrain 'AUTO_INC' so then it should be of type: 'INT' or 'INTEGER'")
            }
        }
    }

}
