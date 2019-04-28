package com.evartem.forexquotes.quotes

import com.evartem.forexquotes.remote.model.Quote

data class QuotesViewState(val quotes: List<Quote>)