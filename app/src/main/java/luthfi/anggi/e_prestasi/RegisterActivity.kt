package luthfi.anggi.e_prestasi

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnToLogin = findViewById<Button>(R.id.btntoLogin)

        btnRegister.setOnClickListener {
            val email = findViewById<EditText>(R.id.etEmail).text.toString().trim()
            //val name = findViewById<EditText>(R.id.etName).text.toString().trim()
            val password = findViewById<EditText>(R.id.etPass).text.toString().trim()
            val confirmPassword = findViewById<EditText>(R.id.etConfirm).text.toString().trim()

            if (email.isEmpty() || password.isEmpty() ||  confirmPassword.isEmpty()) {
                Toast.makeText(this, "Harap isi semua kolom", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password harus minimal 6 karakter", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Konfirmasi password tidak cocok", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registerUser(email, password)
        }

        btnToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.sendEmailVerification()?.addOnCompleteListener { verificationTask ->
                        if (verificationTask.isSuccessful) {
                            saveUserToFirestore(user.uid, email)
                            Toast.makeText(this, "Verifikasi email telah dikirim", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "Gagal mengirim verifikasi email", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Registrasi gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUserToFirestore(uid: String, email: String,) {
        val userData = hashMapOf(
            "email" to email,
            "role" to "wali",
            "emailVerified" to false
        )
        firestore.collection("users").document(uid).set(userData)
            .addOnSuccessListener {
                Toast.makeText(this, "Data pengguna disimpan", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal menyimpan data pengguna", Toast.LENGTH_SHORT).show()
            }
    }
}
