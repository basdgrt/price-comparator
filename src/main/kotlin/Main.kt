package com.github.basdgrt

import com.github.basdgrt.comparator.Comparator
import com.github.basdgrt.products.products
import com.github.basdgrt.scraping.PriceScraper

fun main() {
    val priceScraper = PriceScraper()
    val comparator = Comparator()

    for (product in products) {
        val productPrices = priceScraper.scrape(product)
        val comparisonResult = comparator.compare(productPrices)

        println(comparisonResult)
        println()
    }
}
