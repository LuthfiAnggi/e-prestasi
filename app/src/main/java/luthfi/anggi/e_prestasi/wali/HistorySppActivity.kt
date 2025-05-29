package luthfi.anggi.e_prestasi.wali

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import luthfi.anggi.e_prestasi.PaymentHistory
import luthfi.anggi.e_prestasi.adapter.PaymentHistoryAdapter
import luthfi.anggi.e_prestasi.databinding.ActivityHistorySppBinding

class HistorySppActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistorySppBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var adapter: PaymentHistoryAdapter
    private val paymentList = mutableListOf<PaymentHistory>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistorySppBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        setupRecyclerView()
        loadPaymentHistory()

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = PaymentHistoryAdapter(paymentList)
        binding.rvHistory.layoutManager = LinearLayoutManager(this)
        binding.rvHistory.adapter = adapter
    }

    private fun loadPaymentHistory() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val uidWali = currentUser.uid

            firestore.collection("pembayaran")
                .document(uidWali)
                .collection("all_payments") // You might need to adjust this collection structure
                .get()
                .addOnSuccessListener { result ->
                    paymentList.clear()
                    for (doc in result) {
                        val payment = PaymentHistory(
                            doc.getString("order_id") ?: "",
                            doc.getString("date") ?: "",
                            doc.getDouble("amount") ?: 0.0,
                            doc.getString("status") ?: "",
                            doc.getString("payment_channel") ?: "",
                            doc.getString("santri_name") ?: ""
                        )
                        paymentList.add(payment)
                    }
                    adapter.notifyDataSetChanged()

                    if (paymentList.isEmpty()) {
                        binding.tvEmpty.visibility = View.VISIBLE
                    } else {
                        binding.tvEmpty.visibility = View.GONE
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Gagal memuat riwayat pembayaran", Toast.LENGTH_SHORT).show()
                }
        }
    }
}