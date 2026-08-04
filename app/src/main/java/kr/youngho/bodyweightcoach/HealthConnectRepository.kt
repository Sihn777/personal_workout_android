package kr.youngho.bodyweightcoach

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class HealthConnectRepository(private val context: Context) {
    companion object {
        const val PROVIDER_PACKAGE = "com.google.android.apps.healthdata"
        val REQUIRED_PERMISSIONS: Set<String> = setOf(
            HealthPermission.getReadPermission(ExerciseSessionRecord::class),
            HealthPermission.getReadPermission(DistanceRecord::class),
            HealthPermission.getReadPermission(HeartRateRecord::class),
            HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
        )
    }

    fun sdkStatus(): Int = HealthConnectClient.getSdkStatus(context, PROVIDER_PACKAGE)

    private fun client(): HealthConnectClient = HealthConnectClient.getOrCreate(context, PROVIDER_PACKAGE)

    suspend fun grantedPermissions(): Set<String> = client().permissionController.getGrantedPermissions()

    suspend fun hasAllPermissions(): Boolean = grantedPermissions().containsAll(REQUIRED_PERMISSIONS)

    suspend fun readRunningAndSwimming(daysBack: Int = 30): List<WorkoutSummary> {
        require(hasAllPermissions()) { "Health Connect 읽기 권한이 모두 필요합니다." }
        val hc = client()
        val end = Instant.now()
        val start = end.minus(Duration.ofDays(daysBack.coerceIn(1, 30).toLong()))
        val sessions = hc.readRecords(
            ReadRecordsRequest(
                recordType = ExerciseSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(start, end),
                ascendingOrder = false,
                pageSize = 1000,
            )
        ).records.filter { it.exerciseType in supportedTypes }

        return sessions.mapNotNull { session ->
            runCatching { summarize(hc, session) }.getOrNull()
        }.sortedByDescending { it.startTime }
    }

    private suspend fun summarize(
        hc: HealthConnectClient,
        session: ExerciseSessionRecord,
    ): WorkoutSummary {
        val filter = TimeRangeFilter.between(session.startTime, session.endTime)
        val aggregate = hc.aggregate(
            AggregateRequest(
                metrics = setOf(
                    ExerciseSessionRecord.EXERCISE_DURATION_TOTAL,
                    DistanceRecord.DISTANCE_TOTAL,
                    TotalCaloriesBurnedRecord.ENERGY_TOTAL,
                    HeartRateRecord.BPM_AVG,
                    HeartRateRecord.BPM_MAX,
                ),
                timeRangeFilter = filter,
                dataOriginFilter = setOf(session.metadata.dataOrigin),
            )
        )
        val elapsed = Duration.between(session.startTime, session.endTime)
        val active = aggregate[ExerciseSessionRecord.EXERCISE_DURATION_TOTAL] ?: elapsed
        val activeMinutes = active.toMillis() / 60_000.0
        val elapsedMinutes = elapsed.toMillis() / 60_000.0
        val distanceKm = aggregate[DistanceRecord.DISTANCE_TOTAL]?.inKilometers
        val calories = aggregate[TotalCaloriesBurnedRecord.ENERGY_TOTAL]?.inKilocalories
        val avgHr = aggregate[HeartRateRecord.BPM_AVG]
        val maxHr = aggregate[HeartRateRecord.BPM_MAX]
        val category = if (session.exerciseType in runningTypes) "running" else "swimming"
        val paceKm = if (category == "running" && distanceKm != null && distanceKm > 0.05) activeMinutes / distanceKm else null
        val pace100m = if (category == "swimming" && distanceKm != null && distanceKm > 0.01) activeMinutes / (distanceKm * 10.0) else null
        val zone = ZoneId.systemDefault()
        val local = session.startTime.atZone(zone)
        val sourcePackage = session.metadata.dataOrigin.packageName
        val sourceLabel = applicationLabel(sourcePackage)

        return WorkoutSummary(
            id = session.metadata.id,
            category = category,
            exerciseType = session.exerciseType,
            startTime = session.startTime.toString(),
            endTime = session.endTime.toString(),
            localStart = local.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
            date = local.toLocalDate().toString(),
            elapsedMinutes = round2(elapsedMinutes),
            activeMinutes = round2(activeMinutes),
            distanceKm = distanceKm?.let(::round3),
            caloriesKcal = calories?.let(::round1),
            avgHeartRate = avgHr,
            maxHeartRate = maxHr,
            paceMinPerKm = paceKm?.let(::round3),
            paceMinPer100m = pace100m?.let(::round3),
            sourcePackage = sourcePackage,
            sourceLabel = sourceLabel,
        )
    }

    private fun applicationLabel(packageName: String): String = runCatching {
        val info = context.packageManager.getApplicationInfo(packageName, 0)
        context.packageManager.getApplicationLabel(info).toString()
    }.getOrElse {
        if (packageName == "com.sec.android.app.shealth") "Samsung Health" else packageName
    }

    private fun round1(v: Double) = kotlin.math.round(v * 10.0) / 10.0
    private fun round2(v: Double) = kotlin.math.round(v * 100.0) / 100.0
    private fun round3(v: Double) = kotlin.math.round(v * 1000.0) / 1000.0

    private val runningTypes = setOf(
        ExerciseSessionRecord.EXERCISE_TYPE_RUNNING,
        ExerciseSessionRecord.EXERCISE_TYPE_RUNNING_TREADMILL,
    )
    private val swimmingTypes = setOf(
        ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_POOL,
        ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_OPEN_WATER,
    )
    private val supportedTypes = runningTypes + swimmingTypes
}
