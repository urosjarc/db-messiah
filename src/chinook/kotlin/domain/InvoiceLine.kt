package domain

import Id

data class InvoiceLine(
    var id: Id<InvoiceLine>? = null,
    val invoiceId: Id<Invoice>,
    val trackId: Id<Track>,
    val unitPrice: Float,
    val quantity: Int,
)
