package com.github.basdgrt.scraping

sealed interface ScrapeFailure {
    data class InvalidNumber(val input: String, val errorMessage: String): ScrapeFailure
    data class FailedToFindPriceElement(val element: String, val baseUri: String): ScrapeFailure
}

