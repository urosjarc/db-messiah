package com.urosjarc.dbjesus.domain.columns

import com.urosjarc.dbjesus.domain.serialization.Decoder
import com.urosjarc.dbjesus.domain.serialization.Encoder
import com.urosjarc.dbjesus.domain.table.TableInfo
import java.sql.JDBCType
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class ForeignColumn(
    var foreignTable: TableInfo?,
    val foreignTableKClass: KClass<*>?,
    override val name: String,
    override val value: Any?,
    override val unique: Boolean,
    override val notNull: Boolean,
    override val kprop: KProperty1<*, *>,
    override val kclass: KClass<*>,
    override val dbType: String,
    override val jdbcType: JDBCType,
    override val encoder: Encoder<*>,
    override val decoder: Decoder<*>,
): Column
