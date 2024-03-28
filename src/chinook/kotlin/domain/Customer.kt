package domain

import Id

data class Customer(
    val id: Id<Customer> = Id(),
    val firstName: String,
    val lastName: String,
 // val company: String,
 // val address: String,
 // val city: String,
 // val state: String,
 // val country: String,
 // val postalCode: Int,
 // val phone: String,
 // val fax: String,
 // val email: String,
    val supportRepId: Id<Employee>
)
