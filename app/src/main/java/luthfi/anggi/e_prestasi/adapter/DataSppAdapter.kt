package luthfi.anggi.e_prestasi.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import luthfi.anggi.e_prestasi.R
import luthfi.anggi.e_prestasi.wali.SppSantri

class DataSppAdapter(private val list: List<SppSantri>) :
    RecyclerView.Adapter<DataSppAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNama: TextView = itemView.findViewById(R.id.tvNamaSantri)
        private val tvKelas: TextView = itemView.findViewById(R.id.tvKelas)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)

        fun bind(sppSantri: SppSantri) {
            tvNama.text = sppSantri.nama
            tvKelas.text = sppSantri.kelas

            if (sppSantri.status == "success") {
                tvStatus.text = "LUNAS"
                tvStatus.setBackgroundResource(R.drawable.bg_status_lunas)
            } else {
                tvStatus.text = "BELUM LUNAS"
                tvStatus.setBackgroundResource(R.drawable.bg_status_belum_lunas)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dataspp, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size
}