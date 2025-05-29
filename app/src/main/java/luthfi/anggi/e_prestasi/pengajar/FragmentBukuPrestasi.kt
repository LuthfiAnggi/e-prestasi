package luthfi.anggi.e_prestasi.pengajar

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import luthfi.anggi.e_prestasi.R
import luthfi.anggi.e_prestasi.Santri
import luthfi.anggi.e_prestasi.adapter.BukuPrestasiAdapter

class FragmentBukuPrestasi : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BukuPrestasiAdapter
    private val santriList = mutableListOf<Santri>()
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var toolbar: Toolbar
    private lateinit var btnLogout: ImageButton
    private lateinit var ivProfile: ImageView
    private lateinit var tvUserName: TextView
    private lateinit var tabLayout: TabLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.activity_buku_prestasi, container, false)
    }

    private var modeTambah = false // false = lihat, true = tambah

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Setup toolbar
        toolbar = view.findViewById(R.id.toolbar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = ""

        // Allow options menu in this fragment
        setHasOptionsMenu(true)

        ivProfile = view.findViewById(R.id.ivProfile)
        tvUserName = view.findViewById(R.id.tvUserName)
        btnLogout = view.findViewById(R.id.btn_Logout)
        tabLayout = view.findViewById(R.id.tabLayout)
        recyclerView = view.findViewById(R.id.recyclerViewBukuPrestasi)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = BukuPrestasiAdapter(santriList) { santri ->
            if (modeTambah) {
                val intent = Intent(requireContext(), ActivityTambahBuku::class.java)
                intent.putExtra("santri_uid", santri.uid)
                intent.putExtra("santri_nama", santri.nama)
                intent.putExtra("santri_kelas", santri.kelas)
                intent.putExtra("santri_gambar_url", santri.gambar_url)
                startActivity(intent)
            } else {
                val intent = Intent(requireContext(), ActivityLihatBuku::class.java)
                intent.putExtra("uid", santri.uid)
                intent.putExtra("santri_nama", santri.nama)
                intent.putExtra("santri_kelas", santri.kelas)
                intent.putExtra("santri_gambar_url", santri.gambar_url)
                intent.putExtra("tanggal", "19 Maret 2025")
                startActivity(intent)
            }
        }
        recyclerView.adapter = adapter

        // Setup TabLayout
        setupTabLayout()

        loadSantriData()
        //loadUserName()
        //loadProfilePicture()

    }

    private fun setupTabLayout() {
        // Initialize TabLayout with "Lihat" selected by default
        tabLayout.apply {
            // Make sure we have exactly 2 tabs (in case there are default tabs from XML)
            removeAllTabs()
            addTab(newTab().setText("Tambah"))
            addTab(newTab().setText("Lihat"))

            // Set the default selected tab to "Lihat"
            getTabAt(1)?.select()
            modeTambah = false
        }

        // Set listener for tab selection
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        // Tambah tab selected
                        modeTambah = true
                        Toast.makeText(requireContext(), "Pilih santri untuk menambah buku", Toast.LENGTH_SHORT).show()
                    }
                    1 -> {
                        // Lihat tab selected
                        modeTambah = false
                        Toast.makeText(requireContext(), "Pilih santri untuk melihat buku", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // Nothing to do here
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Re-clicking the selected tab could show the same toast
                when (tab?.position) {
                    0 -> Toast.makeText(requireContext(), "Pilih santri untuk menambah buku", Toast.LENGTH_SHORT).show()
                    1 -> Toast.makeText(requireContext(), "Pilih santri untuk melihat buku", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun loadSantriData() {
        val userUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val firestore = FirebaseFirestore.getInstance()
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
                                santriList.clear()
                                for (document in santriDocuments) {
                                    val nama = document.getString("nama") ?: ""
                                    val kelas = document.getString("kelas") ?: "Kelas 0"
                                    val gambarUrl = document.getString("gambar_url") ?: ""

                                    val santri = Santri(
                                        uid = document.id,     // atau bisa pakai document.getString("uid")
                                        nama = nama,
                                        tanggal_lahir = "",     // dikosongkan
                                        alamat = "",           // dikosongkan
                                        kelas = kelas,
                                        gambar_url = gambarUrl
                                    )
                                    santriList.add(santri)
                                }
                                adapter.updateList(santriList)
                            }
                            .addOnFailureListener {
                                Log.e("FragmentBukuPrestasi", "Gagal memuat data santri", it)
                            }
                    } else {
                        adapter.updateList(mutableListOf())
                    }
                } else {
                    Log.d("FragmentBukuPrestasi", "Pengajar tidak mengajar kelas apapun.")
                    adapter.updateList(mutableListOf())
                }
            }
            .addOnFailureListener {
                Log.e("FragmentBukuPrestasi", "Gagal memuat data kelas pengajar", it)
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
}