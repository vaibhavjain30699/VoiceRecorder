package com.vaibhav.voicerecordertranscripter

data class RecordingScreenState(
    val patientName: String = "",
    val isRecording: Boolean = false,
    val isStopped: Boolean = false,
    val timerText: String = "00:00:00",
    val waveformAmplitudes: List<Float> = emptyList(),
    val transcriptionText: String = "Live transcription will appear here...",
    val translationText: String = "AI Translation will appear here..."
)