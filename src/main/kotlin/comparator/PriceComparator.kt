package com.github.basdgrt.comparator

import com.github.basdgrt.models.Price
import com.github.basdgrt.products.ProductDetailPage
import com.github.basdgrt.scraping.ProductPrices
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

class PriceComparator {

    fun compare(productPrices: ProductPrices): String {
        val product = productPrices.product
        val originalPrice = product.originalPrice
        val scrapeResults = productPrices.scrapeResults

        val successfulScrapes = scrapeResults.count { it.price.isRight() }
        val totalScrapes = scrapeResults.size

        val scrapeStatusMessage = if (successfulScrapes == totalScrapes) {
            "All scrapes were successful (${successfulScrapes}/${totalScrapes})."
        } else {
            "Some scrapes failed (${successfulScrapes}/${totalScrapes} successful)."
        }

        // Find prices lower than the original price
        val lowerPrices = scrapeResults
            .filter { it.price.isRight() }
            .mapNotNull { result ->
                result.price.fold(
                    { null }, // Skip failures
                    { price ->
                        if (price.value < originalPrice.value) {
                            // Find the URL for this webshop
                            val detailPage = product.productDetailPages.find { it.webshop == result.webshop }
                            val priceDifference = originalPrice.value - price.value

                            if (detailPage != null) {
                                LowerPrice(detailPage, price, priceDifference)
                            } else {
                                null
                            }
                        } else {
                            null
                        }
                    }
                )
            }
            .sortedByDescending { it.priceDifference } // Sort by biggest discount first

        val priceComparisonMessage = if (lowerPrices.isEmpty()) {
            "There are no cheaper options available at this point."
        } else {
            val formatter = NumberFormat.getCurrencyInstance(Locale.US)

            buildString {
                append("Found ${lowerPrices.size} lower price(s):\n")
                lowerPrices.forEachIndexed { index, lowerPrice ->
                    val formattedPrice = formatter.format(lowerPrice.price.value)
                    val formattedDifference = formatter.format(lowerPrice.priceDifference)
                    val percentageDifference = (lowerPrice.priceDifference * BigDecimal(100)) / originalPrice.value
                    val formattedPercentage = "%.1f".format(percentageDifference)

                    append("${index + 1}. ${formattedPrice} at ${lowerPrice.detailPage.url}")
                    append(" (${formattedDifference} / ${formattedPercentage}% cheaper)")
                    if (index < lowerPrices.size - 1) {
                        append("\n")
                    }
                }
            }
        }

        return "$scrapeStatusMessage\n$priceComparisonMessage"
    }

    private data class LowerPrice(
        val detailPage: ProductDetailPage,
        val price: Price,
        val priceDifference: BigDecimal
    )
}
