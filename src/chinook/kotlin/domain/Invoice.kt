package domain

import Id
import kotlinx.datetime.LocalDate

data class Invoice(
    var id: Id<Invoice>? = null,
    val customerId: Id<Customer>,
    val invoiceDate: LocalDate,
    val billingAddress: String,
//    val billingCity: String,
//    val billingState: String,
//    val billingCountry: String,
//    val billingPostalCode: Int,
//    val total: Float,
)
