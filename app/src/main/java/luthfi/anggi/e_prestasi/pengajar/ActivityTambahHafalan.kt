package luthfi.anggi.e_prestasi.pengajar

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import luthfi.anggi.e_prestasi.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class ActivityTambahHafalan : AppCompatActivity() {

    private lateinit var tvSantri: TextView
    private lateinit var tvKelas: TextView
    private lateinit var imvSantri: ImageView
    private lateinit var spinnerSurah: Spinner
    private lateinit var spinnerNilaiSurah: Spinner
    private lateinit var spinnerDoa: Spinner
    private lateinit var spinnerNilaiDoa: Spinner
    private lateinit var tvTanggalHariIni: TextView
    private lateinit var btnSubmit: Button

    private val firestore = FirebaseFirestore.getInstance()
    private var santriUid: String? = null // UID santri dari intent

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_hafalan)

        db = FirebaseFirestore.getInstance()

        // Inisialisasi view
        imvSantri = findViewById(R.id.imvSantri)
        tvSantri = findViewById(R.id.tvSantri)
        tvKelas = findViewById(R.id.tvkelas)
        spinnerSurah = findViewById(R.id.spinnerSurah)
        spinnerNilaiSurah = findViewById(R.id.spinnerNilaiSurah)
        spinnerDoa = findViewById(R.id.spinnerDoa)
        spinnerNilaiDoa = findViewById(R.id.spinnerNilaiDoa)
        tvTanggalHariIni = findViewById(R.id.tvtanggalhariini)
        btnSubmit = findViewById(R.id.btnSubmit)

        santriUid = intent.getStringExtra("santri_uid") // Ambil UID santri

        if (santriUid != null) {
            loadSantriData(santriUid!!)
        }


        // Mengambil tanggal yang dipilih dari Intent
        val tanggalDipilih = intent.getStringExtra("tanggal_dipilih") ?: getCurrentDate()

        // Format tanggal yang dipilih
        val formattedTanggal = formatTanggal(tanggalDipilih)
        tvTanggalHariIni.text = formattedTanggal

        // Event klik tombol submit
        btnSubmit.setOnClickListener {
            submitData(formattedTanggal)
        }

    }

    // Fungsi untuk memformat tanggal
    private fun formatTanggal(tanggal: String): String {
        return try {
            val sdfInput = SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID"))
            val sdfOutput = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
            val date = sdfInput.parse(tanggal)
            sdfOutput.format(date!!)
        } catch (e: Exception) {
            // Jika terjadi kesalahan format, kembalikan tanggal saat ini
            getCurrentDate()
        }
    }

    // Fungsi untuk mendapatkan tanggal hari ini
    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
        return sdf.format(Date())
    }
    
    private fun submitData(tanggal: String) {
        val surah = spinnerSurah.selectedItem.toString()
        val nilaiSurah = spinnerNilaiSurah.selectedItem.toString()
        val doa = spinnerDoa.selectedItem.toString()
        val nilaiDoa = spinnerNilaiDoa.selectedItem.toString()

        // Validasi UID santri
        if (santriUid == null) {
            Toast.makeText(this, "UID Santri tidak ditemukan!", Toast.LENGTH_SHORT).show()
            return
        }

        // Siapkan data progress
        val progressData = hashMapOf(
            "surah" to surah,
            "nilai_surah" to nilaiSurah,
            "doa" to doa,
            "nilai_doa" to nilaiDoa
        )

        // Referensi Firestore: progress/{uid_santri}/tanggal/{tanggal}
        val progressRef = firestore.collection("progress")
            .document(santriUid!!) // UID sebagai dokumen utama
            .collection("tanggal") // Subkoleksi berdasarkan tanggal
            .document(tanggal)     // Dokumen per tanggal yang dipilih

        // Simpan atau tambahkan data (merge agar tidak replace seluruh dokumen)
        progressRef.set(progressData, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "Progress berhasil disimpan!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal menyimpan progress!", Toast.LENGTH_SHORT).show()
            }
    }


    private fun loadSantriData(uid: String) {
        firestore.collection("santri").document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val nama = document.getString("nama") ?: "-"
                    val kelasNama = document.getString("kelas") ?: "-"
                    val gambarUrl = document.getString("gambar_url") ?: ""

                    tvSantri.text = nama
                    tvKelas.text = kelasNama

                    // Cari tingkatan dari koleksi "kelas"
                    firestore.collection("kelas")
                        .whereEqualTo("tingkatan", kelasNama)
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            if (!querySnapshot.isEmpty) {
                                val kelasDoc = querySnapshot.documents[0]
                                val tingkatan = kelasDoc.getString("tingkatan") ?: "-"
                                val kelasUid = kelasDoc.id  // ✅ Ambil UID kelas (id dokumen)

                                // ✅ Panggil fungsi ini dengan UID kelas
                                loadSurahToSpinner(kelasUid)
                                loadDoaToSpinner(kelasUid)

                            } else {
                                Toast.makeText(this, "Kelas tidak ditemukan di koleksi 'kelas'", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Gagal mencari tingkatan kelas", Toast.LENGTH_SHORT).show()
                        }

                    Glide.with(this)
                        .load(gambarUrl)
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(imvSantri)
                } else {
                    Toast.makeText(this, "Data santri tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal memuat data santri", Toast.LENGTH_SHORT).show()
            }
    }


    fun loadSurahToSpinner(kelasUid: String) {
        val hafalanRef = FirebaseFirestore.getInstance()
            .collection("hafalan")
            .document(kelasUid) // ✅ Sekarang pakai UID kelas

        hafalanRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val surahList = document.get("surah_pendek") as? List<String>
                    if (!surahList.isNullOrEmpty()) {
                        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, surahList)
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spinnerSurah.adapter = adapter
                    } else {
                        Toast.makeText(this, "Data surah kosong", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Dokumen hafalan tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal memuat data hafalan", Toast.LENGTH_SHORT).show()
            }
    }

    fun loadDoaToSpinner(kelasUid: String) {
        val hafalanRef = FirebaseFirestore.getInstance()
            .collection("hafalan")
            .document(kelasUid) // tetap ambil berdasarkan UID kelas

        hafalanRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val doaList = document.get("doa_pendek") as? List<String>
                    if (!doaList.isNullOrEmpty()) {
                        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, doaList)
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spinnerDoa.adapter = adapter
                    } else {
                        Toast.makeText(this, "Data doa pendek kosong", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Dokumen hafalan tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal memuat data doa pendek", Toast.LENGTH_SHORT).show()
            }
    }


    private fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }
}
