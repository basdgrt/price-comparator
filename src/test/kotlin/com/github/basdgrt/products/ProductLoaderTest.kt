package com.github.basdgrt.products

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.FileNotFoundException

class ProductLoaderTest {

    private val testProductsFilePath = "src/test/resources/test-products.yaml"
    private val testProductsClasspathFile = "/test-products.yaml"

    @Test
    fun `loadProducts should load products from test yaml file`() {
        // When
        val products = ProductLoader.loadProducts(
            filePath = testProductsFilePath,
            classpathFile = testProductsClasspathFile
        )

        // Then
        assertEquals(3, products.size, "Should load 3 products from test file")

        // Verify first product
        val product1 = products[0]
        assertEquals("Test Product 1", product1.name)
        assertEquals(Price.of("99.99").getOrNull(), product1.originalPrice)
        assertEquals(2, product1.productDetailPages.size)
        assertEquals("https://www.bol.com/nl/nl/p/test-product-1/123456789", product1.productDetailPages[0].url)
        assertEquals(Webshop.BOL, product1.productDetailPages[0].webshop)
        assertEquals("https://www.babypark.nl/test-product-1.html", product1.productDetailPages[1].url)
        assertEquals(Webshop.BABY_PARK, product1.productDetailPages[1].webshop)

        // Verify second product
        val product2 = products[1]
        assertEquals("Test Product 2", product2.name)
        assertEquals(Price.of("49.95").getOrNull(), product2.originalPrice)
        assertEquals(2, product2.productDetailPages.size)
        assertEquals("https://www.baby-dump.nl/test-product-2.html", product2.productDetailPages[0].url)
        assertEquals(Webshop.BABY_DUMP, product2.productDetailPages[0].webshop)
        assertEquals("https://www.bol.com/nl/nl/p/test-product-2/123456789", product2.productDetailPages[1].url)
        assertEquals(Webshop.BOL, product2.productDetailPages[1].webshop)

        // Verify third product
        val product3 = products[2]
        assertEquals("Test Product 3", product3.name)
        assertEquals(Price.of("199.00").getOrNull(), product3.originalPrice)
        assertEquals(3, product3.productDetailPages.size)
        assertEquals("https://www.baby-dump.nl/test-product-3", product3.productDetailPages[0].url)
        assertEquals(Webshop.BABY_DUMP, product3.productDetailPages[0].webshop)
        assertEquals("https://www.babypark.nl/test-product-3", product3.productDetailPages[1].url)
        assertEquals(Webshop.BABY_PARK, product3.productDetailPages[1].webshop)
        assertEquals("https://www.vanastenbabysuperstore.nl/test-product-3", product3.productDetailPages[2].url)
        assertEquals(Webshop.VAN_ASTEN, product3.productDetailPages[2].webshop)
    }

    @Test
    fun `loadProducts should throw FileNotFoundException when file does not exist`() {
        // Given
        val nonExistentFilePath = "src/test/resources/non-existent-file.yaml"
        val nonExistentClasspathFile = "/non-existent-file.yaml"

        // When/Then
        assertThrows<FileNotFoundException> {
            ProductLoader.loadProducts(
                filePath = nonExistentFilePath,
                classpathFile = nonExistentClasspathFile
            )
        }
    }

    @Test
    fun `loadProducts should throw IllegalArgumentException when price is invalid`() {
        // Given
        val invalidPriceYaml = """
            - name: "Invalid Price Product"
              originalPrice: "invalid-price"
              urls:
                - "https://www.example.com/invalid-price-product"
        """.trimIndent()

        // Create a temporary file with invalid price
        val tempFile = createTempFile(prefix = "invalid-price", suffix = ".yaml")
        tempFile.writeText(invalidPriceYaml)

        try {
            // When/Then
            assertThrows<IllegalArgumentException> {
                ProductLoader.loadProducts(filePath = tempFile.absolutePath)
            }
        } finally {
            // Clean up
            tempFile.delete()
        }
    }
}
