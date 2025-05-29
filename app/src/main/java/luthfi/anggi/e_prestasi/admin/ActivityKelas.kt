package luthfi.anggi.e_prestasi.admin

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import luthfi.anggi.e_prestasi.R

class ActivityKelas : AppCompatActivity() {

    private lateinit var etNamaKelas: EditText
    private lateinit var spinnerTingkatan: Spinner
    private lateinit var spinnerPengajar: Spinner
    private lateinit var btnSimpan: Button

    private val db = FirebaseFirestore.getInstance()
    private var selectedTingkatan: String = ""
    private var selectedPengajarId: String? = null
    private var selectedPengajarNama: String? = null
    private val tingkatanList = arrayOf("Jilid 1", "Jilid 2", "Jilid 3", "Jilid 4")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kelas)

        etNamaKelas = findViewById(R.id.etNamaKelas)
        spinnerTingkatan = findViewById(R.id.spinnerTingkatan)
        spinnerPengajar = findViewById(R.id.spinnerPengajar)
        btnSimpan = findViewById(R.id.btnSimpan)

        // Set adapter untuk Spinner Tingkatan
        val adapterTingkatan = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, tingkatanList)
        spinnerTingkatan.adapter = adapterTingkatan

        spinnerTingkatan.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedTingkatan = tingkatanList[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Load daftar pengajar dari Firestore
        loadPengajarData()

        // Tombol Simpan Kelas
        btnSimpan.setOnClickListener {
            tambahKelas()
        }
    }

    private fun loadPengajarData() {
        db.collection("pengajar").get()
            .addOnSuccessListener { documents ->
                val pengajarList = mutableListOf<String>()
                val pengajarIdList = mutableListOf<String>()

                for (document in documents) {
                    val nama = document.getString("nama") ?: "Tanpa Nama"
                    val uid = document.id
                    pengajarList.add(nama)
                    pengajarIdList.add(uid)
                }

                val adapterPengajar = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, pengajarList)
                spinnerPengajar.adapter = adapterPengajar

                spinnerPengajar.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        selectedPengajarId = pengajarIdList[position]
                        selectedPengajarNama = pengajarList[position]
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal memuat pengajar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun tambahKelas() {
        val namaKelas = etNamaKelas.text.toString().trim()

        if (namaKelas.isEmpty() || selectedPengajarId.isNullOrEmpty() || selectedPengajarNama.isNullOrEmpty()) {
            Toast.makeText(this, "Harap isi semua data!", Toast.LENGTH_SHORT).show()
            return
        }

        val kelasRef = db.collection("kelas").document() // Auto-generate UID
        val kelasData = hashMapOf(
            "kelas_uid" to kelasRef.id,
            "nama_kelas" to namaKelas,
            "tingkatan" to selectedTingkatan,
            "pengajar_uid" to selectedPengajarId,
            "pengajar_nama" to selectedPengajarNama,
            "created_at" to System.currentTimeMillis()
        )

        kelasRef.set(kelasData)
            .addOnSuccessListener {
                Toast.makeText(this, "Kelas berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal menambahkan kelas: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
