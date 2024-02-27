package com.urosjarc.dbmessiah.data

import com.urosjarc.dbmessiah.exceptions.DbValueException
import java.sql.JDBCType
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1

/**
 * Represents a database value.
 *
 * @property kprop The property reference where this value is located.
 * @property dbType The type of the database value.
 * @property jdbcType The JDBC type of the database value.
 * @property encoder The [Encoder] used to encode the database value.
 * @property decoder The [Decoder] used to decode the database value.
 *
 * @property inited Determines if the [DbValue] has been initialized.
 * @property path The full path of [DbValue] location.
 * @property name The name of the [DbValue] property.
 */
public abstract class DbValue(
    public val kprop: KProperty1<Any, Any?>,
    public val dbType: String,
    public val jdbcType: JDBCType,
    internal val encoder: Encoder<*>,
    internal val decoder: Decoder<*>,
) {
    internal abstract val inited: Boolean
    public abstract val path: String
    public abstract val name: String

    /**
     * Source class in which [kprop] is located.
     */
    public val kclass: KClass<*> = kprop.returnType.classifier as KClass<*>

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
                            "Probably because incompatible types " +
                            "or receiving object is missing matching property " +
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
    private val hash = (this.kclass.simpleName + this.kprop.name).hashCode()

    /** @suppress */
    override fun equals(other: Any?): Boolean = this.hashCode() == other.hashCode()

    /** @suppress */
    override fun hashCode(): Int = this.hash

    /** @suppress */
    override fun toString(): String = "DbValue(name=${this.name}, dbType='${this.dbType}', jdbcType='${this.jdbcType.name}')"
}
