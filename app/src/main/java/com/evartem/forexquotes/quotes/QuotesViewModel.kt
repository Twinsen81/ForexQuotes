package com.evartem.forexquotes.quotes

import androidx.lifecycle.ViewModel
import com.evartem.forexquotes.remote.model.Quote
import com.evartem.forexquotes.usecase.GetQuotesUseCase
import com.evartem.forexquotes.usecase.GetSymbolsUseCase
import com.evartem.forexquotes.util.toQuotesList
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class QuotesViewModel(
    private val getSymbolsUseCase: GetSymbolsUseCase,
    private val getQuotesUseCase: GetQuotesUseCase
) : ViewModel() {

    private val viewState: BehaviorSubject<QuotesViewState> = BehaviorSubject.create()

    private val events: PublishSubject<QuotesEvent> = PublishSubject.create()

    private val forceQuoteUpdate: PublishSubject<Long> = PublishSubject.create()

    private var disposables: CompositeDisposable? = null

    private val quotes = sortedMapOf<String, Quote>()

    private val symbolsToPoll: ConcurrentHashMap<String, Boolean> = ConcurrentHashMap()

    fun getEventsSubject() = events

    fun getViewStateSubject(): BehaviorSubject<QuotesViewState> {
        if (disposables == null) {
            disposables = CompositeDisposable()

            subscribeToEvents()
        }
        return viewState
    }

    fun viewIsStopped() {
        symbolsToPoll.clear()
    }

    private fun subscribeToEvents() {
        events
            .startWith(QuotesEvent.RequestSymbols)
            .flatMap { event -> processEvent(event) }
            .onErrorReturn { QuotesViewState(listOf()) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(viewState::onNext)
            .addTo(disposables!!)
    }

    private fun processEvent(event: QuotesEvent): Observable<QuotesViewState> =
        when (event) {
            is QuotesEvent.RequestSymbols ->
                getSymbolsAndStartPolling()
                    .map { QuotesViewState(quotes.toQuotesList()) }

            is QuotesEvent.RenderUpdatedSymbols ->
                Observable.just(QuotesViewState(quotes.toQuotesList()))

            is QuotesEvent.ChangeVisibleSymbols ->
                Observable.just(QuotesViewState(emptyList()))
                    .doOnNext {
                        setSymbolsToPoll(event.symbols)
                        forceQuoteUpdate.onNext(0)
                    }
                    .filter { false }
        }

    private fun updateQuotesFor(symbols: List<String>): Observable<Map<String, Quote>> =
        getQuotesUseCase.execute(symbols)
            .doOnNext { updatedQuotes -> quotes.putAll(updatedQuotes) }
            .map { quotes }

    private fun getSymbolsAndStartPolling(): Observable<Map<String, Quote>> =
        getSymbolsUseCase.execute()
            .map { listOfSymbols ->
                listOfSymbols.associateWith { symbol -> Quote(symbol) }
            }
            .doOnNext { mapSymbolsQuotes ->
                quotes.putAll(mapSymbolsQuotes)
                setSymbolsToPoll(quotes.keys.toList().take(GetQuotesUseCase.MAX_SYMBOLS_PER_REQUEST))
                subscribeToPollingEvents()
            }

    private fun subscribeToPollingEvents() {
        Flowable.interval(2, TimeUnit.SECONDS)
            .startWith(0)
            .mergeWith(forceQuoteUpdate.toFlowable(BackpressureStrategy.DROP))
            .onBackpressureDrop()
            .filter { symbolsToPoll.isNotEmpty() }
            .flatMap {
                updateQuotesFor(symbolsToPoll.toList().map { it.first })
                    .toFlowable(BackpressureStrategy.DROP)
            }
            .subscribe { events.onNext(QuotesEvent.RenderUpdatedSymbols) }
            .addTo(disposables!!)
    }

    private fun setSymbolsToPoll(symbols: List<String>) {
        symbolsToPoll.clear()
        symbolsToPoll.putAll(symbols.map { it to false }.toMap())
    }

    override fun onCleared() {
        super.onCleared()
        disposables?.clear()
    }
}