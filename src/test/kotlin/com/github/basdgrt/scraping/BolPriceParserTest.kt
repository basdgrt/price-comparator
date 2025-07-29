package com.github.basdgrt.scraping

import arrow.core.Either
import com.github.basdgrt.products.Price
import io.mockk.every
import io.mockk.mockk
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal

private const val PRIMARY_PRICE_ELEMENT = "span[aria-hidden=true].promo-price[data-test=price]"
private const val FRACTION_PRICE_ELEMENT = "sup.promo-price__fraction[data-test=price-fraction]"
private const val TEST_URL = "https://www.bol.com/product/123"

class BolPriceParserTest {

    private lateinit var parser: BolPriceParser
    private lateinit var document: Document
    private lateinit var priceElement: Element
    private lateinit var fractionElement: Element

    @BeforeEach
    fun setUp() {
        parser = BolPriceParser()
        document = mockk<Document>()
        priceElement = mockk<Element>()
        fractionElement = mockk<Element>()

        every { document.baseUri() } returns TEST_URL
    }

    @Test
    fun `should parse price successfully when all elements are present`() {
        setupPriceElements()
        setupPriceValues("179", "99")

        val result = parser.parse(document)

        assertSuccessfulParsing(result, "179.99")
    }

    @Test
    fun `should return FailedToFindPriceElement when primary price element is missing`() {
        setupPriceElements(hasPrimaryElement = false)

        val result = parser.parse(document)

        assertPriceElementFailure(result, PRIMARY_PRICE_ELEMENT)
    }

    @Test
    fun `should return FailedToFindPriceElement when fraction element is missing`() {
        setupPriceElements(hasFractionElement = false)

        val result = parser.parse(document)

        assertPriceElementFailure(result, FRACTION_PRICE_ELEMENT)
    }

    @Test
    fun `should return InvalidNumber when price format is invalid`() {
        setupPriceElements()
        setupPriceValues("invalid", "price")

        val result = parser.parse(document)

        assertInvalidNumberFailure(result, "invalid.price")
    }

    @Test
    fun `should parse zero price successfully`() {
        setupPriceElements()
        setupPriceValues("0", "00")

        val result = parser.parse(document)

        assertSuccessfulParsing(result, "0.00")
    }

    @Test
    fun `should parse large price successfully`() {
        setupPriceElements()
        setupPriceValues("9999999", "99")

        val result = parser.parse(document)

        assertSuccessfulParsing(result, "9999999.99")
    }

    @Test
    fun `should handle whitespace in price elements`() {
        setupPriceElements()
        setupPriceValues(" 123 ", " 45 ")

        val result = parser.parse(document)

        assertSuccessfulParsing(result, "123.45")
    }

    private fun setupPriceElements(hasPrimaryElement: Boolean = true, hasFractionElement: Boolean = true) {
        val priceElements = if (hasPrimaryElement) Elements(listOf(priceElement)) else Elements()
        val fractionElements = if (hasFractionElement) Elements(listOf(fractionElement)) else Elements()

        every { document.select(PRIMARY_PRICE_ELEMENT) } returns priceElements
        if (hasPrimaryElement) {
            every { priceElement.select(FRACTION_PRICE_ELEMENT) } returns fractionElements
        }
    }

    private fun setupPriceValues(mainPrice: String, fractionPrice: String) {
        every { priceElement.ownText() } returns mainPrice
        every { fractionElement.text() } returns fractionPrice
    }

    private fun assertSuccessfulParsing(result: Either<ScrapeFailure, Price>, expectedPrice: String) {
        result.fold(
            { fail("Expected Right but got Left: $it") },
            { price -> assertEquals(BigDecimal(expectedPrice), price.value) }
        )
    }

    private fun assertPriceElementFailure(result: Either<ScrapeFailure, Price>, expectedElement: String) {
        result.fold(
            { failure ->
                assertTrue(failure is ScrapeFailure.FailedToFindPriceElement)
                val error = failure as ScrapeFailure.FailedToFindPriceElement
                assertEquals(expectedElement, error.element)
                assertEquals(TEST_URL, error.baseUri)
            },
            { fail("Expected Left but got Right: $it") }
        )
    }

    private fun assertInvalidNumberFailure(result: Either<ScrapeFailure, Price>, expectedInput: String) {
        result.fold(
            { failure ->
                assertTrue(failure is ScrapeFailure.InvalidNumber)
                val error = failure as ScrapeFailure.InvalidNumber
                assertEquals(expectedInput, error.input)
            },
            { fail("Expected Left but got Right: $it") }
        )
    }
}
