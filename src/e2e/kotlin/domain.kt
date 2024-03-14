import java.util.*
import kotlin.random.Random

data class TestProcedure(
    val parent_pk: Int,
    val parent_col: String
)

class TestProcedureEmpty

data class Input(
    val child_pk: Int,
    val parent_pk: Int,
)

data class Output(
    val child_pk: Int,
    val parent_pk: Int,
)

data class Child(
    var pk: Int? = null,
    val fk: Int,
    val col: String
) {
    companion object {
        fun get(pk: Int? = null, fk: Int, seed: Int): Child {
            val random = Random(seed = seed)
            return Child(
                pk = pk,
                fk = fk,
                col = random.nextInt().toString()
            )
        }
    }
}

data class Parent(
    var pk: Int? = null,
    var col: String
) {
    companion object {
        fun get(pk: Int? = null, seed: Int): Parent {
            val random = Random(seed = seed)
            return Parent(pk = pk, col = random.nextInt().toString())
        }
    }
}

data class UUIDChild(
    var pk: UUID? = null,
    val fk: UUID,
    val col: String
)

data class UUIDParent(
    val pk: UUID = UUID.randomUUID(),
    var col: String
)
