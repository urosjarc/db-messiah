import domain.Album
import domain.Customer
import domain.Invoice
import domain.Track
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Test_Chinook {
    companion object {
        @JvmStatic
        @BeforeAll
        fun seed(): Unit {
            Seed.all(num = 20)
        }
    }

    @Test
    fun plantUML() {
        File("db2.plantuml").writeText(db2_serializer.plantUML())
        File("derby.plantuml").writeText(derby_serializer.plantUML())
        File("h2.plantuml").writeText(h2_serializer.plantUML())
        File("maria.plantuml").writeText(maria_serializer.plantUML())
        File("mssql.plantuml").writeText(mssql_serializer.plantUML())
        File("mysql.plantuml").writeText(mysql_serializer.plantUML())
        File("oracle.plantuml").writeText(oracle_serializer.plantUML())
        File("postgresql.plantuml").writeText(postgresql_serializer.plantUML())
        File("sqlite.plantuml").writeText(sqlite_serializer.plantUML())
    }

    @Test
    fun `test db2`() {
        db2.autocommit { aconn ->

            val customer = aconn.row.select<Customer>(pk = 3) ?: throw Exception("Customer not found!")

            val invoices0 = aconn.run.query<Invoice> {
                """
                    SELECT * FROM "billing"."Invoice"
                    INNER JOIN "people"."Customer" ON "people"."Customer"."id" = "billing"."Invoice"."customerId"
                    WHERE "people"."Customer"."id" = 3
                """
            }
            assertTrue(invoices0.isNotEmpty())

            val invoices1 = aconn.run.query<Invoice> {
                """
                    SELECT * FROM ${it.table<Invoice>()}
                    INNER JOIN ${it.table<Customer>()} ON ${it.column(Customer::id)} = ${it.column(Invoice::customerId)}
                    WHERE ${it.column(Customer::id)} = ${customer.id!!.value}
                """
            }
            assertTrue(invoices1.isNotEmpty())

            assertEquals(invoices1, invoices0)
        }
    }

    @Test
    fun `test derby`() {

        derby.autocommit {

            val customer = it.row.select<Customer>(pk = 3) ?: throw Exception("Customer not found")

            val invoices0 = it.run.query<Invoice> {
                """
                    SELECT * FROM "billing"."Invoice"
                    INNER JOIN "people"."Customer" ON "people"."Customer"."id" = "billing"."Invoice"."customerId"
                    WHERE "people"."Customer"."id" = 3
                """
            }
            assertTrue(invoices0.isNotEmpty())

            val invoices1 = it.run.query<Invoice> {
                """
                    SELECT * FROM ${it.table<Invoice>()}
                    INNER JOIN ${it.table<Customer>()} ON ${it.column(Customer::id)} = ${it.column(Invoice::customerId)}
                    WHERE ${it.column(Customer::id)} = ${customer.id!!.value}
                """
            }
            assertTrue(invoices1.isNotEmpty())

            assertEquals(invoices1, invoices0)
        }
    }

    @Test
    fun `test h2`() {

        h2.autocommit {

            val customer = it.row.select<Customer>(pk = 3) ?: throw Exception("Customer not found")

            val invoices0 = it.run.query<Invoice> {
                """
                    SELECT * FROM "billing"."Invoice"
                    INNER JOIN "people"."Customer" ON "people"."Customer"."id" = "billing"."Invoice"."customerId"
                    WHERE "people"."Customer"."id" = 3
                """
            }
            assertTrue(invoices0.isNotEmpty())

            val invoices1 = it.run.query<Invoice> {
                """
                    SELECT * FROM ${it.table<Invoice>()}
                    INNER JOIN ${it.table<Customer>()} ON ${it.column(Customer::id)} = ${it.column(Invoice::customerId)}
                    WHERE ${it.column(Customer::id)} = ${customer.id!!.value}
                """
            }
            assertTrue(invoices1.isNotEmpty())

            assertEquals(invoices1, invoices0)
        }
    }

    @Test
    fun `test maria`() {
        maria.autocommit { aconn ->

            val customer = aconn.row.select<Customer>(pk = 3) ?: throw Exception("Customer not found!")

            val invoices0 = aconn.run.query<Invoice> {
                """
                    SELECT * FROM `billing`.`Invoice`
                    INNER JOIN `people`.`Customer` ON `people`.`Customer`.`id` = `billing`.`Invoice`.`customerId`
                    WHERE `people`.`Customer`.`id` = 3
                """
            }
            assertTrue(invoices0.isNotEmpty())

            val invoices1 = aconn.run.query<Invoice> {
                """
                    SELECT * FROM ${it.table<Invoice>()}
                    INNER JOIN ${it.table<Customer>()} ON ${it.column(Customer::id)} = ${it.column(Invoice::customerId)}
                    WHERE ${it.column(Customer::id)} = ${customer.id!!.value}
                """
            }
            assertTrue(invoices1.isNotEmpty())

            assertEquals(invoices1, invoices0)
        }
    }

    @Test
    fun `test mysql`() {
        mysql.autocommit { aconn ->

            val customer = aconn.row.select<Customer>(pk = 3) ?: throw Exception("Customer not found!")

            val invoices0 = aconn.run.query<Invoice> {
                """
                    SELECT * FROM `billing`.`Invoice`
                    INNER JOIN `people`.`Customer` ON `people`.`Customer`.`id` = `billing`.`Invoice`.`customerId`
                    WHERE `people`.`Customer`.`id` = 3
                """
            }
            assertTrue(invoices0.isNotEmpty())

            val invoices1 = aconn.run.query<Invoice> {
                """
                    SELECT * FROM ${it.table<Invoice>()}
                    INNER JOIN ${it.table<Customer>()} ON ${it.column(Customer::id)} = ${it.column(Invoice::customerId)}
                    WHERE ${it.column(Customer::id)} = ${customer.id!!.value}
                """
            }
            assertTrue(invoices1.isNotEmpty())

            assertEquals(invoices1, invoices0)
        }
    }

    @Test
    fun `test mssql`() {
        mssql.autocommit { aconn ->

            val album = aconn.row.select<Album>(pk = 3) ?: throw Exception("Album not found!")

            val tracks0 = aconn.run.query<Track> {
                """
                    SELECT * FROM "music"."Track"
                    INNER JOIN "music"."Album" ON "music"."Album"."artistId" = "music"."Track"."id"
                    WHERE "music"."Album"."id" = 3
                """
            }
            assertTrue(tracks0.isNotEmpty())

            val tracks1 = aconn.run.query<Track> {
                """
                    SELECT * FROM ${it.table<Track>()}
                    INNER JOIN ${it.table<Album>()} ON ${it.column(Album::artistId)} = ${it.column(Track::id)}
                    WHERE ${it.column(Album::id)} = ${album.id!!.value}
                """
            }
            assertTrue(tracks1.isNotEmpty())

            assertEquals(tracks1, tracks0)
        }
    }

    @Test
    fun `test postgresql`() {
        pg.autocommit { aconn ->

            val customer = aconn.row.select<Customer>(pk = 3) ?: throw Exception("Customer not found!")

            val invoices0 = aconn.run.query<Invoice> {
                """
                    SELECT * FROM "billing"."Invoice"
                    INNER JOIN "people"."Customer" ON "people"."Customer"."id" = "billing"."Invoice"."customerId"
                    WHERE "people"."Customer"."id" = 3
                """
            }
            assertTrue(invoices0.isNotEmpty())

            val invoices1 = aconn.run.query<Invoice> {
                """
                    SELECT * FROM ${it.table<Invoice>()}
                    INNER JOIN ${it.table<Customer>()} ON ${it.column(Customer::id)} = ${it.column(Invoice::customerId)}
                    WHERE ${it.column(Customer::id)} = ${customer.id!!.value}
                """
            }
            assertTrue(invoices1.isNotEmpty())

            assertEquals(invoices1, invoices0)
        }
    }

    @Test
    fun `test oracle`() {
        oracle.autocommit { aconn ->

            val customer = aconn.row.select<Customer>(pk = 3) ?: throw Exception("Customer not found!")

            val invoices0 = aconn.run.query<Invoice> {
                """
                    SELECT * FROM "SYSTEM"."Invoice"
                    INNER JOIN "SYSTEM"."Customer" ON "SYSTEM"."Customer"."id" = "SYSTEM"."Invoice"."customerId"
                    WHERE "SYSTEM"."Customer"."id" = 3
                """
            }
            assertTrue(invoices0.isNotEmpty())

            val invoices1 = aconn.run.query<Invoice> {
                """
                    SELECT * FROM ${it.table<Invoice>()}
                    INNER JOIN ${it.table<Customer>()} ON ${it.column(Customer::id)} = ${it.column(Invoice::customerId)}
                    WHERE ${it.column(Customer::id)} = ${customer.id!!.value}
                """
            }
            assertTrue(invoices1.isNotEmpty())

            assertEquals(invoices1, invoices0)
        }
    }

    @Test
    fun `test sqlite`() {

        sqlite.autocommit {

            val customer = it.row.select<Customer>(pk = 3) ?: throw Exception("Customer not found!")

            val invoices0 = it.run.query<Invoice> {
                """
                    SELECT * FROM "Invoice"
                    INNER JOIN "Customer" ON "Customer"."id" = "Invoice"."customerId"
                    WHERE "Customer"."id" = 3
                """
            }
            assertTrue(invoices0.isNotEmpty())

            val invoices1 = it.run.query<Invoice> {
                """
                    SELECT * FROM ${it.table<Invoice>()}
                    INNER JOIN ${it.table<Customer>()} ON ${it.column(Customer::id)} = ${it.column(Invoice::customerId)}
                    WHERE ${it.column(Customer::id)} = ${customer.id!!.value}
                """
            }
            assertTrue(invoices1.isNotEmpty())

            assertEquals(invoices1, invoices0)
        }
    }
}
