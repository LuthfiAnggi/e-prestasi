package luthfi.anggi.e_prestasi

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import luthfi.anggi.e_prestasi.pengajar.FragmentBukuPrestasi
import luthfi.anggi.e_prestasi.pengajar.FragmentLihatIzin
import luthfi.anggi.e_prestasi.pengajar.FragmentMainPengajar

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Tampilkan fragment default saat pertama kali aplikasi dibuka
        loadFragment(FragmentMainPengajar())

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_kelola_santri -> {
                    loadFragment(FragmentMainPengajar())
                    true
                }
                R.id.menu_kelola_buku -> {
                    loadFragment(FragmentBukuPrestasi())
                    true
                }
                R.id.menu_kelola_izin-> {
                    loadFragment(FragmentLihatIzin())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
