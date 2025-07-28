package com.github.basdgrt.scraping

import arrow.core.Either
import com.github.basdgrt.models.Price
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
private const val TEST_URL = "https://www.babypark.nl/product/123"

class BabyParkPriceParserTest {

    private lateinit var parser: BabyParkPriceParser
    private lateinit var document: Document
    private lateinit var priceElement: Element

    @BeforeEach
    fun setUp() {
        parser = BabyParkPriceParser()
        document = mockk<Document>()
        priceElement = mockk<Element>()

        every { document.baseUri() } returns TEST_URL
    }

    @Test
    fun `should parse price successfully when price element is present`() {
        setupPriceElement()
        setupPriceValue("149,99")

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
        setupPriceValue("0,00")

        val result = parser.parse(document)

        assertSuccessfulParsing(result, "0.00")
    }

    @Test
    fun `should parse large price successfully`() {
        setupPriceElement()
        setupPriceValue("9999999,99")

        val result = parser.parse(document)

        assertSuccessfulParsing(result, "9999999.99")
    }

    @Test
    fun `should handle whitespace in price element`() {
        setupPriceElement()
        setupPriceValue(" 123,45 ")

        val result = parser.parse(document)

        assertSuccessfulParsing(result, "123.45")
    }

    private fun setupPriceElement(hasPriceElement: Boolean = true) {
        val priceElements = if (hasPriceElement) Elements(listOf(priceElement)) else Elements()
        every { document.select(PRICE_ELEMENT) } returns priceElements
    }

    private fun setupPriceValue(price: String) {
        every { priceElement.text() } returns price
    }

    private fun assertSuccessfulParsing(result: Either<ParseFailure, Price>, expectedPrice: String) {
        result.fold(
            { fail("Expected Right but got Left: $it") },
            { price -> assertEquals(BigDecimal(expectedPrice), price.value) }
        )
    }

    private fun assertPriceElementFailure(result: Either<ParseFailure, Price>, expectedElement: String) {
        result.fold(
            { failure ->
                assertTrue(failure is ParseFailure.FailedToFindPriceElement)
                val error = failure as ParseFailure.FailedToFindPriceElement
                assertEquals(expectedElement, error.element)
                assertEquals(TEST_URL, error.baseUri)
            },
            { fail("Expected Left but got Right: $it") }
        )
    }

    private fun assertInvalidNumberFailure(result: Either<ParseFailure, Price>, expectedInput: String) {
        result.fold(
            { failure ->
                assertTrue(failure is ParseFailure.InvalidNumber)
                val error = failure as ParseFailure.InvalidNumber
                assertEquals(expectedInput.replace(",", "."), error.input)
            },
            { fail("Expected Left but got Right: $it") }
        )
    }
}