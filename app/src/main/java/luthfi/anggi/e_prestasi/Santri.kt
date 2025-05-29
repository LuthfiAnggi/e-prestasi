package luthfi.anggi.e_prestasi

data class Santri(
    val uid: String = "",  // Gunakan nama yang sama dengan Firestore
    val nama: String = "",
    val tanggal_lahir: String = "",
    val alamat: String = "",
    val kelas: String = "",
    val gambar_url: String = "",
    val waliId: String = "",
    val wali_nama: String = "",
    val wali_email: String = "",
    val createdAt: String = "",
)
