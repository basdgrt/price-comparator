package com.github.basdgrt.scraping

import arrow.core.Either
import arrow.core.flatMap
import com.github.basdgrt.products.Product
import com.github.basdgrt.products.ProductDetailPage
import com.github.basdgrt.products.ProductPrices
import com.github.basdgrt.products.Webshop.*
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

private val log = KotlinLogging.logger {}

class PriceScraper(
    private val bolParser: PriceParser = BolPriceParser(),
    private val babyParkParser: PriceParser = BabyParkPriceParser(),
    private val vanAstenPriceParser: PriceParser = VanAstenPriceParser(),
    private val maxiCosiPriceParser: PriceParser = MaxiCosiPriceParser(),
    private val prenatalPriceParser: PriceParser = PrenatalPriceParser(),
    private val littleDutchPriceParser: PriceParser = LittleDutchPriceParser(),
) {

    fun scrape(product: Product): ProductPrices {
        log.info { "Finding prices for ${product.name}" }

        val webshopPrices = product.productDetailPages.map { detailPage ->
            fetchHTMLDocument(detailPage).flatMap { html ->
                when (detailPage.webshop) {
                    BOL -> bolParser.parse(html)
                    BABY_PARK -> babyParkParser.parse(html)
                    VAN_ASTEN -> vanAstenPriceParser.parse(html)
                    MAXI_COSI -> maxiCosiPriceParser.parse(html)
                    PRENATAL -> prenatalPriceParser.parse(html)
                    LITTLE_DUTCH -> littleDutchPriceParser.parse(html)
                }
            }
        }

        return ProductPrices(
            product = product,
            webshopPrices = webshopPrices
        )
    }

    // Connect to the URL and get the HTML document
    // Set a user agent to avoid being blocked
    private fun fetchHTMLDocument(productDetailPage: ProductDetailPage): Either<ScrapeFailure, Document> {
        return Either.catch {
            Jsoup.connect(productDetailPage.url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .timeout(10000)
                .get()
        }.mapLeft { ScrapeFailure.JsoupConnectionError(productDetailPage.url) }
    }
}
