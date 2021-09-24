package com.viplove.myapplication.screens

import android.Manifest
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.potyvideo.library.AndExoPlayerView
import com.viplove.myapplication.R
import android.content.Intent
import android.content.pm.PackageManager

import android.os.Build
import android.app.Activity
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.View.*
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar

import androidx.core.app.ActivityCompat
import androidx.loader.content.CursorLoader
import com.viplove.myapplication.utils.NetworkClient
import com.viplove.myapplication.utils.Utils
import org.koin.android.ext.android.get
import java.io.File
import java.lang.Exception
import java.util.*


class VideoPlayer : AppCompatActivity() {

    var currentPractice = 0
    var currentGestureName: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)


        val intent = intent
        val url = intent.getStringExtra("filePath") ?: intent.getStringExtra("fileAddress")
        val isRecorded = intent.getBooleanExtra("isRecorded", false)
        currentGestureName =
            intent.getStringExtra("currentGestureName") ?: Utils.currentRecording
        currentPractice = intent.getIntExtra("currentPractice", 0)

        Log.d("Video extras", intent.extras.toString())
        Log.d("Video currentPractice", currentPractice.toString())


        if (isRecorded) {
            findViewById<LinearLayout>(R.id.recordedButtons).visibility = VISIBLE
            findViewById<Button>(R.id.practice_button).visibility = GONE
        }

        findViewById<Button>(R.id.upload_button).setOnClickListener {
            val path = intent.getStringExtra("filePath")
            path?.let { uploadFile(it) }
        }

        findViewById<Button>(R.id.cancel_button).setOnClickListener {
            onBackPressed()
        }

        findViewById<Button>(R.id.practice_button).setOnClickListener {
            if (checkCameraPermission(this as Activity)) {
                Utils.currentRecording = currentGestureName

                val values = ContentValues()
                values.put(
                    MediaStore.Video.Media.TITLE,
                    "video name"
                ) // just for generating url in movies folder
                values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
                //videoUri = mediastore path
                val videoUri = contentResolver
                    .insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
                videoUri?.toString()?.let { it1 -> Log.d("generated uri:", it1) }

                val i = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
                i.putExtra(MediaStore.EXTRA_OUTPUT, videoUri)
                i.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 5)
                startActivityForResult(i, 1111)
            }
        }

        val andExoPlayerView: AndExoPlayerView = findViewById(R.id.andExoPlayerView)
        url?.let {
            if (checkPermissionForReadExtertalStorage(this)) {
                andExoPlayerView.setSource(it)
            } else {
                requestPermissionForReadExtertalStorage(this)
            }
        }
    }

    private fun checkPermissionForReadExtertalStorage(context: Context): Boolean {
        val result: Int = context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
    }

    @Throws(Exception::class)
    fun requestPermissionForReadExtertalStorage(context: Context) {
        try {
            ActivityCompat.requestPermissions(
                (context as Activity?)!!,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                0x3
            )
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    private fun uploadFile(path: String) {
        val networkClient = get<NetworkClient>()
        val utils = get<Utils>()
        networkClient.upload(path) { data, isSuccessful ->
            findViewById<ProgressBar>(R.id.ProgressBar).visibility = GONE
            if (isSuccessful && data["status"] == "success") {
                // do something
                Log.d("this is done after upload", "aud")
                utils.makeLongToast("Video Upload Successful")
                findViewById<LinearLayout>(R.id.video_uploaded).visibility = VISIBLE
                if (currentPractice >= 3) {
                    this.setResult(Utils.quitResult)
                    findViewById<Button>(R.id.practice_button).visibility = GONE
                    findViewById<LinearLayout>(R.id.recordedButtons).visibility = VISIBLE
                } else {
                    findViewById<Button>(R.id.practice_button).visibility = VISIBLE
                    findViewById<LinearLayout>(R.id.recordedButtons).visibility = GONE
                }
            } else {
                utils.makeLongToast("Video Upload Failed")
                findViewById<Button>(R.id.upload_button).visibility = VISIBLE
                findViewById<LinearLayout>(R.id.recordedButtons).visibility = GONE
            }
        }
            .also {
                Log.d("this is done before upload", "dfd")
                findViewById<ProgressBar>(R.id.ProgressBar).visibility = VISIBLE
                findViewById<Button>(R.id.upload_button).visibility = GONE
            }
    }

    private fun checkCameraPermission(context: Activity): Boolean {
        return if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(context, arrayOf(Manifest.permission.CAMERA), 111)
            false
        } else {
            true
        }
    }

    private fun getPath(uri: Uri): String? {
        val data = arrayOf(MediaStore.Video.Media.DATA)
        val loader = CursorLoader(this, uri, data, null, null, null)
        val cursor: Cursor? = loader.loadInBackground()
        val columnIndex: Int? = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor?.moveToFirst()
        return columnIndex?.let { cursor.getString(it) }
    }

    private fun newPath(uri: Uri): String {
        val originalPath = getPath(uri)!!

        val lastIndex = originalPath.lastIndexOf('/')
        val dir = originalPath.substring(0, lastIndex)
        val newName = "${currentGestureName}_PRACTICE_${currentPractice + 1}_Mittal.mp4"

        val from = File(originalPath)
        val to = File(dir, newName)
        if (from.exists()) from.renameTo(to)
        return "$dir/${newName}"
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 111) {
            val utils = get<Utils>()
            when (grantResults[0]) {
                PackageManager.PERMISSION_GRANTED -> utils.makeLongToast("Camera Permission Granted, retry now!!")
                PackageManager.PERMISSION_DENIED -> utils.makeLongToast("Camera permission not granted")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("Request, result code, cp", "$requestCode, $resultCode, $currentPractice")
        if (requestCode == 1111) {
            val location = data?.data
            Log.d("Recorded Video location:", location.toString())
            location?.toString()?.let { path ->
                val utils = get<Utils>()
                Utils.currentRecording?.let { currentRecordedGestureName ->
                    utils.saveRecordedAttempt(
                        currentRecordedGestureName,
                        path
                    )
                }
                val i = Intent(this, VideoPlayer::class.java)
                i.putExtra("fileAddress", path)
                i.putExtra("filePath", newPath(location))
                i.putExtra("isRecorded", true)
                i.putExtra("currentGestureName", Utils.currentRecording)
                i.putExtra("currentPractice", currentPractice+1)
                startActivityForResult(i, Utils.quitResult)
            }
        } else if (resultCode == Utils.quitResult) {
            setResult(Utils.quitResult)
            this.finish()
        }
    }
}