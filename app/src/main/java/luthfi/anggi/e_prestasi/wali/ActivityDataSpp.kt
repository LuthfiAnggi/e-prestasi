package luthfi.anggi.e_prestasi.wali

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import luthfi.anggi.e_prestasi.R
import luthfi.anggi.e_prestasi.adapter.DataSppAdapter
import java.util.Calendar

class ActivityDataSpp : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var spinnerBulan: Spinner
    private lateinit var spinnerTahun: Spinner
    private lateinit var rvSppSantri: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvNoData: TextView

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var selectedBulan = Calendar.getInstance().get(Calendar.MONTH) + 1
    private var selectedTahun = Calendar.getInstance().get(Calendar.YEAR)

    private lateinit var adapter: DataSppAdapter
    private val santriList = mutableListOf<SppSantri>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dataspp)

        initViews()
        setupToolbar()
        setupSpinners()
        setupRecyclerView()
        loadSppData()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        spinnerBulan = findViewById(R.id.spinnerBulan)
        spinnerTahun = findViewById(R.id.spinnerTahun)
        rvSppSantri = findViewById(R.id.rvSppSantri)
        progressBar = findViewById(R.id.progressBar)
        tvNoData = findViewById(R.id.tvNoData)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Data SPP Santri"
            setDisplayHomeAsUpEnabled(true)
        }
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupSpinners() {
        // Setup Spinner Bulan
        val bulanArray = resources.getStringArray(R.array.bulan_array)
        val bulanAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, bulanArray)
        bulanAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerBulan.adapter = bulanAdapter
        spinnerBulan.setSelection(selectedBulan - 1)

        spinnerBulan.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedBulan = position + 1
                //loadSppData()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Setup Spinner Tahun
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val tahunList = (currentYear - 1..currentYear + 1).map { it.toString() }
        val tahunAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tahunList)
        tahunAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTahun.adapter = tahunAdapter
        spinnerTahun.setSelection(tahunList.indexOf(currentYear.toString()))

        spinnerTahun.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedTahun = tahunList[position].toInt()
                //loadSppData()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupRecyclerView() {
        adapter = DataSppAdapter(santriList)
        rvSppSantri.apply {
            layoutManager = LinearLayoutManager(this@ActivityDataSpp)
            adapter = this@ActivityDataSpp.adapter
            setHasFixedSize(true)
        }
    }

    private fun loadSppData() {
        val waliUid = auth.currentUser?.uid ?: return
        val bulanTahun = "${String.format("%02d", selectedBulan)}_$selectedTahun"

        progressBar.visibility = View.VISIBLE
        santriList.clear()

        db.collection("santri")
            .whereEqualTo("waliId", waliUid)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val santriUid = document.id
                    val santriNama = document.getString("nama") ?: "Tanpa Nama"
                    val santriKelas = document.getString("kelas") ?: "-"

                    db.collection("pembayaran")
                        .document(waliUid)
                        .collection(santriUid)
                        .document(bulanTahun)
                        .get()
                        .addOnSuccessListener { sppDoc ->
                            val status = if (sppDoc.exists()) {
                                sppDoc.getString("status") ?: "pending"
                            } else {
                                "pending"
                            }

                            santriList.add(SppSantri(
                                uid = santriUid,
                                nama = santriNama,
                                kelas = santriKelas,
                                status = status
                            ))

                            adapter.notifyDataSetChanged()
                        }
                }
                progressBar.visibility = View.GONE
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Gagal memuat data", Toast.LENGTH_SHORT).show()
            }
    }


    private fun showData() {
        progressBar.visibility = View.GONE

        if (santriList.isEmpty()) {
            showNoData()
        } else {
            rvSppSantri.visibility = View.VISIBLE
            tvNoData.visibility = View.GONE
            adapter.notifyDataSetChanged()
        }
    }

    private fun showNoData() {
        progressBar.visibility = View.GONE
        rvSppSantri.visibility = View.GONE
        tvNoData.visibility = View.VISIBLE
    }
}

data class SppSantri(
    val uid: String,
    val nama: String,
    val kelas: String,
    val status: String // "success" atau lainnya
)