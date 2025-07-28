package com.github.basdgrt.scraping

import arrow.core.Either
import org.jsoup.nodes.Document

class BabyParkPriceParser : PriceParser {
    override fun parse(document: Document): Either<ParseFailure, Price> {
        TODO("Not yet implemented")
    }
}