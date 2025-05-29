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

class FragmentKehadiran : Fragment() {

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var tabAdapter: KehadiranPagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_kehadiran, container, false)
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
        tabAdapter = KehadiranPagerAdapter(requireActivity())
        viewPager.adapter = tabAdapter
    }

    private fun setupTabLayout() {
        val tabTitles = arrayOf("Presensi", "Request Izin", "Riwayat")
        val tabIcons = arrayOf(
            R.drawable.ic_profile_placeholder,
            R.drawable.ic_profile_placeholder,
            R.drawable.ic_profile_placeholder
        )

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabTitles[position]
            tab.setIcon(tabIcons[position])
        }.attach()
    }

    // Adapter untuk ViewPager2
    private inner class KehadiranPagerAdapter(fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {

        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> PresensiWrapperFragment() // Wrapper untuk ActivityPresensi
                1 -> FragmentIzin() // Fragment yang sudah ada
                2 -> HistoryIzinWrapperFragment() // Wrapper untuk ActivityHistoryIzin
                else -> PresensiWrapperFragment()
            }
        }
    }

    // Wrapper Fragment untuk ActivityPresensi
    class PresensiWrapperFragment : Fragment() {

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            return inflater.inflate(R.layout.fragment_presensi_wrapper, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            // Button atau area yang ketika diklik akan membuka ActivityPresensi
            val btnOpenPresensi = view.findViewById<View>(R.id.btnOpenPresensi)
            btnOpenPresensi?.setOnClickListener {
                val intent = Intent(requireContext(), ActivityPresensi::class.java)
                startActivity(intent)
            }

            // Atau bisa langsung embed konten dari ActivityPresensi ke dalam fragment
            setupPresensiContent(view)
        }

        private fun setupPresensiContent(view: View) {
            // Implementasi konten presensi langsung di fragment
            // Alternatif dari membuka activity terpisah
        }
    }

    // Wrapper Fragment untuk ActivityHistoryIzin
    class HistoryIzinWrapperFragment : Fragment() {

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            return inflater.inflate(R.layout.fragment_history_wrapper, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            // Button atau area yang ketika diklik akan membuka ActivityHistoryIzin
            val btnOpenHistory = view.findViewById<View>(R.id.btnOpenHistorySPP)
            btnOpenHistory?.setOnClickListener {
                val intent = Intent(requireContext(), ActivityHistoryIzin::class.java)
                startActivity(intent)
            }

            // Atau bisa langsung embed konten dari ActivityHistoryIzin ke dalam fragment
            setupHistoryContent(view)
        }

        private fun setupHistoryContent(view: View) {
            // Implementasi konten history langsung di fragment
            // Alternatif dari membuka activity terpisah
        }
    }
}