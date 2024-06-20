package com.example.topsis_dss

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Tourism(
    val id: String,
    val name: String,
    val description: String,
    val city: String,
    val category: ArrayList<String>,
    val rating_avarage: Float,
    val rating_count: Int,
    val price: Float

): Parcelable