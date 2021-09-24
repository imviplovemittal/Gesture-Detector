package com.viplove.myapplication

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat.getSystemService
import androidx.recyclerview.widget.RecyclerView
import com.viplove.myapplication.models.Gesture
import com.viplove.myapplication.screens.VideoPlayer
import com.viplove.myapplication.utils.Utils


class GestureRecyclerAdapter(
    private val gestureList: List<Gesture>,
    private val context: Context
) : RecyclerView.Adapter<GestureRecyclerAdapter.ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val gesture: Gesture = gestureList[position]

        holder.name2.text = gesture.name.substringAfter("H-")
        if (gesture.downloaded) {
            holder.downloadButton?.text = "Play"
            holder.downloadButton?.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.exo_icon_play,
                0,
                0,
                0
            )
            holder.downloadButton?.setOnClickListener {
                val prefix =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
                val path = "$prefix/${gesture.name}.mp4"
                Log.d("file path:", path)
                Utils.currentRecording = gesture.name
                val intent = Intent(context, VideoPlayer::class.java)
                intent.putExtra("fileAddress", path)
                intent.putExtra("currentGestureName", gesture.name)
                holder.itemView.context.startActivity(intent)
            }
        } else {
            holder.downloadButton?.setOnClickListener {
                val request =
                    DownloadManager.Request(Uri.parse(Utils.baseUrl + "/videos/${gesture.name}"))
                        .setTitle("${gesture.name}.mp4")
                        .setAllowedOverMetered(true)
                        .setDestinationInExternalPublicDir(
                            Environment.DIRECTORY_DOWNLOADS.toString(), "${gesture.name}.mp4"
                        )
                        .setVisibleInDownloadsUi(true)
                val dm = getSystemService(context, DownloadManager::class.java)!!
                val downloadId = dm.enqueue(request)
                Log.d("downloadId: ", downloadId.toString())
                Utils.downloading.putIfAbsent(downloadId, gesture.name to position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.gesture_item, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return gestureList.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name2 = itemView.findViewById<TextView>(R.id.gesture_name_card)
        val downloadButton: Button? = itemView.findViewById(R.id.gesture_download)
    }

}