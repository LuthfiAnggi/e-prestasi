package luthfi.anggi.e_prestasi.wali

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import luthfi.anggi.e_prestasi.IzinModel
import luthfi.anggi.e_prestasi.R
import luthfi.anggi.e_prestasi.adapter.HistoryIzinAdapter

class ActivityHistoryIzin : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var izinAdapter: HistoryIzinAdapter
    private val izinList = mutableListOf<IzinModel>()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history_izin)

        recyclerView = findViewById(R.id.recyclerViewIzin)
        recyclerView.layoutManager = LinearLayoutManager(this)

        izinAdapter = HistoryIzinAdapter(izinList)
        recyclerView.adapter = izinAdapter

        val waliUid = auth.currentUser?.uid ?: return
        loadIzinHistory(waliUid)
    }

    private fun loadIzinHistory(waliUid: String) {
        db.collection("izin")
            .whereEqualTo("wali_uid", waliUid) // Ambil data berdasarkan wali_id yang sedang login
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
                Toast.makeText(this, "Gagal memuat history izin: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
