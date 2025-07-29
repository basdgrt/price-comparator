package com.github.basdgrt.scraping

import com.github.basdgrt.products.Webshop

sealed interface ScrapeFailure {
    fun errorMessage(): String

    data class InvalidNumber(val input: String, val errorMessage: String): ScrapeFailure {
        override fun errorMessage() = "Failed to parse price: $input"
    }
    data class FailedToFindPriceElement(val webshop: Webshop, val element: String, val baseUri: String): ScrapeFailure {
        override fun errorMessage() = "Failed to find price element on $webshop"
    }
}

