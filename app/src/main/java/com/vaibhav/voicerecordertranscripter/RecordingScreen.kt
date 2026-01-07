package com.vaibhav.voicerecordertranscripter

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.vaibhav.voicerecordertranscripter.components.RoundedActionButton
import com.vaibhav.voicerecordertranscripter.components.ImageAndTextPill
import com.vaibhav.voicerecordertranscripter.components.TopBar
import com.vaibhav.voicerecordertranscripter.viewmodel.RecordingScreenViewModel

@Composable
fun RecordingScreen(
    viewModel: RecordingScreenViewModel
) {
    val state by viewModel.recordingScreenState.collectAsState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (!isGranted) {
                Toast.makeText(context, "Please Grant Required Permission", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    )

    LaunchedEffect(Unit) {
        val permissionCheckResult = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        )

        if (permissionCheckResult != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopBar(
                title = "Recording Session",
                subtitle = "Patient : ${state.patientName}",
                onBackClick = { /* Handle navigation */ }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Main content area with overlay support
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = state.timerText,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 64.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ImageAndTextPill(isRecording = state.isRecording)

                    Spacer(modifier = Modifier.height(48.dp))

                    // Transcription View
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.4f),
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )

                    ) {
                        Box(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = state.transcriptionText,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.verticalScroll(rememberScrollState())
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.6f),
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    ) {
                        Box(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = state.translationText,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.verticalScroll(rememberScrollState())
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.3f)
                            .fillMaxHeight(0.8f),
                        contentAlignment = Alignment.Center
                    ) {
                        val barColor = MaterialTheme.colorScheme.primary
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val canvasWidth = size.width
                            val canvasHeight = size.height
                            val barWidth =
                                4.dp.toPx() // Slightly thinner bars look better in a small box
                            val gap = 2.dp.toPx()

                            // Calculate how many bars can fit in this 30% width
                            val maxBarsPossible = (canvasWidth / (barWidth + gap)).toInt()

                            // Take only what fits in the 30% area
                            val amplitudesToDraw =
                                state.waveformAmplitudes.takeLast(maxBarsPossible)
                            val count = amplitudesToDraw.size

                            amplitudesToDraw.forEachIndexed { index, amplitude ->
                                // Calculate x starting from the right edge of the 30% box
                                val x = canvasWidth - (count - index) * (barWidth + gap)

                                // Safety check to ensure we don't draw outside the 0.3 width
                                if (x >= 0) {
                                    // Scale the amplitude height to the Box height
                                    val barHeight =
                                        (amplitude * canvasHeight).coerceAtLeast(4.dp.toPx())

                                    drawLine(
                                        color = barColor,
                                        start = Offset(x, (canvasHeight - barHeight) / 2),
                                        end = Offset(x, (canvasHeight + barHeight) / 2),
                                        strokeWidth = barWidth,
                                        cap = StrokeCap.Round
                                    )
                                }
                            }
                        }
                    }
                }

                // Disabled Overlay when not recording and not stopped
                if (!state.isRecording && !state.isStopped) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                color = MaterialTheme.colorScheme.background.copy(alpha = 0.5f)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (state.isRecording) {
                // Scenario 1: Recording is On or Paused
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RoundedActionButton(
                        icon = Icons.Default.Pause,
                        label = "Pause",
                        onClick = {
                            viewModel.onPauseOrResumeClick(context)
                        },
                        color = MaterialTheme.colorScheme.primaryContainer,
                        size = 72
                    )

                    Spacer(modifier = Modifier.width(32.dp))

                    RoundedActionButton(
                        icon = Icons.Default.Stop,
                        label = "Stop",
                        onClick = { viewModel.onStopClick() },
                        color = MaterialTheme.colorScheme.errorContainer,
                        size = 72
                    )
                }
            } else {
                // Scenario 2: Recording is Stopped
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RoundedActionButton(
                        icon = Icons.Default.PlayArrow,
                        label = "Play",
                        onClick = {
                            val permissionCheckResultForAudio = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            )

                            if (permissionCheckResultForAudio != PackageManager.PERMISSION_GRANTED) {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            } else {
                                viewModel.onPauseOrResumeClick(context)
                            }
                        }
                    )

                    RoundedActionButton(
                        icon = Icons.Default.Done,
                        label = "Save",
                        onClick = { viewModel.onSaveClick(context) },
                        color = MaterialTheme.colorScheme.primary
                    )

                    RoundedActionButton(
                        icon = Icons.Default.Close,
                        label = "Close",
                        onClick = { viewModel.onCloseClick() },
                        color = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
