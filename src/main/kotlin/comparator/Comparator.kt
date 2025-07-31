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
            appendLine("Sites vergeleken: $successCount/$totalScrapes")
            appendLine("Oorspronkelijke prijs: ${productPrices.product.originalPrice}")

            cheapestPrice?.let { webshopPrice ->
                val cheapestValue = webshopPrice.price
                val originalPrice = productPrices.product.originalPrice

                if (cheapestValue < originalPrice) {
                    val difference = originalPrice - cheapestValue
                    appendLine("Lagere prijs gevonden bij: ${webshopPrice.productDetailPage.webshop}: ${webshopPrice.price}.")
                    appendLine("Verschil: $difference")
                    appendLine()
                    appendLine("Link: ${webshopPrice.productDetailPage.url}")
                } else {
                    appendLine("Geen lagere prijs gevonden.")
                }
            }

            if (failedScrapes.isNotEmpty()) {
                appendLine("\n Er zijn fouten opgetreden:")
                failedScrapes.forEach { scrape -> appendLine("- ${scrape.leftOrNull()?.errorMessage()}") }
            }
        }

        return resultBuilder
    }
}
