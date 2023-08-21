package com.example.masterapp.presentation.screen.answerassessment

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.lifecycle.ViewModel
import com.example.masterapp.data.AnswerData
import com.example.masterapp.data.Assessment
import com.example.masterapp.data.DynamicAnswerData
import com.example.masterapp.data.ExerciseSession
import com.example.masterapp.data.HealthConnectManager
import com.example.masterapp.data.HeartRateData
import com.example.masterapp.data.HeartRateDataSample
import com.example.masterapp.data.HeartRateVariabilityData
import com.example.masterapp.data.SleepSessionData
import com.example.masterapp.data.dateTimeWithOffsetOrDefault
import com.example.masterapp.data.roomDatabase.Answer
import com.example.masterapp.data.roomDatabase.AnswerViewModel
import com.example.masterapp.data.roomDatabase.QuestionAnswer
import com.example.masterapp.presentation.screen.SharedViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit


class AnswerAssessmentViewModel(
    private val healthConnectManager: HealthConnectManager,
    private val answerViewModel: AnswerViewModel,
    private val sharedViewModel: SharedViewModel
) : ViewModel() {

    private val _smartwatchDataCollected = MutableStateFlow(false)
    val smartwatchDataCollected: StateFlow<Boolean> = _smartwatchDataCollected
    var sessionsList: MutableState<List<ExerciseSession>> = mutableStateOf(listOf())
        private set

    sealed class AnswerAssessmentUiState {
        object Loading : AnswerAssessmentUiState()
        data class Success(val assessment: Assessment) : AnswerAssessmentUiState()
        object Error : AnswerAssessmentUiState()
        object Submitted : AnswerAssessmentUiState()
    }

    var uiState = mutableStateOf<AnswerAssessmentUiState>(AnswerAssessmentUiState.Loading)

    init {
        getAssessment()
    }
    // Add any necessary logic and data here to handle the assessment answers
    private val healthConnectCompatibleApps = healthConnectManager.healthConnectCompatibleApps

    private val permissions = setOf(
        HealthPermission.getReadPermission(HeartRateVariabilityRmssdRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
    )

    private var permissionsGranted = healthConnectManager.isPermissionGranted

    private fun getAssessment() {
        uiState.value = AnswerAssessmentUiState.Loading

        val assessment = sharedViewModel.getAssessment()

        if (assessment != null) {
            uiState.value = AnswerAssessmentUiState.Success(assessment)
        } else {
            uiState.value = AnswerAssessmentUiState.Error
        }
    }

    suspend fun collectAndSendSmartwatchData(): List<List<Any>>
    {
        val hrvData = readHRVdata()
        val heartRateData = readHeartRateData()
        // Store data or perform necessary operations

        Log.i("HealthConnect", "HRV Data: $hrvData")
        Log.i("HealthConnect", "Heart Rate Data: $heartRateData")
        // Update the answer map with smartwatch data
        return listOf(hrvData, heartRateData)

        // Notify that smartwatch data collection is complete
        _smartwatchDataCollected.value = true

    }

    suspend fun readSleepData(): List<SleepSessionData> {

        return healthConnectManager.readSleepRecords()
    }

    suspend fun readExerciseSessionsData(): List<ExerciseSession> {
        val startOfDay = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
        val now = Instant.now()

        sessionsList.value = healthConnectManager
            .readExerciseSessions(startOfDay.toInstant(), now)
            .map { record ->
                val packageName = record.metadata.dataOrigin.packageName
                ExerciseSession(
                    startTime = dateTimeWithOffsetOrDefault(record.startTime, record.startZoneOffset),
                    endTime = dateTimeWithOffsetOrDefault(record.startTime, record.startZoneOffset),
                    id = record.metadata.id,
                    sourceAppInfo = healthConnectCompatibleApps[packageName],
                    title = record.title
                )
            }
        Log.i("Exercise Sessions", "Exercise Sessions: ")
        sessionsList.value?.forEach { session ->
            Log.i("Exercise Session", session.toString())
        }

        return sessionsList.value
    }

    private suspend fun readHRVdata(): List<HeartRateVariabilityData> {
        // Read HRV data from the device
        val start = Instant.now().minus(7, ChronoUnit.DAYS)
        val end = Instant.now()
        return healthConnectManager.readHeartRateVariabilityRecord(start, end)
    }

    private suspend fun readHeartRateData(): List<HeartRateData> {
        // Read heart rate data from the device
        val start = Instant.now().minus(7, ChronoUnit.DAYS)
        val now = Instant.now()


        return healthConnectManager.readHeartRateRecord(start, now)
    }

// Assuming you have the HeartRateRecord.Sample class

    fun convertToHeartRateDataSample(samples: List<HeartRateRecord.Sample>): List<HeartRateDataSample> {
        return samples.map { sample ->
            HeartRateDataSample(
                beatsPerMinute = sample.beatsPerMinute,
                time = sample.time
            )
        }
    }


    fun saveAnswersToDatabase(
        userId: String,
        assessmentId: String,
        assessmentTitle: String,
        assessmentType: String,
        timestamp: String,
        answerMap: Map<Int, List<AnswerData>>,

    ) {

        val questionAnswers = answerMap.map { (questionIndex, answers) ->
            val dynamicAnswers = answers.map { answerData ->
                when (answerData) {
                    is AnswerData.Textual -> DynamicAnswerData("Textual", answerData.values.joinToString(", "))
                    is AnswerData.Stress -> DynamicAnswerData("Stress", answerData.values.joinToString(", "))
                    is AnswerData.Sleep -> DynamicAnswerData("Sleep", answerData.data)
                    is AnswerData.Exercise -> DynamicAnswerData("Exercise", answerData.data)

                    else -> null
                }
            }.filterNotNull()

            Log.i("AnswerAssessmentViewModel", "Dynamic answers for question $questionIndex: $dynamicAnswers")

            QuestionAnswer(
                questionId = questionIndex,
                answers = dynamicAnswers
            )
        }


        val answer = Answer(
            userId = userId,
            assessmentId = assessmentId,
            assessmentTitle = assessmentTitle,
            assessmentType = assessmentType,
            timestamp = timestamp,
            questionAnswers = questionAnswers
        )
        Log.i("AnswerAssessmentViewModel", "Adding answer to database: $answer")
        // Assuming you have a repository or database instance, save the answer
        // Replace 'yourRepository' with your actual database or repository instance
        answerViewModel.insertAnswer(answer)

        uiState.value = AnswerAssessmentUiState.Submitted
    }
}

