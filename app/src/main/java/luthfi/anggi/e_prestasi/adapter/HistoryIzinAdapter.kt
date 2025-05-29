package luthfi.anggi.e_prestasi.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import luthfi.anggi.e_prestasi.IzinModel
import luthfi.anggi.e_prestasi.R

class HistoryIzinAdapter(private val izinList: List<IzinModel>) : RecyclerView.Adapter<HistoryIzinAdapter.IzinViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IzinViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history_izin , parent, false)
        return IzinViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: IzinViewHolder, position: Int) {
        val currentIzin = izinList[position]
        holder.tvTanggal.text = "Tanggal: ${currentIzin.tanggal}"
        holder.tvAlasan.text = "Alasan: ${currentIzin.alasan}"
        holder.tvStatus.text = "Status: ${currentIzin.status}"
    }

    override fun getItemCount(): Int {
        return izinList.size
    }

    class IzinViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTanggal: TextView = itemView.findViewById(R.id.tvTanggal)
        val tvAlasan: TextView = itemView.findViewById(R.id.tvAlasan)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
    }
}
