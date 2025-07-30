package com.github.basdgrt.products

import org.yaml.snakeyaml.Yaml
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStream

/**
 * Loads products from the products.yaml file.
 */
class ProductLoader {
    companion object {
        private const val PRODUCTS_FILE_PATH = "src/main/resources/products.yaml"
        private const val CLASSPATH_PRODUCTS_FILE = "/resources/products.yaml"

        /**
         * Gets the input stream for the products file, trying first from the classpath
         * and falling back to the file system.
         */
        private fun getProductsInputStream(): InputStream {
            // First try to load from classpath (for JAR deployment)
            val classpathStream = ProductLoader::class.java.getResourceAsStream(CLASSPATH_PRODUCTS_FILE)
            if (classpathStream != null) {
                return classpathStream
            }

            // Fall back to file system (for local development)
            return FileInputStream(PRODUCTS_FILE_PATH)
        }

        /**
         * Loads the products from the products.yaml file.
         * 
         * @return A list of Product objects.
         * @throws FileNotFoundException if the products.yaml file is not found.
         */
        @Suppress("UNCHECKED_CAST")
        fun loadProducts(): List<Product> {
            val inputStream = getProductsInputStream()
            val yamlProducts = Yaml().load(inputStream) as List<Map<String, Any>>

            return yamlProducts.map { yamlProduct ->
                val name = yamlProduct["name"] as String
                val originalPriceStr = yamlProduct["originalPrice"] as String
                val originalPrice = Price.of(originalPriceStr).fold(
                    { error -> throw IllegalArgumentException("Invalid original price for product $name: ${error.errorMessage()}") },
                    { price -> price }
                )

                val urls = yamlProduct["urls"] as List<String>
                val productDetailPages = urls.map { url -> ProductDetailPage(url) }

                Product(
                    name = name,
                    productDetailPages = productDetailPages,
                    originalPrice = originalPrice
                )
            }
        }
    }
}
