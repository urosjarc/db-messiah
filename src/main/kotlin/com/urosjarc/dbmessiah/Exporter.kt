package com.urosjarc.dbmessiah

import com.urosjarc.dbmessiah.extend.ext_kprops
import kotlin.reflect.KClass

/**
 * Represents an exporter used to generate various diagram formats representing
 * database architecture.
 *
 * @property schemas A list of database schemas to be exported.
 */
public open class Exporter(internal open val schemas: List<Schema>) {

    /**
     * Generates [PlantUML class diagram](https://plantuml.com/class-diagram) representing database arhitecture.*
     *
     * @param withPrimaryKey Whether to include primary key in the diagram (default: true).
     * @param withForeignKeys Whether to include foreign keys in the diagram (default: true).
     * @param withOtherColumns Whether to include other columns in the diagram (default: false).
     * @return The PlantUML string for visualizing the database schema.
     */
    public fun plantUML(withPrimaryKey: Boolean = true, withForeignKeys: Boolean = true, withOtherColumns: Boolean = false): String {
        val text = mutableListOf(
            "@startuml", "skinparam backgroundColor darkgray", "skinparam ClassBackgroundColor lightgray"
        )

        val relationships = mutableListOf<String>()
        val kclass_to_path = mutableMapOf<KClass<*>, String>()

        this.schemas.forEach { s ->
            s.tables.forEach { t ->
                kclass_to_path[t.kclass] = "${s.name}.${t.name}"
            }
        }

        text.add("")
        this.schemas.forEach { s ->
            text.add("package ${s.name} <<Folder>> {")
            s.tables.forEach { t ->
                val className = kclass_to_path[t.kclass]
                val type = (t.primaryKey.returnType.classifier as KClass<*>).simpleName
                text.add("\tclass $className {")

                if (withPrimaryKey)
                    text.add("\t\t${t.primaryKey.name}: $type")

                t.foreignKeys.forEach {
                    val fk = it.key.name
                    val kclass = it.value
                    val toClass = kclass_to_path[kclass]!!
                    val fromClass = "${s.name}.${t.name}"
                    relationships.add("${fromClass} -down-> ${toClass}: $fk")

                    if (withForeignKeys)
                        text.add("\t\t$fk: ${kclass.simpleName}")
                }

                if (withOtherColumns)
                    t.kclass.ext_kprops.forEach {
                        text.add("\t\t${it.name}: ${(it.returnType.classifier as KClass<*>).simpleName}")
                    }

                text.add("\t}")
            }
            text.add("}")
        }

        text.add("")
        text.addAll(relationships)
        text.add("")
        text.add("@enduml")

        return text.joinToString("\n")
    }

    /**
     * Generates a [dbdiagram.io](https://dbdiagram.io) class diagram representing the database architecture.
     *
     * @param withPrimaryKey Whether to include primary key in the diagram (default: true).
     * @param withForeignKeys Whether to include foreign keys in the diagram (default: true).
     * @param withOtherColumns Whether to include other columns in the diagram (default: false).
     * @return The dbdiagram.io string for visualizing the database schema.
     */
    public fun dbDiagramIO(withOtherColumns: Boolean = false): String {
        val text = mutableListOf<String>()

        val tableGroups = mutableListOf<String>()
        val relationships = mutableListOf<String>()
        val kclass_to_path = mutableMapOf<KClass<*>, String>()
        val kclass_to_pk = mutableMapOf<KClass<*>, String>()

        this.schemas.forEach { s ->
            s.tables.forEach { t ->
                kclass_to_path[t.kclass] = "${s.name}.${t.name}"
                kclass_to_pk[t.kclass] = "${s.name}.${t.name}.${t.primaryKey.name}"
            }
        }

        this.schemas.forEach { s ->
            tableGroups.add("TableGroup ${s.name} {")
            s.tables.forEach { t ->
                val className = kclass_to_path[t.kclass]
                val type = (t.primaryKey.returnType.classifier as KClass<*>).simpleName

                text.add("Table $className {")
                tableGroups.add("\t$className")
                text.add("\t${t.primaryKey.name} $type [primary key]")

                t.foreignKeys.forEach {
                    val fk = it.key.name
                    val kclass = it.value
                    val pk = kclass_to_pk[kclass]!!
                    text.add("\t$fk ${kclass.simpleName} [ref: > $pk]")
                }

                if (withOtherColumns)
                    t.kclass.ext_kprops.forEach {
                        text.add("\t${it.name} ${(it.returnType.classifier as KClass<*>).simpleName}")
                    }

                text.add("}")
            }
            tableGroups.add("}")
        }

        text.add("")
        text.addAll(tableGroups)

        return text.joinToString("\n")
    }
}
