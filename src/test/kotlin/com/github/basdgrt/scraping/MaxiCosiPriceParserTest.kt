package com.github.basdgrt.scraping

import arrow.core.Either
import com.github.basdgrt.products.WebshopPrice
import io.mockk.every
import io.mockk.mockk
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal

private const val PRICE_ELEMENT = "span.price"
private const val TEST_URL = "https://www.maxi-cosi.nl/product/123"

class MaxiCosiPriceParserTest {

    private lateinit var parser: MaxiCosiPriceParser
    private lateinit var document: Document
    private lateinit var priceElement: Element

    @BeforeEach
    fun setUp() {
        parser = MaxiCosiPriceParser()
        document = mockk<Document>()
        priceElement = mockk<Element>()

        every { document.baseUri() } returns TEST_URL
    }

    @Test
    fun `should parse price successfully when price element is present`() {
        setupPriceElement()
        setupPriceValue("€ 149,99")

        val result = parser.parse(document)

        assertSuccessfulParsing(result, "149.99")
    }

    @Test
    fun `should return FailedToFindPriceElement when price element is missing`() {
        setupPriceElement(hasPriceElement = false)

        val result = parser.parse(document)

        assertPriceElementFailure(result, PRICE_ELEMENT)
    }

    @Test
    fun `should return InvalidNumber when price format is invalid`() {
        setupPriceElement()
        setupPriceValue("invalid price")

        val result = parser.parse(document)

        assertInvalidNumberFailure(result, "invalid price")
    }

    @Test
    fun `should parse zero price successfully`() {
        setupPriceElement()
        setupPriceValue("€ 0,00")

        val result = parser.parse(document)

        assertSuccessfulParsing(result, "0.00")
    }

    @Test
    fun `should parse large price successfully`() {
        setupPriceElement()
        setupPriceValue("€ 9999999,99")

        val result = parser.parse(document)

        assertSuccessfulParsing(result, "9999999.99")
    }

    @Test
    fun `should handle whitespace in price element`() {
        setupPriceElement()
        setupPriceValue(" € 123,45 ")

        val result = parser.parse(document)

        assertSuccessfulParsing(result, "123.45")
    }

    @Test
    fun `should handle price with dash notation`() {
        setupPriceElement()
        setupPriceValue("€ 199,-")

        val result = parser.parse(document)

        assertSuccessfulParsing(result, "199")
    }

    @Test
    fun `should handle price with non-breaking space`() {
        setupPriceElement()
        setupPriceValue("€&nbsp;299,99")

        val result = parser.parse(document)

        assertSuccessfulParsing(result, "299.99")
    }

    private fun setupPriceElement(hasPriceElement: Boolean = true) {
        val priceElements = if (hasPriceElement) Elements(listOf(priceElement)) else Elements()
        every { document.select(PRICE_ELEMENT) } returns priceElements
    }

    private fun setupPriceValue(price: String) {
        every { priceElement.text() } returns price
    }

    private fun assertSuccessfulParsing(result: Either<ScrapeFailure, WebshopPrice>, expectedPrice: String) {
        result.fold(
            { fail("Expected Right but got Left: $it") },
            { webshopPrice -> assertEquals(BigDecimal(expectedPrice), webshopPrice.price.value) }
        )
    }

    private fun assertPriceElementFailure(result: Either<ScrapeFailure, WebshopPrice>, expectedElement: String) {
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

    private fun assertInvalidNumberFailure(result: Either<ScrapeFailure, WebshopPrice>, expectedInput: String) {
        result.fold(
            { failure ->
                assertTrue(failure is ScrapeFailure.InvalidNumber)
                val error = failure as ScrapeFailure.InvalidNumber
                assertEquals(expectedInput.replace(",", "."), error.input)
            },
            { fail("Expected Left but got Right: $it") }
        )
    }
}