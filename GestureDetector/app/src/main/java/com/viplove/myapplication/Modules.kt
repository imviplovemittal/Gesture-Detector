package com.viplove.myapplication

import com.viplove.myapplication.utils.NetworkClient
import com.viplove.myapplication.utils.Service
import com.viplove.myapplication.utils.Utils
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit

val appModule = module {
    single { Utils(get()) }

    single { MainActivity() }

    single { NetworkClient() }
}