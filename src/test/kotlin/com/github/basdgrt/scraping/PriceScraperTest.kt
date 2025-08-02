package com.github.basdgrt.scraping

import arrow.core.Either
import com.github.basdgrt.products.Price
import com.github.basdgrt.products.Product
import com.github.basdgrt.products.ProductDetailPage
import com.github.basdgrt.products.ProductPrices
import com.github.basdgrt.products.Webshop
import com.github.basdgrt.products.WebshopPrice
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertIs

class PriceScraperTest {

    private val mockBolParser = mockk<PriceParser>()
    private val mockBabyParkParser = mockk<PriceParser>()
    private val mockBabyDumpParser = mockk<PriceParser>()
    private val mockVanAstenPriceParser = mockk<PriceParser>()
    private val mockMaxiCosiPriceParser = mockk<PriceParser>()
    private val mockPrenatalPriceParser = mockk<PriceParser>()
    private val mockLittleDutchPriceParser = mockk<PriceParser>()

    private val mockDocument = mockk<Document>()
    private val mockConnection = mockk<Connection>()

    @BeforeEach
    fun setUp() {
        mockkStatic(Jsoup::class)
        every { Jsoup.connect(any()) } returns mockConnection
        every { mockConnection.userAgent(any()) } returns mockConnection
        every { mockConnection.timeout(any()) } returns mockConnection
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(Jsoup::class)
    }

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
    fun `test scrape method processes multiple detail pages concurrently with mocked Jsoup`() = runBlocking {
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

        // Mock Jsoup.connect().get() to return the mock document
        every { mockConnection.get() } returns mockDocument

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

        // Verify Jsoup was called with the correct URLs
        verify(exactly = 3) { Jsoup.connect(any()) }
        verify(exactly = 3) { mockConnection.userAgent(any()) }
        verify(exactly = 3) { mockConnection.timeout(any()) }
        verify(exactly = 3) { mockConnection.get() }
    }

    @Test
    fun `test scrape method handles Jsoup connection errors`() = runBlocking {
        // Given
        val originalPriceEither = Price.of("100.00")
        val originalPrice = originalPriceEither.getOrNull()!!

        val bolDetailPage = ProductDetailPage(url = "https://www.bol.com/test", webshop = Webshop.BOL)

        val product = Product(
            name = "Test Product",
            productDetailPages = listOf(bolDetailPage),
            originalPrice = originalPrice
        )

        // Mock Jsoup.connect().get() to throw an exception
        every { mockConnection.get() } throws java.io.IOException("Connection error")

        // When
        val result = priceScraper.scrape(product)

        // Then
        assertEquals(product, result.product)
        assertEquals(1, result.webshopPrices.size)

        // The result should be a Left with JsoupConnectionError
        assertTrue(result.webshopPrices[0].isLeft())
        val failure = result.webshopPrices[0].leftOrNull()
        assertIs<ScrapeFailure.JsoupConnectionError>(failure)
        assertEquals(bolDetailPage.url, failure.baseUri)

        // Verify Jsoup was called with the correct URL
        verify { Jsoup.connect(bolDetailPage.url) }
        verify { mockConnection.userAgent(any()) }
        verify { mockConnection.timeout(any()) }
        verify { mockConnection.get() }

        // Verify that no parsers were called
        verify(exactly = 0) { mockBolParser.parse(any()) }
    }

    @Test
    fun `test scrape method handles parser failures`() = runBlocking {
        // Given
        val originalPriceEither = Price.of("100.00")
        val originalPrice = originalPriceEither.getOrNull()!!

        val bolDetailPage = ProductDetailPage(url = "https://www.bol.com/test", webshop = Webshop.BOL)
        val babyParkDetailPage = ProductDetailPage(url = "https://www.babypark.nl/test", webshop = Webshop.BABY_PARK)

        val product = Product(
            name = "Test Product",
            productDetailPages = listOf(bolDetailPage, babyParkDetailPage),
            originalPrice = originalPrice
        )

        // Mock Jsoup.connect().get() to return the mock document
        every { mockConnection.get() } returns mockDocument

        // Mock the parsers - one success, one failure
        val bolWebshopPrice = WebshopPrice(
            productDetailPage = bolDetailPage,
            price = Price.of("90.00").getOrNull()!!
        )

        val parserFailure = ScrapeFailure.FailedToFindPriceElement(
            webshop = Webshop.BABY_PARK,
            element = "span.price",
            baseUri = babyParkDetailPage.url
        )

        every { mockBolParser.parse(mockDocument) } returns Either.Right(bolWebshopPrice)
        every { mockBabyParkParser.parse(mockDocument) } returns Either.Left(parserFailure)

        // When
        val result = priceScraper.scrape(product)

        // Then
        assertEquals(product, result.product)
        assertEquals(2, result.webshopPrices.size)

        // First result should be success
        assertTrue(result.webshopPrices[0].isRight())
        assertEquals(bolWebshopPrice, result.webshopPrices[0].getOrNull())

        // Second result should be failure
        assertTrue(result.webshopPrices[1].isLeft())
        assertEquals(parserFailure, result.webshopPrices[1].leftOrNull())

        // Verify that all parsers were called
        verify { mockBolParser.parse(mockDocument) }
        verify { mockBabyParkParser.parse(mockDocument) }

        // Verify Jsoup was called with the correct URLs
        verify(exactly = 2) { Jsoup.connect(any()) }
        verify(exactly = 2) { mockConnection.userAgent(any()) }
        verify(exactly = 2) { mockConnection.timeout(any()) }
        verify(exactly = 2) { mockConnection.get() }
    }

