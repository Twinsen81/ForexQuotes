package com.evartem.forexquotes.usecase

import com.evartem.forexquotes.remote.api.ForexService
import com.evartem.forexquotes.remote.model.Quote
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class GetQuotesUseCase(private val service: ForexService) {

    companion object {
        const val MAX_SYMBOLS_PER_REQUEST = 20
    }

    fun execute(symbols: List<String>): Observable<Map<String, Quote>> {
        return service.getQuotes(symbols.take(MAX_SYMBOLS_PER_REQUEST).joinToString(","))
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .map { remoteResponse ->
                if (remoteResponse.isSuccessful)
                    listOfQuotesToMap(remoteResponse.body()!!)
                else
                    emptyMap()
            }
            .doOnError { Timber.e(it) }
            .onErrorReturn { emptyMap() }
            .toObservable()
    }

    private fun listOfQuotesToMap(quotes: List<Quote>): Map<String, Quote> {
        return quotes.associateBy { it.symbol }
    }
}