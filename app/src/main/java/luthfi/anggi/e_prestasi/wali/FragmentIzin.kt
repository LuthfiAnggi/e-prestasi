package luthfi.anggi.e_prestasi.wali

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import luthfi.anggi.e_prestasi.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FragmentIzin : Fragment() {

    private lateinit var spinnerSantri: AutoCompleteTextView
    private lateinit var etTanggalIzin: EditText
    private lateinit var etAlasanIzin: EditText
    private lateinit var btnKirimIzin: MaterialButton
    private lateinit var btnUploadDokumen: MaterialButton
    private lateinit var tvNamaDokumen: TextView

    private var fileUri: Uri? = null
    private var secondaryFirebaseStorage: FirebaseStorage? = null
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var selectedSantriId: String? = null
    private var selectedSantriName: String? = null
    private var selectedSantriKelas: String? = null
    private val calendar = Calendar.getInstance()

    companion object {
        private const val PICK_FILE_REQUEST = 1001
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_izin_wali, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        initSecondaryFirebaseApp()
        loadSantriData()
        setupListeners()
    }

    private fun initViews(view: View) {
        spinnerSantri = view.findViewById(R.id.spinnerSantri)
        etTanggalIzin = view.findViewById(R.id.etTanggalIzin)
        etAlasanIzin = view.findViewById(R.id.etAlasanIzin)
        btnKirimIzin = view.findViewById(R.id.btnKirimIzin)

        btnUploadDokumen = view.findViewById(R.id.btnUploadDokumen)
        tvNamaDokumen = view.findViewById(R.id.tvNamaDokumen)
    }

    private fun initSecondaryFirebaseApp() {
        try {
            val options = FirebaseOptions.Builder()
                .setProjectId("mobile-lanjut-1e7c4")
                .setApplicationId("1:83783501336:android:769b93293424be9de7e56d")
                .setApiKey("AIzaSyAq36iXjrFOqdK41bVASAMRdnbeh-97vxg")
                .setStorageBucket("mobile-lanjut-1e7c4.appspot.com")
                .build()

            val secondaryApp = FirebaseApp.initializeApp(requireContext(), options, "secondary")
            secondaryFirebaseStorage = FirebaseStorage.getInstance(secondaryApp)
        } catch (e: IllegalStateException) {
            // App already initialized, just get the instance
            secondaryFirebaseStorage = FirebaseStorage.getInstance(FirebaseApp.getInstance("secondary"))
        }
    }

    private fun setupListeners() {
        etTanggalIzin.setOnClickListener { showDatePicker() }
        btnKirimIzin.setOnClickListener { submitIzin() }
        btnUploadDokumen.setOnClickListener { openFileChooser() }

    }

    private fun showDatePicker() {
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
                etTanggalIzin.setText(dateFormat.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun loadSantriData() {
        val waliUid = auth.currentUser?.uid ?: return

        db.collection("santri").whereEqualTo("waliId", waliUid).get()
            .addOnSuccessListener { documents ->
                val santriList = mutableListOf<String>()
                val santriIdList = mutableListOf<String>()
                val kelasList = mutableListOf<String>()

                for (document in documents) {
                    santriList.add(document.getString("nama") ?: "Tanpa Nama")
                    santriIdList.add(document.id)
                    kelasList.add(document.getString("kelas") ?: "Tidak Diketahui")
                }

                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_dropdown_item,
                    santriList
                )
                spinnerSantri.setAdapter(adapter)

                spinnerSantri.setOnItemClickListener { parent, _, position, _ ->
                    selectedSantriId = santriIdList[position]
                    selectedSantriName = parent.getItemAtPosition(position).toString()
                    selectedSantriKelas = kelasList[position]
                }
            }
            .addOnFailureListener { e ->
                showToast("Gagal memuat santri: ${e.message}")
            }
    }

    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(intent, PICK_FILE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_FILE_REQUEST && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                fileUri = uri
                tvNamaDokumen.text = getFileName(uri)
                tvNamaDokumen.visibility = View.VISIBLE
            }
        }
    }

    private fun getFileName(uri: Uri): String {
        requireContext().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            }
        }
        return uri.path?.substringAfterLast('/') ?: "unknown_file"
    }

    private fun submitIzin() {
        val tanggal = etTanggalIzin.text.toString()
        val alasan = etAlasanIzin.text.toString()
        val waliUid = auth.currentUser?.uid ?: return

        if (selectedSantriId.isNullOrEmpty() || tanggal.isEmpty() || alasan.isEmpty()) {
            showToast("Harap lengkapi semua data")
            return
        }

        if (fileUri != null) {
            uploadDocumentAndSubmit(tanggal, alasan, waliUid)
        } else {
            submitIzinData(tanggal, alasan, waliUid, null)
        }
    }

    private fun uploadDocumentAndSubmit(tanggal: String, alasan: String, waliUid: String) {
        val storageRef = secondaryFirebaseStorage?.reference
            ?.child("izin_documents/${System.currentTimeMillis()}_${getFileName(fileUri!!)}")

        storageRef?.putFile(fileUri!!)
            ?.addOnSuccessListener { taskSnapshot ->
                taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                    submitIzinData(tanggal, alasan, waliUid, uri.toString())
                }
            }
            ?.addOnFailureListener { e ->
                showToast("Gagal upload dokumen: ${e.message}")
            }
    }

    private fun submitIzinData(tanggal: String, alasan: String, waliUid: String, documentUrl: String?) {
        val izinData = hashMapOf(
            "santri_id" to selectedSantriId,
            "santri_nama" to selectedSantriName,
            "santri_kelas" to selectedSantriKelas,
            "wali_uid" to waliUid,
            "tanggal" to tanggal,
            "alasan" to alasan,
            "status" to "Pending",
            "created_at" to System.currentTimeMillis(),
            "document_url" to documentUrl
        )

        db.collection("izin").add(izinData)
            .addOnSuccessListener {
                showToast("Izin berhasil diajukan")
                requireActivity().supportFragmentManager.popBackStack()
            }
            .addOnFailureListener { e ->
                showToast("Gagal mengirim izin: ${e.message}")
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}