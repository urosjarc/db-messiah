import org.junit.jupiter.api.Test

class Test_All {
    @Test
    fun `test sqlite basic`() = main_000()

    @Test
    fun `test postgresql basic`() = main_001()

    @Test
    fun `test sqlite custom queries`() = main_002()
}
