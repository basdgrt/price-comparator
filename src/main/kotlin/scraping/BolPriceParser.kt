package com.github.basdgrt.scraping

import org.jsoup.nodes.Document
import java.math.BigDecimal

class BolPriceParser : PriceParser {
    override fun parse(document: Document): BigDecimal? {
        val priceElement = document.select("span[aria-hidden=true].promo-price[data-test=price]").firstOrNull() ?: return null
        val fractionElement = priceElement.select("sup.promo-price__fraction[data-test=price-fraction]").firstOrNull() ?: return null

        val mainPrice = priceElement.ownText().trim()
        val fractionPrice = fractionElement.text().trim()

        return try {
            BigDecimal("$mainPrice.$fractionPrice")
        } catch (e: Exception) {
            null
        }
    }
}
