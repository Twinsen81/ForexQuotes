package com.evartem.forexquotes.util

import com.evartem.forexquotes.remote.model.Quote

fun Map<String, Quote>.toQuotesList(): List<Quote> =
    toList().map { it.second }