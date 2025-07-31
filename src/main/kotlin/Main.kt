package com.github.basdgrt

import arrow.fx.coroutines.parMap
import com.github.basdgrt.comparator.Comparator
import com.github.basdgrt.config.SecretsLoader
import com.github.basdgrt.notifications.TelegramNotifier
import com.github.basdgrt.products.Product
import com.github.basdgrt.products.ProductLoader
import com.github.basdgrt.scraping.PriceScraper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlin.system.exitProcess

fun main(): Unit = runBlocking {
    val botToken = SecretsLoader.getSecret("botToken") ?: throw IllegalStateException("Bot token not found in secrets.yaml")
    val chatId = SecretsLoader.getSecret("chatId") ?: throw IllegalStateException("Chat ID not found in secrets.yaml")
    val botUserName = SecretsLoader.getSecret("botUserName") ?: throw IllegalStateException("Bot user name not found in secrets.yaml")

    val products: List<Product> = ProductLoader.loadProducts()

    val priceScraper = PriceScraper()
    val comparator = Comparator()
    val telegramNotifier = TelegramNotifier(botUserName, botToken, chatId)

    products.parMap(Dispatchers.IO) { product ->
        val productPrices = priceScraper.scrape(product)
        val comparisonResult = comparator.compare(productPrices)

        telegramNotifier.sendMessage(comparisonResult)
    }

    // Force the application to exit.
    // This is necessary because the TelegramLongPollingBot keeps running in the background
    exitProcess(0)
}
