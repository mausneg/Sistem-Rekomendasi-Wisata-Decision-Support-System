package com.example.topsis_dss

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.topsis_dss.databinding.FragmentPrefrenceBinding
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class PrefrenceFragment : Fragment() {
    private lateinit var binding: FragmentPrefrenceBinding
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable
    private val criteria = arrayListOf("Price", "Distance", "Popularity", "Transportation", "Facility")
    private val selectedCriteria = arrayListOf<String?>(null, null, null, null, null)
    private lateinit var adapter: ArrayAdapter<String>
    private val prompt = "Select a criterion"
    private lateinit var sharedPreferences: SharedPreferences
    private val tourismTransformedList = arrayListOf<TourismTransformed>()


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPrefrenceBinding.inflate(inflater, container, false)
        sharedPreferences = requireActivity().getSharedPreferences("user_id", Context.MODE_PRIVATE)
        val imageUri = sharedPreferences.getString("profile_url", null)
        Log.d(ContentValues.TAG, "onCreateView: $imageUri")
        if(imageUri != null) {
            Glide.with(this)
                .load(imageUri)
                .into(binding.profilePicture)
        } else {
            binding.profilePicture.setImageResource(R.drawable.profile)
        }
        setupDateTimeUpdater()
        setupSpinners()
        setupResetButton()
        binding.btnGetRecommendation.setOnClickListener {
            lifecycleScope.launch {
                onClickRecommend()
            }
        }
        return binding.root
    }

    private fun setupSpinners() {
        setupSpinner(binding.spinner1, 0)
        setupSpinner(binding.spinner2, 1)
        setupSpinner(binding.spinner3, 2)
        setupSpinner(binding.spinner4, 3)
        setupSpinner(binding.spinner5, 4)
    }

    private fun setupSpinner(spinner: Spinner, index: Int) {
        updateSpinnerAdapter(spinner, index)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedItem = parent?.getItemAtPosition(position)?.toString()

                if (selectedItem != null && selectedItem != prompt) {
                    selectedCriteria[index] = selectedItem
                    updateAllSpinners(excludeIndex = index)
                } else {
                    selectedCriteria[index] = null
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
    }

    private fun updateAllSpinners(excludeIndex: Int) {
        for (i in selectedCriteria.indices) {
            if (i != excludeIndex) {
                val spinner = when (i) {
                    0 -> binding.spinner1
                    1 -> binding.spinner2
                    2 -> binding.spinner3
                    3 -> binding.spinner4
                    4 -> binding.spinner5
                    else -> null
                }
                spinner?.let { updateSpinnerAdapter(it, i) }
            }
        }
    }

    private fun updateSpinnerAdapter(spinner: Spinner, index: Int) {
        val availableCriteria = arrayListOf(prompt)
        availableCriteria.addAll(criteria.filter { it !in selectedCriteria || it == selectedCriteria[index] })

        // Only create a new adapter if the items have changed
        if (spinner.adapter == null || (spinner.adapter as ArrayAdapter<String>).count != availableCriteria.size) {
            adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, availableCriteria)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        selectedCriteria[index]?.let {
            val position = availableCriteria.indexOf(it)
            if (position >= 0) {
                spinner.setSelection(position)
            } else {
                spinner.setSelection(0)
            }
        } ?: spinner.setSelection(0)
    }

    private fun setupResetButton() {
        binding.resetButton.setOnClickListener {
            for (i in selectedCriteria.indices) {
                selectedCriteria[i] = null
            }
            updateAllSpinners(excludeIndex = -1)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupDateTimeUpdater() {
        runnable = object : Runnable {
            override fun run() {
                val current = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")
                val formatted = current.format(formatter)
                binding.date.text = formatted

                val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
                val timeFormatted = current.format(timeFormatter)
                binding.time.text = timeFormatted

                handler.postDelayed(this, 60000)
            }
        }
        handler.post(runnable)
    }


    private suspend fun onClickRecommend() {
        if (selectedCriteria.any { it == null }) {
            Toast.makeText(context, "Please select a criterion for each field", Toast.LENGTH_SHORT).show()
            return
        }

        val weigth = getSpinnerValuesWithWeights()
        val intent = Intent(context, RecommendActivity::class.java)
        val gson = Gson()
        val json = gson.toJson(weigth)
        intent.putExtra("weigth", json)
        startActivity(intent)
    }

    private fun getSpinnerValuesWithWeights(): Map<String, Int> {
        val weights = mapOf(
            0 to 35,
            1 to 30,
            2 to 20,
            3 to 10,
            4 to 5)
        val valuesWithWeights = mutableMapOf<String, Int>()

        for (i in selectedCriteria.indices) {
            val spinnerValue = selectedCriteria[i]
            if (spinnerValue != null && spinnerValue != prompt && weights.containsKey(i)) {
                valuesWithWeights[spinnerValue] = weights[i] ?: 0
            }
        }
        return valuesWithWeights
    }

}
