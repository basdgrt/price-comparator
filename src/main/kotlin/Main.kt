package com.github.basdgrt

import com.github.basdgrt.scraping.PriceScraper
import com.github.basdgrt.scraping.Product

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {
    val product = Product(
        name = "The Let Them Theory",
        url = "https://www.bol.com/nl/nl/p/titaniumbaby-vidar-zwart-100-150-cm-i-size-autostoel-tb-5907/9300000147972271/?cid=1753696388258-7772181719494&bltgh=l4GZIJ2XdPEBP3pHKI1D6A.4_76.80.ProductImage"
    )

    // Create a price scraper and use it to scrape the price from the product URL
    val priceScraper = PriceScraper()
    val price = priceScraper.scrape(product)

    // Print the raw price to the console
    println("Raw price for ${product.name}: $price")
}
