package com.viplove.myapplication

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.viplove.myapplication.databinding.GestureListBinding
import com.viplove.myapplication.models.Gesture
import com.viplove.myapplication.screens.VideoPlayer
import com.viplove.myapplication.utils.Utils
import org.koin.android.ext.android.get
import android.provider.MediaStore
import androidx.loader.content.CursorLoader
import java.io.File


class MainActivity : AppCompatActivity() {

    private lateinit var binding: GestureListBinding

    private val gestures = (0..9).map { Gesture("H-$it") } +
            listOf<Gesture>(
                Gesture("H-DecreaseFanSpeed"),
                Gesture("H-FanOff"),
                Gesture("H-FanOn"),
                Gesture("H-IncreaseFanSpeed"),
                Gesture("H-LightOff"),
                Gesture("H-LightOn"),
                Gesture("H-SetThermo"),
            )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = GestureListBinding.inflate(layoutInflater)

        val utils = get<Utils>()
        utils.getDownloadedGestures().let { downloadedGestures ->
            gestures.forEach {
                if (downloadedGestures.contains(it.name)) {
                    it.downloaded = true
                }
            }
        }

        Log.d("Gestures after fetch", gestures.toString())
        val recyclerView = findViewById<RecyclerView>(R.id.gesture_recycler_view)

        val llm = LinearLayoutManager(this)
        llm.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = llm
        recyclerView.adapter = GestureRecyclerAdapter(gestures, baseContext)

        val br = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (Utils.downloading.containsKey(id)) {
                    utils.makeLongToast("Downloaded")
                    Utils.downloading[id]?.second?.let { index ->
                        gestures[index].downloaded = true
                        utils.addDownloadedGestures(gestures[index].name)
                        recyclerView.adapter?.notifyItemChanged(index)
                    }
                }
            }
        }
        registerReceiver(br, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

    }
}