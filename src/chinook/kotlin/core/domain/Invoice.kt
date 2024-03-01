package core.domain

import kotlinx.datetime.LocalDate

data class Invoice(
    val invoiceId: Id<Invoice>,
    val customerId: Id<Customer>,
    val InvoiceDate: LocalDate,
    val billingAddress: String,
    val billingCity: String,
    val billingState: String,
    val billingCountry: String,
    val billingPostalCode: Int,
    val total: Float,
)
