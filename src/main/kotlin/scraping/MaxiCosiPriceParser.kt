package com.github.basdgrt.scraping

import arrow.core.Either
import com.github.basdgrt.products.WebshopPrice
import org.jsoup.nodes.Document

class MaxiCosiPriceParser : PriceParser {
    override fun parse(document: Document): Either<ScrapeFailure, WebshopPrice> {
        TODO("Not yet implemented")
    }
}