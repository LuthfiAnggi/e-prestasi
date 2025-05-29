package luthfi.anggi.e_prestasi

data class TransactionStatusResponse(
    val status_code: String,
    val status_message: String,
    val transaction_id: String,
    val order_id: String,
    val gross_amount: Double,
    val transaction_status: String
)
