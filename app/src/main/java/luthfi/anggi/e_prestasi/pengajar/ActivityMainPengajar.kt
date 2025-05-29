package luthfi.anggi.e_prestasi.pengajar

import android.content.Intent
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import luthfi.anggi.e_prestasi.R
import luthfi.anggi.e_prestasi.Santri
import luthfi.anggi.e_prestasi.SantriAdapter

class ActivityMainPengajar : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var santriAdapter: SantriAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var santriList = mutableListOf<Santri>()

    private lateinit var tvUserName: TextView
    private lateinit var btnProfile: Button
    private lateinit var btnTambahSantri: Button
    private lateinit var ivProfile: ImageView
    private lateinit var etSearchSantri: EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_pengajar)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        tvUserName = findViewById(R.id.tvUserName)

        ivProfile = findViewById(R.id.ivProfile)


        recyclerView = findViewById(R.id.rvSantri)
        recyclerView.layoutManager = LinearLayoutManager(this)

        santriAdapter = SantriAdapter(santriList) { santri ->
            hapusSantri(santri)
        }
        recyclerView.adapter = santriAdapter

        FirebaseFirestore.getInstance().collection("santri")
            .get()
            .addOnSuccessListener { documents ->
                val santriList = mutableListOf<Santri>()
                for (document in documents) {
                    val santri = document.toObject(Santri::class.java).copy(uid = document.getString("uid") ?: "")
                    santriList.add(santri)
                }
                santriAdapter.updateList(santriList) // Perbarui adapter
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal mengambil data!", Toast.LENGTH_SHORT).show()
            }

        loadUserName()
        loadProfilePicture()
        loadSantri()


        // Tambahkan event klik untuk tombol profile
        btnProfile.setOnClickListener {
            val intent = Intent(this, ActivityProfile::class.java)
            startActivity(intent)
        }

        
    }

    // Tambahkan onResume() agar RecyclerView update otomatis
    override fun onResume() {
        super.onResume()
        loadSantri()  // Refresh daftar santri setelah kembali dari TambahSantriActivity
    }

    /*private fun filterSantri(query: String) {
        val filteredList = santriList.filter { it.nama.contains(query, ignoreCase = true) }
        santriAdapter.updateList(filteredList)
    }
     */


    // Fungsi untuk mengambil nama pengguna dari koleksi "pengajar"
    private fun loadUserName() {
        val user = auth.currentUser
        if (user != null) {
            Log.d("AUTH_DEBUG", "UID user login: ${user.uid}")
            firestore.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    Log.d("USER_NAME_DEBUG", "Isi dokumen: ${document.data}")
                    if (document.exists()) {
                        val nama = document.getString("name")
                        Log.d("USER_NAME_DEBUG", "Nama ditemukan: $nama")
                        tvUserName.text = nama ?: "Pengajar"
                    } else {
                        Log.d("USER_NAME_DEBUG", "Dokumen tidak ditemukan")
                        tvUserName.text = "Pengajar"
                    }
                }

        }
    }


    private fun loadSantri() {
        val userUid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val pengajarRef = firestore.collection("pengajar").document(userUid)

        pengajarRef.collection("kelas")
            .get()
            .addOnSuccessListener { kelasDocuments ->

                if (!kelasDocuments.isEmpty) {
                    val tingkatanList = kelasDocuments.mapNotNull { it.getString("tingkatan") }

                    if (tingkatanList.isNotEmpty()) {
                        firestore.collection("santri")
                            .whereIn("kelas", tingkatanList)
                            .get()
                            .addOnSuccessListener { santriDocuments ->
                                val newSantriList = mutableListOf<Santri>()
                                for (document in santriDocuments) {
                                    val santri = Santri(
                                        document.getString("uid") ?: "",
                                        document.getString("nama") ?: "",
                                        document.getString("tanggalLahir") ?: "",
                                        document.getString("alamat") ?: "",
                                        document.getString("kelas") ?: "",
                                        document.getString("gambar_url") ?: ""
                                    )
                                    newSantriList.add(santri)
                                }
                                santriList.clear()
                                santriList.addAll(newSantriList)
                                santriAdapter.updateList(santriList)
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Gagal memuat data santri!", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        santriAdapter.updateList(mutableListOf())
                    }
                } else {
                    Toast.makeText(this, "Tidak ada kelas yang diajar", Toast.LENGTH_SHORT).show()
                    santriAdapter.updateList(mutableListOf())
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal memuat kelas!", Toast.LENGTH_SHORT).show()
            }
    }




    private fun hapusSantri(santri: Santri) {
        firestore.collection("santri")
            .whereEqualTo("nama", santri.nama)
            .whereEqualTo("tanggalLahir", santri.tanggal_lahir)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    firestore.collection("santri").document(document.id).delete()
                        .addOnSuccessListener {
                            santriList.remove(santri)
                            santriAdapter.updateList(santriList)
                            Toast.makeText(this, "Santri berhasil dihapus", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Gagal menghapus santri", Toast.LENGTH_SHORT).show()
                        }
                }
            }
    }

    private fun loadProfilePicture() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(this, "User belum login!", Toast.LENGTH_SHORT).show()
            return
        }

        val db = FirebaseFirestore.getInstance()

        // Ambil URL gambar dari Firestore
        db.collection("pengajar").document(user.uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val imageUrl = document.getString("gambar_url")
                    if (!imageUrl.isNullOrEmpty()) {
                        // Gunakan Glide untuk menampilkan gambar ke ivProfile
                        Glide.with(this)
                            .load(imageUrl)
                            .placeholder(R.drawable.circle_background) // Gambar default jika kosong
                            .into(ivProfile)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal mengambil gambar profil!", Toast.LENGTH_SHORT).show()
            }
    }

}
 