package com.github.basdgrt

import com.github.basdgrt.products.products
import com.github.basdgrt.scraping.PriceScraper

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {

    // Create a price scraper and use it to scrape the price from the product URL
    val priceScraper = PriceScraper()

    products.forEach { product ->
        val results = priceScraper.scrape(product)

        println(results)
    }
}
