package com.evartem.forexquotes.remote.model

import java.math.BigDecimal

data class Quote(
    val symbol: String,
    val price: BigDecimal = BigDecimal.ZERO,
    val bid: BigDecimal = BigDecimal.ZERO,
    val ask: BigDecimal = BigDecimal.ZERO,
    val timestamp: Long = 0
)