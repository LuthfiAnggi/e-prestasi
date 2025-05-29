package luthfi.anggi.e_prestasi.wali

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
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
import luthfi.anggi.e_prestasi.adapter.ProgressAdapter

class FragmentProgress : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProgressAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var tvUserName: TextView
    private lateinit var btnProfile: Button
    private lateinit var btnLogout: ImageButton
    private lateinit var ivProfile: ImageView

    private val santriList = mutableListOf<Santri>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.activity_progress, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        tvUserName = view.findViewById(R.id.tvUserName)
        ivProfile = view.findViewById(R.id.ivProfile)
        btnLogout = view.findViewById(R.id.btn_Logout)

        recyclerView = view.findViewById(R.id.recyclerViewProgress)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = ProgressAdapter(santriList) // Gunakan ProgressAdapter
        recyclerView.adapter = adapter

        loadSantriData()
        loadUserName()
        loadProfilePicture()

        ivProfile.setOnClickListener {
            val user = auth.currentUser ?: return@setOnClickListener
            firestore.collection("wali").document(user.uid).get()
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

    }

    private fun loadUserName() {
        val user = auth.currentUser
        if (user != null) {
            firestore.collection("wali").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val nama = document.getString("nama") ?: "Wali"
                        tvUserName.text = nama // Gunakan nama tanpa interpolasi string
                    }
                }
                .addOnFailureListener {
                    tvUserName.text = "Wali"
                }
        }
    }

    private fun loadProfilePicture() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "User belum login!", Toast.LENGTH_SHORT).show()
            return
        }

        firestore.collection("wali").document(user.uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val imageUrl = document.getString("gambar_url")
                    if (!imageUrl.isNullOrEmpty()) {
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
            startActivity(Intent(requireContext(), ActivityProfileWali::class.java))
        }
    }

    private fun loadSantriData() {
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser == null) {
            Log.e("FragmentProgress", "User belum login")
            Toast.makeText(requireContext(), "User belum login", Toast.LENGTH_SHORT).show()
            return
        }

        val db = FirebaseFirestore.getInstance()
        val waliUid = auth.currentUser?.uid ?: return
        Log.d("FragmentProgress", "Mencari santri dengan waliId: $waliUid")

        db.collection("santri").whereEqualTo("waliId", waliUid).get()
            .addOnSuccessListener { result ->
                Log.d("FragmentProgress", "Data santri berhasil diambil. Jumlah dokumen: ${result.size()}")
                santriList.clear()

                for (document in result) {
                    Log.d("FragmentProgress", "Memproses dokumen: ${document.id}")
                    try {
                        val waliId = document.getString("waliId")
                        if (waliId != null) {
                            val santri = document.toObject(Santri::class.java)
                            santriList.add(santri)
                            Log.d("FragmentProgress", "Santri ditambahkan: $santri")
                        } else {
                            Log.w("FragmentProgress", "WaliId tidak ditemukan pada dokumen: ${document.id}")
                        }
                    } catch (e: Exception) {
                        Log.e("FragmentProgress", "Error parsing santri: ${document.id}", e)
                    }
                }

                adapter.updateList(santriList)
                Log.d("FragmentProgress", "List santri berhasil diperbarui dengan ${santriList.size} item")
            }
            .addOnFailureListener { e ->
                Log.e("FragmentProgress", "Gagal mengambil data santri", e)
                Toast.makeText(requireContext(), "Gagal mengambil data santri", Toast.LENGTH_SHORT).show()
            }
    }

}
