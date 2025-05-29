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

class BukuPrestasiAdapter(
    private var santriList: MutableList<Santri>,
    private val onItemClick: (Santri) -> Unit
) : RecyclerView.Adapter<BukuPrestasiAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imvSantri: ImageView = view.findViewById(R.id.imvSantri)
        val tvNamaSantri: TextView = view.findViewById(R.id.tvNamaSantri)
        val tvKelasSantri: TextView =view.findViewById(R.id.tvKelasSantri)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_santri_buku_prestasi, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val santri = santriList[position]


        holder.tvNamaSantri.text = santri.nama
        holder.tvKelasSantri.text = santri.kelas
        Glide.with(holder.itemView.context)
            .load(santri.gambar_url)
            .placeholder(R.drawable.ic_profile_placeholder)
            .error(R.drawable.ic_launcher_foreground)
            .into(holder.imvSantri)

        holder.itemView.setOnClickListener {
            onItemClick(santri)
        }
    }

    override fun getItemCount() = santriList.size

    fun updateList(newList: MutableList<Santri>) {
        santriList = newList
        notifyDataSetChanged()
    }
}
