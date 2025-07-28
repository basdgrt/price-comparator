package com.github.basdgrt.scraping

import com.github.basdgrt.products.Product
import com.github.basdgrt.products.ProductDetailPage
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.math.BigDecimal

private val log = KotlinLogging.logger {}

class PriceScraper(
    private val bolParser: PriceParser = BolPriceParser(),
    private val babyParkParser: PriceParser = BabyParkPriceParser(),
    private val vanAstenPriceParser: PriceParser = VanAstenPriceParser()
) {

    fun scrape(product: Product) {
        log.info { "Finding prices for ${product.name}" }

        product.productDetailPages.forEach { detailPage ->
            val html = fetchHTMLDocument(detailPage)

           val result = when (detailPage.webshop) {
                Webshop.BOL -> bolParser.parse(html).getOrNull()?.value ?: BigDecimal.ZERO
                Webshop.BABY_PARK -> babyParkParser.parse(html).getOrNull()?.value ?: BigDecimal.ZERO
               Webshop.VAN_ASTEN -> vanAstenPriceParser.parse(html).getOrNull()?.value ?: BigDecimal.ZERO
           }

            log.info { "Scraped price from ${detailPage.webshop}: €$result" }
        }
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
