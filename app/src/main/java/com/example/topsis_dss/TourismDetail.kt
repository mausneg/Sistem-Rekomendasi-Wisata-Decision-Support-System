package com.example.topsis_dss

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.topsis_dss.databinding.ActivityTourismDetailBinding

class TourismDetail : AppCompatActivity() {
    private lateinit var binding: ActivityTourismDetailBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTourismDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val intent = intent
        val tourism = intent.getParcelableExtra<Tourism>("tourism")
        binding.tourismTitle.text = tourism?.name
        binding.tourismDescription.text = tourism?.description
        binding.tourismRatingAverage.text = tourism?.rating_avarage.toString()
        binding.tourismRatingCount.text = tourism?.rating_count.toString()
        binding.tourismCategory.text = tourism?.category.toString()
        binding.tourismCity.text = tourism?.city
        Glide.with(this)
            .load("https://via.placeholder.com/100")
            .into(binding.tourismImage)


    }
}