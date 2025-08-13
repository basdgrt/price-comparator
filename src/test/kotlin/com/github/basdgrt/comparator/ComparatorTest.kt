package com.github.basdgrt.comparator

import arrow.core.left
import arrow.core.right
import com.github.basdgrt.products.*
import com.github.basdgrt.scraping.ScrapeFailure
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ComparatorTest {

    private lateinit var comparator: Comparator
    private lateinit var product: Product
    private var originalPrice: Price? = null

    @BeforeEach
    fun setUp() {
        comparator = Comparator()
        originalPrice = Price.of("100.00").getOrNull()!!
        product = Product(
            name = "Test Product",
            productDetailPages = listOf(
                ProductDetailPage(url = "https://www.bol.com/test"),
                ProductDetailPage(url = "https://www.babypark.nl/test"),
                ProductDetailPage(url = "https://www.vanastenbabysuperstore.nl/test")
            ),
            originalPrice = originalPrice!!
        )
    }

    @Test
    fun `should return formatted result when cheaper price is found`() {
        // Given
        val bolPrice = createWebshopPrice(Webshop.BOL, "90.00")
        val babyParkPrice = createWebshopPrice(Webshop.BABY_PARK, "95.00")
        val vanAstenPrice = createWebshopPrice(Webshop.VAN_ASTEN, "85.00") // Cheapest

        val productPrices = ProductPrices(
            product = product,
            webshopPrices = listOf(
                bolPrice.right(),
                babyParkPrice.right(),
                vanAstenPrice.right()
            )
        )

        // When
        val result = comparator.compare(productPrices)

        // Then
        val expectedResult = """
            Test Product
            Originele prijs: €100.00
            Sites vergeleken: 3/3
            Goedkoper gevonden op: VAN_ASTEN: €85.00.
            Verschil: €15.00

            Product detail pagina: https://www.vanastenbabysuperstore.nl/test
            """.trimIndent()

        assertEquals(expectedResult, result.trim())
    }

    @Test
    fun `should indicate no cheaper prices when all prices are higher than original`() {
        // Given
        val bolPrice = createWebshopPrice(Webshop.BOL, "110.00")
        val babyParkPrice = createWebshopPrice(Webshop.BABY_PARK, "105.00")
        val vanAstenPrice = createWebshopPrice(Webshop.VAN_ASTEN, "115.00")

        val productPrices = ProductPrices(
            product = product,
            webshopPrices = listOf(
                bolPrice.right(),
                babyParkPrice.right(),
                vanAstenPrice.right()
            )
        )

        // When
        val result = comparator.compare(productPrices)

        // Then
        val expectedResult = """
            Test Product
            Originele prijs: €100.00
            Sites vergeleken: 3/3
            Geen goedkopere opties gevonden.
            """.trimIndent()

        assertEquals(expectedResult, result.trim())
    }

    @Test
    fun `should indicate no cheaper prices when all prices are equal to original`() {
        // Given
        val bolPrice = createWebshopPrice(Webshop.BOL, "100.00")
        val babyParkPrice = createWebshopPrice(Webshop.BABY_PARK, "100.00")
        val vanAstenPrice = createWebshopPrice(Webshop.VAN_ASTEN, "100.00")

        val productPrices = ProductPrices(
            product = product,
            webshopPrices = listOf(
                bolPrice.right(),
                babyParkPrice.right(),
                vanAstenPrice.right()
            )
        )

        // When
        val result = comparator.compare(productPrices)

        // Then
        val expectedResult = """
            Test Product
            Originele prijs: €100.00
            Sites vergeleken: 3/3
            Geen goedkopere opties gevonden.
            """.trimIndent()

        assertEquals(expectedResult, result.trim())
    }

    @Test
    fun `should handle some failed scrapes and still find cheaper price`() {
        // Given
        val bolPrice = createWebshopPrice(Webshop.BOL, "90.00")
        val babyParkFailure = ScrapeFailure.FailedToFindPriceElement(
            webshop = Webshop.BABY_PARK,
            element = "price",
            baseUri = "https://www.babypark.nl/test"
        )
        val vanAstenPrice = createWebshopPrice(Webshop.VAN_ASTEN, "95.00")

        val productPrices = ProductPrices(
            product = product,
            webshopPrices = listOf(
                bolPrice.right(),
                babyParkFailure.left(),
                vanAstenPrice.right()
            )
        )

        // When
        val result = comparator.compare(productPrices)

        // Then
        val expectedResult = """
            Test Product
            Originele prijs: €100.00
            Sites vergeleken: 2/3
            Goedkoper gevonden op: BOL: €90.00.
            Verschil: €10.00

            Product detail pagina: https://www.bol.com/test

             Ophalen van prijzen mislukt:
            Failed to find price element on BABY_PARK
            """.trimIndent()

        assertEquals(expectedResult, result.trim())
    }

    @Test
    fun `should handle all failed scrapes`() {
        // Given
        val bolFailure = ScrapeFailure.FailedToFindPriceElement(
            webshop = Webshop.BOL,
            element = "price",
            baseUri = "https://www.bol.com/test"
        )
        val babyParkFailure = ScrapeFailure.InvalidNumber(
            input = "invalid",
            errorMessage = "Failed to parse"
        )
        val vanAstenFailure = ScrapeFailure.FailedToFindPriceElement(
            webshop = Webshop.VAN_ASTEN,
            element = "price",
            baseUri = "https://www.vanastenbabysuperstore.nl/test"
        )

        val productPrices = ProductPrices(
            product = product,
            webshopPrices = listOf(
                bolFailure.left(),
                babyParkFailure.left(),
                vanAstenFailure.left()
            )
        )

        // When
        val result = comparator.compare(productPrices)

        // Then
        val expectedResult = """
            Test Product
            Originele prijs: €100.00
            Sites vergeleken: 0/3

             Ophalen van prijzen mislukt:
            Failed to find price element on BOL
            Failed to parse price: invalid
            Failed to find price element on VAN_ASTEN
            """.trimIndent()

        assertEquals(expectedResult, result.trim())
    }

    @Test
    fun `should handle empty webshop prices list`() {
        // Given
        val productPrices = ProductPrices(
            product = product,
            webshopPrices = emptyList()
        )

        // When
        val result = comparator.compare(productPrices)

        // Then
        val expectedResult = """
            Test Product
            Originele prijs: €100.00
            Sites vergeleken: 0/0
            """.trimIndent()

        assertEquals(expectedResult, result.trim())
    }

    private fun createWebshopPrice(webshop: Webshop, priceValue: String): WebshopPrice {
        val price = Price.of(priceValue).getOrNull()!!
        val productDetailPage = product.productDetailPages.first { it.webshop == webshop }
        return WebshopPrice(productDetailPage, price)
    }
}
