package com.example.topsis_dss

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.topsis_dss.databinding.ActivityRecommendBinding
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecommendActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecommendBinding
    private lateinit var tourismTransformedList: ArrayList<TourismTransformed>
    private lateinit var tourismList: ArrayList<Tourism>
    private lateinit var tourismListAdapter: TourismListAdapter
    private val db = Firebase.firestore
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecommendBinding.inflate(layoutInflater)
        setContentView(binding.root)
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Getting Recommendation...")
        val intent = intent
        val json = intent.getStringExtra("weigth")
        val gson = Gson()
        val weights: Map<String, Int> = gson.fromJson(json, object : TypeToken<Map<String, Int>>() {}.type)
        binding.ivCalculate.setOnClickListener {
            onClickCalculate(json)
        }
        progressDialog.show()
        lifecycleScope.launch {
            tourismTransformedList = getTourismTransformed()
            val topsis = Topsis(tourismTransformedList, weights)
            tourismTransformedList = topsis.recommendation
            getTourism()
            progressDialog.dismiss()
        }
    }

    suspend fun getTourismTransformed(): ArrayList<TourismTransformed> = withContext(
        Dispatchers.IO) {
        val tourismTransformedList = arrayListOf<TourismTransformed>()
        val tourismData = db.collection("tourism_transformed")
        val task = tourismData.get()

        try {
            val result = Tasks.await(task)
            for (document in result) {
                val id = document.id
                val name = document.data["Place_Name"] as String
                val distance = document.data["Distance From Airport (KM)"] as Long
                val facility = document.data["Facility"] as Long
                val popularity = document.data["Popularity Score"] as Double
                val price = document.data["Price"] as Long
                val transportation = document.data["Transportation"] as Long

                val tourism = TourismTransformed(id, name, distance.toInt(), facility.toInt(), popularity.toFloat(), price.toInt(), transportation.toInt())
                tourismTransformedList.add(tourism)
            }
        } catch (e: Exception) {
            Log.e("FirestoreError", e.message, e)
        }

        tourismTransformedList
    }

    private fun getTourism(){
        val tourismData = db.collection("tourism")
        val tourismNames = tourismTransformedList.map { it.name }

        tourismData.whereIn("Place_Name", tourismNames).get()
            .addOnSuccessListener { result ->
                tourismList = arrayListOf()
                for (document in result) {
                    val id = document.id
                    val name = document.data["Place_Name"] as String
                    val category_string = document.data["Category"] as String
                    val category = ArrayList(category_string.split(","))
                    val city = document.data["City"] as String
                    val description = document.data["Description"] as String
                    val rating_average = document.data["Rating"] as Double
                    val rating_count = document.data["Rating_Count"] as Long

                    val tourism = Tourism(id, name, description, city, category, rating_average.toFloat(), rating_count.toInt())
                    tourismList.add(tourism)
                }
                tourismList.sortBy { tourism ->
                    tourismTransformedList.indexOfFirst { it.name == tourism.name }
                }
                tourismListAdapter = TourismListAdapter(tourismList)
                binding.tvRecommendTitle.text = "Recommendation"
                binding.tvRecommendSubtitle.text = "Based on your preference"
                binding.ivCalculate.setImageResource(R.drawable.ic_calculate)
                binding.rvRecommend.layoutManager = LinearLayoutManager(this)
                binding.rvRecommend.adapter = tourismListAdapter
                binding.rvRecommend.isNestedScrollingEnabled = false

            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", e.message, e)

            }
    }

    private fun onClickCalculate(json: String? ) {
        val intent = Intent(this, CalculateResultActivity::class.java)
        intent.putExtra("weigth", json)
        startActivity(intent)
    }
}