import java.util.*

/**
 * This is value class for primary and foreign keys that helps to achieve
 * better type safety then if we would use pure integers.
 */
@JvmInline
value class Id<T>(val value: UUID = UUID.randomUUID()) {
    override fun toString(): String = this.value.toString()
}
