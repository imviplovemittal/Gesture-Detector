package com.viplove.myapplication.utils

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.widget.Toast

class Utils(private val context: Context) {

    companion object {
        val downloading = mutableMapOf<Long, Pair<String, Int>>()
        var currentRecording: String? = null
        const val cameraResult = 1111
        const val quitResult = 311
        val baseUrl = if (Build.FINGERPRINT.contains("generic")) "http://10.0.2.2:5000" else "http://localhost:5000"
    }

    enum class Keys(name: String) {
        DOWNLOADED_GESTURES("downloadedGestures")
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("basePrefs", Context.MODE_PRIVATE)

    fun addDownloadedGestures(gestureName: String) {
        val currentGestures = getDownloadedGestures().toMutableSet()
        currentGestures.add(gestureName)
        sharedPreferences.edit().let {
            it.putStringSet(Keys.DOWNLOADED_GESTURES.name, currentGestures)
            it.commit()
        }
    }

    fun getDownloadedGestures(): Set<String> {
        return sharedPreferences.getStringSet(Keys.DOWNLOADED_GESTURES.name, setOf()) ?: setOf()
    }

    fun removeDownloadedGesture(gestureName: String) {
        val currentGestures = getDownloadedGestures().toMutableSet()
        if (currentGestures.contains(gestureName)) {
            currentGestures.remove(gestureName)
            with(sharedPreferences.edit()) {
                putStringSet(Keys.DOWNLOADED_GESTURES.name, currentGestures)
                commit()
            }
        }
    }

    fun saveRecordedAttempt(gestureName: String, path: String) {
        val currentAttempts = getRecordedAttempts(gestureName).toMutableSet()
        currentAttempts.add(path)
        with(sharedPreferences.edit()) {
            putStringSet(gestureName, currentAttempts)
            commit()
        }
    }

    fun getRecordedAttempts(gestureName: String): Set<String> {
        return sharedPreferences.getStringSet(gestureName, setOf()) ?: setOf()
    }

    fun makeLongToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }


}