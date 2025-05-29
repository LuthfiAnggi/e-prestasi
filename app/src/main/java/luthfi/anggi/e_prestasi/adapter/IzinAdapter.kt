package luthfi.anggi.e_prestasi.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import luthfi.anggi.e_prestasi.IzinModel
import luthfi.anggi.e_prestasi.R

class IzinAdapter(private val izinList: List<IzinModel>) :
    RecyclerView.Adapter<IzinAdapter.IzinViewHolder>() {

    inner class IzinViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNama: TextView = itemView.findViewById(R.id.tvNama)
        val tvTanggal: TextView = itemView.findViewById(R.id.tvTanggal)
        val tvAlasan: TextView = itemView.findViewById(R.id.tvAlasan)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IzinViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_izin, parent, false)
        return IzinViewHolder(view)
    }

    override fun onBindViewHolder(holder: IzinViewHolder, position: Int) {
        val izin = izinList[position]

        // Bind data to the TextViews
        holder.tvNama.text = izin.santri_nama
        holder.tvTanggal.text = izin.tanggal
        holder.tvAlasan.text = izin.alasan
        holder.tvStatus.text = izin.status

        // Dynamically set background color based on the status
        if (izin.status == "Disetujui") {
            holder.tvStatus.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.status_approved)) // Green
            holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.white)) // White text
        } else {
            holder.tvStatus.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.status_pending)) // Yellow
            holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.white)) // Black text
        }
    }

    override fun getItemCount(): Int = izinList.size
}

