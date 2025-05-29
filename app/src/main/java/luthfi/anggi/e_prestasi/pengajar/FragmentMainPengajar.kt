package luthfi.anggi.e_prestasi.pengajar

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import luthfi.anggi.e_prestasi.LoginActivity
import luthfi.anggi.e_prestasi.R
import luthfi.anggi.e_prestasi.Santri
import luthfi.anggi.e_prestasi.SantriAdapter

class FragmentMainPengajar : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var santriAdapter: SantriAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var santriList = mutableListOf<Santri>()

    private lateinit var btnLogout: ImageButton
    private lateinit var ivProfile: ImageView
    private lateinit var tvUserName: TextView
    private lateinit var tvKelas: TextView
    private lateinit var btnTambahSantri: Button
    private lateinit var etSearchSantri: SearchView



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_main_pengajar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        ivProfile = view.findViewById(R.id.ivProfile)
        tvUserName = view.findViewById(R.id.tvUserName)
        btnLogout = view.findViewById(R.id.btn_Logout)
        //btnTambahSantri = view.findViewById(R.id.btnTambahSantri)
        recyclerView = view.findViewById(R.id.rvSantri)
        etSearchSantri = view.findViewById(R.id.etSearchSantri)
        tvKelas = view.findViewById(R.id.tvKelas)
        

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        santriAdapter = SantriAdapter(santriList) { santri ->
            hapusSantri(santri)
        }
        recyclerView.adapter = santriAdapter

        loadSantri()
        loadNamaKelasPengajar(tvKelas)
        loadUserName()
        loadProfilePicture()

        ivProfile.setOnClickListener {
            val user = auth.currentUser ?: return@setOnClickListener
            firestore.collection("pengajar").document(user.uid).get()
                .addOnSuccessListener { document ->
                    val imageUrl = document.getString("gambar_url") ?: ""
                    if (imageUrl.isNotEmpty()) {
                        showProfileDialog(imageUrl)
                    } else {
                        Toast.makeText(requireContext(), "Foto belum tersedia", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }

        etSearchSantri.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false // kita tidak pakai submit
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterSantriList(newText.orEmpty())
                return true
            }
        })

    }

    override fun onResume() {
        super.onResume()
        loadSantri()
    }

    private fun loadNamaKelasPengajar(tvKelas: TextView) {
        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()
        val uid = auth.currentUser?.uid

        uid?.let { pengajarUid ->
            firestore.collection("pengajar")
                .document(pengajarUid)
                .collection("kelas")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val kelasDoc = querySnapshot.documents[0]
                        val namaKelas = kelasDoc.getString("nama_kelas")
                        tvKelas.text = namaKelas ?: "Belum ada kelas"
                    } else {
                        tvKelas.text = "Belum ada kelas"
                    }
                }
                .addOnFailureListener { e ->
                    tvKelas.text = "Gagal memuat kelas"
                    Log.e("FirestoreError", "Error: ${e.message}")
                }
        }
    }

    private fun loadUserName() {
        val user = auth.currentUser
        user?.let {
            firestore.collection("pengajar").document(user.uid).get()
                .addOnSuccessListener { document ->
                    tvUserName.text = document.getString("nama") ?: "Pengajar"
                }
                .addOnFailureListener {
                    tvUserName.text = "Pengajar"
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
                                        document.getString("tanggal_lahir") ?: "",
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
                                Toast.makeText(requireContext(), "Gagal memuat data santri!", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        santriAdapter.updateList(mutableListOf())
                    }
                } else {
                    Toast.makeText(requireContext(),  "Tidak ada kelas yang diajar", Toast.LENGTH_SHORT).show()
                    santriAdapter.updateList(mutableListOf())
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(),  "Gagal memuat kelas!", Toast.LENGTH_SHORT).show()
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
                            Toast.makeText(requireContext(), "Santri berhasil dihapus", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Gagal menghapus santri", Toast.LENGTH_SHORT).show()
                        }
                }
            }
    }

    private fun filterSantriList(query: String) {
        val filteredList = santriList.filter {
            it.nama.contains(query, ignoreCase = true)
        }.toMutableList()

        santriAdapter.updateList(filteredList)
    }

    private fun loadProfilePicture() {
        val user = auth.currentUser ?: return

        firestore.collection("pengajar").document(user.uid).get()
            .addOnSuccessListener { document ->
                document.getString("gambar_url")?.let { imageUrl ->
                    if (imageUrl.isNotEmpty()) {
                        Glide.with(this)
                            .load(imageUrl)
                            .placeholder(R.drawable.circle_background)
                            .into(ivProfile)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Gagal mengambil gambar profil!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showProfileDialog(imageUrl: String) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_profile_image, null)
        val dialogBuilder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)

        val alertDialog = dialogBuilder.create()
        alertDialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
        alertDialog.show()

        val imageView = dialogView.findViewById<ImageView>(R.id.imgProfileDialog)
        val btnEdit = dialogView.findViewById<Button>(R.id.btnEditProfile)

        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.circle_background)
            .into(imageView)

        btnEdit.setOnClickListener {
            alertDialog.dismiss()
            startActivity(Intent(requireContext(), ActivityProfile::class.java))
        }
    }


}
