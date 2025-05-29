package luthfi.anggi.e_prestasi.pengajar

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import luthfi.anggi.e_prestasi.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ActivityTambahBuku : AppCompatActivity() {
    private lateinit var tvSantri: TextView
    private lateinit var tvKelas: TextView
    private lateinit var imvSantri: ImageView
    private lateinit var etPage: EditText
    private lateinit var spinnerGrade: Spinner
    private lateinit var btnSubmit: Button
    private lateinit var etTanggal: EditText


    private val firestore = FirebaseFirestore.getInstance()
    private var santriUid: String? = null // UID santri dari intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_buku)

        // Inisialisasi View
        tvSantri = findViewById(R.id.tvSantri)
        tvKelas = findViewById(R.id.tvkelas)
        imvSantri = findViewById(R.id.imvSantri)
        etTanggal = findViewById(R.id.etTanggal)
        //tvTanggalHariIni = findViewById(R.id.tvTanggalHariIni)
        etPage = findViewById(R.id.etHalaman)
        spinnerGrade = findViewById(R.id.spinnerNilai)
        btnSubmit = findViewById(R.id.btnSubmit)


        // Ambil data dari intent
        val kelasSantri = intent.getStringExtra("santri_kelas")
        val namaSantri = intent.getStringExtra("santri_nama")
        val gambarSantri = intent.getStringExtra("santri_gambar_url")
        santriUid = intent.getStringExtra("santri_uid") // Ambil UID santri

        // Tampilkan nama santri
        tvSantri.text = namaSantri ?: "Nama Santri"
        tvKelas.text = kelasSantri ?: "Kelas Santri"

        // Load gambar santri menggunakan Glide
        Glide.with(this)
            .load(gambarSantri)
            .placeholder(R.drawable.ic_profile_placeholder)
            .error(R.drawable.ic_launcher_foreground)
            .into(imvSantri)

        // Listener untuk EditText Tanggal
        etTanggal.setOnClickListener {
            // Ambil tanggal saat ini
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            // Buat DatePickerDialog
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year1, month1, dayOfMonth ->
                    // Format tanggal yang dipilih
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(year1, month1, dayOfMonth)

                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    etTanggal.setText(sdf.format(selectedDate.time))
                },
                year, month, day
            )
            datePickerDialog.show()
        }

        fun formatTanggal(tanggal: String): String {
            val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
            val date = SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID")).parse(tanggal) // Misal tanggal yang dimasukkan seperti dd/MM/yyyy
            return sdf.format(date!!)
        }

        btnSubmit.setOnClickListener {

            val tanggalHariIni = etTanggal.text.toString()
            // Format tanggal
            val tanggalFormatted = formatTanggal(tanggalHariIni)
            // Kirimkan tanggal yang sudah diformat ke fungsi submitData
            submitData(tanggalFormatted)
        }

    }

    private fun submitData(tanggal: String) {
        val halaman = etPage.text.toString().trim()
        val grade = spinnerGrade.selectedItem.toString()

        if (halaman.isEmpty()) {
            etPage.error = "Halaman tidak boleh kosong"
            return
        }

        if (santriUid == null) {
            Toast.makeText(this, "UID Santri tidak ditemukan!", Toast.LENGTH_SHORT).show()
            return
        }

        // Data yang akan disimpan
        val progressData = hashMapOf(
            "halaman" to halaman,
            "nilai" to grade,
        )

        // Struktur Firestore: progress/{uid_santri}/tanggal/{tanggal}
        val progressRef = firestore.collection("progress")
            .document(santriUid!!) // UID Santri sebagai document
            .collection("tanggal") // Sub-koleksi per tanggal
            .document(tanggal) // Dokumen per tanggal yang sudah diformat

        // Simpan atau perbarui progress
        progressRef.set(progressData)
            .addOnSuccessListener {
                Toast.makeText(this, "Progress berhasil disimpan!", Toast.LENGTH_SHORT).show()
                val tanggalDipilih = etTanggal.text.toString() // Atau ambil dari DatePicke

                // Intent ke ActivityTambahHafalan
                val intent = Intent(this, ActivityTambahHafalan::class.java)
                intent.putExtra("santri_uid", santriUid)
                intent.putExtra("santri_nama", tvSantri.text.toString())
                intent.putExtra("santri_kelas", tvKelas.text.toString())
                intent.putExtra("santri_gambar_url", intent.getStringExtra("santri_gambar_url")) // forwarding gambar juga
                intent.putExtra("tanggal_dipilih", tanggalDipilih) // Menambahkan tanggal yang dipilih
                startActivity(intent)

                finish() // Menutup activity ini agar tidak bisa kembali ke form yang sudah submit
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal menyimpan progress!", Toast.LENGTH_SHORT).show()
            }
    }



}