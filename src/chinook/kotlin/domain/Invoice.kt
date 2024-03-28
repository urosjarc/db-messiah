package domain

import Id
import java.time.LocalDate

data class Invoice(
    val id: Id<Invoice> = Id(),
    val customerId: Id<Customer>,
    val invoiceDate: LocalDate,
    val billingAddress: String,
//    val billingCity: String,
//    val billingState: String,
//    val billingCountry: String,
//    val billingPostalCode: Int,
//    val total: Float,
)
