package domain

import Id

data class InvoiceLine(
    val id: Id<InvoiceLine> = Id(),
    val invoiceId: Id<Invoice>,
    val trackId: Id<Track>,
    val unitPrice: Float,
    val quantity: Int,
)
