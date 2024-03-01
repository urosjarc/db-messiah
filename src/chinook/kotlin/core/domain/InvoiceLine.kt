package core.domain

data class InvoiceLine(
    val invoiceLineId: Id<InvoiceLine>,
    val invoiceId: Id<Invoice>,
    val trackId: Id<Track>,
    val unitPrice: Float,
    val quantity: Int,
)
