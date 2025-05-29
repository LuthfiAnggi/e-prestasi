package luthfi.anggi.e_prestasi

data class PaymentHistory(
    val orderId: String,
    val date: String,
    val amount: Double,
    val status: String,
    val paymentChannel: String,
    val santriName: String
)