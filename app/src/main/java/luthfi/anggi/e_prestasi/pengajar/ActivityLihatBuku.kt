package luthfi.anggi.e_prestasi.pengajar

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import luthfi.anggi.e_prestasi.R
import luthfi.anggi.e_prestasi.adapter.TanggalAdapter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ActivityLihatBuku : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var tvSantri: TextView
    private lateinit var imvSantri: ImageView
    private lateinit var tvTanggal: TextView
    private lateinit var tvJilid: TextView
    private lateinit var tvHalaman: TextView
    private lateinit var tvNilai: TextView
    private lateinit var tvSurah: TextView
    private lateinit var tvNilaiSurah: TextView
    private lateinit var tvDoa: TextView
    private lateinit var tvNilaiDoa: TextView
    private lateinit var etTanggalAwal: EditText
    private lateinit var etTanggalAkhir: EditText
    private lateinit var rvTanggal: RecyclerView

    private var santriUid: String? = null // UID santri dari intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lihat_buku)

        db = FirebaseFirestore.getInstance()

        // Ambil UID Santri dari Intent
        santriUid = intent.getStringExtra("uid")

        // Inisialisasi TextView dan EditText
        tvSantri = findViewById(R.id.tvSantri)
        imvSantri = findViewById(R.id.imvSantri)
        tvTanggal = findViewById(R.id.tvTanggal)
        //tvJilid = findViewById(R.id.tvJilid)
        tvHalaman = findViewById(R.id.tvHalaman)
        tvNilai = findViewById(R.id.tvNilai)
        tvSurah = findViewById(R.id.tvSurah)
        tvNilaiSurah = findViewById(R.id.tvNilaiSurah)
        tvDoa = findViewById(R.id.tvDoa)
        tvNilaiDoa = findViewById(R.id.tvNilaiDoa)


        etTanggalAwal = findViewById(R.id.etTanggalAwal)
        etTanggalAkhir = findViewById(R.id.etTanggalAkhir)
        rvTanggal = findViewById(R.id.rvTanggal)
        val btnTampilkanTanggal = findViewById<Button>(R.id.btnTampilkanTanggal)

        // Ambil data dari intent
        val namaSantri = intent.getStringExtra("santri_nama")
        val gambarSantri = intent.getStringExtra("santri_gambar_url")

        // Tampilkan nama santri
        tvSantri.text = namaSantri ?: "Nama Santri"

        // Load gambar santri menggunakan Glide
        Glide.with(this)
            .load(gambarSantri)
            .placeholder(R.drawable.ic_profile_placeholder)
            .error(R.drawable.ic_launcher_foreground)
            .into(imvSantri)

        // Menampilkan DatePicker untuk memilih tanggal awal
        etTanggalAwal.setOnClickListener {
            showDatePicker(etTanggalAwal, true)
        }
        etTanggalAkhir.setOnClickListener {
            if (etTanggalAwal.text.isNotEmpty()) {
                showDatePicker(etTanggalAkhir, false)
            } else {
                Toast.makeText(this, "Pilih tanggal awal terlebih dahulu", Toast.LENGTH_SHORT).show()
            }
        }


        // OnClickListener untuk tombol menampilkan tombol tanggal
        btnTampilkanTanggal.setOnClickListener {
            if (etTanggalAwal.text.trim().isNotEmpty() && etTanggalAkhir.text.trim().isNotEmpty()) {
                tampilkanTombolTanggal()  // Panggil fungsi untuk menampilkan tombol tanggal
            } else {
                Toast.makeText(this, "Pilih tanggal awal dan akhir terlebih dahulu", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun tampilkanTombolTanggal() {
        try {
            // Parsing tanggal dari EditText
            val startDate = SimpleDateFormat("dd MMMM yyyy", Locale("id")).parse(etTanggalAwal.text.toString())
            val endDate = SimpleDateFormat("dd MMMM yyyy", Locale("id")).parse(etTanggalAkhir.text.toString())

            if (startDate == null || endDate == null) {
                Toast.makeText(this, "Tanggal tidak valid", Toast.LENGTH_SHORT).show()
                return
            }

            val calendar = Calendar.getInstance()
            calendar.time = startDate
            val tanggalList = mutableListOf<String>()

            // Membuat daftar tanggal
            while (calendar.time <= endDate) {
                val formattedDate = SimpleDateFormat("dd MMMM yyyy", Locale("id")).format(calendar.time)
                tanggalList.add(formattedDate)
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }

            // Pastikan tanggalList tidak kosong
            if (tanggalList.isEmpty()) {
                Toast.makeText(this, "Tidak ada tanggal dalam rentang yang dipilih", Toast.LENGTH_SHORT).show()
                return
            }

            // Log jumlah tanggal yang akan ditampilkan
            Log.d("TanggalList", "Jumlah tombol tanggal yang akan ditampilkan: ${tanggalList.size}")

            // Menampilkan tombol tanggal di RecyclerView
            val adapter = TanggalAdapter(tanggalList) { tanggal ->
                // Ambil data progress untuk tanggal yang dipilih
                ambilDataProgress(santriUid!!, tanggal)
            }
            rvTanggal.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            rvTanggal.adapter = adapter

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Terjadi kesalahan dalam pemilihan tanggal", Toast.LENGTH_SHORT).show()
        }
    }




    private fun showDatePicker(etTanggal: EditText, isStartDate: Boolean) {
        val calendar = Calendar.getInstance()

        // Jika memilih tanggal akhir, batasi rentang tanggal maksimal 7 hari dari tanggal awal
        val selectedDateInMillis = if (isStartDate) calendar.timeInMillis else {
            // Ambil tanggal awal yang dipilih
            val selectedStartDate = SimpleDateFormat("dd MMMM yyyy", Locale("id")).parse(etTanggalAwal.text.toString())
            selectedStartDate?.time ?: calendar.timeInMillis
        }

        // Menambahkan 7 hari ke tanggal awal
        calendar.timeInMillis = selectedDateInMillis
        calendar.add(Calendar.DAY_OF_MONTH, 7)
        val maxDateInMillis = calendar.timeInMillis

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(selectedYear, selectedMonth, selectedDay)

            // Format tanggal dalam bahasa Indonesia
            val indonesianFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id"))
            val formattedDate = indonesianFormat.format(selectedDate.time)

            // Set tanggal ke EditText
            etTanggal.setText(formattedDate)

            // Log data yang dipilih
            Log.d("DatePicker", "Tanggal yang dipilih: $formattedDate")

            // Anda bisa menambahkan logika tambahan jika perlu, seperti ambil data progres berdasarkan tanggal
        }, year, month, day)

        // Jika tanggal akhir, batasi pemilihan tanggal dalam rentang 7 hari setelah tanggal awal
        if (!isStartDate) {
            datePickerDialog.datePicker.minDate = selectedDateInMillis
            datePickerDialog.datePicker.maxDate = maxDateInMillis
        }

        // Tampilkan dialog
        datePickerDialog.show()
    }


    private fun ambilDataProgress(uidSantri: String, tanggal: String) {
        val progressRef = db.collection("progress")
            .document(uidSantri)
            .collection("tanggal")
            .document(tanggal)

        Log.d("FirestoreDebug", "Mengambil data dari: progress/$uidSantri/tanggal/$tanggal")

        progressRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val halaman = document.getString("halaman") ?: "Tidak ada data"
                    val jilid = document.getString("jilid") ?: "Tidak ada data"
                    val nilai = document.getString("nilai") ?: "Tidak ada data"
                    val surah = document.getString("surah") ?: "Tidak ada data"
                    val nilaiSurah = document.getString("nilai_surah") ?: "Tidak ada data"
                    val doa = document.getString("doa") ?: "Tidak ada data"
                    val nilaiDoa = document.getString("nilai_doa") ?: "Tidak ada data"

                    tvTanggal.text = "Tanggal: $tanggal"
                    tvJilid.text = "Jilid: $jilid"
                    tvHalaman.text = "Halaman: $halaman"
                    tvNilai.text = "Nilai: $nilai"
                    tvSurah.text = "Surah: $surah"
                    tvNilaiSurah.text = "Nilai Surah: $nilaiSurah"
                    tvDoa.text = "Doa: $doa"
                    tvNilaiDoa.text = "Nilai Doa: $nilaiDoa"

                    Log.d("FirestoreDebug", "Data ditemukan: $halaman, $jilid, $nilai, $surah, $nilaiSurah, $doa, $nilaiDoa")
                } else {
                    Log.d("FirestoreDebug", "Dokumen tidak ditemukan!")
                    Toast.makeText(this, "Data tidak ditemukan untuk tanggal ini", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreDebug", "Error mengambil data", e)
                Toast.makeText(this, "Gagal mengambil data", Toast.LENGTH_SHORT).show()
            }
    }

}
