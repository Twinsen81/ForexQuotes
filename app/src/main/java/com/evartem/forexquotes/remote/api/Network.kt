package com.evartem.forexquotes.remote.api

import com.evartem.forexquotes.remote.model.BigDecimalAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit

fun createForexServiceNetworkClient(baseUrl: String, authInterceptor: AuthInterceptor? = null, debug: Boolean = false): ForexService =
    Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(MoshiConverterFactory.create(createMoshi()))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .client(createHttpClient(authInterceptor, debug))
        .build()
        .create(ForexService::class.java)

fun createHttpClient(authInterceptor: AuthInterceptor? = null, debug: Boolean = false, connectTimeout: Long = 5, readTimeout: Long = 10): OkHttpClient {
    val httpClientBuilder = OkHttpClient.Builder()
        .connectTimeout(connectTimeout, TimeUnit.SECONDS)
        .readTimeout(readTimeout, TimeUnit.SECONDS)

    if (debug) {
        val logging: HttpLoggingInterceptor
        logging = HttpLoggingInterceptor { message -> Timber.tag("SERVER-BODY: ").d(message) }
        logging.level = HttpLoggingInterceptor.Level.BODY
        httpClientBuilder.addInterceptor(logging)
    }

    if (authInterceptor != null)
        httpClientBuilder.addInterceptor(authInterceptor)

    return httpClientBuilder.build()
}

fun createMoshi(): Moshi =
    Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .add(BigDecimalAdapter())
        .build()