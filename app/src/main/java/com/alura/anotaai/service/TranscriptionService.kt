package com.alura.anotaai.service

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

class TranscriptionService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun transcribeAudio(audioFilePath: String, apiKey: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val audioFile = File(audioFilePath)
                if (!audioFile.exists()) {
                    return@withContext Result.failure(Exception("Arquivo não encontrado em: $audioFilePath"))
                }
                
                if (audioFile.length() < 100L) {
                    return@withContext Result.failure(Exception("Áudio muito curto. Grave por pelo menos 2 segundos."))
                }

                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("model", "whisper-large-v3")
                    .addFormDataPart("file", audioFile.name, audioFile.asRequestBody("audio/mp4".toMediaType()))
                    .addFormDataPart("language", "pt")
                    .build()

                val request = Request.Builder()
                    .url("https://api.groq.com/openai/v1/audio/transcriptions")
                    .addHeader("Authorization", "Bearer ${apiKey.trim()}")
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    val json = JSONObject(responseBody)
                    Result.success(json.getString("text"))
                } else {
                    val errorMessage = try {
                        val json = JSONObject(responseBody)
                        json.getJSONObject("error").getString("message")
                    } catch (e: Exception) {
                        "Erro ${response.code}: $responseBody"
                    }
                    Log.e("Transcription", "Erro da API: $errorMessage")
                    Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                Log.e("Transcription", "Falha na requisição", e)
                Result.failure(e)
            }
        }
    }
}