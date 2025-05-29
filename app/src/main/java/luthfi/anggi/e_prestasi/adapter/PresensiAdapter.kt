package luthfi.anggi.e_prestasi.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import luthfi.anggi.e_prestasi.R
import luthfi.anggi.e_prestasi.wali.SantriPresensi

class PresensiAdapter(
    private var santriList: List<SantriPresensi>,
    private var daysInMonth: Int
) : RecyclerView.Adapter<PresensiAdapter.PresensiViewHolder>() {

    class PresensiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNamaSantri: TextView = itemView.findViewById(R.id.tvNamaSantri)
        val tvKelas: TextView = itemView.findViewById(R.id.tvKelas)
        val containerHari: LinearLayout = itemView.findViewById(R.id.containerHari)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PresensiViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_presensi_row, parent, false)
        return PresensiViewHolder(view)
    }

    override fun onBindViewHolder(holder: PresensiViewHolder, position: Int) {
        val santri = santriList[position]

        holder.tvNamaSantri.text = santri.nama
        holder.tvKelas.text = santri.kelas

        // Clear container sebelum menambah item baru
        holder.containerHari.removeAllViews()

        // Tambahkan status untuk setiap hari
        for (day in 1..daysInMonth) {
            val inflater = LayoutInflater.from(holder.itemView.context)
            val statusView = inflater.inflate(R.layout.item_status_harian, holder.containerHari, false)

            val tvTanggal = statusView.findViewById<TextView>(R.id.tvTanggal)
            val tvStatus = statusView.findViewById<TextView>(R.id.tvStatus)

            // Set tanggal
            tvTanggal.text = day.toString().padStart(2, '0')

            // Set status - handle null safely
            val status = santri.statusPerHari[day] // This might be null
            setupStatusAppearance(tvStatus, status)

            holder.containerHari.addView(statusView)
        }
    }

    private fun setupStatusAppearance(textView: TextView, status: String?) {
        when (status) {
            "v" -> {
                textView.text = "✓"
                textView.setTextColor(ContextCompat.getColor(textView.context, android.R.color.white))
                textView.background = ContextCompat.getDrawable(textView.context, R.drawable.bg_status_hadir)
            }
            "i" -> {
                textView.text = "I"
                textView.setTextColor(ContextCompat.getColor(textView.context, android.R.color.white))
                textView.background = ContextCompat.getDrawable(textView.context, R.drawable.bg_status_izin)
            }
            "l" -> {
                textView.text = "L"
                textView.setTextColor(ContextCompat.getColor(textView.context, android.R.color.white))
                textView.background = ContextCompat.getDrawable(textView.context, R.drawable.bg_status_libur)
            }
            "" -> {
                textView.text = "A"
                textView.setTextColor(ContextCompat.getColor(textView.context, android.R.color.white))
                textView.background = ContextCompat.getDrawable(textView.context, R.drawable.bg_status_alpha)
            }
            null -> {
                // Hari belum terjadi - kosong
                textView.text = ""
                textView.background = ContextCompat.getDrawable(textView.context, R.drawable.bg_status_empty)
            }
            else -> {
                textView.text = "-"
                textView.setTextColor(ContextCompat.getColor(textView.context, android.R.color.darker_gray))
                textView.background = ContextCompat.getDrawable(textView.context, R.drawable.bg_status_default)
            }
        }
    }

    override fun getItemCount(): Int = santriList.size

    fun updateData(newSantriList: List<SantriPresensi>, newDaysInMonth: Int) {
        santriList = newSantriList
        daysInMonth = newDaysInMonth
        notifyDataSetChanged()
    }
}