package core.repos

import core.domain.Customer
import core.domain.Id

interface CustomerRepoContract {
    fun getCustomer(pk: Id<Customer>): Customer

}
