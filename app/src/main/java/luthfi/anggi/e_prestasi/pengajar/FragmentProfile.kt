package luthfi.anggi.e_prestasi.pengajar

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import luthfi.anggi.e_prestasi.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FragmentProfile : Fragment() {

    private lateinit var etNamaProfile: EditText
    private lateinit var etTanggalLahir: EditText
    private lateinit var etNoHp: EditText
    private lateinit var etAlamat: EditText
    private lateinit var ivGambar: ImageView
    private lateinit var btnPilihGambar: Button
    private lateinit var btnSimpan: Button
    private lateinit var otherProjectStorage: FirebaseStorage

    private var imageUri: Uri? = null
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi FirebaseApp untuk project lain
        val options = FirebaseOptions.Builder()
            .setProjectId("mobile-lanjut-1e7c4")
            .setApplicationId("1:83783501336:android:769b93293424be9de7e56d")
            .setApiKey("AIzaSyAq36iXjrFOqdK41bVASAMRdnbeh-97vxg")
            .setStorageBucket("mobile-lanjut-1e7c4.appspot.com")
            .build()

        val existingApp = FirebaseApp.getApps(requireContext()).find { it.name == "OtherProject" }
        val otherProjectApp = existingApp ?: FirebaseApp.initializeApp(requireContext(), options, "OtherProject")
        otherProjectStorage = FirebaseStorage.getInstance(otherProjectApp!!)

        // Inisialisasi View
        etNamaProfile = view.findViewById(R.id.etNamaProfile)
        etTanggalLahir = view.findViewById(R.id.etTanggalLahir)
        etNoHp = view.findViewById(R.id.etNoHp)
        etAlamat = view.findViewById(R.id.etAlamat)
        ivGambar = view.findViewById(R.id.ivGambar)
        btnPilihGambar = view.findViewById(R.id.btnPilihGambar)
        btnSimpan = view.findViewById(R.id.btnSimpan)

        loadProfileData()

        btnPilihGambar.setOnClickListener {
            ImagePicker.with(this)
                .cropSquare()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .galleryOnly()
                .createIntent { intent -> imagePickerLauncher.launch(intent) }
        }

        etTanggalLahir.setOnClickListener {
            showDatePickerDialog()
        }

        btnSimpan.setOnClickListener {
            uploadImageToFirebase()
        }
    }

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val data: Intent? = result.data
                imageUri = data?.data
                ivGambar.setImageURI(imageUri)
            }
        }

    private fun showDatePickerDialog() {
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)

                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                etTanggalLahir.setText(dateFormat.format(selectedDate.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun uploadImageToFirebase() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "User belum login!", Toast.LENGTH_SHORT).show()
            return
        }

        if (imageUri == null) {
            Toast.makeText(requireContext(), "Pilih gambar terlebih dahulu!", Toast.LENGTH_SHORT).show()
            return
        }

        val fileName = "profile_images/${user.uid}.jpg"
        val storageRef = otherProjectStorage.reference.child(fileName)

        storageRef.putFile(imageUri!!)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    saveProfileToFirestore(user.uid, uri.toString())
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Gagal mengunggah gambar! ${it.message}", Toast.LENGTH_LONG)
                    .show()
            }
    }

    private fun saveProfileToFirestore(uid: String, imageUrl: String) {
        val user = hashMapOf(
            "nama" to etNamaProfile.text.toString(),
            "tanggal_lahir" to etTanggalLahir.text.toString(),
            "no_hp" to etNoHp.text.toString(),
            "alamat" to etAlamat.text.toString(),
            "gambar_url" to imageUrl
        )

        db.collection("pengajar").document(uid)
            .set(user)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Profil berhasil disimpan!", Toast.LENGTH_SHORT).show()
                loadProfileData()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Gagal menyimpan data!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadProfileData() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "User belum login!", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("pengajar").document(user.uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    etNamaProfile.setText(document.getString("nama"))
                    etTanggalLahir.setText(document.getString("tanggal_lahir"))
                    etNoHp.setText(document.getString("no_hp"))
                    etAlamat.setText(document.getString("alamat"))

                    val imageUrl = document.getString("gambar_url")
                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(this).load(imageUrl).into(ivGambar)
                    }
                } else {
                    Toast.makeText(requireContext(), "Data profil tidak ditemukan!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Gagal mengambil data profil!", Toast.LENGTH_SHORT).show()
            }
    }
}
