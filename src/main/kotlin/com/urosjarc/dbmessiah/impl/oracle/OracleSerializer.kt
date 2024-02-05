package com.urosjarc.dbmessiah.impl.db2

import com.urosjarc.dbmessiah.Serializer
import com.urosjarc.dbmessiah.domain.querie.Query
import com.urosjarc.dbmessiah.domain.serialization.TypeSerializer
import kotlin.reflect.KClass


open class OracleSerializer(
    schemas: List<OracleSchema> = listOf(),
    globalSerializers: List<TypeSerializer<*>> = listOf(),
    globalInputs: List<KClass<*>> = listOf(),
    globalOutputs: List<KClass<*>> = listOf(),
    globalProcedures: List<KClass<*>> = listOf()
) : Serializer(
    schemas = schemas,
    globalSerializers = globalSerializers,
    globalInputs = globalInputs,
    globalOutputs = globalOutputs,
    globalProcedures = globalProcedures
) {
    override fun <T : Any> selectLastId(row: T): String {
        val T = this.mapper.getTableInfo(obj = row)
        val pkSeq = T.primaryKey.path.replace(".", "_")
        return "SELECT $pkSeq.nextval from ${T.path} where rownum = 1"
    }

    override fun <T : Any> createQuery(kclass: KClass<T>): Query {
        val T = this.mapper.getTableInfo(kclass = kclass)

        val col = mutableListOf<String>()
        val constraints = mutableListOf<String>()

        //Primary key
        constraints.add("PRIMARY KEY(${T.primaryKey.name})")
        col.add("${T.primaryKey.name} ${T.primaryKey.dbType}")

        //Foreign keys
        T.foreignKeys.forEach {
            val isNull = if (it.notNull) "" else "NOT NULL"
            col.add("${it.name} ${it.dbType} $isNull")
            constraints.add("FOREIGN KEY (${it.name}) REFERENCES ${it.foreignTable.name} (${it.foreignTable.primaryKey.name})")
        }

        //Other columns
        T.otherColumns.forEach {
            val isNull = if (it.notNull) "" else "NOT NULL"
            col.add("${it.name} ${it.dbType} $isNull")
        }

        //Connect all column definitions to one string
        val columns = (col + constraints).joinToString(", ")

        val pkSeq = T.primaryKey.path.replace(".", "_")

        //Return created query
        return Query(sql = """
            CREATE TABLE ${T.path} ($columns);
            CREATE SEQUENCE $pkSeq INCREMENT BY 1 START WITH 1;
            CREATE OR REPLACE TRIGGER trig_$pkSeq
            BEFORE INSERT ON ${T.path} FOR EACH ROW
            BEGIN
                :new.${T.primaryKey.name} := $pkSeq.nextval;
            END;
        """)
    }
    override fun insertQuery(obj: Any, batch: Boolean): Query {
        val T = this.mapper.getTableInfo(obj = obj)
        val pkSeq = T.primaryKey.path.replace(".", "_")
        return Query(
            sql = """
                INSERT INTO ${T.path} (${T.sqlInsertColumns()}) VALUES (${T.sqlInsertQuestions()})
            """,
            *T.queryValues(obj = obj),
        )
    }

    override fun <T : Any> dropQuery(kclass: KClass<T>, cascade: Boolean): Query {
        val T = this.mapper.getTableInfo(kclass = kclass)
        return Query(sql = "DROP TABLE ${T.path}")
    }
}
