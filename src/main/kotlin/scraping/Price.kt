package com.github.basdgrt.scraping

import arrow.core.Either
import arrow.core.raise.either
import java.math.BigDecimal

@JvmInline
value class Price private constructor(val value: BigDecimal) {
    init {
        require(value >= BigDecimal.ZERO) { "Price must be greater than or equal to zero" }
    }

    companion object {
        fun of(value: String): Either<ParseFailure, Price> = either {
            try {
                Price(value.toBigDecimal())
            } catch (e: Exception) {
                raise(ParseFailure.InvalidNumber(value, e.message ?: "Unknown reason"))
            }
        }
    }
}