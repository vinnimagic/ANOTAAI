package com.alura.anotaai

import android.Manifest
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.compose.rememberNavController
import com.alura.anotaai.ui.navigation.NavHost
import com.alura.anotaai.ui.theme.AnotaAITheme
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.values.all { it }
            if (!allGranted) {
                Toast.makeText(this, "Algumas permissões foram negadas. O app pode não funcionar corretamente.", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Solicita todas as permissões necessárias na inicialização
        val permissions = mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
        }
        requestPermissionLauncher.launch(permissions.toTypedArray())

        setContent {
            AnotaAITheme {
                NavHost(
                    rememberNavController(),
                    startRecording = { startRecording(it) },
                    stopRecording = { stopRecording() },
                    startPlaying = { startPlaying(it) },
                    stopPlaying = { stopPlaying() }
                )
            }
        }
    }

    private fun startRecording(audioPath: String) {
        val mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(this)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }

        recorder = mediaRecorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(audioPath)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            try {
                prepare()
                start()
            } catch (e: IOException) {
                Log.e("AudioRecorder", "Falha ao iniciar gravação", e)
                recorder = null
            }
        }
    }

    private fun stopRecording() {
        try {
            recorder?.stop()
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Erro ao parar recorder", e)
        } finally {
            recorder?.release()
            recorder = null
        }
    }

    private fun startPlaying(fileName: String) {
        stopPlaying()
        player = MediaPlayer().apply {
            try {
                setDataSource(fileName)
                prepare()
                start()
                setOnCompletionListener { stopPlaying() }
            } catch (e: IOException) {
                Log.e("AudioPlayer", "Falha ao reproduzir", e)
                player = null
            }
        }
    }

    private fun stopPlaying() {
        player?.apply {
            if (isPlaying) stop()
            release()
        }
        player = null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRecording()
        stopPlaying()
    }
}
