package luthfi.anggi.e_prestasi.pengajar

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import luthfi.anggi.e_prestasi.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ActivityTambahSantri : AppCompatActivity() {

    private lateinit var etNamaSantri: EditText
    private lateinit var etTanggalLahirSantri: EditText
    private lateinit var etAlamatSantri: EditText
    private lateinit var spinnerKelasSantri: Spinner
    private lateinit var spinnerWaliSantri: Spinner
    private lateinit var btnSimpanSantri: Button
    private lateinit var ivGambar: ImageView
    private lateinit var btnPilihGambar: Button
    private val calendar = Calendar.getInstance()
    private var selectedImageUri: Uri? = null
    private lateinit var otherProjectStorage: FirebaseStorage
    private val firestore = FirebaseFirestore.getInstance()
    private val waliList = mutableListOf<String>()
    private val waliIdList = mutableListOf<String>()
    private var selectedWaliUid: String? = null
    private val waliEmailList = mutableListOf<String>()
    private var selectedWaliNama: String = ""
    private var selectedWaliEmail: String = ""




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_santri)

        val options = FirebaseOptions.Builder()
            .setProjectId("mobile-lanjut-1e7c4")
            .setApplicationId("1:83783501336:android:769b93293424be9de7e56d")
            .setApiKey("YOUR_API_KEY")
            .setStorageBucket("mobile-lanjut-1e7c4.appspot.com")
            .build()

        val existingApp = FirebaseApp.getApps(this).find { it.name == "OtherProject" }
        val otherProjectApp = existingApp ?: FirebaseApp.initializeApp(this, options, "OtherProject")
        otherProjectStorage = FirebaseStorage.getInstance(otherProjectApp!!)

        etNamaSantri = findViewById(R.id.etNamaSantri)
        etTanggalLahirSantri = findViewById(R.id.etTanggalLahirSantri)
        etAlamatSantri = findViewById(R.id.etAlamatSantri)
        spinnerKelasSantri = findViewById(R.id.spinnerKelasSantri)
        spinnerWaliSantri = findViewById(R.id.spinnerWali)
        btnSimpanSantri = findViewById(R.id.btnSimpan)
        ivGambar = findViewById(R.id.ivGambar)
        btnPilihGambar = findViewById(R.id.btnPilihGambar)


        btnPilihGambar.setOnClickListener {
            ImagePicker.with(this)
                .cropSquare()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .galleryOnly()
                .createIntent { intent ->
                    imagePickerLauncher.launch(intent)
                }
        }

        val kelasList = arrayOf("Jilid 1", "Jilid 2", "Jilid 3", "Jilid 4")
        val kelasAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, kelasList)
        kelasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerKelasSantri.adapter = kelasAdapter

        etTanggalLahirSantri.setOnClickListener {
            showDatePickerDialog()
        }

        loadWaliSantri()

        btnSimpanSantri.setOnClickListener {
            val nama = etNamaSantri.text.toString()
            val tanggalLahir = etTanggalLahirSantri.text.toString()
            val alamat = etAlamatSantri.text.toString()
            val kelas = spinnerKelasSantri.selectedItem.toString()
            val waliIndex = spinnerWaliSantri.selectedItemPosition

            if (nama.isEmpty() || tanggalLahir.isEmpty() || alamat.isEmpty() || kelas.isEmpty() || waliIndex == -1) {
                Toast.makeText(this, "Lengkapi semua data!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val waliId = waliIdList[waliIndex]
            val santriRef = firestore.collection("santri").document()
            val santriId = santriRef.id

            if (selectedImageUri != null) {
                val fileRef = otherProjectStorage.reference.child("santri/$santriId.jpg")
                fileRef.putFile(selectedImageUri!!)
                    .addOnSuccessListener { taskSnapshot ->
                        fileRef.downloadUrl
                            .addOnSuccessListener { uri ->
                                saveSantriToFirestore(santriId, nama, tanggalLahir, alamat, kelas, waliId, selectedWaliNama, selectedWaliEmail, uri.toString())
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Gagal mendapatkan URL gambar!", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Gagal mengunggah gambar!", Toast.LENGTH_SHORT).show()
                    }
            } else {
                saveSantriToFirestore(santriId, nama, tanggalLahir, alamat, kelas, waliId, selectedWaliNama, selectedWaliEmail, "")

            }
        }
    }

    private fun showDatePickerDialog() {
        val datePicker = DatePickerDialog(this, { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, dayOfMonth)
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            etTanggalLahirSantri.setText(dateFormat.format(selectedDate.time))
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        datePicker.show()
    }

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                selectedImageUri = data?.data
                ivGambar.setImageURI(selectedImageUri)
            }
        }

    private fun loadWaliSantri() {
        firestore.collection("wali")
            .get()
            .addOnSuccessListener { documents ->
                waliList.clear()
                waliIdList.clear()

                for (document in documents) {
                    val nama = document.getString("nama") ?: "Tanpa Nama"
                    val email = document.getString("email") ?: "-"
                    val uid = document.getString("uid") ?: document.id

                    waliList.add(nama)
                    waliIdList.add(uid)
                    waliEmailList.add(email)
                }


                val waliAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, waliList)
                waliAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerWaliSantri.adapter = waliAdapter

                spinnerWaliSantri.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        selectedWaliUid = waliIdList[position]
                        selectedWaliNama = waliList[position]
                        selectedWaliEmail = waliEmailList[position]

                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        selectedWaliUid = null
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal memuat data wali: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun saveSantriToFirestore(
        uid: String,
        nama: String,
        tanggalLahir: String,
        alamat: String,
        kelas: String,
        waliId: String,
        waliNama: String,
        waliEmail: String,
        imageUrl: String
    ) {
        val santri = hashMapOf(
            "uid" to uid,
            "nama" to nama,
            "tanggalLahir" to tanggalLahir,
            "alamat" to alamat,
            "kelas" to kelas,
            "waliId" to waliId,
            "wali_nama" to waliNama,
            "wali_email" to waliEmail,
            "gambar_url" to imageUrl
        )
        firestore.collection("santri").document(uid).set(santri)
            .addOnSuccessListener {
                Toast.makeText(this, "Santri berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal menambahkan santri", Toast.LENGTH_SHORT).show()
            }
    }

}
