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
            appendLine("Originele prijs: ${productPrices.product.originalPrice}")
            appendLine("Sites vergeleken: $successCount/$totalScrapes")

            cheapestPrice?.let { webshopPrice ->
                val cheapestValue = webshopPrice.price
                val originalPrice = productPrices.product.originalPrice

                if (cheapestValue < originalPrice) {
                    val difference = originalPrice - cheapestValue
                    appendLine("Goedkoper gevonden op: ${webshopPrice.productDetailPage.webshop.name}: ${webshopPrice.price}.")
                    appendLine("Verschil: $difference")
                    appendLine()
                    appendLine("Product detail pagina: ${webshopPrice.productDetailPage.url}")
                } else {
                    appendLine("Geen goedkopere opties gevonden.")
                }
            }

            if (failedScrapes.isNotEmpty()) {
                appendLine("\n Ophalen van prijzen mislukt:")
                failedScrapes.forEach { scrape -> appendLine("${scrape.leftOrNull()?.errorMessage()}") }
            }
        }

        return resultBuilder
    }
}
