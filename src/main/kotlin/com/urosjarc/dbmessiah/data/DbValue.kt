package com.urosjarc.dbmessiah.data

import com.urosjarc.dbmessiah.exceptions.DbValueException
import java.sql.JDBCType
import java.sql.ResultSet
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1

/**
 * Represents a database value for columns or procedure arguments.
 *
 * @property kprop The property reference where this value is located.
 * @property dbType The type of the database value.
 * @property jdbcType The JDBC type of the database value.
 * @property encoder The [Encoder] used to encode the database value.
 * @property decoder The [Decoder] used to decode the database value.
 */
public abstract class DbValue(
    public val kprop: KProperty1<Any, Any?>,
    public val dbType: String,
    public val jdbcType: JDBCType,
    internal val encoder: Encoder<*>,
    private val decoder: Decoder<*>,
) {

    /**
     * Represents whether the [DbValue] has been initialized or not.
     */
    internal abstract val inited: Boolean

    /**
     * Unescaped path where this [DbValue] will be located.
     */
    public abstract val path: String

    /**
     * Unescaped name for this [DbValue]
     */
    public abstract val name: String

    /**
     * Source class in which [kprop] is located.
     */
    public val kclass: KClass<*> = kprop.returnType.classifier as KClass<*>

    /**
     * Represents the decoding information for a specific parameter in a Kotlin class.
     * Information is used in [Decoder] callback to help write custom [TypeSerializer].
     */
    public val decodeInfo: DecodeInfo = DecodeInfo(kclass = this.kclass, kprop = this.kprop, kparam = null)

    /**
     * Decodes a value from the given [ResultSet] at the specified index [i].
     *
     * @param rs The ResultSet to decode the value from.
     * @param i The index of the column in the ResultSet.
     * @return The decoded value as an instance of [Any], or null if decoding fails.
     */
    public fun decode(rs: ResultSet, i: Int): Any? = this.decoder(rs, i, this.decodeInfo)

    /**
     * Important method which extracts appropriate [QueryValue] from [obj] instance that this [DbValue] represents.
     *
     * @param obj The object from which to generate the [QueryValue].
     * @return The generated [QueryValue].
     */
    public fun queryValue(obj: Any): QueryValue =
        QueryValue(name = this.name, value = this.getValue(obj = obj), jdbcType = this.jdbcType, encoder = this.encoder)

    /**
     * Important method which sets appropriate value to the [obj] property which this [DbValue] represents.
     *
     * @param obj The object on which to set the value.
     * @param value The value to set on the [obj] property.
     * @throws DbValueException if the property is immutable, if types are incompatible, if this [DbValue] does not belong to the [obj].
     */
    public fun setValue(obj: Any, value: Any?) {
        try {
            val kp = this.kprop as KMutableProperty1<Any, Any?>
            try {
                kp.set(receiver = obj, value = value)
            } catch (e: ClassCastException) {
                throw DbValueException(
                    msg = "Trying to set property '$kp' to '$value' but failed! " +
                            "Probably because incompatible types, " +
                            "or receiving object is missing matching property, " +
                            "or property does not belong to the receiver: $obj",
                    cause = e
                )
            }
        } catch (e: ClassCastException) {
            throw DbValueException("Trying to set property '${this.kprop}' to '$value' but the property is probably immutable", e)
        }

    }

    /**
     * Retrieves the property value from an [obj] which this [DbValue] represents.
     *
     * @param obj The object from which to retrieve the value.
     * @return The value of the property, or null if the property does not exist or cannot be retrieved.
     * @throws DbValueException if there is an error retrieving the value from the object.
     */
    public fun getValue(obj: Any): Any? {
        try {
            return this.kprop.get(receiver = obj)
        } catch (e: Throwable) {
            throw DbValueException(
                msg = "Trying to get value '${this.kprop}' but failed! " +
                        "Probably because receiving object is missing matching property or " +
                        "property does not belong to the receiver: $obj",
                cause = e
            )
        }
    }

    /** @suppress */
    override fun hashCode(): Int = 31 * kprop.hashCode() + kclass.hashCode() //OK

    /** @suppress */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as DbValue
        if (kprop != other.kprop) return false
        if (kclass != other.kclass) return false
        return true
    }

    /** @suppress */
    override fun toString(): String = "DbValue(name=${this.name}, dbType='${this.dbType}', jdbcType='${this.jdbcType.name}')"
}
