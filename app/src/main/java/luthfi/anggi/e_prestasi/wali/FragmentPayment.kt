package luthfi.anggi.e_prestasi.wali

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.midtrans.sdk.corekit.callback.TransactionFinishedCallback
import com.midtrans.sdk.corekit.core.MidtransSDK
import com.midtrans.sdk.corekit.core.TransactionRequest
import com.midtrans.sdk.corekit.models.BillingAddress
import com.midtrans.sdk.corekit.models.CustomerDetails
import com.midtrans.sdk.corekit.models.ItemDetails
import com.midtrans.sdk.corekit.models.ShippingAddress
import com.midtrans.sdk.corekit.models.snap.TransactionResult
import com.midtrans.sdk.uikit.SdkUIFlowBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import luthfi.anggi.e_prestasi.R
import luthfi.anggi.e_prestasi.Santri
import luthfi.anggi.e_prestasi.TransactionStatusResponse
import luthfi.anggi.e_prestasi.adapter.SppSantriAdapter
import luthfi.anggi.e_prestasi.databinding.FragmentPaymentBinding
import okhttp3.Credentials
import okhttp3.OkHttpClient
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class FragmentPayment : Fragment(), TransactionFinishedCallback {
    private lateinit var binding: FragmentPaymentBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var recyclerView: RecyclerView
    private lateinit var santriAdapter: SppSantriAdapter
    private val santriList = mutableListOf<Santri>()
    private var accumulatedPayments: List<String> = listOf() // Untuk menyimpan bulan-bulan yang diakumulasikan

    private var selectedSantri: Santri? = null
    private var selectedPaymentField: String? = null

    var uid = ""
    var nama = ""
    var no_hp = ""
    var email = ""
    var alamat = ""
    var city = ""
    var postalCode = "65147"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initMidtransSDK()

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val user = auth.currentUser

        if (user != null) {
            firestore.collection("wali").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        uid = user.uid
                        nama = document.getString("nama").toString()
                        no_hp = document.getString("no_hp").toString()
                        email = document.getString("email").toString()
                        alamat = document.getString("alamat").toString()
                        //city = document.getString("alamat").toString()
                    }
                }
        }
        // Pemanggilan List Santri
        recyclerView = view.findViewById(R.id.rvSantriSpp)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        santriAdapter = SppSantriAdapter(santriList) { santri ->
            // Handle saat item diklik
            checkAndCreatePembayaran(santri)
            goToPayment(santri)
            // Anda bisa navigasi ke detail pembayaran atau dialog konfirmasi di sini
        }
        recyclerView.adapter = santriAdapter
        loadSantriData()

    }

    private fun loadSantriData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val uidWali = currentUser.uid

            firestore.collection("santri")
                .whereEqualTo("waliId", uidWali)
                .get()
                .addOnSuccessListener { result ->
                    santriList.clear()
                    for (doc in result) {
                        val santri = doc.toObject(Santri::class.java)
                        santriList.add(santri)
                    }
                    santriAdapter.notifyDataSetChanged()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Gagal memuat data santri", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "User belum login", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkAndCreatePembayaran(santri: Santri) {
        val user = auth.currentUser
        if (user != null) {
            val uidWali = user.uid
            val uidSantri = santri.uid
            val currentMonth = getCurrentMonth()
            val currentYear = getCurrentYear()
            val paymentField = "${String.format("%02d", currentMonth)}_${currentYear}"

            val pembayaranRef = firestore
                .collection("pembayaran")
                .document(uidWali)
                .collection(uidSantri)
                .document(paymentField)

            pembayaranRef.get()
                .addOnSuccessListener { document ->
                    if (!document.exists()) {
                        val newPaymentData = hashMapOf(
                            "status" to "unpaid",
                            "amount" to 30000.0,
                            "order_id" to "",
                            "payment_channel" to "",
                            "date" to ""
                        )
                        pembayaranRef.set(newPaymentData)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    requireContext(),
                                    "Pembayaran pertama untuk ${santri.nama} berhasil dibuat.",
                                    Toast.LENGTH_LONG
                                ).show()
                                goToPayment(santri) // lanjut ke pembayaran
                            }
                            .addOnFailureListener {
                                Toast.makeText(requireContext(), "Gagal membuat dokumen pembayaran.", Toast.LENGTH_LONG).show()
                            }
                    } else {
                        Toast.makeText(requireContext(), "Data pembayaran bulan ini sudah ada.", Toast.LENGTH_LONG).show()
                        goToPayment(santri) // tetap lanjut ke pembayaran
                    }
                }
        }
    }


    private fun initMidtransSDK() {
        val sdkUIFlowBuilder: SdkUIFlowBuilder = SdkUIFlowBuilder.init()
            .setClientKey("SB-Mid-client-xCPNEXRWGEzFkdpr")
            .setContext(requireContext())
            .setTransactionFinishedCallback(this)
            .setMerchantBaseUrl("http://192.168.1.5:80/midtrans/midtrans-mobile-merchant-server--php-sample-/charge/index.php/")
            .enableLog(true)
            .setLanguage("id")
        sdkUIFlowBuilder.buildSDK()
    }

    fun goToPayment(santri: Santri) {
        Log.d("goToPayment", "Dipanggil dengan santri: ${santri.nama} (${santri.uid})")

        val qty = 1
        val harga = 30000.0
        val amount = qty * harga

        val currentMonth = getCurrentMonth()
        val currentYear = getCurrentYear()
        val paymentField = "${String.format("%02d", currentMonth)}_${currentYear}"

        // Simpan santri dan paymentField agar bisa digunakan nanti
        selectedSantri = santri
        selectedPaymentField = paymentField

        Log.d("goToPayment", "paymentField: $paymentField")

        firestore.collection("pembayaran")
            .document(uid)
            .collection(santri.uid)
            .get()
            .addOnSuccessListener { querySnapshot ->
                var totalAmount = amount
                val monthsToPay = mutableListOf(paymentField) // Bulan yang akan dibayar

                // Dapatkan bulan pertama santri (dari created_at)
                val (startMonth, startYear) = getFirstPaymentMonth(santri.createdAt)
x
                // Hitung bulan-bulan yang belum dibayar mulai dari bulan pertama
                for (year in startYear..currentYear) {
                    val startM = if (year == startYear) startMonth else 1
                    val endM = if (year == currentYear) currentMonth - 1 else 12

                    for (month in startM..endM) {
                        val monthStr = String.format("%02d", month)
                        val paymentFieldToCheck = "${monthStr}_$year"

                        // Cek apakah bulan ini sudah dibayar
                        var isPaid = false
                        for (document in querySnapshot) {
                            if (document.id == paymentFieldToCheck) {
                                val status = document.getString("status") ?: ""
                                if (status != "success" && status != "settlement") {
                                    totalAmount += amount
                                    monthsToPay.add(paymentFieldToCheck)
                                }
                                isPaid = true
                                break
                            }
                        }

                        // Jika tidak ada data pembayaran sama sekali
                        if (!isPaid) {
                            totalAmount += amount
                            monthsToPay.add(paymentFieldToCheck)
                        }
                    }
                }

                // Simpan bulan-bulan yang akan dibayar
                accumulatedPayments = monthsToPay

                Log.d("goToPayment", "Total amount after accumulation: $totalAmount")
                Log.d("goToPayment", "Months to pay: ${monthsToPay.joinToString()}")

                val transactionRequest = TransactionRequest("Luthfi-TA-" + System.currentTimeMillis().toShort(), totalAmount)

                // Buat deskripsi item yang menjelaskan akumulasi
                val itemDescription = if (monthsToPay.size > 1) {
                    "Akumulasi SPP bulan ${monthsToPay.joinToString { it.substring(0, 2) }}"
                } else {
                    "SPP Bulan ${getCurrentMonthYear()}"
                }

                val detail = ItemDetails("SPP", totalAmount, 1, itemDescription)

                val itemDetails = ArrayList<ItemDetails>()
                itemDetails.add(detail)

                uiKitDetails(transactionRequest)
                transactionRequest.itemDetails = itemDetails

                MidtransSDK.getInstance().transactionRequest = transactionRequest
                MidtransSDK.getInstance().startPaymentUiFlow(requireActivity())
            }
    }

    private fun uiKitDetails(transactionRequest: TransactionRequest) {
        val customerDetails = CustomerDetails()
        customerDetails.customerIdentifier = uid
        customerDetails.phone = no_hp
        customerDetails.firstName = nama
        customerDetails.lastName = nama
        customerDetails.email = email

        val shippingAddress = ShippingAddress()
        shippingAddress.address = alamat
        shippingAddress.city = city
        shippingAddress.postalCode = postalCode
        customerDetails.shippingAddress = shippingAddress

        val billingAddress = BillingAddress()
        shippingAddress.address = alamat
        shippingAddress.city = city
        shippingAddress.postalCode = postalCode
        customerDetails.billingAddress = billingAddress

        transactionRequest.customerDetails = customerDetails
    }

    override fun onTransactionFinished(result: TransactionResult) {
        if (result.response != null && selectedSantri != null && accumulatedPayments.isNotEmpty()) {
            val transactionStatus = result.response.transactionStatus
            val orderId = result.response.transactionId
            val paymentChannel = result.response.paymentType
            val currentTime = System.currentTimeMillis()
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val formattedDate = sdf.format(Date(currentTime))

            // Update semua bulan yang diakumulasikan
            for (paymentField in accumulatedPayments) {
                val pembayaranRef = firestore
                    .collection("pembayaran")
                    .document(uid)
                    .collection(selectedSantri!!.uid)
                    .document(paymentField)

                val paymentData = hashMapOf(
                    "order_id" to orderId,
                    "date" to formattedDate,
                    "amount" to 30000.0,
                    "status" to transactionStatus,
                    "payment_channel" to paymentChannel,
                    "santri_name" to selectedSantri!!.nama,
                    "santri_id" to selectedSantri!!.uid,
                    "bulan_tahun" to paymentField
                )

                when (transactionStatus) {
                    "success", "settlement" -> {
                        pembayaranRef.set(paymentData)
                        saveToHistory(paymentData)
                    }
                    "pending" -> {
                        pembayaranRef.set(paymentData)
                        // Tidak perlu panggil checkMidtransStatus di sini
                    }
                    "expired", "failed" -> {
                        pembayaranRef.set(paymentData)
                    }
                }
            }

            // Untuk status pending, panggil pengecekan status setelah loop
            if (transactionStatus == "pending") {
                checkMidtransStatus(orderId, formattedDate, paymentChannel)
            } else if (transactionStatus == "success" || transactionStatus == "settlement") {
                Toast.makeText(requireContext(), "Pembayaran berhasil", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun checkMidtransStatus(
        orderId: String,
        formattedDate: String,
        paymentChannel: String
    ) {
        val uidWali = uid
        val santri = selectedSantri
        val paymentField = "${String.format("%02d", getCurrentMonth())}_${getCurrentYear()}"

        if (santri == null) {
            Toast.makeText(requireContext(), "Data santri tidak tersedia.", Toast.LENGTH_SHORT).show()
            return
        }

        // Prepare payment data
        val paymentData = hashMapOf(
            "order_id" to orderId,
            "date" to formattedDate,
            "amount" to 30000.0,
            "status" to "pending", // Default status
            "payment_channel" to paymentChannel,
            "santri_name" to santri.nama,
            "santri_id" to santri.uid,
            "bulan_tahun" to paymentField
        )

        val pembayaranRef = firestore
            .collection("pembayaran")
            .document(uidWali)
            .collection(santri.uid)
            .document(paymentField)

        val historyRef = firestore
            .collection("pembayaran")
            .document(uidWali)
            .collection("all_payments")
            .document(orderId)

        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("Authorization", Credentials.basic("SB-Mid-server-umZzwFA6DFMLLacrSNLr_sB5", ""))
                    .build()
                chain.proceed(request)
            }
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.sandbox.midtrans.com/")
            .client(client)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()

        val service = retrofit.create(MidtransApi::class.java)
        val call = service.getTransactionStatus(orderId)

        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    response.body()?.let { responseBody ->
                        try {
                            val json = JSONObject(responseBody)
                            val status = json.getString("transaction_status")

                            // Update semua bulan yang diakumulasikan
                            for (paymentField in accumulatedPayments) {
                                val pembayaranRef = firestore
                                    .collection("pembayaran")
                                    .document(uidWali)
                                    .collection(santri.uid)
                                    .document(paymentField)

                                val historyRef = firestore
                                    .collection("pembayaran")
                                    .document(uidWali)
                                    .collection("all_payments")
                                    .document("$orderId-$paymentField") // Gunakan ID unik

                                val paymentData = hashMapOf(
                                    "order_id" to orderId,
                                    "date" to formattedDate,
                                    "amount" to 30000.0,
                                    "status" to status,
                                    "payment_channel" to paymentChannel,
                                    "santri_name" to santri.nama,
                                    "santri_id" to santri.uid,
                                    "bulan_tahun" to paymentField
                                )

                                pembayaranRef.set(paymentData)
                                historyRef.set(paymentData)
                            }

                            when (status) {
                                "settlement" -> {
                                    Toast.makeText(
                                        requireContext(),
                                        "Pembayaran berhasil diselesaikan",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                "pending" -> {
                                    Toast.makeText(
                                        requireContext(),
                                        "Pembayaran masih dalam proses",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                else -> {
                                    Toast.makeText(
                                        requireContext(),
                                        "Status pembayaran: ${status.capitalize()}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                        } catch (e: Exception) {
                            Log.e("Midtrans", "Error parsing response", e)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e("Midtrans", "API call failed", t)
                Toast.makeText(
                    requireContext(),
                    "Gagal memeriksa status pembayaran",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun getFirstPaymentMonth(createdAt: String): Pair<Int, Int> {
        return try {
            // Format tanggal: "2025-03-15 10:30:00" (contoh)
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = sdf.parse(createdAt)

            val cal = Calendar.getInstance()
            cal.time = date

            // Ambil bulan dan tahun
            val month = cal.get(Calendar.MONTH) + 1 // +1 karena bulan dimulai dari 0
            val year = cal.get(Calendar.YEAR)

            Pair(month, year)
        } catch (e: Exception) {
            Log.e("Payment", "Error parsing createdAt: $createdAt", e)
            // Default ke bulan dan tahun sekarang jika error
            Pair(getCurrentMonth(), getCurrentYear())
        }
    }

    private fun getMonthListFromPaymentFields(): List<String> {
        return accumulatedPayments.map {
            val parts = it.split("_")
            if (parts.size == 2) {
                "${parts[0]}/${parts[1]}"
            } else {
                it
            }
        }
    }

    private fun saveToHistory(paymentData: Map<String, Any>) {
        try {
            val orderId = paymentData["order_id"]?.toString() ?: return
            val historyRef = firestore
                .collection("pembayaran")
                .document(uid)
                .collection("all_payments")
                .document(orderId)

            historyRef.set(paymentData)
                .addOnSuccessListener {
                    Log.d("History", "Riwayat berhasil disimpan")
                }
                .addOnFailureListener { e ->
                    Log.e("History", "Gagal menyimpan riwayat", e)
                }
        } catch (e: Exception) {
            Log.e("History", "Error processing payment data", e)
        }
    }



    // Interface Retrofit
    interface MidtransApi {
        @GET("/v2/{order_id}/status")
        fun getTransactionStatus(
            @Path("order_id") orderId: String
        ): Call<String>  // Ambil response sebagai String
    }

    private fun getCurrentMonth(): Int {
        return Calendar.getInstance().get(Calendar.MONTH) + 1
    }

    private fun getCurrentYear(): Int {
        return Calendar.getInstance().get(Calendar.YEAR)
    }

    private fun getCurrentMonthYear(): String {
        val calendar = Calendar.getInstance()
        val month = String.format("%02d", calendar.get(Calendar.MONTH) + 1)
        val year = calendar.get(Calendar.YEAR)
        return "${month}_$year"
    }

    // Retrofit interface untuk memverifikasi status transaksi
    interface MidtransService {
        @GET("v2/{transaction_id}/status")
        suspend fun getTransactionStatus(
            @Path("transaction_id") transactionId: String,
            @Header("Authorization") authorization: String
        ): Response<TransactionStatusResponse>
    }
}
