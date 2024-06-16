package com.example.topsis_dss

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.topsis_dss.databinding.ActivityRegisterBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest


class RegisterActivity : AppCompatActivity() {
    private lateinit var binding : ActivityRegisterBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        txtLoginListener()
        btnRegisterListener()
    }

    private fun txtLoginListener(){
        binding.moveToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun btnRegisterListener() {
        binding.registerButton.setOnClickListener {
            val fullname = binding.inputFullname.text.toString()
            val email = binding.inputEmail.text.toString()
            val password = binding.inputPassword.text.toString()
            val confirmPassword = binding.inputConfirmPassword.text.toString()
            if (checkInput(fullname, email, password, confirmPassword)) {
                lifecycleScope.launch {
                    register(fullname, email, password)
                }
            }
        }
    }

    private fun checkInput(fullName: String, email: String, password: String, confirmPassword: String): Boolean {
        if (fullName.isEmpty()) {
            binding.inputFullname.error = "Please enter your name"
            return false
        }
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.inputEmail.error = "Please enter a valid email"
            return false
        }
        if (password.isEmpty() || password.length < 8) {
            binding.inputPassword.error = "Password must be at least 8 characters"
            return false
        }
        if (confirmPassword.isEmpty() || password != confirmPassword) {
            binding.inputConfirmPassword.error = "Passwords do not match"
            return false
        }
        return true
    }
    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }
    private suspend fun register(fullName: String, email: String, password: String) {
        val db = Firebase.firestore
        val user = hashMapOf(
            "fullName" to fullName,
            "email" to email,
            "password" to hashPassword(password)
        )
        return try {
            val existingUser = db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .await()

            if (!existingUser.isEmpty) {
                Toast.makeText(this@RegisterActivity, "Email already exists", Toast.LENGTH_SHORT).show()
                return
            }

            db.collection("users")
                .add(user)
                .await()
            Toast.makeText(this@RegisterActivity, "Registration Success", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
            finish()

        } catch (e: Exception) {
            Log.w(TAG, "Error adding document", e)
            Toast.makeText(this@RegisterActivity, "Registration Failed", Toast.LENGTH_SHORT).show()
        }
    }
}