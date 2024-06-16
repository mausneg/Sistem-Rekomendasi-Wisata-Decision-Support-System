package com.example.topsis_dss

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.topsis_dss.databinding.FragmentProfileBinding


class ProfileFragment : Fragment() {
    lateinit var binding : FragmentProfileBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        sharedPreferences = requireActivity().getSharedPreferences("user_id", Context.MODE_PRIVATE)
        val imageUri = sharedPreferences.getString("profile_url", null)
        val fullName = sharedPreferences.getString("full_name", null)
        val email = sharedPreferences.getString("email", null)
        binding.profileFullname.text = fullName
        binding.profileEmail.text = email
        if(imageUri != null) {
            Glide.with(this)
                .load(imageUri)
                .into(binding.profilePicture)
        } else {
            binding.profilePicture.setImageResource(R.drawable.profile)
        }
        btnLogoutListener()
        btnEditListener()
        return binding.root
    }
    private fun btnLogoutListener(){
        binding.btn4Profile.setOnClickListener {
            val sharedPreferences = requireActivity().getSharedPreferences("user_id", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun btnEditListener(){
        binding.btn2Profile.setOnClickListener {
            val intent = Intent(requireActivity(), EditProfileActivity::class.java)
            startActivity(intent)
        }
    }
}