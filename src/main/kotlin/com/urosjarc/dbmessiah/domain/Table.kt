package com.urosjarc.dbmessiah.domain

import com.urosjarc.dbmessiah.data.TypeSerializer
import com.urosjarc.dbmessiah.exceptions.SerializerTestsException
import com.urosjarc.dbmessiah.extend.ext_notUnique
import com.urosjarc.dbmessiah.extend.ext_owner
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.extensionReceiverParameter
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

/**
 * A class representing a table in a database or schema.
 * This class is intermediate structure created by the user which is
 * then converted by the [Mapper] to more appropriate [TableInfo] structure.
 *
 * @param T the type that represents database table.
 * @property primaryKey the primary key of the table.
 * @property serializers the serializers which will overrides global and schema serializer that will help serialize table columns (optional).
 */
public class Table<T : Any>(
    public val primaryKey: KProperty1<T, *>,
    foreignKeys: List<Pair<KProperty1<T, *>, KClass<*>>> = listOf(),
    constraints: List<Pair<KProperty1<T, *>, List<C>>> = listOf(),
    internal val serializers: List<TypeSerializer<*>> = listOf(),
    columnSerializers: List<Pair<KProperty1<T, *>, TypeSerializer<*>>> = listOf()
) {

    public companion object {
        /**
         * If primary and foreign keys are of the same
         * typed inline class `inline class Id<T>`, you can
         * use this function to extract map of foreign keys pointing to T class.
         * You can use return value of this function directly in [Table] constructor.
         *
         * @param primaryKey The primary key property of type KProperty1<T, *>.
         * @return Map of foreign keys pointing to the right table
         * @throws Exception if the owner class of the primary key cannot be determined.
         */
        public fun <T : Any> getInlineTypedForeignKeys(primaryKey: KProperty1<T, *>): MutableList<Pair<KProperty1<T, *>, KClass<*>>> {
            //Get KClass of primary key
            val pkKClass = primaryKey.returnType.classifier as KClass<*>

            //First extract the owner of the primary key.
            val owner = primaryKey.javaField?.declaringClass?.kotlin ?: throw Exception("Could not get owner of $primaryKey")

            //Define foreign key map where we will fill all foreign keys.
            val fkMap: MutableList<Pair<KProperty1<T, *>, KClass<*>>> = mutableListOf()

            //Get all properties of the owner class.
            val kprops = owner.memberProperties.filter { it.javaField != null }

            //Scan all properties
            kprops.forEach {

                //If property type is of type Id and is not primary key then it must be foreign key
                if (it != primaryKey && it.returnType.classifier == pkKClass) {

                    //Extract T from Id<T> as KClass
                    val foreignKClass = it.returnType.arguments.first().type?.classifier

                    //Fill foreign key map
                    fkMap.add(Pair(it, foreignKClass) as Pair<KProperty1<T, *>, KClass<*>>)
                }
            }
            return fkMap
        }
    }

    /**
     * Unescaped name of the [Table].
     */
    public val name: String =
        ((primaryKey.instanceParameter ?: primaryKey.extensionReceiverParameter)!!.type.classifier as KClass<*>).simpleName.toString()

    /**
     * Map of [Table] foreign keys that points to specific foreign class.
     */
    public val foreignKeys: Map<KProperty1<T, *>, KClass<*>>

    /**
     * Map of [Table] column constraints.
     */
    internal val constraints: Map<KProperty1<T, *>, List<C>>

    /**
     * Represents a map of column serializers.
     * This serializer have the highest priority over all other serializers defined
     * inside database serializer.
     */
    internal val columnSerializers: Map<KProperty1<T, *>, TypeSerializer<*>>

    init {
        val fkDuplicates = foreignKeys.map { it.first }.ext_notUnique
        val constDuplicates = constraints.map { it.first }.ext_notUnique
        val columnSerializersDuplicates = columnSerializers.map { it.first }.ext_notUnique

        if (fkDuplicates.isNotEmpty())
            throw SerializerTestsException("Table $name has foreign keys registered multiple times: $fkDuplicates")

        if (constDuplicates.isNotEmpty())
            throw SerializerTestsException("Table $name has constraints registered multiple times: $constDuplicates")

        if (columnSerializersDuplicates.isNotEmpty())
            throw SerializerTestsException("Table $name has column serializers registered multiple times: $columnSerializersDuplicates")

        this.foreignKeys = foreignKeys.toMap()
        this.constraints = constraints.toMap()
        this.columnSerializers = columnSerializers.toMap()
    }

    /**
     * Kotlin class that represents this [Table].
     */
    public val kclass: KClass<*> = primaryKey.ext_owner


    /** @suppress */
    override fun hashCode(): Int = name.hashCode()//OK


    /** @suppress */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Table<*>) return false
        return name == other.name
    }

    /** @suppress */
    override fun toString(): String = "'${this.name}'"
}
