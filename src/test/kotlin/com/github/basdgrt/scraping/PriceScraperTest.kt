package com.github.basdgrt.scraping

import arrow.core.Either
import com.github.basdgrt.products.Price
import com.github.basdgrt.products.Product
import com.github.basdgrt.products.ProductDetailPage
import com.github.basdgrt.products.ProductPrices
import com.github.basdgrt.products.Webshop
import com.github.basdgrt.products.WebshopPrice
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.jsoup.nodes.Document
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PriceScraperTest {

    private val mockBolParser = mockk<PriceParser>()
    private val mockBabyParkParser = mockk<PriceParser>()
    private val mockBabyDumpParser = mockk<PriceParser>()
    private val mockVanAstenPriceParser = mockk<PriceParser>()
    private val mockMaxiCosiPriceParser = mockk<PriceParser>()
    private val mockPrenatalPriceParser = mockk<PriceParser>()
    private val mockLittleDutchPriceParser = mockk<PriceParser>()

    private val mockDocument = mockk<Document>()

    private val priceScraper = spyk(
        PriceScraper(
            bolParser = mockBolParser,
            babyParkParser = mockBabyParkParser,
            babyDumpParser = mockBabyDumpParser,
            vanAstenPriceParser = mockVanAstenPriceParser,
            maxiCosiPriceParser = mockMaxiCosiPriceParser,
            prenatalPriceParser = mockPrenatalPriceParser,
            littleDutchPriceParser = mockLittleDutchPriceParser
        )
    )

    @Test
    fun `test scrape method processes multiple detail pages concurrently`() = runBlocking {
        // Given
        // Create original price
        val originalPriceEither = Price.of("100.00")
        val originalPrice = originalPriceEither.getOrNull()!!

        val bolDetailPage = ProductDetailPage(url = "https://www.bol.com/test", webshop = Webshop.BOL)
        val babyParkDetailPage = ProductDetailPage(url = "https://www.babypark.nl/test", webshop = Webshop.BABY_PARK)
        val babyDumpDetailPage = ProductDetailPage(url = "https://www.baby-dump.nl/test", webshop = Webshop.BABY_DUMP)

        val product = Product(
            name = "Test Product",
            productDetailPages = listOf(
                bolDetailPage,
                babyParkDetailPage,
                babyDumpDetailPage
            ),
            originalPrice = originalPrice
        )

        // Mock fetchHTMLDocument to return a success with the mock document
        coEvery { priceScraper["fetchHTMLDocument"](any<ProductDetailPage>()) } returns Either.Right(mockDocument)

        // Create prices for each webshop
        val bolPriceEither = Price.of("90.00")
        val babyParkPriceEither = Price.of("95.00")
        val babyDumpPriceEither = Price.of("85.00")

        // Create WebshopPrice instances
        val bolWebshopPrice = WebshopPrice(
            productDetailPage = bolDetailPage,
            price = bolPriceEither.getOrNull()!!
        )

        val babyParkWebshopPrice = WebshopPrice(
            productDetailPage = babyParkDetailPage,
            price = babyParkPriceEither.getOrNull()!!
        )

        val babyDumpWebshopPrice = WebshopPrice(
            productDetailPage = babyDumpDetailPage,
            price = babyDumpPriceEither.getOrNull()!!
        )

        // Mock the parsers to return a success with a WebshopPrice
        every { mockBolParser.parse(mockDocument) } returns Either.Right(bolWebshopPrice)
        every { mockBabyParkParser.parse(mockDocument) } returns Either.Right(babyParkWebshopPrice)
        every { mockBabyDumpParser.parse(mockDocument) } returns Either.Right(babyDumpWebshopPrice)

        // When
        val result = priceScraper.scrape(product)

        // Then
        assertEquals(product, result.product)
        assertEquals(3, result.webshopPrices.size)

        // Verify that all parsers were called
        verify { mockBolParser.parse(mockDocument) }
        verify { mockBabyParkParser.parse(mockDocument) }
        verify { mockBabyDumpParser.parse(mockDocument) }
    }
}
