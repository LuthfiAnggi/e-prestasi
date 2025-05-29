package luthfi.anggi.e_prestasi.pengajar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import luthfi.anggi.e_prestasi.IzinModel
import luthfi.anggi.e_prestasi.R
import luthfi.anggi.e_prestasi.adapter.IzinAdapter

class FragmentLihatIzin : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tabFilterStatus: TabLayout
    private lateinit var izinAdapter: IzinAdapter
    private val izinList = mutableListOf<IzinModel>()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var kelasPengajar: List<String> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_lihat_izin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.recyclerViewIzin)
        tabFilterStatus = view.findViewById(R.id.tabFilter)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        izinAdapter = IzinAdapter(izinList)
        recyclerView.adapter = izinAdapter

        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        Toast.makeText(requireContext(), "UID: $currentUserUid", Toast.LENGTH_SHORT).show()

        getKelasPengajar()
        setupTabLayout()
    }

    private fun getKelasPengajar() {
        val pengajarUid = auth.currentUser?.uid ?: return

        db.collection("pengajar").document(pengajarUid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Diasumsikan field "kelas" berupa array atau string dipisah koma
                    val kelasField = document.get("kelas")
                    kelasPengajar = when (kelasField) {
                        is List<*> -> kelasField.filterIsInstance<String>()
                        is String -> kelasField.split(",").map { it.trim() }
                        else -> listOf()
                    }
                    // Load data izin secara default dengan filter "Semua"
                    loadKelasDanIzin("Semua")
                } else {
                    Toast.makeText(requireContext(), "Data pengajar tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Gagal mengambil data pengajar", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupTabLayout() {
        // Tambahkan listener untuk TabLayout
        tabFilterStatus.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                // Mendapatkan teks dari tab yang dipilih
                val selectedStatus = when(tab.position) {
                    0 -> "Semua"
                    1 -> "Disetujui"
                    2 -> "Pending"
                    else -> "Semua"
                }
                // Memuat data izin berdasarkan filter yang dipilih
                loadKelasDanIzin(selectedStatus)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                // Tidak ada aksi yang diperlukan
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                // Tidak ada aksi yang diperlukan
            }
        })
    }

    private fun loadKelasDanIzin(statusFilter: String) {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("kelas")
            .whereEqualTo("pengajar_uid", currentUserUid)
            .get()
            .addOnSuccessListener { kelasSnapshot ->
                val daftarTingkatan = kelasSnapshot.mapNotNull { it.getString("tingkatan") }

                if (daftarTingkatan.isEmpty()) {
                    Toast.makeText(requireContext(), "Tidak ada kelas yang diampu.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                loadIzinData(statusFilter, daftarTingkatan)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Gagal mengambil data kelas", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadIzinData(statusFilter: String, kelasList: List<String>) {
        var query = db.collection("izin")
            .whereIn("santri_kelas", kelasList)

        if (statusFilter != "Semua") {
            query = query.whereEqualTo("status", statusFilter)
        }

        query.get()
            .addOnSuccessListener { result ->
                izinList.clear()
                for (document in result) {
                    val izin = document.toObject(IzinModel::class.java)
                    izinList.add(izin)
                }
                izinAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Gagal mengambil data izin: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}