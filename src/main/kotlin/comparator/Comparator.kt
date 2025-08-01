package com.github.basdgrt.comparator

import com.github.basdgrt.products.ProductPrices

class Comparator {

    fun compare(productPrices: ProductPrices): String {

        val totalScrapes = productPrices.webshopPrices.size
        val successfulScrapes = productPrices.webshopPrices.filter { it.isRight() }
        val failedScrapes = productPrices.webshopPrices.filter { it.isLeft() }
        val successCount = successfulScrapes.size

        val cheapestPrice = successfulScrapes
            .mapNotNull { it.getOrNull() }
            .minByOrNull { it.price }

        val resultBuilder = buildString {

            appendLine(productPrices.product.name)
            appendLine("Original price: ${productPrices.product.originalPrice}")
            appendLine("Sites checked: $totalScrapes/$successCount")

            cheapestPrice?.let { webshopPrice ->
                val cheapestValue = webshopPrice.price
                val originalPrice = productPrices.product.originalPrice

                if (cheapestValue < originalPrice) {
                    val difference = originalPrice - cheapestValue
                    appendLine("Cheaper price found at ${webshopPrice.productDetailPage.webshop.name}: ${webshopPrice.price}.")
                    appendLine("Difference: $difference")
                    appendLine()
                    appendLine("Product detail page: ${webshopPrice.productDetailPage.url}")
                } else {
                    appendLine("No cheaper prices found.")
                }
            }

            if (failedScrapes.isNotEmpty()) {
                appendLine("\n Scrapes failed for:")
                failedScrapes.forEach { scrape -> appendLine("${scrape.leftOrNull()?.errorMessage()}") }
            }
        }

        return resultBuilder
    }
}
