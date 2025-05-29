package luthfi.anggi.e_prestasi

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import luthfi.anggi.e_prestasi.wali.FragmentKehadiran
import luthfi.anggi.e_prestasi.wali.FragmentPembayaran
import luthfi.anggi.e_prestasi.wali.FragmentProgress

class MainActivityWali : AppCompatActivity() {

    private var currentTabId = R.id.menu_kelola_progress
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_wali)

        bottomNavigationView = findViewById(R.id.bottom_navigation)

        // Setup listener pertama
        bottomNavigationView.setOnItemSelectedListener { item ->
            if (currentTabId != item.itemId) {
                selectTab(item.itemId)
                true
            } else {
                false
            }
        }

        // Handle first launch
        if (savedInstanceState == null) {
            selectTab(R.id.menu_kelola_progress)
        } else {
            currentTabId = savedInstanceState.getInt("CURRENT_TAB", R.id.menu_kelola_progress)
            bottomNavigationView.selectedItemId = currentTabId
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("CURRENT_TAB", currentTabId)
    }

    private fun selectTab(tabId: Int) {
        currentTabId = tabId
        when (tabId) {
            R.id.menu_kelola_progress -> showFragment(FragmentProgress())
            R.id.menu_kelola_izin -> showFragment(FragmentKehadiran())
            R.id.menu_kelola_spp -> showFragment(FragmentPembayaran())
        }
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    override fun onBackPressed() {
        if (currentTabId != R.id.menu_kelola_progress) {
            // Kembali ke tab Progress
            selectTab(R.id.menu_kelola_progress)
            bottomNavigationView.selectedItemId = R.id.menu_kelola_progress
        } else {
            super.onBackPressed()
        }
    }
}