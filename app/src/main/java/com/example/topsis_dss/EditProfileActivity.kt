package com.example.topsis_dss

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.topsis_dss.databinding.ActivityEditProfileBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var sharedPreferences: SharedPreferences
    private val firestore = Firebase.firestore
    private val storage = Firebase.storage
    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("user_id", MODE_PRIVATE)

        val userId = sharedPreferences.getString("user_id", null)
        val imageUrl = sharedPreferences.getString("profile_url", null)
        val fullName = sharedPreferences.getString("full_name", null)
        val email = sharedPreferences.getString("email", null)

        binding.inputFullname.setText(fullName)
        binding.inputEmail.setText(email)
        if (imageUrl != null) {
            Glide.with(this).load(imageUrl).into(binding.profilePicture)
        } else {
            binding.profilePicture.setImageResource(R.drawable.profile)
        }

        binding.profilePicture.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        btnBackEproListener()
        btnSaveProfileListener(userId)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            binding.profilePicture.setImageURI(imageUri)
        }
    }

    private fun btnBackEproListener() {
        binding.backEpro.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun btnSaveProfileListener(userId: String?) {
        binding.btn1Epro.setOnClickListener {
            val updatedFullName = binding.inputFullname.text.toString()
            val updatedEmail = binding.inputEmail.text.toString()
            val oldPassword = binding.inputOldPassword.text.toString()
            val newPassword = binding.inputNewPassword.text.toString()

            if (checkInput(updatedFullName, updatedEmail, oldPassword, newPassword)) {
                lifecycleScope.launch {
                    saveProfile(userId, updatedFullName, updatedEmail, oldPassword, newPassword)
                }
            }
        }
    }

    private fun checkInput(
        fullName: String,
        email: String,
        oldPassword: String,
        newPassword: String
    ): Boolean {
        if (fullName.isEmpty()) {
            binding.inputFullname.error = "Please enter your name"
            return false
        }
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.inputEmail.error = "Please enter a valid email"
            return false
        }
        if (newPassword.isNotEmpty() && newPassword.length < 8) {
            binding.inputNewPassword.error = "Password must be at least 8 characters"
            return false
        }
        return true
    }

    private suspend fun saveProfile(
        userId: String?,
        fullName: String,
        email: String,
        oldPassword: String,
        newPassword: String
    ) {
        if (userId == null) {
            Toast.makeText(this, "User ID is missing", Toast.LENGTH_SHORT).show()
            return
        }

        val userRef = firestore.collection("users").document(userId)
        val userSnapshot = userRef.get().await()
        val currentPasswordHash = userSnapshot.getString("password")

        if (oldPassword.isNotEmpty() && currentPasswordHash != hashPassword(oldPassword)) {
            Toast.makeText(this, "Old password is incorrect", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedData = hashMapOf<String, Any>(
            "fullName" to fullName,
            "email" to email
        )

        if (newPassword.isNotEmpty()) {
            updatedData["password"] = hashPassword(newPassword)
        }

        if (imageUri != null) {
            val imageRef = storage.reference.child("users/$userId.jpg")
            imageRef.putFile(imageUri!!).await()
            val imageUrl = imageRef.downloadUrl.await().toString()

            // Save the URL to SharedPreferences for local use
            sharedPreferences.edit().putString("profile_url", imageUrl).apply()
        }

        userRef.update(updatedData).await()

        sharedPreferences.edit().apply {
            putString("full_name", fullName)
            putString("email", email)
            if (newPassword.isNotEmpty()) {
                putString("password", hashPassword(newPassword))
            }
            apply()
        }

        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }
}
