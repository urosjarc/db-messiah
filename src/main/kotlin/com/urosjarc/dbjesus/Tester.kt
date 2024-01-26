package com.urosjarc.dbjesus

import com.urosjarc.dbjesus.domain.table.Table
import com.urosjarc.dbjesus.domain.table.TableInfo
import com.urosjarc.dbjesus.exceptions.TesterException
import com.urosjarc.dbjesus.extend.ext_notUnique

class Tester(val tables: List<Table<*>>, val tableInfos: List<TableInfo>) {

    fun test_0_if_all_tables_has_unique_names() {
        val notUnique = this.tableInfos.map { it.name }.ext_notUnique
        if (notUnique.isNotEmpty()) throw TesterException("Following table names are registered multiple times: ${notUnique.keys}")
    }

    fun test_1_if_table_registered_multiple_times() {
        val notUnique = this.tableInfos.map { it.kclass }.ext_notUnique
        if (notUnique.isNotEmpty()) throw TesterException("Following table classes are registered multiple times: ${notUnique.keys}")
    }

    fun test_2_if_primary_key_in_foreign_keys() {
        this.tableInfos.forEach {
            if (it.primaryKey != null) {
                it.foreignKeys.forEach { col ->
                    if (it.primaryKey.kprop == col.kprop) throw TesterException("Table '${it.name}' has primary key registered in foreign keys!")
                }
            }
        }
    }

    fun test_3_if_foreign_key_points_to_registered_table() {
        val allTables = this.tableInfos.map { it.kclass }
        val allForeignKeys = this.tableInfos.map { it.foreignKeys }.flatten()

        allForeignKeys.forEach {
            allTables.contains(it.foreignTable?.kclass)
        }
    }

    fun test_4_if_foreign_key_registered_multiple_times() {
        this.tableInfos.forEach {
            val uniqueFks = it.foreignKeys.map { it.kprop }.toSet()
            if (uniqueFks.size != it.foreignKeys.size) throw TesterException("Table '${it.name}' does not have unique foreign keys!")
        }
    }

    fun test_5_if_constraints_registered_multiple_times() {
        this.tables.forEach {
            val notUnique = it.constraints.map { it.first }.ext_notUnique
            if (notUnique.isNotEmpty()) throw TesterException("Table '${it.name}' does not have unique constraint columns!")
        }
    }


}
