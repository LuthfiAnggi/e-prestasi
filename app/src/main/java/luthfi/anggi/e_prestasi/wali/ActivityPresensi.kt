package luthfi.anggi.e_prestasi.wali

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
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
import luthfi.anggi.e_prestasi.adapter.PresensiAdapter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ActivityPresensi : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var spinnerBulan: Spinner
    private lateinit var spinnerTahun: Spinner
    private lateinit var spinnerKelas: Spinner
    private lateinit var btnFilter: Button
    private lateinit var recyclerViewPresensi: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvNoData: TextView

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var selectedBulan = Calendar.getInstance().get(Calendar.MONTH) + 1
    private var selectedTahun = Calendar.getInstance().get(Calendar.YEAR)
    private var selectedKelas = ""

    private lateinit var presensiAdapter: PresensiAdapter
    private val santriList = mutableListOf<SantriPresensi>()
    private val kelasList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_presensi)

        initViews()
        setupToolbar()
        setupSpinners()
        setupRecyclerView()
        loadKelasList()
        loadPresensiData()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        spinnerBulan = findViewById(R.id.bulanspinner)
        spinnerTahun = findViewById(R.id.tahunspinner)
        spinnerKelas = findViewById(R.id.kelasspinner)
        btnFilter = findViewById(R.id.FilterBtn)
        recyclerViewPresensi = findViewById(R.id.rvPresensi)
        progressBar = findViewById(R.id.progressBar)
        tvNoData = findViewById(R.id.tv_NoData)

        btnFilter.setOnClickListener { loadPresensiData() }
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Presensi Santri"
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
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Setup Spinner Tahun
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val tahunList = (currentYear - 5..currentYear + 1).map { it.toString() }
        val tahunAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tahunList)
        tahunAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTahun.adapter = tahunAdapter
        spinnerTahun.setSelection(tahunList.indexOf(currentYear.toString()))

        spinnerTahun.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedTahun = tahunList[position].toInt()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Setup Spinner Kelas
        spinnerKelas.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedKelas = if (position == 0) "" else kelasList[position - 1]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupRecyclerView() {
        presensiAdapter = PresensiAdapter(santriList, getDaysInMonth(selectedBulan, selectedTahun))
        recyclerViewPresensi.apply {
            layoutManager = LinearLayoutManager(this@ActivityPresensi)
            adapter = presensiAdapter
            // Tambahkan scroll horizontal jika diperlukan
            setHasFixedSize(true)
        }
    }

    private fun loadKelasList() {
        val waliUid = auth.currentUser?.uid ?: return

        db.collection("santri")
            .whereEqualTo("waliId", waliUid)
            .get()
            .addOnSuccessListener { documents ->
                val tempKelasList = mutableSetOf<String>()

                for (document in documents) {
                    val kelas = document.getString("kelas") ?: "-"
                    tempKelasList.add(kelas)
                }

                kelasList.clear()
                kelasList.addAll(tempKelasList.sorted())

                // Update spinner kelas
                val kelasListWithAll = mutableListOf("Semua Kelas").apply { addAll(kelasList) }
                val kelasAdapter = ArrayAdapter(this@ActivityPresensi, android.R.layout.simple_spinner_item, kelasListWithAll)
                kelasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerKelas.adapter = kelasAdapter
            }
            .addOnFailureListener { e ->
                showToast("Gagal memuat kelas: ${e.message}")
            }
    }

    private fun loadPresensiData() {
        val waliUid = auth.currentUser?.uid ?: return

        progressBar.visibility = View.VISIBLE
        recyclerViewPresensi.visibility = View.GONE
        tvNoData.visibility = View.GONE

        // Query santri berdasarkan wali yang login
        var query = db.collection("santri").whereEqualTo("waliId", waliUid)

        // Filter berdasarkan kelas jika dipilih
        if (selectedKelas.isNotEmpty()) {
            query = query.whereEqualTo("kelas", selectedKelas)
        }

        query.get()
            .addOnSuccessListener { documents ->
                santriList.clear()

                if (documents.isEmpty) {
                    showNoData()
                    return@addOnSuccessListener
                }

                val daysInMonth = getDaysInMonth(selectedBulan, selectedTahun)
                var processedCount = 0
                val totalSantri = documents.size()

                for (document in documents) {
                    val santriData = document.data
                    val santriUid = santriData["uid"] as? String ?: continue
                    val santriNama = santriData["nama"] as? String ?: "Tanpa Nama"
                    val santriKelas = santriData["kelas"] as? String ?: "-"

                    // Load presensi untuk santri ini
                    loadSantriPresensi(santriUid, santriNama, santriKelas, daysInMonth) {
                        processedCount++
                        if (processedCount == totalSantri) {
                            // Semua santri sudah diproses
                            showPresensiData(daysInMonth)
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                showToast("Gagal memuat data presensi: ${e.message}")
            }
    }

    private fun loadSantriPresensi(
        santriUid: String,
        santriNama: String,
        santriKelas: String,
        daysInMonth: Int,
        onComplete: () -> Unit
    ) {
        val statusPerHari = mutableMapOf<Int, String>()
        var processedDays = 0
        val currentDate = Calendar.getInstance()

        for (day in 1..daysInMonth) {
            val calendar = Calendar.getInstance().apply {
                set(selectedTahun, selectedBulan - 1, day)
            }

            // Jika hari belum terjadi, biarkan kosong (tidak ada status)
            if (calendar.after(currentDate)) {
                processedDays++
                if (processedDays == daysInMonth) {
                    santriList.add(SantriPresensi(santriNama, santriKelas, statusPerHari))
                    onComplete()
                }
                continue
            }

            // Cek apakah hari Sabtu atau Minggu
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
                statusPerHari[day] = "l" // Libur weekend
                processedDays++
                checkIfDayProcessingComplete(day, daysInMonth, processedDays, santriNama, santriKelas, statusPerHari, onComplete)
                continue
            }

            val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
            val formattedDate = dateFormat.format(calendar.time)

            // Cek libur khusus di Firestore (format: YYYY-MM-DD)
            val dateForLibur = String.format("%04d-%02d-%02d", selectedTahun, selectedBulan, day)

            db.collection("libur")
                .document(dateForLibur)
                .get()
                .addOnSuccessListener { liburDoc ->
                    if (liburDoc.exists()) {
                        statusPerHari[day] = "l" // Libur khusus
                        processedDays++
                        checkIfDayProcessingComplete(day, daysInMonth, processedDays, santriNama, santriKelas, statusPerHari, onComplete)
                    } else {
                        // Cek presensi di collection progress
                        db.collection("progress")
                            .document(santriUid)
                            .collection("tanggal")
                            .document(formattedDate)
                            .get()
                            .addOnSuccessListener { progressDoc ->
                                if (progressDoc.exists()) {
                                    statusPerHari[day] = "v" // Hadir
                                } else {
                                    // Cek izin
                                    db.collection("izin")
                                        .whereEqualTo("santri_id", santriUid)
                                        .whereEqualTo("tanggal", formattedDate)
                                        .get()
                                        .addOnSuccessListener { izinDocs ->
                                            if (!izinDocs.isEmpty) {
                                                statusPerHari[day] = "i" // Izin
                                            } else {
                                                statusPerHari[day] = "" // Alpha
                                            }
                                            processedDays++
                                            checkIfDayProcessingComplete(day, daysInMonth, processedDays, santriNama, santriKelas, statusPerHari, onComplete)
                                        }
                                        .addOnFailureListener {
                                            statusPerHari[day] = "" // Alpha jika gagal cek izin
                                            processedDays++
                                            checkIfDayProcessingComplete(day, daysInMonth, processedDays, santriNama, santriKelas, statusPerHari, onComplete)
                                        }
                                }

                                // Jika bukan dari callback izin, langsung process
                                if (progressDoc.exists()) {
                                    processedDays++
                                    checkIfDayProcessingComplete(day, daysInMonth, processedDays, santriNama, santriKelas, statusPerHari, onComplete)
                                }
                            }
                            .addOnFailureListener {
                                statusPerHari[day] = "" // Alpha jika gagal cek progress
                                processedDays++
                                checkIfDayProcessingComplete(day, daysInMonth, processedDays, santriNama, santriKelas, statusPerHari, onComplete)
                            }
                    }
                }
                .addOnFailureListener {
                    // Jika gagal cek libur, lanjutkan dengan pengecekan normal
                    db.collection("progress")
                        .document(santriUid)
                        .collection("tanggal")
                        .document(formattedDate)
                        .get()
                        .addOnSuccessListener { progressDoc ->
                            if (progressDoc.exists()) {
                                statusPerHari[day] = "v" // Hadir
                            } else {
                                db.collection("izin")
                                    .whereEqualTo("santri_id", santriUid)
                                    .whereEqualTo("tanggal", formattedDate)
                                    .get()
                                    .addOnSuccessListener { izinDocs ->
                                        if (!izinDocs.isEmpty) {
                                            statusPerHari[day] = "i" // Izin
                                        } else {
                                            statusPerHari[day] = "" // Alpha
                                        }
                                        processedDays++
                                        checkIfDayProcessingComplete(day, daysInMonth, processedDays, santriNama, santriKelas, statusPerHari, onComplete)
                                    }
                                    .addOnFailureListener {
                                        statusPerHari[day] = ""
                                        processedDays++
                                        checkIfDayProcessingComplete(day, daysInMonth, processedDays, santriNama, santriKelas, statusPerHari, onComplete)
                                    }
                            }

                            if (progressDoc.exists()) {
                                processedDays++
                                checkIfDayProcessingComplete(day, daysInMonth, processedDays, santriNama, santriKelas, statusPerHari, onComplete)
                            }
                        }
                        .addOnFailureListener {
                            statusPerHari[day] = ""
                            processedDays++
                            checkIfDayProcessingComplete(day, daysInMonth, processedDays, santriNama, santriKelas, statusPerHari, onComplete)
                        }
                }
        }
    }

    private fun checkIfDayProcessingComplete(
        day: Int,
        daysInMonth: Int,
        processedDays: Int,
        santriNama: String,
        santriKelas: String,
        statusPerHari: MutableMap<Int, String>,
        onComplete: () -> Unit
    ) {
        if (processedDays == daysInMonth) {
            santriList.add(SantriPresensi(santriNama, santriKelas, statusPerHari))
            onComplete()
        }
    }

    private fun showPresensiData(daysInMonth: Int) {
        progressBar.visibility = View.GONE

        if (santriList.isEmpty()) {
            showNoData()
        } else {
            recyclerViewPresensi.visibility = View.VISIBLE
            tvNoData.visibility = View.GONE

            // Update adapter dengan data baru
            presensiAdapter.updateData(santriList, daysInMonth)
        }
    }

    private fun showNoData() {
        progressBar.visibility = View.GONE
        recyclerViewPresensi.visibility = View.GONE
        tvNoData.visibility = View.VISIBLE
    }

    private fun getDaysInMonth(month: Int, year: Int): Int {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1)
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

// Data class untuk menyimpan data presensi santri

