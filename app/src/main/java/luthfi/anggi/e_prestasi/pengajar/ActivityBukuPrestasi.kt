package luthfi.anggi.e_prestasi.pengajar


import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import luthfi.anggi.e_prestasi.R
import luthfi.anggi.e_prestasi.Santri
import luthfi.anggi.e_prestasi.adapter.BukuPrestasiAdapter

class ActivityBukuPrestasi : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BukuPrestasiAdapter
    private val santriList = mutableListOf<Santri>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buku_prestasi)

        recyclerView = findViewById(R.id.recyclerViewBukuPrestasi)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Inisialisasi adapter tanpa tombol
        //adapter = BukuPrestasiAdapter(santriList)
        recyclerView.adapter = adapter

        loadSantriData()
    }

    private fun loadSantriData() {
        val db = FirebaseFirestore.getInstance()

        db.collection("santri")
            .get()
            .addOnSuccessListener { result ->
                santriList.clear()
                for (document in result) {
                    val santri = document.toObject(Santri::class.java)
                    santriList.add(santri)
                }
                adapter.updateList(santriList)
            }
            .addOnFailureListener { e ->
                Log.e("ActivityBukuPrestasi", "Error mengambil data santri", e)
            }
    }
}
