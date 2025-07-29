package com.github.basdgrt.scraping

import arrow.core.Either
import arrow.core.raise.either
import com.github.basdgrt.products.Price
import com.github.basdgrt.products.Webshop.VAN_ASTEN
import com.github.basdgrt.products.WebshopPrice
import com.github.basdgrt.scraping.ScrapeFailure.FailedToFindPriceElement
import org.jsoup.nodes.Document

private const val PRICE_ELEMENT = "span.price"

class VanAstenPriceParser : PriceParser {
    override fun parse(document: Document): Either<ScrapeFailure, WebshopPrice> {
        return either {
            val priceElement = document.select(PRICE_ELEMENT).firstOrNull() ?: raise(
                FailedToFindPriceElement(
                    element = PRICE_ELEMENT,
                    baseUri = document.baseUri()
                )
            )

            val priceText = priceElement.text().trim().replace(",", ".")

            WebshopPrice(
                webshop = VAN_ASTEN,
                price = Price.of(priceText).bind()
            )
        }
    }
}
