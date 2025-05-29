package luthfi.anggi.e_prestasi.adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import luthfi.anggi.e_prestasi.R
import luthfi.anggi.e_prestasi.Santri
import luthfi.anggi.e_prestasi.pengajar.ActivityLihatBuku

class ProgressAdapter(
    private var santriList: MutableList<Santri>
) : RecyclerView.Adapter<ProgressAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imvSantri: ImageView = view.findViewById(R.id.imvSantri)
        val tvNamaSantri: TextView = view.findViewById(R.id.tvNamaSantri)
        val btnEye: ImageButton = view.findViewById(R.id.btnEye)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_progress_santri, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val santri = santriList[position]

        // Debugging untuk memastikan data santri yang diterima
        Log.d("ProgressAdapter", "Santri UID: ${santri.uid}, Nama: ${santri.nama}")

        // Load foto profil santri
        Log.d("ProgressAdapter", "Memuat gambar untuk santri: ${santri.gambar_url}")
        Glide.with(holder.itemView.context)
            .load(santri.gambar_url)
            .placeholder(R.drawable.ic_profile_placeholder)
            .error(R.drawable.ic_launcher_foreground)
            .into(holder.imvSantri)

        holder.tvNamaSantri.text = santri.nama


        // Set OnClickListener pada Button Eye untuk melihat progres
        holder.btnEye.setOnClickListener {
            val intent = Intent(holder.itemView.context, ActivityLihatBuku::class.java)
            intent.putExtra("santri_nama", santri.nama)
            intent.putExtra("santri_gambar_url", santri.gambar_url)
            intent.putExtra("uid", santri.uid)
            intent.putExtra("tanggal", "19 Maret 2025") // Sesuaikan dengan Firestore

            // Log pengiriman data ke ActivityLihatBuku
            Log.d("ProgressAdapterView", "Mengirim UID ke intent: ${santri.uid}")
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount() = santriList.size

    // Fungsi untuk memperbarui list santri
    fun updateList(newList: MutableList<Santri>) {
        Log.d("ProgressAdapter", "Menerima ${newList.size} data santri untuk update.")
        santriList = newList
        notifyDataSetChanged()  // Memberitahu adapter untuk memperbarui tampilan
    }
}
