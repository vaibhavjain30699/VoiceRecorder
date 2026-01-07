package com.vaibhav.voicerecordertranscripter.viewmodel

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vaibhav.voicerecordertranscripter.model.RecordingScreenState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

class RecordingScreenViewModel() : ViewModel() {
    private val _mutableRecordingScreenState = MutableStateFlow(RecordingScreenState())
    val recordingScreenState: StateFlow<RecordingScreenState> = _mutableRecordingScreenState

    private var timerJob: Job? = null
    private var secondsElapsed = 0L

    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null

    init {
        _mutableRecordingScreenState.update {
            it.copy(
                patientName = "John Davis"
            )
        }
    }

    private var speechRecognizer: SpeechRecognizer? = null

    fun setupSpeechToText(context: Context) {
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onResults(results: Bundle?) {
                    val data = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    _mutableRecordingScreenState.update {
                        it.copy(
                            transcriptionText = data?.get(0) ?: ""
                        )
                    }
                    // Restart listening if still recording
                    if (_mutableRecordingScreenState.value.isRecording) startListening()
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val data =
                        partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    _mutableRecordingScreenState.update {
                        it.copy(
                            transcriptionText = data?.get(0) ?: ""
                        )
                    }
                }

                override fun onError(error: Int) {
                    // Handle errors (e.g., Error 7 is no match/silence)
                    if (_mutableRecordingScreenState.value.isRecording) startListening()
                }

                // Other overrides can be empty
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
    }

    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        speechRecognizer?.startListening(intent)
    }


    fun onPauseOrResumeClick(context: Context) {
        setupSpeechToText(context) // Ensure initialized

        val currentState = _mutableRecordingScreenState.value
        val newState = !currentState.isRecording

        _mutableRecordingScreenState.update { it.copy(isRecording = newState, isStopped = false) }

        if (newState) {
            if (currentState.timerText == "00:00:00") startRecording(context) else resumeRecording()
            startTimer()
            startListening() // Start STT
        } else {
            pauseRecording()
            pauseTimer()
            speechRecognizer?.stopListening() // Stop STT
        }
    }

    fun onStopClick() {
        pauseTimer()
        stopRecording()
        _mutableRecordingScreenState.update {
            it.copy(isRecording = false, isStopped = true)
        }
    }

    fun onSaveClick(context: Context) {
        val fileToSave = audioFile
        if (fileToSave == null || !fileToSave.exists()) {
            Toast.makeText(context, "No recording found to save", Toast.LENGTH_SHORT).show()
            return
        }

        val randomName = "VoiceRecording_${System.currentTimeMillis()}.m4a"

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, randomName)
            put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp4")
            put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_MUSIC + "/VoiceRecorder/${_mutableRecordingScreenState.value.patientName}"
            )
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues)

        if (uri != null) {
            try {
                resolver.openOutputStream(uri)?.use { outputStream ->
                    fileToSave.inputStream().use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                Toast.makeText(
                    context,
                    "File Saved to Music/VoiceRecorder/${_mutableRecordingScreenState.value.patientName}",
                    Toast.LENGTH_SHORT
                )
                    .show()
                fileToSave.delete()
                audioFile = null
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Error saving file", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Failed to create MediaStore entry", Toast.LENGTH_SHORT).show()
        }

        onCloseClick()
    }

    fun onCloseClick() {
        resetTimer()
        // If we were recording, clean up
        mediaRecorder?.release()
        mediaRecorder = null
        _mutableRecordingScreenState.update {
            it.copy(isRecording = false, isStopped = false, waveformAmplitudes = emptyList())
        }
    }

    private fun startRecording(context: Context) {
        val outputDir = context.externalCacheDir
        audioFile = File.createTempFile("recording", ".m4a", outputDir)

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(128000)
            setAudioSamplingRate(44100)
            setOutputFile(audioFile?.absolutePath)

            try {
                prepare()
                start()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun pauseRecording() {
        try {
            mediaRecorder?.pause()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun resumeRecording() {
        try {
            mediaRecorder?.resume()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopRecording() {
        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            mediaRecorder = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(100)
                secondsElapsed += 100

                val amplitude = try {
                    mediaRecorder?.maxAmplitude?.toFloat() ?: 0f
                } catch (e: Exception) {
                    0f
                }

                // Normalize amplitude to 0.1 - 1.0 range
                val normalized = (amplitude / 32767f).coerceIn(0.1f, 1f)

                _mutableRecordingScreenState.update { state ->
                    state.copy(
                        // Format time only on full seconds, otherwise keep current
                        timerText = formatTime(secondsElapsed / 1000),
                        // Add new amplitude and keep only the last 50 points
                        waveformAmplitudes = (state.waveformAmplitudes + normalized).takeLast(50)
                    )
                }
            }
        }
    }

    private fun pauseTimer() {
        timerJob?.cancel()
    }

    private fun resetTimer() {
        timerJob?.cancel()
        secondsElapsed = 0
        _mutableRecordingScreenState.update {
            it.copy(timerText = formatTime(secondsElapsed))
        }
    }

    private fun formatTime(seconds: Long): String {
        val hrs = seconds / 3600
        val mins = (seconds % 3600) / 60
        val secs = seconds % 60
        return String.format("%02d:%02d:%02d", hrs, mins, secs)
    }

    override fun onCleared() {
        super.onCleared()
        mediaRecorder?.release()
        mediaRecorder = null
        speechRecognizer?.destroy()
    }
}