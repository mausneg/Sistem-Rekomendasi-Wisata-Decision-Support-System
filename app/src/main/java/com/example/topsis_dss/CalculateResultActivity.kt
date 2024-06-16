package com.example.topsis_dss

import android.os.Bundle
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.topsis_dss.databinding.ActivityCalculateResultBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

class CalculateResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCalculateResultBinding
    private lateinit var tourismTransformedList: ArrayList<TourismTransformed>
    private lateinit var unprocessedMatrix: ArrayList<ArrayList<Double>>
    private lateinit var normalizedMatrix: ArrayList<ArrayList<Double>>
    private lateinit var weightedMatrix: ArrayList<ArrayList<Double>>
    private lateinit var positiveIdealSolution: ArrayList<Double>
    private lateinit var negativeIdealSolution: ArrayList<Double>
    private lateinit var distancePositive: ArrayList<Double>
    private lateinit var distanceNegative: ArrayList<Double>
    private lateinit var closeness: ArrayList<Double>
    private lateinit var recommendation: ArrayList<TourismTransformed>
    override  fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalculateResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            getCalculateStep()
        }
    }

    private suspend fun getCalculateStep(){
        val json = intent.getStringExtra("weigth")
        val gson = Gson()
        val weights: Map<String, Int> = gson.fromJson(json, object : TypeToken<Map<String, Int>>() {}.type)
        val recommendActivity = RecommendActivity()
        tourismTransformedList = recommendActivity.getTourismTransformed()

        val topsis = Topsis(tourismTransformedList, weights)
        unprocessedMatrix = topsis.unprocessedMatrix
        normalizedMatrix = topsis.normalizedMatrix
        weightedMatrix = topsis.weightedMatrix
        positiveIdealSolution = topsis.positiveIdealSolution
        negativeIdealSolution = topsis.negativeIdealSolution
        distancePositive = topsis.distancePositive
        distanceNegative = topsis.distanceNegative
        closeness = topsis.closeness
        recommendation = topsis.recommendation

        val table_unprocessedMatrix = binding.tableUnprocessed
        for (i in 0 until 6) {
            val row = TableRow(this)
            for (j in 0 until unprocessedMatrix[0].size) {
                val textView = TextView(this)
                if (i == 5){
                    textView.text = "..."
                }  else{
                    textView.text = String.format("%.2f", unprocessedMatrix[i][j])
                }
                row.addView(textView)
            }
            table_unprocessedMatrix.addView(row)
        }

        val table_normalizedMatrix = binding.tableNormalize
        for (i in 0 until 6) {
            val row = TableRow(this)
            for (j in 0 until normalizedMatrix[0].size) {
                val textView = TextView(this)
                if (i == 5){
                    textView.text = "..."
                }  else{
                    textView.text = String.format("%.2f", normalizedMatrix[i][j])
                }
                row.addView(textView)
            }
            table_normalizedMatrix.addView(row)
        }

        val table_weightedMatrix = binding.tableWeight
        for (i in 0 until 6) {
            val row = TableRow(this)
            for (j in 0 until weightedMatrix[0].size) {
                val textView = TextView(this)
                if (i == 5){
                    textView.text = "..."
                }  else{
                    textView.text = String.format("%.2f", weightedMatrix[i][j])
                }
                row.addView(textView)
            }
            table_weightedMatrix.addView(row)
        }

        val table_idealSolution = binding.tableIdeal
        for (i in 0 until positiveIdealSolution.size) {
            val row = TableRow(this)
            val textViewPositive = TextView(this)
            val textViewNegative = TextView(this)
            textViewPositive.text = String.format("%.2f", positiveIdealSolution[i])
            textViewNegative.text = String.format("%.2f", negativeIdealSolution[i])
            row.addView(textViewPositive)
            row.addView(textViewNegative)
            table_idealSolution.addView(row)
        }

        val table_distance = binding.tableDistance
        for (i in 0 until 6) {
            val row = TableRow(this)
            val textViewPositive = TextView(this)
            val textViewNegative = TextView(this)
            if (i == 5){
                textViewPositive.text = "..."
                textViewNegative.text = "..."
            }  else{
                textViewPositive.text = String.format("%.2f", distancePositive[i])
                textViewNegative.text = String.format("%.2f", distanceNegative[i])
            }
            row.addView(textViewPositive)
            row.addView(textViewNegative)
            table_distance.addView(row)
        }

        val table_closeness = binding.tableClosest
        for (i in 0 until 6) {
            val row = TableRow(this)
            val textView = TextView(this)
            if (i == 5){
                textView.text = "..."
            }  else{
                textView.text = String.format("%.2f", closeness[i])
            }
            row.addView(textView)
            table_closeness.addView(row)
        }

        val table_recommendation = binding.tableRanking
        for (i in 0 until 10) {
            val row = TableRow(this)
            val textViewName = TextView(this)
            val textViewScore = TextView(this)
            textViewName.text = recommendation[i].name
            textViewScore.text = String.format("%.2f", recommendation[i].score)
            row.addView(textViewName)
            row.addView(textViewScore)
            table_recommendation.addView(row)
        }

    }


}
