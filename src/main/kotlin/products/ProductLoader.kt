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
         * 
         * @param filePath Optional custom file path to use instead of the default.
         * @param classpathFile Optional custom classpath file to use instead of the default.
         * @return An InputStream for the products file.
         */
        private fun getProductsInputStream(filePath: String = PRODUCTS_FILE_PATH, 
                                          classpathFile: String = CLASSPATH_PRODUCTS_FILE): InputStream {
            // First try to load from classpath (for JAR deployment)
            val classpathStream = ProductLoader::class.java.getResourceAsStream(classpathFile)
            if (classpathStream != null) {
                return classpathStream
            }

            // Fall back to file system (for local development)
            return FileInputStream(filePath)
        }

        /**
         * Loads the products from the products.yaml file.
         * 
         * @param filePath Optional custom file path to use instead of the default.
         * @param classpathFile Optional custom classpath file to use instead of the default.
         * @return A list of Product objects.
         * @throws FileNotFoundException if the products.yaml file is not found.
         */
        @Suppress("UNCHECKED_CAST")
        fun loadProducts(filePath: String = PRODUCTS_FILE_PATH, 
                         classpathFile: String = CLASSPATH_PRODUCTS_FILE): List<Product> {
            val inputStream = getProductsInputStream(filePath, classpathFile)
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
