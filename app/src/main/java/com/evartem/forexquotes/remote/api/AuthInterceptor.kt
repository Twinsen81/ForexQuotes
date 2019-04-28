package com.evartem.forexquotes.remote.api

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val apiKeyParamName: String, private val apiKey: String) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        val httpUrl = chain.request().url()
            .newBuilder()
            .addQueryParameter(apiKeyParamName, apiKey)
            .build()

        val request = chain.request().newBuilder().url(httpUrl).build()

        return chain.proceed(request)
    }
}