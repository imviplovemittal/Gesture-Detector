package com.viplove.myapplication.utils

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface Service {
    @Multipart
    @POST("/upload")
    fun postVideo(
        @Part video: MultipartBody.Part,
        @Part("file") file: RequestBody
    ): Call<Map<Any, Any>>
}