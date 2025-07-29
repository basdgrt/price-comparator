package com.github.basdgrt

import com.github.basdgrt.comparator.PriceComparator
import com.github.basdgrt.products.products
import com.github.basdgrt.scraping.PriceScraper

fun main() {

    // Create a price scraper and use it to scrape the price from the product URL
    val priceScraper = PriceScraper()
    val priceComparator = PriceComparator()

    products.forEach { product ->
        println("Comparing prices for: ${product.name}")
        println("Original price: ${product.originalPrice.value}")
        println("-".repeat(50))

        val results = priceScraper.scrape(product)
        val comparisonResult = priceComparator.compare(results)

        println(comparisonResult)
        println("=".repeat(50))
    }
}
