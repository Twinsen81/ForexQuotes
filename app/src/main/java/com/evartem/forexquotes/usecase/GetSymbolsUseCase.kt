package com.evartem.forexquotes.usecase

import com.evartem.forexquotes.remote.api.ForexService
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class GetSymbolsUseCase(private val service: ForexService) {

    fun execute() : Observable<List<String>> {
        return service.getAllSymbols()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .map { remoteResponse -> if (remoteResponse.isSuccessful) remoteResponse.body()!! else emptyList() }
            .doOnError { Timber.e(it) }
            .onErrorReturn { emptyList() }
            .toObservable()
    }
}