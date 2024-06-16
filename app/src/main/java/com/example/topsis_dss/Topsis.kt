package com.example.topsis_dss

import com.google.gson.Gson

class Topsis(private val tourismList: ArrayList<TourismTransformed>, private val weight: Map<String, Int>) {
    private val gson = Gson()
    private val criteria = arrayListOf("Price", "Distance", "Popularity", "Transportation", "Facility")
    val unprocessedMatrix = arrayListOf<ArrayList<Double>>()
    val normalizedMatrix = arrayListOf<ArrayList<Double>>()
    val weightedMatrix = arrayListOf<ArrayList<Double>>()
    val positiveIdealSolution = arrayListOf<Double>()
    val negativeIdealSolution = arrayListOf<Double>()
    val distancePositive = arrayListOf<Double>()
    val distanceNegative = arrayListOf<Double>()
    val closeness = arrayListOf<Double>()
    val recommendation = arrayListOf<TourismTransformed>()

    init {
        unprocessedMatrix()
        normalizeMatrix()
        weightMatrix()
        calculatePositiveIdealSolution()
        calculateNegativeIdealSolution()
        calculateDistancePositive()
        calculateDistanceNegative()
        calculateCloseness()
        getRecommendation()
    }

    private fun unprocessedMatrix() {
        for (i in 0 until tourismList.size) {
            val row = arrayListOf<Double>()
            row.add(tourismList[i].price.toDouble())
            row.add(tourismList[i].distance.toDouble())
            row.add(tourismList[i].popularity.toDouble())
            row.add(tourismList[i].transportation.toDouble())
            row.add(tourismList[i].facility.toDouble())
            unprocessedMatrix.add(row)
        }
    }
    private fun normalizeMatrix() {
        for (i in 0 until tourismList.size) {
            val row = arrayListOf<Double>()
            row.add(tourismList[i].price.toDouble())
            row.add(tourismList[i].distance.toDouble())
            row.add(tourismList[i].popularity.toDouble())
            row.add(tourismList[i].transportation.toDouble())
            row.add(tourismList[i].facility.toDouble())
            normalizedMatrix.add(row)
        }

        for (i in 0 until normalizedMatrix[0].size) {
            var sum = 0.0
            for (j in 0 until normalizedMatrix.size) {
                sum += normalizedMatrix[j][i] * normalizedMatrix[j][i]
            }
            sum = Math.sqrt(sum)
            for (j in 0 until normalizedMatrix.size) {
                normalizedMatrix[j][i] /= sum
            }
        }
    }

    private fun weightMatrix() {
        for (i in 0 until normalizedMatrix.size) {
            val row = arrayListOf<Double>()
            for (j in 0 until normalizedMatrix[0].size) {
                row.add((normalizedMatrix[i][j] * weight[criteria[j]]!!)/100.0)
            }
            weightedMatrix.add(row)
        }
    }

    private fun calculatePositiveIdealSolution() {
        for (i in 0 until weightedMatrix[0].size) {
            var idealValue: Double
            if (criteria[i] == "Price" || criteria[i] == "Distance") {
                idealValue = Double.MAX_VALUE
                for (j in 0 until weightedMatrix.size) {
                    if (weightedMatrix[j][i] < idealValue) {
                        idealValue = weightedMatrix[j][i]
                    }
                }
            } else {
                idealValue = 0.0
                for (j in 0 until weightedMatrix.size) {
                    if (weightedMatrix[j][i] > idealValue) {
                        idealValue = weightedMatrix[j][i]
                    }
                }
            }
            positiveIdealSolution.add(idealValue)
        }
    }

    private fun calculateNegativeIdealSolution() {
        for (i in 0 until weightedMatrix[0].size) {
            var idealValue: Double
            if (criteria[i] == "Price" || criteria[i] == "Distance") {
                idealValue = 0.0
                for (j in 0 until weightedMatrix.size) {
                    if (weightedMatrix[j][i] > idealValue) {
                        idealValue = weightedMatrix[j][i]
                    }
                }
            } else {
                idealValue = Double.MAX_VALUE
                for (j in 0 until weightedMatrix.size) {
                    if (weightedMatrix[j][i] < idealValue) {
                        idealValue = weightedMatrix[j][i]
                    }
                }
            }
            negativeIdealSolution.add(idealValue)
        }
    }
    private fun calculateDistancePositive() {
        for (i in 0 until weightedMatrix.size) {
            var sum = 0.0
            for (j in 0 until weightedMatrix[0].size) {
                sum += Math.pow(weightedMatrix[i][j] - positiveIdealSolution[j], 2.0)
            }
            distancePositive.add(Math.sqrt(sum))
        }
    }

    private fun calculateDistanceNegative() {
        for (i in 0 until weightedMatrix.size) {
            var sum = 0.0
            for (j in 0 until weightedMatrix[0].size) {
                sum += Math.pow(weightedMatrix[i][j] - negativeIdealSolution[j], 2.0)
            }
            distanceNegative.add(Math.sqrt(sum))
        }
    }

    private fun calculateCloseness() {
        for (i in 0 until weightedMatrix.size) {
            closeness.add(distanceNegative[i] / (distanceNegative[i] + distancePositive[i]))
        }
    }

    private fun getRecommendation() {
        val sortedCloseness = closeness.sortedDescending()
        for (i in 0 until 10) {
            val index = closeness.indexOf(sortedCloseness[i])
            tourismList[index].score = sortedCloseness[i]
            recommendation.add(tourismList[index])
        }
    }
}

