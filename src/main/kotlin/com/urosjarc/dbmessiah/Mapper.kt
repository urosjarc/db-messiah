package com.urosjarc.dbmessiah

import com.urosjarc.dbmessiah.data.DecodeInfo
import com.urosjarc.dbmessiah.data.Procedure
import com.urosjarc.dbmessiah.data.TableInfo
import com.urosjarc.dbmessiah.data.TypeSerializer
import com.urosjarc.dbmessiah.exceptions.MapperException
import com.urosjarc.dbmessiah.extend.ext_kparams
import com.urosjarc.dbmessiah.extend.ext_kprops
import java.sql.ResultSet
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1


/**
 * A class that maps Kotlin classes to their associated database tables, procedures, and serializers and vice versa.
 * Mapper is responsible to use kotlin reflection to inspects all user data and create maps from provided information.
 * Kotlin's reflection is CPU expensive so reflection is used only on initialization of building database configuration after that no reflection is used by the sistem.
 *
 * @property schemas A list of all the schemas.
 * @property globalSerializers A list of all the global serializers.
 * @property globalInputs A list of all the global inputs.
 * @property globalOutputs A list of all the global outputs.
 * @property globalProcedures A list of all the global procedures.
 */
public class Mapper(
    schemas: List<Schema>,
    globalSerializers: List<TypeSerializer<*>>,
    globalInputs: List<KClass<*>>,
    globalOutputs: List<KClass<*>>,
    globalProcedures: List<KClass<*>>
) : MapperCache(
    schemas = schemas,
    globalSerializers = globalSerializers,
    globalInputs = globalInputs,
    globalOutputs = globalOutputs,
    globalProcedures = globalProcedures,
) {

    init {
        /**
         * Test only public interface we don't need other!
         */
        val procedures = schemas.flatMap { it.procedures }
        val tables = schemas.flatMap { it.tables.map { it.kclass } }

        //getSerializer (kprop, kparam), getConstructor (decodeMany), getConstructorParameters (decodeMany)
        (globalInputs + globalOutputs + procedures + tables).forEach {
            it.ext_kparams!!.forEach { this.getSerializer(kparam = it) }
            it.ext_kprops.forEach { this.getSerializer(kprop = it) }
            this.getConstructor(kclass = it)
            this.getConstructorParameters(kclass = it)
        }

        //Get constructor parameters
        //getTableInfo
        tables.forEach { this.getTableInfo(kclass = it) }

        //getProcedure
        procedures.forEach { this.getProcedure(kclass = it) }

    }

    public override fun getSerializer(kparam: KParameter): TypeSerializer<out Any> = super.getSerializer(kparam)
    public override fun getSerializer(kprop: KProperty1<out Any, Any?>): TypeSerializer<out Any> = super.getSerializer(kprop)

    /**
     * Retrieves the [TableInfo] object for the given [kclass].
     *
     * @param kclass The Kotlin class representing the table.
     * @return The [TableInfo] object for the given table class.
     * @throws MapperException if the table info for the table cannot be found.
     */
    public fun <T : Any> getTableInfo(kclass: KClass<T>): TableInfo =
        this.tableKClass_to_tableInfo[kclass] ?: throw MapperException("Could not find table info for table: '${kclass.simpleName}'")

    /**
     * Retrieves the [TableInfo] object for the given table object.
     *
     * @param obj The table class or object for which to retrieve the [TableInfo].
     * @return The TableInfo object for the given table class or object.
     * @throws MapperException if the table info for the table cannot be found.
     */
    public fun <T : Any> getTableInfo(obj: T): TableInfo = this.getTableInfo(kclass = obj::class)

    /**
     * Retrieves the [Procedure] object based on the provided [obj].
     *
     * @param obj The object for which to retrieve the [Procedure].
     * @return The [Procedure] object.
     */
    public fun <T : Any> getProcedure(obj: T): Procedure = this.getProcedure(kclass = obj::class)

    /**
     * Retrieves the [Procedure] object based on the provided [kclass].
     *
     * @param kclass The Kotlin [KClass] representing the [Procedure].
     * @return The [Procedure] object.
     * @throws MapperException If the [Procedure] for the class cannot be found.
     */
    public fun getProcedure(kclass: KClass<*>): Procedure =
        this.procedureKClass_to_procedure[kclass] ?: throw MapperException("Could not find procedure for kclass: '${kclass.simpleName}'")

    /**
     * Decodes the result set into a list of objects of the specified output classes.
     *
     * @param resultSet The result set to decode.
     * @param i The index of the output class in the vararg list.
     * @param outputs The vararg list of output classes.
     * @return The list of decoded objects.
     * @throws MapperException If there are missing output classes.
     */
    public fun decodeMany(resultSet: ResultSet, i: Int, vararg outputs: KClass<*>): List<Any> {
        val output = outputs.getOrNull(i)
        val objs = mutableListOf<Any>()

        if (output != null) while (resultSet.next()) objs.add(this.decodeOne(resultSet, output))
        else throw MapperException("Missing output classes, because there are more queries listed in the query: ${outputs.map { it.simpleName }}")

        return objs
    }

    /**
     * Decodes the given [ResultSet] into an object of the specified class [T].
     *
     * @param resultSet The [ResultSet] to decode.
     * @param kclass The [KClass] representing the desired class of the decoded object.
     * @return The decoded object of type [T].
     * @throws MapperException if there is an error during decoding.
     */
    public fun <T : Any> decodeOne(resultSet: ResultSet, kclass: KClass<T>): T {

        val constructor = this.getConstructor(kclass = kclass)
        val constructorParameters = this.getConstructorParameters(kclass = kclass)

        val args = mutableMapOf<KParameter, Any?>()
        for (kparam in constructorParameters) {
            try {
                val i = resultSet.findColumn(kparam.name)
                val decodeInfo = DecodeInfo(kclass = kclass, kparam = kparam)
                val decoder = this.getSerializer(kparam).decoder
                args[kparam] = decoder(resultSet, i, decodeInfo)
            } catch (e: Throwable) {
                throw MapperException("Decoding error", cause = e)
            }
        }

        try {
            return constructor.callBy(args = args) as T
        } catch (e: Throwable) {
            throw MapperException("Class ${kclass.simpleName} can't be constructed with arguments: $args", e)
        }
    }

}
