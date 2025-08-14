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

private const val PRICE_ELEMENT = "span.css-ai40hw"
private const val TEST_URL = "https://www.coolblue.nl/product/123"

class CoolbluePriceParserTest {

    private lateinit var parser: CoolbluePriceParser
    private lateinit var document: Document
    private lateinit var priceElement: Element

    @BeforeEach
    fun setUp() {
        parser = CoolbluePriceParser()
        document = mockk<Document>()
        priceElement = mockk<Element>()

        every { document.baseUri() } returns TEST_URL
    }

    @Test
    fun `should parse price successfully when price element is present`() {
        setupPriceElement()
        setupPriceValue("250<!-- -->,-")

        val result = parser.parse(document)

        assertSuccessfulParsing(result, "250.00")
    }

    @Test
    fun `should return FailedToFindPriceElement when price element is missing`() {
        setupPriceElement(hasElement = false)

        val result = parser.parse(document)

        assertPriceElementFailure(result, PRICE_ELEMENT)
    }

    @Test
    fun `should parse price with different formatting`() {
        setupPriceElement()
        setupPriceValue("99<!-- -->,99")

        val result = parser.parse(document)

        assertSuccessfulParsing(result, "99.99")
    }

    @Test
    fun `should parse price without HTML comments`() {
        setupPriceElement()
        setupPriceValue("179,95")

        val result = parser.parse(document)

        assertSuccessfulParsing(result, "179.95")
    }

    @Test
    fun `should parse price with dash format`() {
        setupPriceElement()
        setupPriceValue("299<!-- -->,-")

        val result = parser.parse(document)

        assertSuccessfulParsing(result, "299.00")
    }

    @Test
    fun `should return InvalidNumber when price format is invalid`() {
        setupPriceElement()
        setupPriceValue("invalid<!-- -->price")

        val result = parser.parse(document)

        assertInvalidNumberFailure(result, "invalidprice")
    }

    @Test
    fun `should parse zero price successfully`() {
        setupPriceElement()
        setupPriceValue("0<!-- -->,-")

        val result = parser.parse(document)

        assertSuccessfulParsing(result, "0.00")
    }

    @Test
    fun `should parse large price successfully`() {
        setupPriceElement()
        setupPriceValue("9999<!-- -->,99")

        val result = parser.parse(document)

        assertSuccessfulParsing(result, "9999.99")
    }

    @Test
    fun `should handle whitespace in price elements`() {
        setupPriceElement()
        setupPriceValue(" 123<!-- -->,45 ")

        val result = parser.parse(document)

        assertSuccessfulParsing(result, "123.45")
    }

    private fun setupPriceElement(hasElement: Boolean = true) {
        val elements = if (hasElement) Elements(listOf(priceElement)) else Elements()
        every { document.select(PRICE_ELEMENT) } returns elements
    }

    private fun setupPriceValue(priceText: String) {
        every { priceElement.text() } returns priceText
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
                assertEquals(expectedInput, error.input)
            },
            { fail("Expected Left but got Right: $it") }
        )
    }
}