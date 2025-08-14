package com.github.basdgrt.scraping

import arrow.core.Either
import arrow.core.raise.either
import com.github.basdgrt.products.Price
import com.github.basdgrt.products.ProductDetailPage
import com.github.basdgrt.products.Webshop.*
import com.github.basdgrt.products.WebshopPrice
import com.github.basdgrt.scraping.ScrapeFailure.FailedToFindPriceElement
import org.jsoup.nodes.Document

private const val PRICE_ELEMENT = "span.price-wrapper"

class BabyDumpPriceParser : PriceParser {
    override fun parse(document: Document): Either<ScrapeFailure, WebshopPrice> {
        return either {
            val priceElement = document.select(PRICE_ELEMENT).firstOrNull() ?: raise(
                FailedToFindPriceElement(
                    webshop = BABY_DUMP,
                    element = PRICE_ELEMENT,
                    baseUri = document.baseUri()
                )
            )

            val priceText = priceElement.attr("data-price-amount").trim()
                .replace(",", ".")

            WebshopPrice(
                productDetailPage = ProductDetailPage(document.baseUri()),
                price = Price.of(priceText).bind()
            )
        }
    }
}
