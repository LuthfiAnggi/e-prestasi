package luthfi.anggi.e_prestasi.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import luthfi.anggi.e_prestasi.PaymentHistory
import luthfi.anggi.e_prestasi.R

class PaymentHistoryAdapter(private val paymentList: List<PaymentHistory>) :
    RecyclerView.Adapter<PaymentHistoryAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(payment: PaymentHistory) {
            itemView.apply {
                findViewById<TextView>(R.id.tvOrderId).text = "ID: ${payment.orderId}"
                findViewById<TextView>(R.id.tvDate).text = payment.date
                findViewById<TextView>(R.id.tvAmount).text = "Rp ${payment.amount}"
                findViewById<TextView>(R.id.tvStatus).text = payment.status
                findViewById<TextView>(R.id.tvChannel).text = payment.paymentChannel
                findViewById<TextView>(R.id.tvSantri).text = payment.santriName

                // Set status color
                val statusColor = when(payment.status.lowercase()) {
                    "success" -> ContextCompat.getColor(context, R.color.status_approved)
                    "pending" -> ContextCompat.getColor(context, R.color.status_pending)
                    else -> ContextCompat.getColor(context, R.color.status_failed)
                }
                findViewById<TextView>(R.id.tvStatus).setTextColor(statusColor)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_payment_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(paymentList[position])
    }

    override fun getItemCount() = paymentList.size
}