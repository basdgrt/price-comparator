package com.github.basdgrt.scraping

sealed interface ParseFailure {
    data class InvalidNumber(val input: String, val errorMessage: String): ParseFailure
    data class FailedToFindPriceElement(val element: String, val baseUri: String): ParseFailure
}

