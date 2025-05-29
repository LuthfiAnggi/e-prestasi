package luthfi.anggi.e_prestasi

data class IzinModel(
    val santri_nama: String = "",
    val santri_kelas: String = "",
    val tanggal: String = "",
    val alasan: String = "",
    val status: String = "",
    val created_at: Long = 0L
)
