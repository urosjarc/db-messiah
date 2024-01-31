package com.urosjarc.dbmessiah.tests

import com.urosjarc.dbmessiah.DbMessiahRepository
import com.urosjarc.dbmessiah.domain.table.TableInfo
import com.urosjarc.dbmessiah.exceptions.RepositoryException
import com.urosjarc.dbmessiah.exceptions.SerializerException
import com.urosjarc.dbmessiah.extend.ext_notUnique

class TestRepository(val repo: DbMessiahRepository) {
    /**
     * Check for emptyness
     */

    fun `1-th Test - If at least one table has been created`() {
        if (this.repo.tableInfos.isEmpty()) throw RepositoryException("No table info was created")
    }

    fun `2-th Test - If table escapers are consistent`() {
        val firstEscapedChar = this.repo.tableInfos.first().escaper
        this.repo.tableInfos.forEach {
            if (it.escaper != firstEscapedChar) {
                throw RepositoryException("Found first inconsistent escaper ${it.escaper} on table ${it.path}")
            }
        }
    }

    /**
     * REGISTERING MULTIPLE TIMES
     */
    fun `3-th Test - If all tables have unique path, kclass, primaryKey`() {
        val notUniquePath = this.repo.tableInfos.map { it.path }.ext_notUnique
        val notUniqueKClass = this.repo.tableInfos.map { it.kclass }.ext_notUnique
        if (notUniquePath.isNotEmpty()) throw RepositoryException("Following tables have been created multiple times: ${notUniquePath.keys}")
        if (notUniqueKClass.isNotEmpty()) throw RepositoryException("Following tables have been created multiple times: ${notUniqueKClass.keys}")
    }

    fun `4-th Test - If all columns are unique`() {
        this.repo.tableInfos.forEach { T ->
            val notUnique = (listOf(T.primaryKey) + T.foreignKeys + T.otherColumns).ext_notUnique
            if (notUnique.isNotEmpty()) throw RepositoryException("Table ${T.path} does not have unique columns: ${notUnique.keys}")
        }
    }

    fun `5-th Test - If all tables own their own columns`() {
        this.repo.tableInfos.forEach { T: TableInfo ->
            val kprops = this.repo.getKProps(kclass = T.kclass)
            if (!kprops.contains(T.primaryKey.kprop))
                throw RepositoryException("Table ${T.path} does own primary key: ${T.primaryKey}")
            T.foreignKeys.forEach {
                if (!kprops.contains(it.kprop))
                    throw RepositoryException("Table ${T.path} does own foreign key: $it")
            }
            T.otherColumns.forEach {
                if (!kprops.contains(it.kprop))
                    throw RepositoryException("Table ${T.path} does own column: $it")
            }
        }
    }

    fun `6-th Test - If all foreign columns are connected to registered table`() {
        this.repo.tableInfos.forEach { T ->
            T.foreignKeys.forEach {
                if (!it.inited) throw RepositoryException("Foreign key ${it.path} is not initialized and connected to foreign table")
                if (!this.repo.tableInfos.contains(it.foreignTable))
                    throw RepositoryException("Foreign key $it of table $T does not points to registered table: ${it.foreignTable}")
            }
        }
    }

    fun `7-th Test - If all columns have been inited and connected with parent table`() {
        this.repo.tableInfos.forEach { T ->
            (listOf(T.primaryKey) + T.foreignKeys + T.otherColumns).forEach {
                if (T != it.table) throw RepositoryException("Column ${it.path} have parent ${it.table.path} but it should have parent: ${T.path}")
            }
        }
    }

    fun `8-th Test - If all primary keys that have auto inc are of type integer`() {
        nextTable@ for (T in this.repo.tableInfos) {
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
