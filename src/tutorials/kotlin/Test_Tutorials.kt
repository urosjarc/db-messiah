import basic_postgresql.basic_postgresql
import basic_sqlite.basic_sqlite
import constraints.constraints
import custom_database_serializers.custom_database_serializers
import custom_type_serializers.custom_type_serializers
import exceptions.exceptions
import indexing_and_profiling.indexing_and_profiling
import org.junit.jupiter.api.Test
import primary_keys.primary_keys
import procedures.procedures
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

    @Test
    fun `test procedures`() = procedures()

    @Test
    fun `test indexing and profiling`() = indexing_and_profiling()

    @Test
    fun `test exceptions`() = exceptions()

    @Test
    fun `test custom type serializer`() = custom_type_serializers()

    @Test
    fun `test custom database serializer`() = custom_database_serializers()
}
