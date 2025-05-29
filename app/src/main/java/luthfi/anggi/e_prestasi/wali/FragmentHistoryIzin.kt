package luthfi.anggi.e_prestasi.wali


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import luthfi.anggi.e_prestasi.IzinModel
import luthfi.anggi.e_prestasi.R
import luthfi.anggi.e_prestasi.adapter.HistoryIzinAdapter

class FragmentHistoryIzin : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var izinAdapter: HistoryIzinAdapter
    private val izinList = mutableListOf<IzinModel>()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.activity_history_izin, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewIzin)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        izinAdapter = HistoryIzinAdapter(izinList)
        recyclerView.adapter = izinAdapter

        val waliUid = auth.currentUser?.uid ?: return view
        loadIzinHistory(waliUid)

        return view
    }

    private fun loadIzinHistory(waliUid: String) {
        db.collection("izin")
            .whereEqualTo("wali_uid", waliUid)
            .get()
            .addOnSuccessListener { result ->
                izinList.clear()
                for (document in result) {
                    val izin = document.toObject(IzinModel::class.java)
                    izinList.add(izin)
                }
                izinAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Gagal memuat history izin: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}