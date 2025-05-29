package luthfi.anggi.e_prestasi.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import luthfi.anggi.e_prestasi.R

class TanggalAdapter(private val tanggalList: List<String>, private val onTanggalClick: (String) -> Unit) :
    RecyclerView.Adapter<TanggalAdapter.TanggalViewHolder>() {

    inner class TanggalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardContainer: CardView = itemView.findViewById(R.id.cardContainer)
        val tvTanggal: TextView = itemView.findViewById(R.id.tvTanggal)
        val iconCalendar: ImageView = itemView.findViewById(R.id.iconCalendar)
        val iconArrow: ImageView = itemView.findViewById(R.id.iconArrow)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TanggalViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tanggal, parent, false)
        return TanggalViewHolder(view)
    }

    override fun onBindViewHolder(holder: TanggalViewHolder, position: Int) {
        val tanggal = tanggalList[position]
        holder.tvTanggal.text = tanggal

        // Set click listener pada CardView
        holder.cardContainer.setOnClickListener {
            onTanggalClick(tanggal)
        }

        // Optional: Tambah animasi saat diklik
        holder.cardContainer.setOnTouchListener { view, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    view.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start()
                }
                android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                    view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start()
                }
            }
            false
        }
    }

    override fun getItemCount(): Int = tanggalList.size
}