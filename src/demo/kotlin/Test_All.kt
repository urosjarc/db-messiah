import org.junit.jupiter.api.Test

class Test_All {
    @Test
    fun `test basic sqlite`() = main_000()

    @Test
    fun `test basic postgresql`() = main_001()

    @Test
    fun `test query sqlite`() = main_002()

    @Test
    fun `test query postgresql`() = main_003()

    @Test
    fun `test transactions`() = main_004()

    @Test
    fun `test procedures`() = main_005()

    @Test
    fun `test constraints`() = main_006()

    @Test
    fun `test custom type serializers`() = main_008()
}
