package kr.youngho.bodyweightcoach

import org.json.JSONObject

data class WorkoutSummary(
    val id: String,
    val category: String,
    val exerciseType: Int,
    val startTime: String,
    val endTime: String,
    val localStart: String,
    val date: String,
    val elapsedMinutes: Double,
    val activeMinutes: Double,
    val distanceKm: Double?,
    val caloriesKcal: Double?,
    val avgHeartRate: Long?,
    val maxHeartRate: Long?,
    val paceMinPerKm: Double?,
    val paceMinPer100m: Double?,
    val sourcePackage: String,
    val sourceLabel: String,
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("category", category)
        put("exerciseType", exerciseType)
        put("startTime", startTime)
        put("endTime", endTime)
        put("localStart", localStart)
        put("date", date)
        put("elapsedMinutes", elapsedMinutes)
        put("activeMinutes", activeMinutes)
        put("distanceKm", distanceKm ?: JSONObject.NULL)
        put("caloriesKcal", caloriesKcal ?: JSONObject.NULL)
        put("avgHeartRate", avgHeartRate ?: JSONObject.NULL)
        put("maxHeartRate", maxHeartRate ?: JSONObject.NULL)
        put("paceMinPerKm", paceMinPerKm ?: JSONObject.NULL)
        put("paceMinPer100m", paceMinPer100m ?: JSONObject.NULL)
        put("sourcePackage", sourcePackage)
        put("sourceLabel", sourceLabel)
    }
}
