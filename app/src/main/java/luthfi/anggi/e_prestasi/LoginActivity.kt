package luthfi.anggi.e_prestasi

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val btnLogin = findViewById<Button>(R.id.btnLoginn)
        val tvSignup = findViewById<TextView>(R.id.tvsignup)
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)

        btnLogin.setOnClickListener {
            val email = findViewById<EditText>(R.id.etEmail).text.toString().trim()
            val password = findViewById<EditText>(R.id.etPass).text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(this, "Email atau Password kosong", Toast.LENGTH_SHORT).show()
            }
        }

        tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ResetPassActivity::class.java))
        }

        tvSignup.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null && user.isEmailVerified) {
                        checkUserRole(user.uid)
                    } else {
                        Toast.makeText(this, "Silakan verifikasi email terlebih dahulu", Toast.LENGTH_SHORT).show()
                        auth.signOut()
                    }
                } else {
                    Toast.makeText(this, "Login gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkUserRole(uid: String) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val role = document.getString("role")
                    when (role) {
                        "pengajar" -> {
                            Toast.makeText(this, "Login sebagai Pengajar", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                        }
                        "wali" -> {
                            Toast.makeText(this, "Login sebagai Wali", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivityWali::class.java))
                        }
                        else -> {
                            Toast.makeText(this, "Role tidak dikenali!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    finish()
                } else {
                    Toast.makeText(this, "Data pengguna tidak ditemukan!", Toast.LENGTH_SHORT).show()
                    auth.signOut()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal mengambil data pengguna", Toast.LENGTH_SHORT).show()
                auth.signOut()
            }
    }
}
