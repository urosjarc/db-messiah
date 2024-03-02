@JvmInline
value class Id<T>(val value: Int) {
    override fun toString(): String = this.value.toString()
}
