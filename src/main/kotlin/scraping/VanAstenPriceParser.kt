package com.github.basdgrt.scraping

import arrow.core.Either
import arrow.core.raise.either
import com.github.basdgrt.models.Price
import com.github.basdgrt.scraping.ParseFailure.FailedToFindPriceElement
import org.jsoup.nodes.Document

private const val PRICE_ELEMENT = "span.price"

class VanAstenPriceParser : PriceParser {
    override fun parse(document: Document): Either<ParseFailure, Price> {
        return either {
            val priceElement = document.select(PRICE_ELEMENT).firstOrNull() ?: raise(
                FailedToFindPriceElement(
                    element = PRICE_ELEMENT,
                    baseUri = document.baseUri()
                )
            )

            val priceText = priceElement.text().trim().replace(",", ".")

            Price.of(priceText).bind()
        }
    }
}
