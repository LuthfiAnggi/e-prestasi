package luthfi.anggi.e_prestasi
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class SantriAdapter(
    private var santriList: MutableList<Santri>,
    private val onDelete: (Santri) -> Unit // Callback untuk menghapus
) : RecyclerView.Adapter<SantriAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNama: TextView = view.findViewById(R.id.tvNamaSantri)
        val tvTanggalLahir: TextView = view.findViewById(R.id.tvTanggalLahirSantri)
        val tvAlamat: TextView = view.findViewById(R.id.tvAlamatSantri)
        val tvKelas: TextView = view.findViewById(R.id.tvKelasSantri)
        //val btnHapus: ImageButton = view.findViewById(R.id.btnHapusSantri)
        val imvSantri: ImageView = view.findViewById(R.id.imvSantri)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_santri, parent, false)
        return ViewHolder(view)
    }



    fun updateList(newList: MutableList<Santri>) {
        santriList = newList
        notifyDataSetChanged() // Perbarui RecyclerView
    }



    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val santri = santriList[position]

        // Cek apakah gambar_url tidak null atau kosong
        if (!santri.gambar_url.isNullOrEmpty()) {
            Log.d("SantriAdapter", "Memuat gambar dari: ${santri.gambar_url}")
            Glide.with(holder.itemView.context)
                .load(santri.gambar_url)
                .placeholder(R.drawable.ic_profile_placeholder)
                .error(R.drawable.ic_launcher_foreground) // Jika gambar gagal dimuat
                .into(holder.imvSantri)
        } else {
            Log.d("SantriAdapter", "URL gambar kosong, menampilkan default")
            holder.imvSantri.setImageResource(R.drawable.ic_profile_placeholder)
        }

        holder.tvNama.text = santri.nama
        holder.tvTanggalLahir.text = "Tanggal Lahir: ${santri.tanggal_lahir}"
        holder.tvAlamat.text = "Alamat: ${santri.alamat}"
        holder.tvKelas.text = "Kelas: ${santri.kelas}"


    }

    override fun getItemCount() = santriList.size


}
