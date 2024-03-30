import domain.Album
import domain.Artist
import domain.Customer
import domain.Invoice
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Test_Chinook {
    @Test
    fun `test db2`() {
        File("./build/db2.plantuml").writeText(db2_serializer.plantUML())
        File("./build/db2.dbdiagram.txt").writeText(db2_serializer.dbDiagramIO())
        Seed.db2()
        db2.autocommit { aconn ->

            val customer = aconn.table.select<Customer>().first()

            /**
             * Lets see how we can retrieve all invoices for customer with primary key 3...
             * First we will use RAW SQL to show how will SQL query looked like when we
             * will use type safe function for injecting table names etc...
             */
            val invoices0 = aconn.query.get<Invoice> {
                """
                    SELECT * FROM "billing"."Invoice"
                    INNER JOIN "people"."Customer" ON "people"."Customer"."id" = "billing"."Invoice"."customerId"
                    WHERE "people"."Customer"."id" = '${customer.id.value}'
                """
            }

            /**
             * We confirm that list of invoices are indeed selected.
             */
            assertTrue(invoices0.isNotEmpty())

            /**
             * The query that we joust perform is not type safe we can to better by using
             * extraction functions that will help us extract information about the element
             * and wrapp names with quotation characters. Final result will be the same as
             * first query that we performed.
             */
            val invoices1 = aconn.query.get<Invoice> {
                """
                    SELECT * FROM ${it.table<Invoice>()}
                    INNER JOIN ${it.table<Customer>()} ON ${it.column(Customer::id)} = ${it.column(Invoice::customerId)}
                    WHERE ${it.column(Customer::id)} = '${customer.id.value}'
                """
            }
            /**
             * We confirm that list of invoices are indeed selected.
             */
            assertTrue(invoices1.isNotEmpty())

            /**
             * And indeed list of invoices are for both queries the same!
             */
            assertEquals(invoices1, invoices0)
        }
    }

    /**
     * We repeat this for all databases.
     */

    @Test
    fun `test derby`() {
        File("./build/derby.plantuml").writeText(derby_serializer.plantUML())
        File("./build/derby.dbdiagram.txt").writeText(derby_serializer.dbDiagramIO())
        Seed.derby()
        derby.autocommit {

            val customer = it.table.select<Customer>().first()

            val invoices0 = it.query.get<Invoice> {
                """
                    SELECT * FROM "billing"."Invoice"
                    INNER JOIN "people"."Customer" ON "people"."Customer"."id" = "billing"."Invoice"."customerId"
                    WHERE "people"."Customer"."id" = '${customer.id.value}'
                """
            }
            assertTrue(invoices0.isNotEmpty())

            val invoices1 = it.query.get<Invoice> {
                """
                    SELECT * FROM ${it.table<Invoice>()}
                    INNER JOIN ${it.table<Customer>()} ON ${it.column(Customer::id)} = ${it.column(Invoice::customerId)}
                    WHERE ${it.column(Customer::id)} = '${customer.id.value}'
                """
            }
            assertTrue(invoices1.isNotEmpty())

            assertEquals(invoices1, invoices0)
        }
    }

    @Test
    fun `test h2`() {
        File("./build/h2.plantuml").writeText(h2_serializer.plantUML())
        File("./build/h2.dbdiagram.txt").writeText(h2_serializer.dbDiagramIO())
        Seed.h2()
        h2.autocommit {

            val invoices0 = it.query.get<Invoice> {
                """
                    SELECT * FROM "billing"."Invoice"
                    INNER JOIN "people"."Customer" ON "people"."Customer"."id" = "billing"."Invoice"."customerId"
                """
            }
            assertTrue(invoices0.isNotEmpty())

            val invoices1 = it.query.get<Invoice> {
                """
                    SELECT * FROM ${it.table<Invoice>()}
                    INNER JOIN ${it.table<Customer>()} ON ${it.column(Customer::id)} = ${it.column(Invoice::customerId)}
                """
            }
            assertTrue(invoices1.isNotEmpty())

            assertEquals(invoices1, invoices0)
        }
    }

    @Test
    fun `test maria`() {
        File("./build/maria.plantuml").writeText(maria_serializer.plantUML())
        File("./build/maria.dbdiagram.txt").writeText(maria_serializer.dbDiagramIO())
        Seed.maria()
        maria.autocommit { aconn ->

            val invoices0 = aconn.query.get<Invoice> {
                """
                    SELECT * FROM `billing`.`Invoice`
                    INNER JOIN `people`.`Customer` ON `people`.`Customer`.`id` = `billing`.`Invoice`.`customerId`
                """
            }
            assertTrue(invoices0.isNotEmpty())

            val invoices1 = aconn.query.get<Invoice> {
                """
                    SELECT * FROM ${it.table<Invoice>()}
                    INNER JOIN ${it.table<Customer>()} ON ${it.column(Customer::id)} = ${it.column(Invoice::customerId)}
                """
            }
            assertTrue(invoices1.isNotEmpty())

            assertEquals(invoices1, invoices0)
        }
    }

    @Test
    fun `test mysql`() {
        File("./build/mysql.plantuml").writeText(mysql_serializer.plantUML())
        File("./build/mysql.dbdiagram.txt").writeText(mysql_serializer.dbDiagramIO())
        Seed.mysql()
        mysql.autocommit { aconn ->

            val invoices0 = aconn.query.get<Invoice> {
                """
                    SELECT * FROM `billing`.`Invoice`
                    INNER JOIN `people`.`Customer` ON `people`.`Customer`.`id` = `billing`.`Invoice`.`customerId`
                """
            }
            assertTrue(invoices0.isNotEmpty())

            val invoices1 = aconn.query.get<Invoice> {
                """
                    SELECT * FROM ${it.table<Invoice>()}
                    INNER JOIN ${it.table<Customer>()} ON ${it.column(Customer::id)} = ${it.column(Invoice::customerId)}
                """
            }
            assertTrue(invoices1.isNotEmpty())

            assertEquals(invoices1, invoices0)
        }
    }

    @Test
    fun `test mssql`() {
        File("./build/mssql.plantuml").writeText(mssql_serializer.plantUML())
        File("./build/mssql.dbdiagram.txt").writeText(mssql_serializer.dbDiagramIO())
        Seed.mssql()
        mssql.autocommit { aconn ->

            val artists0 = aconn.query.get<Artist> {
                """
                    SELECT * FROM "music"."Artist"
                    INNER JOIN "music"."Album" ON "music"."Album"."artistId" = "music"."Artist"."id"
                """
            }
            assertTrue(artists0.isNotEmpty())

            val artists = aconn.query.get<Artist> {
                """
                    SELECT * FROM ${it.table<Artist>()}
                    INNER JOIN ${it.table<Album>()} ON ${it.column(Album::artistId)} = ${it.column(Artist::id)}
                """
            }
            assertTrue(artists.isNotEmpty())

            assertEquals(artists, artists0)
        }
    }

    @Test
    fun `test postgresql`() {
        File("./build/postgresql.plantuml").writeText(postgresql_serializer.plantUML())
        File("./build/postgresql.dbdiagram.txt").writeText(postgresql_serializer.dbDiagramIO())
        Seed.pg()
        pg.autocommit { aconn ->

            val invoices0 = aconn.query.get<Invoice> {
                """
                    SELECT * FROM "billing"."Invoice"
                    INNER JOIN "people"."Customer" ON "people"."Customer"."id" = "billing"."Invoice"."customerId"
                """
            }
            assertTrue(invoices0.isNotEmpty())

            val invoices1 = aconn.query.get<Invoice> {
                """
                    SELECT * FROM ${it.table<Invoice>()}
                    INNER JOIN ${it.table<Customer>()} ON ${it.column(Customer::id)} = ${it.column(Invoice::customerId)}
                """
            }
            assertTrue(invoices1.isNotEmpty())

            assertEquals(invoices1, invoices0)
        }
    }

    @Test
    fun `test oracle`() {
        File("./build/oracle.plantuml").writeText(oracle_serializer.plantUML())
        File("./build/oracle.dbdiagram.txt").writeText(oracle_serializer.dbDiagramIO())
        Seed.oracle()
        oracle.autocommit { aconn ->

            val invoices0 = aconn.query.get<Invoice> {
                """
                    SELECT * FROM "SYSTEM"."Invoice"
                    INNER JOIN "SYSTEM"."Customer" ON "SYSTEM"."Customer"."id" = "SYSTEM"."Invoice"."customerId"
                """
            }
            assertTrue(invoices0.isNotEmpty())

            val invoices1 = aconn.query.get<Invoice> {
                """
                    SELECT * FROM ${it.table<Invoice>()}
                    INNER JOIN ${it.table<Customer>()} ON ${it.column(Customer::id)} = ${it.column(Invoice::customerId)}
                """
            }
            assertTrue(invoices1.isNotEmpty())

            assertEquals(invoices1, invoices0)
        }
    }

    @Test
    fun `test sqlite`() {
        File("./build/sqlite.plantuml").writeText(sqlite_serializer.plantUML())
        File("./build/sqlite.dbdiagram.txt").writeText(sqlite_serializer.dbDiagramIO())
        Seed.sqlite()
        sqlite.autocommit {

            val invoices0 = it.query.get<Invoice> {
                """
                    SELECT * FROM "Invoice"
                    INNER JOIN "Customer" ON "Customer"."id" = "Invoice"."customerId"
                """
            }
            assertTrue(invoices0.isNotEmpty())

            val invoices1 = it.query.get<Invoice> {
                """
                    SELECT * FROM ${it.table<Invoice>()}
                    INNER JOIN ${it.table<Customer>()} ON ${it.column(Customer::id)} = ${it.column(Invoice::customerId)}
                """
            }
            assertTrue(invoices1.isNotEmpty())

            assertEquals(invoices1, invoices0)
        }
    }
}
