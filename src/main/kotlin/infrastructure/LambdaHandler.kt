package com.github.basdgrt.infrastructure

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.github.basdgrt.comparator.Comparator
import com.github.basdgrt.config.SecretsLoader
import com.github.basdgrt.notifications.TelegramNotifier
import com.github.basdgrt.products.ProductLoader
import com.github.basdgrt.scraping.PriceScraper
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * AWS Lambda handler for the Price Comparator application.
 * This handler executes the main logic of the application when invoked.
 */
class LambdaHandler : RequestHandler<Map<String, Any>, String> {

    private val logger = KotlinLogging.logger {}

    override fun handleRequest(input: Map<String, Any>, context: Context): String {
        logger.info { "Starting price comparison Lambda function" }

        try {
            val botToken = SecretsLoader.getSecret("botToken") 
                ?: throw IllegalStateException("Bot token not found in secrets.yaml")
            val chatId = SecretsLoader.getSecret("chatId") 
                ?: throw IllegalStateException("Chat ID not found in secrets.yaml")

            val products = ProductLoader.loadProducts()
            logger.info { "Loaded ${products.size} products for comparison" }

            val priceScraper = PriceScraper()
            val comparator = Comparator()
            val telegramNotifier = TelegramNotifier(botToken, chatId)

            for (product in products) {
                logger.info { "Processing product: ${product.name}" }
                val productPrices = priceScraper.scrape(product)
                val comparisonResult = comparator.compare(productPrices)

                telegramNotifier.sendMessage(comparisonResult)
            }

            logger.info { "Price comparison completed successfully" }
            return "Price comparison completed successfully"
        } catch (e: Exception) {
            logger.error(e) { "Error during price comparison: ${e.message}" }
            throw e
        }
    }
}
