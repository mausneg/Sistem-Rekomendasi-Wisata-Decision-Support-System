package com.example.topsis_dss

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.topsis_dss.databinding.ActivityLoginBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest

class LoginActivity : AppCompatActivity() {
    lateinit var binding : ActivityLoginBinding
    private lateinit var sharedPreferences: SharedPreferences
    val db = Firebase.firestore
    val storage = Firebase.storage
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPreferences = getSharedPreferences("user_id", MODE_PRIVATE)
        if (sharedPreferences.getString("user_id", null) != null) {
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        txtRegisterListener()
        btnLoginListener()
    }


    private fun txtRegisterListener(){
        binding.moveToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun btnLoginListener(){
        binding.loginButton.setOnClickListener {
            val email = binding.inputEmail.text.toString()
            val password = binding.inputPassword.text.toString()
            if (checkInput(email, password)) {
                lifecycleScope.launch {
                    login(email, password)
                }
            }
        }
    }

    private fun checkInput(email: String, password: String): Boolean {
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.inputEmail.error = "Please enter a valid email"
            return false
        }
        if (password.isEmpty()) {
            binding.inputPassword.error = "Password must not be empty"
            binding.inputPassword.requestFocus()
            return false
        }
        return true
    }
    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }
    private suspend fun login(email: String, password: String) {
        val users = db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .await()

        if (users.documents.isNotEmpty()) {
            val user = users.documents[0]
            val userPassword = user.getString("password")
            if (userPassword == hashPassword(password)) {
                Toast.makeText(this, "Login Success", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                sharedPreferences.edit().putString("user_id", user.id).apply()
                sharedPreferences.edit().putString("full_name", user.getString("fullName")).apply()
                sharedPreferences.edit().putString("email", user.getString("email")).apply()
                sharedPreferences.edit().putString("password", user.getString("password")).apply()
                getProfileImage(user.id)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Password is incorrect", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun getProfileImage(user_id: String) {
        try {
            val profileRef = storage.reference.child("users/$user_id.jpg")
            val uri = profileRef.downloadUrl.await()
            sharedPreferences.edit().putString("profile_url", uri.toString()).apply()
        } catch (e: Exception) {
            sharedPreferences.edit().putString("profile_url", null).apply()
        }
    }
}