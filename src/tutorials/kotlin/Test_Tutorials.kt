import basic_postgresql.basic_postgresql
import basic_sqlite.basic_sqlite
import constraints.constraints
import org.junit.jupiter.api.Test
import primary_keys.primary_keys
import query_postgresql.query_postgresql
import query_sqlite.query_sqlite
import transactions.transactions

class Test_Tutorials {
    @Test
    fun `test basic sqlite`() = basic_sqlite()

    @Test
    fun `test basic postgresql`() = basic_postgresql()

    @Test
    fun `test query sqlite`() = query_sqlite()

    @Test
    fun `test query postgresql`() = query_postgresql()

    @Test
    fun `test primary keys`() = primary_keys()

    @Test
    fun `test constraints`() = constraints()

    @Test
    fun `test transactions`() = transactions()
//
//    @Test
//    fun `test exceptions`() = main_007()
//
//    @Test
//    fun `test custom type serializer`() = main_008()
//
//    @Test
//    fun `test custom database serializer`() = main_009()
}