    @Test
    fun `test scrape method processes all remaining webshop types`() = runBlocking {
        // Given
        val originalPriceEither = Price.of("100.00")
        val originalPrice = originalPriceEither.getOrNull()!!

        val vanAstenDetailPage = ProductDetailPage(url = "https://www.vanastenbabysuperstore.nl/test", webshop = Webshop.VAN_ASTEN)
        val maxiCosiDetailPage = ProductDetailPage(url = "https://www.maxi-cosi.nl/test", webshop = Webshop.MAXI_COSI)
        val prenatalDetailPage = ProductDetailPage(url = "https://www.prenatal.nl/test", webshop = Webshop.PRENATAL)
        val littleDutchDetailPage = ProductDetailPage(url = "https://www.little-dutch.com/test", webshop = Webshop.LITTLE_DUTCH)

        val product = Product(
            name = "Test Product",
            productDetailPages = listOf(
                vanAstenDetailPage,
                maxiCosiDetailPage,
                prenatalDetailPage,
                littleDutchDetailPage
            ),
            originalPrice = originalPrice
        )

        // Mock Jsoup.connect().get() to return the mock document
        every { mockConnection.get() } returns mockDocument

        // Create prices for each webshop
        val vanAstenPriceEither = Price.of("90.00")
        val maxiCosiPriceEither = Price.of("95.00")
        val prenatalPriceEither = Price.of("85.00")
        val littleDutchPriceEither = Price.of("80.00")

        // Create WebshopPrice instances
        val vanAstenWebshopPrice = WebshopPrice(
            productDetailPage = vanAstenDetailPage,
            price = vanAstenPriceEither.getOrNull()!!
        )

        val maxiCosiWebshopPrice = WebshopPrice(
            productDetailPage = maxiCosiDetailPage,
            price = maxiCosiPriceEither.getOrNull()!!
        )

        val prenatalWebshopPrice = WebshopPrice(
            productDetailPage = prenatalDetailPage,
            price = prenatalPriceEither.getOrNull()!!
        )

        val littleDutchWebshopPrice = WebshopPrice(
            productDetailPage = littleDutchDetailPage,
            price = littleDutchPriceEither.getOrNull()!!
        )

        // Mock the parsers to return a success with a WebshopPrice
        every { mockVanAstenPriceParser.parse(mockDocument) } returns Either.Right(vanAstenWebshopPrice)
        every { mockMaxiCosiPriceParser.parse(mockDocument) } returns Either.Right(maxiCosiWebshopPrice)
        every { mockPrenatalPriceParser.parse(mockDocument) } returns Either.Right(prenatalWebshopPrice)
        every { mockLittleDutchPriceParser.parse(mockDocument) } returns Either.Right(littleDutchWebshopPrice)

        // When
        val result = priceScraper.scrape(product)

        // Then
        assertEquals(product, result.product)
        assertEquals(4, result.webshopPrices.size)

        // Verify that all parsers were called
        verify { mockVanAstenPriceParser.parse(mockDocument) }
        verify { mockMaxiCosiPriceParser.parse(mockDocument) }
        verify { mockPrenatalPriceParser.parse(mockDocument) }
        verify { mockLittleDutchPriceParser.parse(mockDocument) }

        // Verify Jsoup was called with the correct URLs
        verify(exactly = 4) { Jsoup.connect(any()) }
        verify(exactly = 4) { mockConnection.userAgent(any()) }
        verify(exactly = 4) { mockConnection.timeout(any()) }
        verify(exactly = 4) { mockConnection.get() }
    }

    @Test
    fun `test scrape method handles empty product detail pages list`() = runBlocking {
        // Given
        val originalPriceEither = Price.of("100.00")
        val originalPrice = originalPriceEither.getOrNull()!!

        val product = Product(
            name = "Test Product",
            productDetailPages = emptyList(),
            originalPrice = originalPrice
        )

        // When
        val result = priceScraper.scrape(product)

        // Then
        assertEquals(product, result.product)
        assertEquals(0, result.webshopPrices.size)

        // Verify that no parsers were called
        verify(exactly = 0) { mockBolParser.parse(any()) }
        verify(exactly = 0) { mockBabyParkParser.parse(any()) }
        verify(exactly = 0) { mockBabyDumpParser.parse(any()) }
        verify(exactly = 0) { mockVanAstenPriceParser.parse(any()) }
        verify(exactly = 0) { mockMaxiCosiPriceParser.parse(any()) }
        verify(exactly = 0) { mockPrenatalPriceParser.parse(any()) }
        verify(exactly = 0) { mockLittleDutchPriceParser.parse(any()) }

        // Verify Jsoup was not called
        verify(exactly = 0) { Jsoup.connect(any()) }
    }
}
