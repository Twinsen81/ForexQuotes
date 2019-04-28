package com.evartem.forexquotes.quotes


sealed class QuotesEvent {
    object RequestSymbols: QuotesEvent()
    object RenderUpdatedSymbols: QuotesEvent()
    data class ChangeVisibleSymbols(val symbols: List<String>): QuotesEvent()
}