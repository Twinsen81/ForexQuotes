package com.evartem.forexquotes.remote.api

import com.evartem.forexquotes.remote.model.Quote
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query


interface ForexService {

    @GET("/1.0.3/symbols")
    fun getAllSymbols(): Single<Response<List<String>>>

    @GET("/1.0.3/quotes")
    fun getQuotes(@Query("pairs") pairs: String): Single<Response<List<Quote>>>
}