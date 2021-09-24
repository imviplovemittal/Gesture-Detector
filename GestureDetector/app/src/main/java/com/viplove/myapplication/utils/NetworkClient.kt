package com.viplove.myapplication.utils

import android.content.Context
import android.util.Log
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Callback
import retrofit2.Retrofit

import java.io.File
import retrofit2.Response
import retrofit2.converter.gson.GsonConverterFactory


class NetworkClient {

    fun upload(
        videoUrl: String,
        action: (data: Map<Any, Any>, success: Boolean) -> Unit
    ) {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

        val service =
            Retrofit.Builder().baseUrl(Utils.baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build().create(
                    Service::class.java
                )

        val file = File(videoUrl)

        val reqFile: RequestBody = RequestBody.create(MediaType.parse("video/*"), file)
        val body = MultipartBody.Part.createFormData("file", file.getName(), reqFile)
        val name = RequestBody.create(MediaType.parse("text/plain"), "upload_test")

        val req = service.postVideo(body, name)

        req.enqueue(object : Callback<Map<Any, Any>> {
            override fun onFailure(call: retrofit2.Call<Map<Any, Any>>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(
                call: retrofit2.Call<Map<Any, Any>>,
                response: Response<Map<Any, Any>>
            ) {
                Log.d("upload response", response.isSuccessful.toString())

                response.body()?.let {
                    Log.d("Response body:", it.toString())
                    action(it, response.isSuccessful)
                }
            }

        })
    }

}