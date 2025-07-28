package com.github.basdgrt.scraping

import arrow.core.Either
import com.github.basdgrt.models.Price
import com.github.basdgrt.models.Webshop
import com.github.basdgrt.models.Webshop.*
import com.github.basdgrt.products.Product
import com.github.basdgrt.products.ProductDetailPage
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

private val log = KotlinLogging.logger {}

data class ProductPrices(val product: Product, val scrapeResults: List<ScrapeResult>)
data class ScrapeResult(val webshop: Webshop, val price: Either<ParseFailure, Price>)

class PriceScraper(
    private val bolParser: PriceParser = BolPriceParser(),
    private val babyParkParser: PriceParser = BabyParkPriceParser(),
    private val vanAstenPriceParser: PriceParser = VanAstenPriceParser()
) {

    fun scrape(product: Product): ProductPrices {
        log.info { "Finding prices for ${product.name}" }

        val scrapeResults = product.productDetailPages.map { detailPage ->
            val html = fetchHTMLDocument(detailPage)

            when (detailPage.webshop) {
                BOL -> ScrapeResult(BOL, bolParser.parse(html))
                BABY_PARK -> ScrapeResult(BABY_PARK, babyParkParser.parse(html))
                VAN_ASTEN -> ScrapeResult(VAN_ASTEN, vanAstenPriceParser.parse(html))
            }
        }

        return ProductPrices(
            product = product,
            scrapeResults = scrapeResults
        )
    }

    // Connect to the URL and get the HTML document
    // Set a user agent to avoid being blocked
    private fun fetchHTMLDocument(productDetailPage: ProductDetailPage): Document {
        return Jsoup.connect(productDetailPage.url)
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
            .timeout(10000)
            .get()
    }
}
