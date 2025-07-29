package com.github.basdgrt

import com.github.basdgrt.comparator.Comparator
import com.github.basdgrt.config.SecretsLoader
import com.github.basdgrt.notifications.TelegramNotifierExample
import com.github.basdgrt.products.products
import com.github.basdgrt.scraping.PriceScraper
import kotlin.system.exitProcess

fun main() {
    val priceScraper = PriceScraper()
    val comparator = Comparator()

    // Load secrets from secrets.yaml
    val botToken = SecretsLoader.getSecret("botToken") ?: throw IllegalStateException("Bot token not found in secrets.yaml")
    val chatId = SecretsLoader.getSecret("chatId") ?: throw IllegalStateException("Chat ID not found in secrets.yaml")

    for (product in products) {
        val productPrices = priceScraper.scrape(product)
        val comparisonResult = comparator.compare(productPrices)

        TelegramNotifierExample.sendComparisonResult(productPrices, botToken, chatId)

        println(comparisonResult)
        println()
    }

    // Force the application to exit after sending the message
    // This is necessary because the TelegramLongPollingBot keeps running in the background
    exitProcess(0)
}
