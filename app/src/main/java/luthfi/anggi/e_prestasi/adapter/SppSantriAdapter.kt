package luthfi.anggi.e_prestasi.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import luthfi.anggi.e_prestasi.R
import luthfi.anggi.e_prestasi.Santri

class SppSantriAdapter(
    private val santriList: List<Santri>,
    private val onItemClick: (Santri) -> Unit
) : RecyclerView.Adapter<SppSantriAdapter.SantriViewHolder>() {

    inner class SantriViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val namaTextView: TextView = itemView.findViewById(R.id.tvNamaSantri)
        val kelasTextView: TextView = itemView.findViewById(R.id.tvKelasSantri)
        val imageView: ImageView = itemView.findViewById(R.id.ivFotoSantri)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SantriViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_spp_santri, parent, false)
        return SantriViewHolder(view)
    }

    override fun onBindViewHolder(holder: SantriViewHolder, position: Int) {
        val santri = santriList[position]
        holder.namaTextView.text = santri.nama
        holder.kelasTextView.text = santri.kelas

        Glide.with(holder.itemView.context)
            .load(santri.gambar_url)
            .placeholder(R.drawable.ic_profile_placeholder) // Ganti dengan gambar placeholder Anda
            .into(holder.imageView)

        holder.itemView.setOnClickListener {
            onItemClick(santri)
        }
    }

    override fun getItemCount(): Int = santriList.size
}
