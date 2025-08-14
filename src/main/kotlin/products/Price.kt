package com.github.basdgrt.products

import arrow.core.Either
import arrow.core.raise.either
import com.github.basdgrt.scraping.ScrapeFailure
import java.math.BigDecimal

@JvmInline
value class Price private constructor(val value: BigDecimal) : Comparable<Price> {

    init {
        require(value >= BigDecimal.ZERO) { "Price must be greater than or equal to zero" }
    }

    companion object {
        fun of(value: String): Either<ScrapeFailure, Price> = either {
            try {
                Price(value.toBigDecimal())
            } catch (e: Exception) {
                raise(ScrapeFailure.InvalidNumber(value, e.message ?: "Unknown reason"))
            }
        }
    }

    operator fun minus(other: Price): Price {
        return Price(value - other.value)
    }

    override fun compareTo(other: Price): Int = value.compareTo(other.value)

    override fun toString(): String = "€$value"
}