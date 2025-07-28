package com.github.basdgrt.scraping

import arrow.core.Either
import org.jsoup.nodes.Document

interface PriceParser {

    fun parse(document: Document): Either<ParseFailure, Price>
}