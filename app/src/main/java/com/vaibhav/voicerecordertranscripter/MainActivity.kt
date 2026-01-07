package com.vaibhav.voicerecordertranscripter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.vaibhav.voicerecordertranscripter.ui.theme.VoiceRecorderTranscripterTheme
import com.vaibhav.voicerecordertranscripter.viewmodel.RecordingScreenViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel: RecordingScreenViewModel by viewModels()

        setContent {
            VoiceRecorderTranscripterTheme {
                RecordingScreen(viewModel)
            }
        }
    }
}