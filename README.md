# Voice Recorder & Transcripter

A modern Android application built with **Jetpack Compose** designed for healthcare professionals or general users to record audio, view real-time transcriptions, and save sessions organized by patient name.

## 🚀 Features

- **High-Quality Audio**: Records audio in `.m4a` (AAC) format for clear playback and small file sizes.
- **Live Transcription**: Integrated Google Speech-to-Text for real-time visual feedback during recording sessions.
- **Waveform Visualizer**: A custom-drawn Canvas waveform that reacts to microphone input levels.
- **WAV/PCM Ready**: (Optionally) uses low-level `AudioRecord` for granular sound control.
- **Organized Storage**: Automatically saves recordings to the public `Music/VoiceRecorder/` folder, organized into sub-folders by patient name.
- **Material Design 3**: Modern, clean UI featuring dynamic action buttons and themed surfaces.

## 🛠 Prerequisites

- **Physical Android Device**: Highly recommended. Speech recognition and microphone hardware timing often fail or behave unpredictably on emulators.
- **Android Version**: Minimum SDK 24 (Android 7.0) or higher.
- **Google App**: Ensure the Google app is installed and updated on the device, as it provides the underlying engine for the `SpeechRecognizer`.

## 📦 How to Run

# Voice Recorder & Transcripter Setup Guide

### 1. Setup Environment
* Download and install **Android Studio Jellyfish** or newer.
* Ensure you have the **Android SDK for API 34** installed.

### 2. Clone and Open
* Clone this repository to your local machine.
* Open Android Studio and select **File > Open**, then navigate to the project folder.

### 3. Build the Project
* Allow Gradle to sync and download dependencies.
* Go to **Build > Make Project** to ensure all components are compiled.

### 4. Run on Device
* Connect your physical Android device via USB and enable **USB Debugging** in Developer Options.
* Select your device in the top toolbar of Android Studio.
* Click the **Run** button (green play icon).

### 5. Using the App
* **Permissions**: Upon first launch, the app will request Microphone permission. Click "Allow."
* **Record**: Tap the Play button to start recording. You will see the timer start and the waveform move.
* **Live Text**: Speak clearly; the "Transcription Box" will update in real-time.
* **Save**: Tap "Stop" then "Save" to move the recording from temporary storage to your public `Music/VoiceRecorder/` folder.

---

## Project Structure
* **`MainActivity.kt`**: Entry point and ViewModel initialization.
* **`RecordingScreen.kt`**: The main UI layout using Jetpack Compose.
* **`RecordingScreenViewModel.kt`**: Logic for AudioRecord, SpeechRecognizer, and Timer management.
* **`RecordingScreenState.kt`**: Data class holding the UI state.

  
