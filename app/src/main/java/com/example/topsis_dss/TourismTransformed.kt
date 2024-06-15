package com.example.topsis_dss

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TourismTransformed(
    val id: String,
    val name: String,
    val distance: Int,
    val facility: Int,
    val popularity: Float,
    val price: Int,
    val transportation: Int

): Parcelable