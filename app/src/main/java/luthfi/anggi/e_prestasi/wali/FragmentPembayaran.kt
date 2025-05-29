package luthfi.anggi.e_prestasi.wali

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import luthfi.anggi.e_prestasi.R

class FragmentPembayaran : Fragment() {

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var tabAdapter: PembayaranPagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pembayaran, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupViewPager()
        setupTabLayout()
    }

    private fun initViews(view: View) {
        tabLayout = view.findViewById(R.id.tabLayout)
        viewPager = view.findViewById(R.id.viewPager)
    }

    private fun setupViewPager() {
        tabAdapter = PembayaranPagerAdapter(requireActivity())
        viewPager.adapter = tabAdapter
    }

    private fun setupTabLayout() {
        val tabTitles = arrayOf("Bayar SPP", "Riwayat Bayar", "Data SPP Santri")
        val tabIcons = arrayOf(
            R.drawable.ic_more_vert, // Ganti dengan icon pembayaran
            R.drawable.ic_more_vert, // Ganti dengan icon riwayat
            R.drawable.ic_more_vert     // Ganti dengan icon data
        )

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabTitles[position]
            tab.setIcon(tabIcons[position])
        }.attach()
    }

    // Adapter untuk ViewPager2
    private inner class PembayaranPagerAdapter(fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {

        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> FragmentPayment()
                1 -> RiwayatBayarSppWrapperFragment() // Wrapper untuk Riwayat Bayar SPP
                2 -> DataSppSantriWrapperFragment() // Wrapper untuk Data SPP Santri
                else -> FragmentPayment()
            }
        }
    }

    // Wrapper Fragment untuk Riwayat Bayar SPP
    class RiwayatBayarSppWrapperFragment : Fragment() {

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            return inflater.inflate(R.layout.fragment_historyspp_wrapper, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            // Button atau area yang ketika diklik akan membuka Activity Riwayat Bayar SPP
            val btnOpenRiwayat = view.findViewById<View>(R.id.btnOpenHistorySPP)
            btnOpenRiwayat?.setOnClickListener {
                val intent = Intent(requireContext(), HistorySppActivity::class.java)
                startActivity(intent)
            }

            // Atau bisa langsung embed konten riwayat bayar SPP ke dalam fragment
            setupRiwayatContent(view)
        }

        private fun setupRiwayatContent(view: View) {

        }

        private fun setupRiwayatAdapter(recyclerView: androidx.recyclerview.widget.RecyclerView?) {
            // Setup adapter untuk menampilkan riwayat pembayaran
            // Implementasi adapter RecyclerView untuk riwayat pembayaran SPP
        }
    }

    // Wrapper Fragment untuk Data SPP Santri
    class DataSppSantriWrapperFragment : Fragment() {

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            return inflater.inflate(R.layout.fragment_dataspp_wrapper, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            // Button atau area yang ketika diklik akan membuka Activity Data SPP Santri
            val btnOpenDataSpp = view.findViewById<View>(R.id.btnOpenDataSPP)
            btnOpenDataSpp?.setOnClickListener {
                // Uncomment jika ingin membuka activity terpisah
                val intent = Intent(requireContext(), ActivityDataSpp::class.java)
                startActivity(intent)
            }

            // Atau bisa langsung embed konten data SPP santri ke dalam fragment
            setupDataSppContent(view)
        }

        private fun setupDataSppContent(view: View) {
            // Implementasi konten data SPP santri langsung di fragment
            val rvSantriSpp = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvSantriSpp)

            // Setup RecyclerView untuk menampilkan data SPP per santri per bulan
            setupDataSppAdapter(rvSantriSpp)
        }

        private fun setupDataSppAdapter(recyclerView: androidx.recyclerview.widget.RecyclerView?) {
            // Setup adapter untuk menampilkan data SPP santri
            // Menampilkan SPP per bulan untuk setiap santri
            // Implementasi adapter RecyclerView untuk data SPP santri
        }
    }
}