package com.github.basdgrt.notifications

import com.github.basdgrt.comparator.Comparator
import com.github.basdgrt.products.ProductPrices
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

/**
 * Example showing how to use the TelegramNotifier to send price comparison results.
 */
object TelegramNotifierExample {

    /**
     * Sends the comparison result for the given product prices to Telegram.
     *
     * @param productPrices The product prices to compare
     * @param botToken The Telegram bot token
     * @param chatId The Telegram chat ID to send the message to
     * @return true if the message was sent successfully, false otherwise
     */
    fun sendComparisonResult(
        productPrices: ProductPrices,
        botToken: String,
        chatId: String
    ): Boolean {
        // Create the comparator and generate the comparison message
        val comparator = Comparator()
        val comparisonResult = comparator.compare(productPrices)

        // Create and register the Telegram notifier
        val telegramNotifier = TelegramNotifier(botToken, chatId)

        try {
            // Register the bot with the Telegram Bots API
            val botsApi = TelegramBotsApi(DefaultBotSession::class.java)
            botsApi.registerBot(telegramNotifier)

            // Send the comparison result
            val result = telegramNotifier.sendMessage(comparisonResult)

            return result
        } catch (e: Exception) {
            // Log the error and return false
            println("Failed to send comparison result to Telegram: ${e.message}")

            return false
        }
    }
}
