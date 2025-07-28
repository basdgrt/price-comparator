package com.github.basdgrt.scraping

import arrow.core.Either
import arrow.core.raise.either
import com.github.basdgrt.models.Price
import com.github.basdgrt.scraping.ParseFailure.FailedToFindPriceElement
import org.jsoup.nodes.Document

private const val PRIMARY_PRICE_ELEMENT = "span[aria-hidden=true].promo-price[data-test=price]"
private const val FRACTION_PRICE_ELEMENT = "sup.promo-price__fraction[data-test=price-fraction]"

class BolPriceParser : PriceParser {

    override fun parse(document: Document): Either<ParseFailure, Price> {
        return either {
            val priceElement = document.select(PRIMARY_PRICE_ELEMENT).firstOrNull() ?: raise(
                FailedToFindPriceElement(
                    element = PRIMARY_PRICE_ELEMENT,
                    baseUri = document.baseUri()
                )
            )
            val fractionElement = priceElement.select(FRACTION_PRICE_ELEMENT).firstOrNull() ?: raise(
                FailedToFindPriceElement(
                    element = FRACTION_PRICE_ELEMENT,
                    baseUri = document.baseUri()
                )
            )

            val primaryPrice = priceElement.ownText().trim()
            val fractionPrice = if(fractionElement.text().trim() == "-") "00" else fractionElement.text().trim()

            Price.of("$primaryPrice.$fractionPrice").bind()
        }
    }
}
