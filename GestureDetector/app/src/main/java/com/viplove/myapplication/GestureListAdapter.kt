package com.viplove.myapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView


class GestureListAdapter(context: Context, private val gestures: Array<String>): ArrayAdapter<String>(context, 0, gestures) {
    val test = ""

    override fun getCount(): Int {
        return gestures.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val gesture = getItem(position)
        val newConvertView = convertView ?: LayoutInflater.from(context).inflate(R.layout.gesture_item, parent, false)

        newConvertView?.findViewById<TextView>(R.id.gesture_name_card)?.text = gesture
        return newConvertView!!
    }

}