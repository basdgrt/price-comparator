package com.github.basdgrt.scraping

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.math.BigDecimal

class PriceScraper(
    private val bolParser: PriceParser = BolPriceParser()
) {

    fun scrape(product: Product): BigDecimal {
        try {
            val document: Document = fetchHTMLDocument(product)

            return when (product.webshop) {
                Webshop.BOL -> bolParser.parse(document) ?: BigDecimal.ZERO
                Webshop.BABYPARK -> TODO()
            }
        } catch (e: Exception) {
            println(e)
            return BigDecimal.ZERO
        }
    }

    // Connect to the URL and get the HTML document
    // Set a user agent to avoid being blocked
    private fun fetchHTMLDocument(product: Product): Document {
        return Jsoup.connect(product.url)
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
            .timeout(10000)
            .get()
    }
}

