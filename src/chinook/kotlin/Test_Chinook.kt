import domain.Customer
import domain.Invoice
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Test_Chinook {

    companion object {
        @JvmStatic
        @BeforeAll
        fun init() {
            Seed.all(num = 20)
        }
    }

    @Test
    fun plantUML() {
        File("sqlite.plantuml").writeText(sqlite_serializer.plantUML())
        File("postgresql.plantuml").writeText(postgresql_serializer.plantUML())
    }

    @Test
    fun `test postgresql`() {
        pg.autocommit {

            val customer = it.row.select(Customer::class, pk = 3) ?: throw Exception("Customer not found!")

            val invoices0 = it.run.query(Invoice::class, input = customer) {
                """
                    SELECT * FROM "billing"."Invoice" I
                    INNER JOIN "people"."Customer" C ON C."id" = I."customerId"
                    WHERE C."id" = 3
                """
            }
            assertTrue(invoices0.isNotEmpty())

            val invoices1 = it.run.query(Invoice::class, input = customer) {
                """
                    SELECT * FROM "billing".${name<Invoice>()} I
                    INNER JOIN "people".${name<Customer>()} C ON C."${Customer::id.name}" = I."${Invoice::customerId.name}"
                    WHERE C."${Customer::id.name}" = ${customer.id!!.value}
                """
            }
            assertTrue(invoices1.isNotEmpty())

            assertEquals(invoices1, invoices0)
        }
    }

    @Test
    fun `test sqlite`() {

        sqlite.autocommit {

            val customer = it.row.select(Customer::class, pk = 3) ?: throw Exception("Customer not found!")

            val invoices0 = it.run.query(Invoice::class, input = customer) {
                """
                    SELECT * FROM "Invoice" I
                    INNER JOIN "Customer" C ON C."id" = I."customerId"
                    WHERE C."id" = 3
                """
            }
            assertTrue(invoices0.isNotEmpty())

            val invoices1 = it.run.query(Invoice::class, input = customer) {
                """
                    SELECT * FROM ${name<Invoice>()} I
                    INNER JOIN ${name<Customer>()} C ON C."${Customer::id.name}" = I."${Invoice::customerId.name}"
                    WHERE C."${Customer::id.name}" = ${customer.id!!.value}
                """
            }
            assertTrue(invoices1.isNotEmpty())

            assertEquals(invoices1, invoices0)
        }
    }

}
