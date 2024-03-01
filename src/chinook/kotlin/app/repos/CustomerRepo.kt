package app.repos

import app.services.PostgresService
import app.services.SqlitService
import core.domain.Customer
import core.domain.Id
import core.repos.CustomerRepoContract

class CustomerRepo(
    sqliteService: SqlitService,
    postgresService: PostgresService
) : CustomerRepoContract {

    val sqlite = sqliteService.db
    val pg = postgresService.db
    override fun getCustomer(pk: Id<Customer>): Customer {
        /**
         * Inputs
         */
        // var customer: Customer? = null

        /**
         * Db actions
         */
        // sqlite.autocommit {
        //     customer = it.row.select(Customer::class, pk = pk.value)
        // }

        /**
         * Returns
         */
        // return customer
        return Customer(firstName = "asdf", lastName = "asdf", supportRepId = Id(32))
    }
}
