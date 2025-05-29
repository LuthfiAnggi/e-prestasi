package luthfi.anggi.e_prestasi

data class Payment(
    val transactionId: String,
    val amount: Double,
    val status: String,
    val timestamp: Long,
    val month: String,  // Menandakan bulan tagihan
    val userId: String  // ID pengguna untuk referensi
)
