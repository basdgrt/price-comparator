package com.github.basdgrt.products

import org.yaml.snakeyaml.Yaml
import java.io.FileInputStream
import java.io.FileNotFoundException

/**
 * Loads products from the products.yaml file.
 */
class ProductLoader {
    companion object {
        private const val PRODUCTS_FILE_PATH = "src/main/resources/products.yaml"
        
        /**
         * Loads the products from the products.yaml file.
         * 
         * @return A list of Product objects.
         * @throws FileNotFoundException if the products.yaml file is not found.
         */
        @Suppress("UNCHECKED_CAST")
        fun loadProducts(): List<Product> {
            val inputStream = FileInputStream(PRODUCTS_FILE_PATH)
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